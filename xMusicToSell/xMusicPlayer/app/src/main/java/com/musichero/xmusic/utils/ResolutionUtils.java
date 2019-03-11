package com.musichero.xmusic.utils;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;

public class ResolutionUtils {
	
	public static final String PORTRAIT="Portrait";
	public static final String LANSCAPE="Landscape";
	
	public static int[] getDeviceResolution(Activity mContext){
		int[] res = null;
		Display display = mContext.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int i = mContext.getResources().getConfiguration().orientation;
		if (i == Configuration.ORIENTATION_PORTRAIT){
			res = new int[2];
			int finalWidth = height >= width ? width : height;
			int finalHeight = height <= width ? width : height;
			res[0]= finalWidth;
			res[1]= finalHeight;
		}
		else if (i == Configuration.ORIENTATION_LANDSCAPE){
			res = new int[2];
			int finalWidth = height <= width ? width : height;
			int finalHeight = height >= width ? width : height;
			res[0]= finalWidth;
			res[1]= finalHeight;
		}
		return res;
	}

	/**
	 * This method convets dp unit to equivalent device specific value in pixels. 
	 * @param context Context to get resources and device specific display metrics
	 * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static float convertDpToPixel(Context context, float dp){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}
	/**
	 * This method converts device specific pixels to device independent pixels.
	 * @param context Context to get resources and device specific display metrics
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @return A float value to represent db equivalent to px value
	 */
	public static float convertPixelsToDp(Context context, float px){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;

	}
	public static float convertPixelsToSp(Context context, float px) {
	    float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
	    return px/scaledDensity;
	}
	
	public static float convertSpToPixel(Context context, float sp) {
	    float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
	    return sp*(scaledDensity/160f);
	}
}	
