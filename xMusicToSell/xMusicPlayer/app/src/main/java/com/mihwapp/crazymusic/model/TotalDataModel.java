package com.mihwapp.crazymusic.model;

import java.util.ArrayList;

public class TotalDataModel {
	private ArrayList<TrackModel> listTrackObjects;
	private ArrayList<PlaylistModel> playlistObjects;

	public TotalDataModel() {

	}

	public ArrayList<TrackModel> getListTrackObjects() {
		return listTrackObjects;
	}

	public void setListTrackObjects(ArrayList<TrackModel> listTrackObjects) {
		this.listTrackObjects = listTrackObjects;
	}


	public ArrayList<PlaylistModel> getPlaylistObjects() {
		return playlistObjects;
	}

	public void setPlaylistObjects(ArrayList<PlaylistModel> playlistObjects) {
		this.playlistObjects = playlistObjects;
	}

	public void onDestroy(){
		try{
			if(listTrackObjects!=null){
				listTrackObjects.clear();
				listTrackObjects=null;
			}
			if(playlistObjects!=null){
				playlistObjects.clear();
				playlistObjects=null;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
}
