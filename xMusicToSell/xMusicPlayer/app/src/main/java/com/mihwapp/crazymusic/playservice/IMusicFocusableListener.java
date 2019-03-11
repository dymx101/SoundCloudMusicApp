package com.mihwapp.crazymusic.playservice;

public interface IMusicFocusableListener {
	public void onGainedAudioFocus();
	public void onLostAudioFocus(boolean canDuck);
}
