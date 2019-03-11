package com.mihwapp.crazymusic.model;


import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.ads.NativeExpressAdView;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * 
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.mihwapp.com
 * @Project:MusicPlayer
 * @Date:Dec 29, 2014
 * 
 */
public class PlaylistModel {

	@SerializedName("id")
	private long id;

	@SerializedName("name")
	private String name;

	@SerializedName("datas")
	private ArrayList<Long> listTrackIds;

	private transient ArrayList<TrackModel> listTrackObjects;

	private transient String artist;
	private transient String artwork;

	private transient boolean isNativeAds;
	private transient NativeExpressAdView nativeExpressAdView;

	public PlaylistModel(long id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.listTrackObjects = new ArrayList<>();
	}

	public PlaylistModel(String name) {
		super();
		this.name = name;
	}

	public PlaylistModel() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<TrackModel> getListTrackObjects() {
		return listTrackObjects;
	}

	public void setListTrackObjects(ArrayList<TrackModel> listTrackObjects) {
		this.listTrackObjects = listTrackObjects;
	}

	public ArrayList<Long> getListTrackIds() {
		return listTrackIds;
	}

	public void setListTrackIds(ArrayList<Long> listTrackIds) {
		this.listTrackIds = listTrackIds;
		if(listTrackObjects==null){
			listTrackObjects= new ArrayList<>();
		}
	}

	public void addTrackObject(TrackModel mTrackObject, boolean isAddId) {
		if(listTrackObjects==null){
			listTrackObjects= new ArrayList<>();
		}
		if(listTrackObjects!=null && mTrackObject!=null){
			listTrackObjects.add(mTrackObject);
			if(isAddId){
				if(listTrackIds!=null){
					listTrackIds.add(mTrackObject.getId());
				}
			}
		}
	}

	public void removeTrackObject(TrackModel mTrackObject) {
		if (listTrackObjects != null && listTrackObjects.size()>0 && mTrackObject != null) {
			Iterator<TrackModel> mIterator = listTrackObjects.iterator();
			while (mIterator.hasNext()) {
				TrackModel trackObject = mIterator.next();
				if (trackObject.getId() == mTrackObject.getId()) {
					mIterator.remove();
					break;
				}
			}
			if(listTrackIds!=null && listTrackIds.size()>0){
				Iterator<Long> mTrackIdIterator = listTrackIds.iterator();
				while (mTrackIdIterator.hasNext()) {
					long id = mTrackIdIterator.next();
					if (id == mTrackObject.getId()) {
						mTrackIdIterator.remove();
						break;
					}
				}
			}

		}
	}

	public boolean isSongAlreadyExited(long id) {
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			for(TrackModel mTrackObject:listTrackObjects){
				if(mTrackObject.getId()==id){
					return true;
				}
			}
		}
		return false;
	}


	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getArtwork() {
		if(TextUtils.isEmpty(artwork)){
			if(listTrackObjects!=null && listTrackObjects.size()>0){
				return listTrackObjects.get(0).getArtworkUrl();
			}
		}
		return artwork;
	}

	public Uri getURI() {
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			return listTrackObjects.get(0).getURI();
		}
		return null;
	}

	public void setArtwork(String artwork) {
		this.artwork = artwork;
	}

	public boolean isNativeAds() {
		return isNativeAds;
	}

	public void setNativeAds(boolean nativeAds) {
		isNativeAds = nativeAds;
	}

	public NativeExpressAdView getNativeExpressAdView() {
		return nativeExpressAdView;
	}

	public void setNativeExpressAdView(NativeExpressAdView nativeExpressAdView) {
		this.nativeExpressAdView = nativeExpressAdView;
	}
	public long getNumberVideo() {
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			return listTrackObjects.size();
		}
		return 0;
	}
}
