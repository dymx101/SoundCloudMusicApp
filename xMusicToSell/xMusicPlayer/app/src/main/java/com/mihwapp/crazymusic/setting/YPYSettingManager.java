package com.mihwapp.crazymusic.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Setting Manager
 * @author 
 * Nov 16, 2012
 * 
 * 
 */
public class YPYSettingManager implements IYPYSettingConstants {
	
	public static final String TAG = YPYSettingManager.class.getSimpleName();
	
	public static final String DOBAO_SHARPREFS = "ypyproductions_prefs";

	public static void saveSetting(Context mContext,String mKey,String mValue){
		try{
			if(mContext!=null){
				SharedPreferences mSharedPreferences =mContext.getSharedPreferences(DOBAO_SHARPREFS, Context.MODE_PRIVATE);
				if(mSharedPreferences!=null){
					Editor editor = mSharedPreferences.edit();
					editor.putString(mKey, mValue);
					editor.commit();
				}
			}


		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
	
	public static String getSetting(Context mContext,String mKey,String mDefValue){
		try{
			if(mContext!=null){
				SharedPreferences mSharedPreferences =mContext.getSharedPreferences(DOBAO_SHARPREFS, Context.MODE_PRIVATE);
				if(mSharedPreferences!=null){
					return mSharedPreferences.getString(mKey, mDefValue);
				}
			}

		}
		catch (Exception e){
			e.printStackTrace();
		}
		return mDefValue;

	}
	
	public static boolean getOnline(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_ONLINE, "false"));
	}
	
	public static void setOnline(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_ONLINE, String.valueOf(mValue));
	}


	public static void setShuffle(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_SHUFFLE, String.valueOf(mValue));
	}
	public static boolean getShuffle(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_SHUFFLE, "false"));
	}
	public static void setRateApp(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_RATE_APP, String.valueOf(mValue));
	}
	public static boolean getRateApp(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_RATE_APP, "false"));
	}

	public static boolean getEqualizer(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_EQUALIZER_ON, "false"));
	}

	public static void setEqualizer(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_EQUALIZER_ON, String.valueOf(mValue));
	}

	public static String getEqualizerPreset(Context mContext){
		return getSetting(mContext, KEY_EQUALIZER_PRESET, "0");
	}

	public static void setEqualizerPreset(Context mContext, String mValue){
		saveSetting(mContext, KEY_EQUALIZER_PRESET, mValue);
	}

	public static String getEqualizerParams(Context mContext){
		return getSetting(mContext, KEY_EQUALIZER_PARAMS, "");
	}

	public static void setEqualizerParams(Context mContext, String mValue){
		saveSetting(mContext, KEY_EQUALIZER_PARAMS, mValue);
	}

	public static short getBassBoost(Context mContext){
		return Short.parseShort(getSetting(mContext, KEY_BASSBOOST, "0"));
	}
	public static void setBassBoost(Context mContext, short mValue){
		saveSetting(mContext, KEY_BASSBOOST, String.valueOf(mValue));
	}

	public static short getVirtualizer(Context mContext){
		return Short.parseShort(getSetting(mContext, KEY_VIRTUALIZER, "0"));
	}

	public static void setVirtualizer(Context mContext, short mValue){
		saveSetting(mContext, KEY_VIRTUALIZER, String.valueOf(mValue));
	}
	public static int getSleepMode(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_TIME_SLEEP, "0"));
	}

	public static void setSleepMode(Context mContext, int mValue){
		saveSetting(mContext, KEY_TIME_SLEEP, String.valueOf(mValue));
	}

	public static void setNewRepeat(Context mContext, int mValue){
		saveSetting(mContext, KEY_REPEAT1, String.valueOf(mValue));
	}
	public static int getNewRepeat(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_REPEAT1, "0"));
	}

	public static void setBackground(Context mContext, String mValue){
		saveSetting(mContext, KEY_BACKGROUND, mValue);
	}

	public static String getBackground(Context mContext){
		return getSetting(mContext, KEY_BACKGROUND, "");
	}

	
}
