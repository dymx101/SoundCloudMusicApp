package com.musichero.xmusic.listener;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project: TemplateChangeTheme
 * Created by dotrungbao on 8/8/15.
 */
public interface IDBMusicPlayerListener {
	public void onPlayerUpdateState(boolean isPlay);
	public void onPlayerStop();
	public void onPlayerLoading();
	public void onPlayerStopLoading();
	public void onPlayerUpdatePos(int currentPos);
	public void onPlayerError();
	public void onPlayerUpdateStatus();
}
