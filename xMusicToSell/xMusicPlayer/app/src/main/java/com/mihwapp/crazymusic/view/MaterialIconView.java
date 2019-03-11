package com.mihwapp.crazymusic.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MaterialIconView extends TextView {

	public static final String FONT_MATERIAL= "fonts/MaterialIcons.ttf";
	
	private static Typeface sMaterialDesignIcons;

	public MaterialIconView(Context context) {
		this(context, null);
	}

	public MaterialIconView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MaterialIconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		setTypeface();
	}
	
	private void setTypeface() {
		if (sMaterialDesignIcons == null) {
			sMaterialDesignIcons = Typeface.createFromAsset(getContext().getAssets(), FONT_MATERIAL);
		}
		setTypeface(sMaterialDesignIcons);
	}
}
