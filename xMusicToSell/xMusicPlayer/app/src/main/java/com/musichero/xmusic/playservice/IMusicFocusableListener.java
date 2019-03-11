package com.musichero.xmusic.playservice;

public interface IMusicFocusableListener {
	public void onGainedAudioFocus();
	public void onLostAudioFocus(boolean canDuck);
}
