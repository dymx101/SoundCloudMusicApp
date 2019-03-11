package com.mihwapp.crazymusic.fragment;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mihwapp.crazymusic.R;
import com.mihwapp.crazymusic.YPYMainActivity;
import com.mihwapp.crazymusic.abtractclass.fragment.DBFragment;
import com.mihwapp.crazymusic.adapter.TrackAdapter;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.dataMng.MusicNetUtils;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.model.ConfigureModel;
import com.mihwapp.crazymusic.model.GenreModel;
import com.mihwapp.crazymusic.model.PlaylistModel;
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.playservice.IYPYMusicConstant;
import com.mihwapp.crazymusic.utils.DBLog;
import com.mihwapp.crazymusic.utils.StringUtils;
import com.mihwapp.crazymusic.view.CircularProgressBar;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * 
 * 
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:MusicPlayer
 * @Date:Dec 25, 2014
 * 
 */
public class FragmentYPYDetailTracks extends DBFragment implements IXMusicConstants,IYPYMusicConstant {

	public static final String TAG = FragmentYPYDetailTracks.class.getSimpleName();

	private YPYMainActivity mContext;

	@BindView(R.id.tv_no_result)
	TextView mTvNoResult;

	@BindView(R.id.progressBar1)
	CircularProgressBar mProgressBar;

	@BindView(R.id.list_datas)
	RecyclerView mRecyclerView;

	private ArrayList<TrackModel> mListObjects;
	private TrackAdapter mTrackAdapter;

	private int mTypeDetail;

	private int mTypeUI;

	@Override
	public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_recyclerview, container, false);
	}

	@Override
	public void findView() {
		mContext = (YPYMainActivity) getActivity();
		ConfigureModel configureModel= mContext.mTotalMng.getConfigureModel();
		mTypeUI=configureModel!=null?configureModel.getTypeDetail():TYPE_UI_LIST;
		if(mTypeUI==TYPE_UI_LIST){
			mContext.setUpRecyclerViewAsListView(mRecyclerView,null);
		}
		else{
			mContext.setUpRecyclerViewAsGridView(mRecyclerView,2);
		}
		startLoadData();
	}

	@Override
	public void startLoadData() {
		if(mContext!=null && mRecyclerView !=null){
			mProgressBar.setVisibility(View.VISIBLE);
			DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
				ArrayList<TrackModel> mListNormal = getListTracks();
				final ArrayList<TrackModel> mListTracks = mListNormal!=null? (ArrayList<TrackModel>) mListNormal.clone() :null;
				mContext.runOnUiThread(() -> {
					mProgressBar.setVisibility(View.GONE);
					setUpInfo(mListTracks);
				});
			});
		}

	}

	private ArrayList<TrackModel> getListTracks(){
		if(mTypeDetail== TYPE_DETAIL_PLAYLIST){
			PlaylistModel mPlaylistObject = mContext.mTotalMng.getPlaylistObject();
			if(mPlaylistObject!=null){
				ArrayList<TrackModel> mListObjects = mPlaylistObject.getListTrackObjects();
				return mListObjects;
			}
		}
		else if(mTypeDetail== TYPE_DETAIL_TOP_PLAYLIST){
			PlaylistModel mPlaylistObject = mContext.mTotalMng.getPlaylistObject();
			if(mPlaylistObject!=null){
				ArrayList<TrackModel> mListObjects = mPlaylistObject.getListTrackObjects();
				if(mListObjects==null){
					String query = StringUtils.urlEncodeString(mPlaylistObject.getName())+"+"+StringUtils.urlEncodeString(mPlaylistObject.getArtist());
					mListObjects = MusicNetUtils.getListTrackObjectsByQuery(query, 0, MAX_TOP_PLAYLIST_SONG);
					DBLog.d(TAG,"=========>size playlist="+(mListObjects!=null?mListObjects.size():0));
					if(mListObjects!=null && mListObjects.size()>0){
						mPlaylistObject.setListTrackObjects(mListObjects);
					}
				}
				return mListObjects;
			}
		}
		else if(mTypeDetail== TYPE_DETAIL_GENRE){
			GenreModel mGenreObject = mContext.mTotalMng.getGenreObject();
			if(mGenreObject !=null){
				ArrayList<TrackModel> mListTracks = mGenreObject.getListTrackObjects();
				if(mListTracks==null){
					mListTracks = MusicNetUtils.getListHotTrackObjectsInGenre(mGenreObject.getKeyword(),0,MAX_SONG_CACHED);
				}
				return mListTracks;

			}
		}
		return null;
	}


	private void setUpInfo(final ArrayList<TrackModel> mListDatas) {
		mRecyclerView.setAdapter(null);
		if(this.mListObjects !=null){
			this.mListObjects.clear();
			this.mListObjects =null;
		}
		this.mListObjects = mListDatas;
		if(mListDatas!=null && mListDatas.size()>0){
			mTrackAdapter = new TrackAdapter(mContext,mListDatas,mTypeUI);
			mRecyclerView.setAdapter(mTrackAdapter);
			mTrackAdapter.setOnTrackListener(new TrackAdapter.OnTrackListener() {
				@Override
				public void onListenTrack(TrackModel mTrackObject) {
					mContext.startPlayingList(mTrackObject, mListDatas);
				}

				@Override
				public void onShowMenu(View mView, TrackModel mTrackObject) {
					if(mTypeDetail!=TYPE_DETAIL_PLAYLIST){
						mContext.showPopupMenu(mView, mTrackObject);
					}
					else{
						mContext.showPopupMenu(mView, mTrackObject,mContext.mTotalMng.getPlaylistObject());
					}

				}
			});
		}
		updateInfo();
	}


	private void updateInfo() {
		if(mTvNoResult!=null){
			boolean b= mListObjects != null && mListObjects.size() > 0;
			mTvNoResult.setVisibility(b ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(this.mListObjects !=null){
			this.mListObjects.clear();
			this.mListObjects =null;
		}
	}

	@Override
	public void notifyData(){
		startLoadData();
	}

	@Override
	public void justNotifyData() {
		super.justNotifyData();
		if(mTrackAdapter!=null){
			mTrackAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onExtractData() {
		super.onExtractData();
		Bundle args = getArguments();
		if (args != null) {
			mTypeDetail =args.getInt(KEY_TYPE,-1);
		}
	}
	public boolean isCheckBack(){
		if(mProgressBar!=null && mProgressBar.getVisibility()==View.VISIBLE){
			return true;
		}
		return false;
	}
}
