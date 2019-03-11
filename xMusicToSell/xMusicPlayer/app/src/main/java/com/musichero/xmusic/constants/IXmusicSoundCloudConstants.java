package com.musichero.xmusic.constants;

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
public interface IXmusicSoundCloudConstants {
	
	public static final String URL_API="http://api.soundcloud.com/";
	public static final String METHOD_TRACKS="tracks";

	public static final String URL_TOP_MUSIC="https://itunes.apple.com/%1$s/rss/topsongs/limit=%2$s/json";
	
	public static final String FORMAT_CLIENT_ID="?client_id=%1$s";
	public static final String JSON_PREFIX=".json";
	
	public static final String OFFSET="&offset=%1$s&limit=%2$s";
	
	public static final String FILTER_QUERY="&q=%1$s";
	public static final String FILTER_GENRE="&genres=%1$s";

	public static final String FORMAT_URL_SONG = "http://api.soundcloud.com/tracks/%1$s/stream?client_id=%2$s";

	public static final String URL_API_V2="https://api-v2.soundcloud.com/";
	public static final String METHOD_CHARTS="charts?";
	public static final String PARAMS_GENRES ="&genre=soundcloud:genres:%1$s";
	public static final String PARAMS_LINKED_PARTITION="&linked_partitioning=1";
	public static final String PARAMS_OFFSET ="&offset=%1$s&limit=%2$s";
	public static final String PARAMS_KIND="&kind=%1$s";
	public static final String PARAMS_NEW_CLIENT_ID="&client_id=%1$s";

	public static final String KIND_TOP="top";
	public static final String KIND_TRENDING="trending";

	public static final String ALL_MUSIC_GENRE="all-music";


}
