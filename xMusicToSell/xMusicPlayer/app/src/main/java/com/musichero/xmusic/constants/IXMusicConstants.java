package com.musichero.xmusic.constants;


public interface IXMusicConstants {
	
	public static final boolean DEBUG = false;

	public static final String ANDROID8_CHANNEL_ONE_NAME = "XMusicChannel";//you can change the channel name for android 8.0

	public static final boolean SHOW_ADS = true; //enable all ads
	public static final boolean SHOW_BANNER_ADS_IN_HOME = true; // show admob banner in home screen
	public static final boolean STOP_MUSIC_WHEN_EXITS_APP = false; // stop music app when exiting app

	/**
	 * Configure for app rate dialog
	 */

	public static final int NUMBER_INSTALL_DAYS=0;//it is the number install days to show dialog rate.default is 0
	public static final int NUMBER_LAUNCH_TIMES=3;//it is the number launch times to show dialog rate.default is 3
	public static final int REMIND_TIME_INTERVAL=1;//it is the number repeat days to show dialog rate.default is 1

	public static final String SOUND_CLOUD_CLIENT_ID = "SOUND_CLOUD_CLIENT_ID";
	public static final String ADMOB_BANNER_ID = "ADMOB_BANNER_ID";
	public static final String ADMOB_INTERSTITIAL_ID = "ADMOB_INTERSTITIAL_ID";
	public static final String ADMOB_APP_ID = "ADMOB_APP_ID";
	public static final String DIR_CACHE = "xmusic_app";

	public static final boolean SHOW_SOUND_CLOUD_TAB = true; //enable sound cloud tab

	public static final String URL_WEBSITE = "URL_WEBSITE";
	public static final String YOUR_CONTACT_EMAIL = "YOUR_CONTACT_EMAIL";
	public static final String PREFIX_UNKNOWN = "<unknown>";


	public static final int MAX_SONG_CACHED=50;//5 min
	public static final int MAX_TOP_PLAYLIST_SONG=25;//5 min
	public static final int MAX_SEARCH_SONG=80;//5 min

	//this is test id of your device when you test admob.You can replace it
	public static final String ADMOB_TEST_DEVICE = "895FBCB15AAEAC84EA229AB710D92ED0";

	public static final int NOTIFICATION_ID = 1;

	public static final String ACTION_FAVORITE = ".action.ACTION_FAVORITE";
	public static final String ACTION_PLAYLIST = ".action.ACTION_PLAYLIST";
	public static final String ACTION_DELETE_SONG = ".action.ACTION_DELETE_SONG";

	public static final String TAG_FRAGMENT_TOP_PLAYLIST = "TAG_FRAGMENT_TOP_PLAYLIST";
	public static final String TAG_FRAGMENT_DETAIL_GENRE = "TAG_FRAGMENT_DETAIL_GENRE";
	public static final String TAG_FRAGMENT_SEARCH = "TAG_FRAGMENT_SEARCH";
	public static final String TAG_FRAGMENT_DETAIL_PLAYLIST = "TAG_FRAGMENT_DETAIL_PLAYLIST";

	public static final String URL_FORMAT_LINK_APP = "https://play.google.com/store/apps/details?id=%1$s";

	public static final String KEY_HEADER = "KEY_HEADER";
	public static final String KEY_SHOW_URL = "KEY_SHOW_URL";
	public static final String KEY_SONG_ID = "KEY_SONG_ID";
	public static final String KEY_BONUS = "bonus_data";
	public static final String KEY_TYPE = "type";

	public static final int TYPE_FILTER_SAVED =5;

	public static final int TYPE_PLAYLIST =9;
	public static final int TYPE_DELETE =11;
	public static final int TYPE_EDIT_SONG =12;

	public static final int TYPE_DETAIL_PLAYLIST =12;
	public static final int TYPE_DETAIL_TOP_PLAYLIST =13;
	public static final int TYPE_DETAIL_GENRE=16;

	public static final int TYPE_UI_LIST=1;
	public static final int TYPE_UI_GRID=2;

	public static final String FILE_GENRE = "genre.dat";
	public static final String FILE_PLAYLIST = "playlists.dat";
	public static final String FILE_SAVED_TRACK = "tracks.dat";
	public static final String FILE_CONFIGURE= "config.json";

	public static final String DIR_TEMP = ".temp";

	public static final int RATE_EFFECT = 10;
	public static final int ONE_MINUTE = 60000;
	public static final int MAX_SLEEP_MODE = 120;
	public static final int MIN_SLEEP_MODE = 5;
	public static final int STEP_SLEEP_MODE = 5;

	public static final String URL_FORMAT_SUGESSTION="http://suggestqueries.google.com/complete/search?ds=yt&output=toolbar&hl=%1$s&q=%2$s";
	public static final long TIME_OUT_LOAD_ADS = 10000;
	public static final String FORMAT_URI = "content://media/external/audio/media/%1$s";


}
