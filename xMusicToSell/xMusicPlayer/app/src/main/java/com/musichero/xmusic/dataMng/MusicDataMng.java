package com.musichero.xmusic.dataMng;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;

import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.utils.DBLog;

import java.util.ArrayList;
import java.util.Random;

public class MusicDataMng {

	private static final String TAG = MusicDataMng.class.getSimpleName();

	private static MusicDataMng instance;
	private ArrayList<TrackModel> listTrackObjects;
	private int currentIndex=-1;
	private TrackModel currentTrackObject;
	private Random mRandom;
	private MediaPlayer player;
	private long currentPlayingId;

	private boolean isLoading;

	private Equalizer equalizer;
	private BassBoost bassBoost;
	private Virtualizer virtualizer;

	private MusicDataMng() {
		mRandom = new Random();
	}

	public static MusicDataMng getInstance() {
		if (null == instance) {
			instance = new MusicDataMng();
		}
		return instance;
	}

	public void onDestroy() {
		DBLog.d(TAG, "====================>onDestroy");
		if (listTrackObjects != null) {
			listTrackObjects.clear();
			listTrackObjects = null;
		}
		currentTrackObject=null;
		mRandom=null;
		player=null;
		instance = null;
	}

	public void releaseEffect(){
		try {
			if(equalizer!=null){
				equalizer.release();
				equalizer=null;
			}
			if(bassBoost!=null){
				bassBoost.release();
				bassBoost=null;
			}
			if(virtualizer!=null){
				virtualizer.release();
				virtualizer=null;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void onResetData() {
		if (listTrackObjects != null) {
			listTrackObjects.clear();
			listTrackObjects = null;
		}
		isLoading=false;
		currentIndex=-1;
		currentTrackObject=null;
	}

	public void setListPlayingTrackObjects(ArrayList<TrackModel> listTrackObjects) {
		if(this.listTrackObjects!=null){
			this.listTrackObjects.clear();
			this.listTrackObjects=null;
		}
		isLoading=false;
		if(listTrackObjects!=null){
			if(listTrackObjects.size()>0){
				boolean isNeedToReset=true;
				if(currentTrackObject!=null){
					for(TrackModel mTrackObject:listTrackObjects){
						if(currentTrackObject.getId()==mTrackObject.getId()){
							isNeedToReset=false;
							currentIndex=listTrackObjects.indexOf(mTrackObject);
							break;
						}
					}

				}
				if(isNeedToReset){
					currentIndex=0;
				}
			}
			else{
				currentIndex=-1;
			}
		}
		this.listTrackObjects = listTrackObjects;
	}


	public int getCurrentIndex() {
		return currentIndex;
	}

	public boolean setCurrentIndex(TrackModel mTrackObject) {
		if(listTrackObjects!=null && listTrackObjects.size()>0 && mTrackObject!=null){
			this.currentTrackObject = mTrackObject;
			for(TrackModel mTrackObject1:listTrackObjects){
				if(mTrackObject1.getId()==mTrackObject.getId()){
					this.currentIndex = listTrackObjects.indexOf(mTrackObject1);
					break;
				}
			}
			DBLog.d(TAG, "===========>mTrackObject="+mTrackObject.getId()+"===>currentIndex="+currentIndex);
			if(currentIndex<0){
				currentIndex=0;
				return false;
			}
			return true;
		}
		return false;
	}


	public ArrayList<TrackModel> getListPlayingTrackObjects() {
		return listTrackObjects;
	}

	public TrackModel getTrackObject(long id){
		if(listTrackObjects!=null ){
			int size = listTrackObjects.size();
			if(size>0){
				for(TrackModel mTrackObject:listTrackObjects){
					if(mTrackObject.getId()==id){
						return mTrackObject;
					}
				}
			}
		}
		return null;
	}

	public TrackModel getCurrentTrackObject(){
		return currentTrackObject;
	}


	public TrackModel getNextTrackObject(Context mContext, boolean isComplete){
		if(listTrackObjects!=null){
			int size = listTrackObjects.size();
			DBLog.d(TAG, "==========>currentIndex="+currentIndex);
			if( size>0 && currentIndex>=0 && currentIndex<size){
				int typeRepeat= YPYSettingManager.getNewRepeat(mContext);
				if( typeRepeat==1 && isComplete && currentTrackObject!=null){
					return currentTrackObject;
				}
				if (YPYSettingManager.getShuffle(mContext)){
					currentIndex = mRandom.nextInt(size);
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
				else{
					currentIndex++;
					if(currentIndex>=size){
						currentIndex=0;
						currentTrackObject=listTrackObjects.get(currentIndex);
						if(typeRepeat==2){
							return currentTrackObject;
						}
						else{
							return null;
						}
					}
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
			}
		}
		return null;
	}
	public TrackModel getPrevTrackObject(Context mContext){
		if(listTrackObjects!=null){
			int size = listTrackObjects.size();
			if( size>0 && currentIndex>=0 && currentIndex<=size){
				int typeRepeat= YPYSettingManager.getNewRepeat(mContext);
				if(YPYSettingManager.getShuffle(mContext)){
					currentIndex = mRandom.nextInt(size);
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
				else{
					currentIndex--;
					if(currentIndex<0){
						currentIndex=size-1;
						currentTrackObject=listTrackObjects.get(currentIndex);
						if(typeRepeat==2){
							return currentTrackObject;
						}
						else{
							return null;
						}
					}
					currentTrackObject=listTrackObjects.get(currentIndex);
					return currentTrackObject;
				}
			}
		}
		return null;
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	public void setPlayer(MediaPlayer player) {
		this.player = player;
	}

	public long getCurrentPlayingId() {
		return currentPlayingId;
	}

	public void setCurrentPlayingId(long currentPlayingId) {
		this.currentPlayingId = currentPlayingId;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean loading) {
		isLoading = loading;
	}

	public boolean isPlayingMusic(){
		try {
			if (isPrepaireDone()) {
				return player.isPlaying();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean isPrepaireDone(){
		try {
			int sessionId = 0;
			if (player != null) {
				sessionId = player.getAudioSessionId();
			}
			if (sessionId != 0) {
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public Equalizer getEqualizer() {
		return equalizer;
	}

	public void setEqualizer(Equalizer equalizer) {
		this.equalizer = equalizer;
	}

	public BassBoost getBassBoost() {
		return bassBoost;
	}

	public void setBassBoost(BassBoost bassBoost) {
		this.bassBoost = bassBoost;
	}

	public Virtualizer getVirtualizer() {
		return virtualizer;
	}

	public void setVirtualizer(Virtualizer virtualizer) {
		this.virtualizer = virtualizer;
	}
}
