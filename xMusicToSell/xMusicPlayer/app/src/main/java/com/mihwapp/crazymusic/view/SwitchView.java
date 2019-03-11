package com.mihwapp.crazymusic.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.mihwapp.crazymusic.R;


public class SwitchView extends CustomView {

	int backgroundColor = Color.parseColor("#4CAF50");

	Ball ball;

	boolean check = false;
	boolean press = false;

	OnCheckListener onCheckListener;

	private Paint mMainPaint;

	private Bitmap mTempBitmap;

	private Canvas mTempCanvas;

	private Paint mTransparentPaint;

	public SwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAttributes(attrs);
		init();
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (check){
					setChecked(false);
				}
				else{
					setChecked(true);
				}
			}
		});
	}

	private void init() {
		mMainPaint = new Paint();
		mMainPaint.setAntiAlias(true);
		mMainPaint.setStrokeWidth(ViewUtils.dpToPx(2, getResources()));
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setAntiAlias(true);
		mTransparentPaint.setColor(getResources().getColor(android.R.color.transparent));
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	}

	// Set atributtes of XML to View
	protected void setAttributes(AttributeSet attrs) {

		setBackgroundResource(R.drawable.background_transparent);

		// Set size of view
		setMinimumHeight(ViewUtils.dpToPx(48, getResources()));
		setMinimumWidth(ViewUtils.dpToPx(80, getResources()));

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

		check = attrs.getAttributeBooleanValue(MATERIALDESIGNXML, "check", false);
		ball = new Ball(getContext());
		RelativeLayout.LayoutParams params = new LayoutParams(ViewUtils.dpToPx(20, getResources()), ViewUtils.dpToPx(20, getResources()));
		params.addRule(CENTER_VERTICAL, TRUE);
		ball.setLayoutParams(params);
		addView(ball);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isEnabled()) {
			isLastTouch = true;
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				press = true;
			}
			else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				float x = event.getX();
				x = (x < ball.xIni) ? ball.xIni : x;
				x = (x > ball.xFin) ? ball.xFin : x;
				if (x > ball.xCen) {
					check = true;
				}
				else {
					check = false;
				}
				ball.setX(x);
				ball.changeBackground();
				if ((event.getX() <= getWidth() && event.getX() >= 0)) {
					isLastTouch = false;
					press = false;
				}
			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				press = false;
				isLastTouch = false;
				if (onCheckListener != null){
					onCheckListener.onCheck(check);
				}
				if ((event.getX() <= getWidth() && event.getX() >= 0)) {
					ball.animateCheck();
				}
			}
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!placedBall)
			placeBall();

		// Crop line to transparent effect
		if (mTempCanvas == null) {
			mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			mTempCanvas = new Canvas(mTempBitmap);
		}
		else {
			if(mTempBitmap!=null){
				mTempBitmap.recycle();
				mTempBitmap=null;
			}
			mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
			mTempCanvas.setBitmap(mTempBitmap);
		}
		mMainPaint.setColor((check) ? backgroundColor : Color.parseColor("#B0B0B0"));
		
		mTempCanvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, mMainPaint);
		mTempCanvas.drawCircle(ball.getX() + ball.getWidth() / 2, ball.getY() + ball.getHeight() / 2, ball.getWidth() / 2, mTransparentPaint);
		canvas.drawBitmap(mTempBitmap, 0, 0, null);

		if (press) {
			mMainPaint.setColor((check) ? makePressColor() : Color.parseColor("#446D6D6D"));
			canvas.drawCircle(ball.getX() + ball.getWidth() / 2, getHeight() / 2, getHeight() / 2, mMainPaint);
		}
		invalidate();

	}

	/**
	 * Make a dark color to press effect
	 * 
	 * @return
	 */
	protected int makePressColor() {
		int r = (this.backgroundColor >> 16) & 0xFF;
		int g = (this.backgroundColor >> 8) & 0xFF;
		int b = (this.backgroundColor >> 0) & 0xFF;
		r = (r - 30 < 0) ? 0 : r - 30;
		g = (g - 30 < 0) ? 0 : g - 30;
		b = (b - 30 < 0) ? 0 : b - 30;
		return Color.argb(70, r, g, b);
	}

	// Move ball to first position in view
	boolean placedBall = false;

	private void placeBall() {
		ball.setX(getHeight() / 2 - ball.getWidth() / 2);
		ball.xIni = ball.getX();
		ball.xFin = getWidth() - getHeight() / 2 - ball.getWidth() / 2;
		ball.xCen = getWidth() / 2 - ball.getWidth() / 2;
		placedBall = true;
		ball.animateCheck();
	}

	// SETTERS

	@Override
	public void setBackgroundColor(int color) {
		backgroundColor = color;
		if (isEnabled()){
			beforeBackground = backgroundColor;
		}
		invalidate();

	}

	public void setChecked(boolean check) {
		this.check = check;
		invalidate();
		ball.animateCheck();
	}

	public boolean isCheck() {
		return check;
	}

	class Ball extends View {

		float xIni, xFin, xCen;

		public Ball(Context context) {
			super(context);
			setBackgroundResource(R.drawable.background_switch_ball_uncheck);
		}

		public void changeBackground() {
			if (check) {
				setBackgroundResource(R.drawable.background_checkbox);
				LayerDrawable layer = (LayerDrawable) getBackground();
				GradientDrawable shape = (GradientDrawable) layer.findDrawableByLayerId(R.id.shape_bacground);
				shape.setColor(backgroundColor);
			}
			else {
				setBackgroundResource(R.drawable.background_switch_ball_uncheck);
			}
		}

		public void animateCheck() {
			changeBackground();
			ObjectAnimator objectAnimator;
			if (check) {
				objectAnimator = ObjectAnimator.ofFloat(this, "x", ball.xFin);

			}
			else {
				objectAnimator = ObjectAnimator.ofFloat(this, "x", ball.xIni);
			}
			objectAnimator.setDuration(300);
			objectAnimator.start();
		}

	}

	public void setOncheckListener(OnCheckListener onCheckListener) {
		this.onCheckListener = onCheckListener;
	}

	public interface OnCheckListener {
		public void onCheck(boolean check);
	}

}
