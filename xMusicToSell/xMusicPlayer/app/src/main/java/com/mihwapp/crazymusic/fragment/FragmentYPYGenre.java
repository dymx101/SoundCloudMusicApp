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
import com.mihwapp.crazymusic.adapter.GenreAdapter;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.model.ConfigureModel;
import com.mihwapp.crazymusic.model.GenreModel;
import com.mihwapp.crazymusic.view.CircularProgressBar;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;

public class FragmentYPYGenre extends DBFragment implements IXMusicConstants {

    public static final String TAG = FragmentYPYGenre.class.getSimpleName();

    private YPYMainActivity mContext;

    @BindView(R.id.tv_no_result)
    TextView mTvNoResult;

    @BindView(R.id.progressBar1)
    CircularProgressBar mProgressBar;

    @BindView(R.id.list_datas)
    RecyclerView mRecyclerViewTrack;

    private int mTypeUI;
    private GenreAdapter mGenreAdapter;
    private ArrayList<GenreModel> mListGenres;


    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void findView() {
        mContext = (YPYMainActivity) getActivity();

        ConfigureModel configureModel= mContext.mTotalMng.getConfigureModel();
        mTypeUI=configureModel!=null?configureModel.getTypeGenre():TYPE_UI_LIST;
        if(mTypeUI==TYPE_UI_LIST){
            mContext.setUpRecyclerViewAsListView(mRecyclerViewTrack,null);
        }
        else{
            mContext.setUpRecyclerViewAsGridView(mRecyclerViewTrack,3);
        }
        if (isFirstInTab()) {
            startLoadData();
        }

    }

    @Override
    public void startLoadData() {
        if(!isLoadingData() && mContext!=null){
            setLoadingData(true);
            startGetData();
        }
    }



    private void startGetData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTvNoResult.setVisibility(View.GONE);
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            ArrayList<GenreModel> mList = mContext.mTotalMng.getListGenreObjects();
            if(mList==null){
                mContext.mTotalMng.readGenreData(mContext);
                mList = mContext.mTotalMng.getListGenreObjects();
            }
            final ArrayList<GenreModel> finalMList = mList;
            mContext.runOnUiThread(() -> {
                mProgressBar.setVisibility(View.GONE);
                setUpInfo(finalMList);
            });
        });
    }

    private void setUpInfo(ArrayList<GenreModel> mListTracks) {
        mRecyclerViewTrack.setAdapter(null);
        if (this.mListGenres != null) {
            this.mListGenres.clear();
            this.mListGenres = null;
        }
        Collections.reverse(mListTracks);
        this.mListGenres = mListTracks;
        if (mListTracks != null && mListTracks.size() > 0) {
            mGenreAdapter = new GenreAdapter(mContext, mListTracks,mTypeUI);
            mRecyclerViewTrack.setAdapter(mGenreAdapter);
            mGenreAdapter.setOnGenreListener(mGenreModel ->  mContext.goToGenre(mGenreModel));
        }
        updateInfo();

    }
    private void updateInfo() {
        if (mTvNoResult != null) {
            boolean b = mListGenres != null && mListGenres.size() > 0;
            mTvNoResult.setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mListGenres !=null){
            mListGenres.clear();
            mListGenres =null;
        }
    }
}
