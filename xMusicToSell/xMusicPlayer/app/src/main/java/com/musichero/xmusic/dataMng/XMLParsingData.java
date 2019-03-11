package com.musichero.xmusic.dataMng;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 
 *
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:AndroidCloundMusicPlayer
 * @Date:Dec 14, 2014 
 *
 */
public class XMLParsingData {

	public static final String TAG = XMLParsingData.class.getSimpleName();

	public static final String TAG_SUGGESTION = "suggestion";


	public static ArrayList<String> parsingSuggestion(InputStream in) {
		if(in==null){
			return null;
		}
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, "ISO-8859-9");
			int eventType = parser.getEventType();
			ArrayList<String> listSuggestionStr = new ArrayList<String>();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equals(TAG_SUGGESTION)) {
						String sugges=parser.getAttributeValue(0);
						listSuggestionStr.add(sugges);
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}
			return listSuggestionStr;
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	

}
