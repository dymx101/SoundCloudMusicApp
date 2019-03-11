package com.musichero.xmusic.utils;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTimeUtils {
	
	public static final String[] DAY_ENGLISH={"Sun","Mon","Tue","Wed","Thur","Fri","Sat"};

	public static final String[] MONTH_ENGLISH={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug"
											,"Sep","Oct","Nov","Dec"};
	
	public static final String TAG = DateTimeUtils.class.getSimpleName();
	
	public static Date getDateFromString(String mData, String mPattern){
		SimpleDateFormat format = new SimpleDateFormat(mPattern);
		try {  
		    Date date = format.parse(mData);
		    return date;
		} catch (ParseException e) {
		    e.printStackTrace();  
		}
		return null;
	}
	
	
	
	public static String convertDateToString(Date mDate, String mPattern){
		if(mDate==null || mPattern==null){
			return null;
		}
		String mStrDate = DateFormat.format(mPattern, mDate.getTime()).toString();
		return mStrDate;
	}
	
	public static String getFullDate(){
		Calendar c = Calendar.getInstance();
		String minutes = String.valueOf(c.get(Calendar.MINUTE));
		if(minutes.length()==1){
			minutes="0"+minutes;
		}
		String hours = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		if(hours.length()==1){
			hours="0"+hours;
		}
		String year = String.valueOf(c.get(Calendar.YEAR));
		int indexMonths =c.get(Calendar.MONTH);
		int indexDay = c.get(Calendar.DAY_OF_WEEK);
		
		String months=MONTH_ENGLISH[indexMonths];
		String day=DAY_ENGLISH[indexDay-1];
		
		String mDayOfMonths = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		if(mDayOfMonths.length()==1){
			mDayOfMonths="0"+mDayOfMonths;
		}
		String totalString = day+" "+mDayOfMonths+"-"+months+"-"+year+" "+hours+":"+minutes;
		return totalString;
		
	}

	public static String getShortStringDate(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String year = String.valueOf(c.get(Calendar.YEAR));
		int indexMonths =c.get(Calendar.MONTH);
		String dayOfMonths = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String month= String.valueOf(indexMonths+1);
		if(month.length()<2){
			month="0"+month;
		}
		String totalString = dayOfMonths+"."+month+"."+year;
		return totalString;
	}


	public static String getCurrentDate(String mPattern){
		Date mDate = new Date();
		String mStrDate = DateFormat.format(mPattern, mDate.getTime()).toString();
		return mStrDate;
	}
	
	public static String convertMilliToStrDate(long mCurrentDate, String mPattern){
		if(mCurrentDate>0){
			String mStrDate = DateFormat.format(mPattern, mCurrentDate).toString();
			return mStrDate;
		}
		return null;
	}
}
