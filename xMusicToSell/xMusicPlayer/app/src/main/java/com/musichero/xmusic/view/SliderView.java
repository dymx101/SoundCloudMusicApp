package com.musichero.xmusic.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.musichero.xmusic.R;


public class SliderView extends CustomView {

	// Event when slider change value
	public interface OnValueChangedListener {
		public void onValueChanged(int value);
	}

	int processColor = Color.parseColor("#4CAF50");
	int bgColor = Color.parseColor("#B0B0B0");

	Ball ball;
	NumberIndicator numberIndicator;

	boolean showNumberIndicator = false;
	boolean press = false;

	int value = 0;
	int max = 100;
	int min = 0;

	OnValueChangedListener onValueChangedListener;

	private Paint mPaintIndicator;

	private Paint mPaintNormal;

	private Paint mTransparentPaint;

	private Bitmap mTempBitmap;

	private Canvas mTempCanvas;

	private Paint mPaintActive;


	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAttributes(attrs);
		initBall();
		init();
	}
	public SliderView(Context context) {
		super(context);
		init();
		initBall();
	}
	
	private void init(){
		mPaintIndicator = new Paint();
		mPaintIndicator.setColor(processColor);
		mPaintIndicator.setAntiAlias(true);
		
		mPaintNormal = new Paint();
		mPaintNormal.setColor(bgColor);
		mPaintNormal.setStrokeWidth(ViewUtils.dpToPx(2, getResources()));
		
		mPaintActive = new Paint();
		mPaintActive.setColor(processColor);
		mPaintActive.setStrokeWidth(ViewUtils.dpToPx(2, getResources()));
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setColor(getResources().getColor(android.R.color.transparent));
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		setBackgroundResource(R.drawable.background_transparent);
		// Set size of view
		setMinimumHeight(ViewUtils.dpToPx(48, getResources()));
		setMinimumWidth(ViewUtils.dpToPx(80, getResources()));
	}

	// Set atributtes of XML to View
	protected void setAttributes(AttributeSet attrs) {

		// Set background Color
		// Color by resource
		int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML, "themes", -1);
		if (bacgroundColor != -1) {
			setBackgroundColor(getResources().getColor(bacgroundColor));
		}
		else {
			// Color by hexadecimal
			int background = attrs.getAttributeIntValue(ANDROIDXML, "themes", -1);
			if (background != -1)
				setBackgroundColor(background);
		}

		showNumberIndicator = attrs.getAttributeBooleanValue(MATERIALDESIGNXML, "showNumberIndicator", false);
		min = attrs.getAttributeIntValue(MATERIALDESIGNXML, "min", 0);
		max = attrs.getAttributeIntValue(MATERIALDESIGNXML, "max", 0);
		value = attrs.getAttributeIntValue(MATERIALDESIGNXML, "value", min);

		

	}
	private void initBall(){
		ball = new Ball(getContext());
		RelativeLayout.LayoutParams params = new LayoutParams(ViewUtils.dpToPx(20, getResources()), ViewUtils.dpToPx(20, getResources()));
		params.addRule(CENTER_VERTICAL, TRUE);
		ball.setLayoutParams(params);
		addView(ball);

		// Set if slider content number indicator
		// TODO
		if (showNumberIndicator) {
			numberIndicator = new NumberIndicator(getContext());
		}
	}

	@Override
	public void invalidate() {
		if(ball!=null){
			ball.invalidate();
		}
		super.invalidate();
	}

	public void setOnReCalculate(boolean b){
		this.placedBall=!b;
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!placedBall)
			placeBall();

		if (value == min) {
			if(mTempBitmap==null){
				mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
				mTempCanvas = new Canvas(mTempBitmap);
			}
			else{
				if(mTempBitmap!=null){
					mTempBitmap.recycle();
					mTempBitmap=null;
				}
				mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
				mTempCanvas.setBitmap(mTempBitmap);
			}
			mTempCanvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, mPaintNormal);
			mTempCanvas.drawCircle(ball.getX() + ball.getWidth() / 2, ball.getY() + ball.getHeight() / 2, ball.getWidth() / 2, mTransparentPaint);
			canvas.drawBitmap(mTempBitmap, 0, 0, null);
		}
		else {
			canvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, mPaintNormal);
			float division = (ball.xFin - ball.xIni) / (max - min);
			int value = this.value - min;
			canvas.drawLine(getHeight() / 2, getHeight() / 2, value * division + getHeight() / 2, getHeight() / 2, mPaintActive);
		}

		if (press && !showNumberIndicator) {
			canvas.drawCircle(ball.getX() + ball.getWidth() / 2, getHeight() / 2, getHeight() / 3, mPaintIndicator);
		}
		invalidate();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		isLastTouch = true;
		if (isEnabled()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				if (numberIndicator != null && numberIndicator.isShowing() == false)
					numberIndicator.show();
				if ((event.getX() <= getWidth() && event.getX() >= 0)) {
					press = true;
					// calculate value
					int newValue = 0;
					float division = (ball.xFin - ball.xIni) / (max - min);
					if (event.getX() > ball.xFin) {
						newValue = max;
					}
					else if (event.getX() < ball.xIni) {
						newValue = min;
					}
					else {
						newValue = min + (int) ((event.getX() - ball.xIni) / division);
					}
					if (value != newValue) {
						value = newValue;
						if (onValueChangedListener != null)
							onValueChangedListener.onValueChanged(newValue);
					}
					// move ball indicator
					float x = event.getX();
					x = (x < ball.xIni) ? ball.xIni : x;
					x = (x > ball.xFin) ? ball.xFin : x;
					ball.setX(x);
					ball.changeBackground();

					// If slider has number indicator
					if (numberIndicator != null) {
						// move number indicator
						numberIndicator.indicator.x = x;
						numberIndicator.indicator.finalY = ViewUtils.getRelativeTop(this) - getHeight() / 2;
						numberIndicator.indicator.finalSize = getHeight() / 2;
						numberIndicator.numberIndicator.setText("");
					}

				}
				else {
					press = false;
					isLastTouch = false;
					if (numberIndicator != null)
						numberIndicator.dismiss();

				}

			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				if (numberIndicator != null)
					numberIndicator.dismiss();
				isLastTouch = false;
				press = false;
			}
		}
		return true;
	}

	/**
	 * Make a dark color to press effect
	 *
	 * @return
	 */
	protected int makePressColor() {
		int r = (this.processColor >> 16) & 0xFF;
		int g = (this.processColor >> 8) & 0xFF;
		int b = (this.processColor >> 0) & 0xFF;
		r = (r - 30 < 0) ? 0 : r - 30;
		g = (g - 30 < 0) ? 0 : g - 30;
		b = (b - 30 < 0) ? 0 : b - 30;
		return Color.argb(70, r, g, b);
	}

	private void placeBall() {
		ball.setX(getHeight() / 2 - ball.getWidth() / 2);
		ball.xIni = ball.getX();
		ball.xFin = getWidth() - getHeight() / 2 - ball.getWidth() / 2;
		ball.xCen = getWidth() / 2 - ball.getWidth() / 2;
		placedBall = true;
	}

	// GETERS & SETTERS

	public OnValueChangedListener getOnValueChangedListener() {
		return onValueChangedListener;
	}

	public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
		this.onValueChangedListener = onValueChangedListener;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		if (placedBall == false)
			post(new Runnable() {

				@Override
				public void run() {
					setValue(value);
				}
			});
		else {
			this.value = value;
			float division = (ball.xFin - ball.xIni) / max;
			ball.setX(value * division + getHeight() / 2 - ball.getWidth() / 2);
			ball.changeBackground();
		}

	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public boolean isShowNumberIndicator() {
		return showNumberIndicator;
	}

	public void setShowNumberIndicator(boolean showNumberIndicator) {
		this.showNumberIndicator = showNumberIndicator;
		numberIndicator = (showNumberIndicator) ? new NumberIndicator(getContext()) : null;
	}

	public void setProcessColor(int color) {
		processColor = color;
		if(mPaintActive!=null){
			mPaintActive.setColor(processColor);
		}
		if(mPaintIndicator!=null){
			mPaintIndicator.setColor(processColor);
		}
		if (isEnabled()){
			beforeBackground = processColor;
		}
		invalidate();
	}

	@Override
	public void setBackgroundColor(int color) {
		bgColor=color;
		if(mPaintNormal!=null){
			mPaintNormal.setColor(bgColor);
		}
		invalidate();
	}

	boolean placedBall = false;

	class Ball extends View {

		float xIni, xFin, xCen;

		public Ball(Context context) {
			super(context);
			setBackgroundResource(R.drawable.background_switch_ball_uncheck);
		}

		public void changeBackground() {
			if (value != min) {
				setBackgroundResource(R.drawable.background_checkbox);
				LayerDrawable layer = (LayerDrawable) getBackground();
				GradientDrawable shape = (GradientDrawable) layer.findDrawableByLayerId(R.id.shape_bacground);
				shape.setColor(processColor);
			}
			else {
				setBackgroundResource(R.drawable.background_switch_ball_uncheck);
			}
		}

	}

	// Slider Number Indicator

	class NumberIndicator extends Dialog {

		Indicator indicator;
		TextView numberIndicator;

		public NumberIndicator(Context context) {
			super(context, android.R.style.Theme_Translucent);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.number_indicator_spinner);
			setCanceledOnTouchOutside(false);

			RelativeLayout content = (RelativeLayout) this.findViewById(R.id.number_indicator_spinner_content);
			indicator = new Indicator(this.getContext());
			content.addView(indicator);

			numberIndicator = new TextView(getContext());
			numberIndicator.setTextColor(Color.WHITE);
			numberIndicator.setGravity(Gravity.CENTER);
			content.addView(numberIndicator);

			indicator.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		}

		@Override
		public void dismiss() {
			super.dismiss();
			indicator.y = 0;
			indicator.size = 0;
			indicator.animate = true;
		}

		@Override
		public void onBackPressed() {
		}

	}

	class Indicator extends RelativeLayout {

		// Position of number indicator
		float x = 0;
		float y = 0;
		// Size of number indicator
		float size = 0;

		// Final y position after animation
		float finalY = 0;
		// Final size after animation
		float finalSize = 0;

		boolean animate = true;

		boolean numberIndicatorResize = false;
		private Paint paint;

		public Indicator(Context context) {
			super(context);
			setBackgroundColor(getResources().getColor(android.R.color.transparent));

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(processColor);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (numberIndicatorResize == false) {
				RelativeLayout.LayoutParams params = (LayoutParams) numberIndicator.numberIndicator.getLayoutParams();
				params.height = (int) finalSize * 2;
				params.width = (int) finalSize * 2;
				numberIndicator.numberIndicator.setLayoutParams(params);
			}

			if (animate) {
				if (y == 0)
					y = finalY + finalSize * 2;
				y -= ViewUtils.dpToPx(6, getResources());
				size += ViewUtils.dpToPx(2, getResources());
			}
			canvas.drawCircle(ball.getX() + ViewUtils.getRelativeLeft((View) ball.getParent()) + ball.getWidth() / 2, y, size, paint);
			if (animate && size >= finalSize)
				animate = false;
			if (animate == false) {
				numberIndicator.numberIndicator.setX((ball.getX() + ViewUtils.getRelativeLeft((View) ball.getParent()) + ball.getWidth() / 2) - size);
				numberIndicator.numberIndicator.setY( y - size);
				numberIndicator.numberIndicator.setText(value + "");
			}

			invalidate();
		}

	}

}
