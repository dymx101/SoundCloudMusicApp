package com.musichero.xmusic.fragment;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYMainActivity;
import com.musichero.xmusic.abtractclass.DBRecyclerViewAdapter;
import com.musichero.xmusic.abtractclass.fragment.DBFragment;
import com.musichero.xmusic.adapter.PlaylistAdapter;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.PlaylistModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.task.IYPYCallback;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:MusicPlayer
 * @Date:Dec 25, 2014
 */
public class FragmentYPYPlaylist extends DBFragment implements IXMusicConstants, OnClickListener {

    public static final String TAG = FragmentYPYPlaylist.class.getSimpleName();

    private YPYMainActivity mContext;
    private ArrayList<PlaylistModel> mListPlaylist;
    private PlaylistAdapter mPlaylistAdapter;

    @BindView(R.id.list_datas)
    RecyclerView mRecyclerView;

    private View mHeader;
    private int mTypeUI;

    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void findView() {
        mContext = (YPYMainActivity) getActivity();

        setUpHeader();
        ConfigureModel configureModel = mContext.mTotalMng.getConfigureModel();
        mTypeUI = configureModel != null ? configureModel.getTypePlaylist() : TYPE_UI_LIST;
        if (mTypeUI == TYPE_UI_LIST) {
            mContext.setUpRecyclerViewAsListView(mRecyclerView, null);
        }
        else {
            mContext.setUpRecyclerViewAsGridView(mRecyclerView, 2);
            GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (mPlaylistAdapter.getItemViewType(position)) {
                        case DBRecyclerViewAdapter.TYPE_HEADER_VIEW:
                            return 2;
                        default:
                            return 1;
                    }
                }
            });
        }


    }

    public void setUpHeader() {
        mHeader = LayoutInflater.from(mContext).inflate(R.layout.item_header_playlist, null);
        TextView mTvAddPlaylist = mHeader.findViewById(R.id.tv_add_new_playlist);
        mTvAddPlaylist.setTypeface(mContext.mTypefaceBold);
        mHeader.findViewById(R.id.btn_add_playlist).setOnClickListener(this);

    }

    @Override
    public void startLoadData() {
        super.startLoadData();
        if (!isLoadingData() && mContext != null) {
            setLoadingData(true);
            startGetPlaylist();
        }
    }

    private void startGetPlaylist() {
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            ArrayList<PlaylistModel> mListPlaylist = mContext.mTotalMng.getListPlaylistObjects();
            if (mListPlaylist == null) {
                mContext.mTotalMng.readCached(TYPE_FILTER_SAVED);
                mContext.mTotalMng.readPlaylistCached();
                mListPlaylist = mContext.mTotalMng.getListPlaylistObjects();
            }
            ArrayList<PlaylistModel> finalMListPlaylist = mListPlaylist;
            mContext.runOnUiThread(() -> setUpInfo(finalMListPlaylist));
        });
    }

    private void setUpInfo(ArrayList<PlaylistModel> mListPlaylistObjects) {
        mRecyclerView.setAdapter(null);
        this.mListPlaylist = mListPlaylistObjects;
        if (mListPlaylistObjects != null) {
            mPlaylistAdapter = new PlaylistAdapter(mContext, mListPlaylistObjects, mHeader, mTypeUI);
            mRecyclerView.setAdapter(mPlaylistAdapter);
            mPlaylistAdapter.setOnPlaylistListener(new PlaylistAdapter.OnPlaylistListener() {

                @Override
                public void onViewDetail(PlaylistModel mPlaylistObject) {
                    mContext.goToDetailPlaylist(mPlaylistObject, TYPE_DETAIL_PLAYLIST);
                }

                @Override
                public void showPopUpMenu(View v, PlaylistModel mPlaylistObject) {
                    showMenuForPlaylist(v, mPlaylistObject);
                }
            });
        }
    }

    public void showMenuForPlaylist(View v, final PlaylistModel mCurrentPlaylist) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_playlist, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_play_all:
                    if (mCurrentPlaylist != null) {
                        final ArrayList<TrackModel> mListTrackObjects = mCurrentPlaylist.getListTrackObjects();
                        if (mListTrackObjects != null && mListTrackObjects.size() > 0) {
                            mContext.startPlayingList(mListTrackObjects.get(0), mListTrackObjects);
                        }
                        else {
                            mContext.showToast(R.string.info_nosong_playlist);
                        }
                    }
                    break;
                case R.id.action_rename_playlist:
                    if (mCurrentPlaylist != null) {
                        mContext.showDialogCreatePlaylist(true, mCurrentPlaylist, () -> {
                            if (mPlaylistAdapter != null) {
                                mPlaylistAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    break;
                case R.id.action_delete_playlist:
                    if (mCurrentPlaylist != null) {
                        showDialogDelete(mCurrentPlaylist);
                    }
                    break;
            }
            return true;
        });
        popupMenu.show();

    }

    private void showDialogDelete(final PlaylistModel mPlaylistObject) {
        mContext.showFullDialog(R.string.title_confirm, getString(R.string.info_delete_playlist), R.string.title_ok, R.string.title_cancel, new IYPYCallback() {
            @Override
            public void onAction() {
                mContext.mTotalMng.removePlaylistObject(mPlaylistObject);
                notifyData();

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_playlist:
                mContext.showDialogCreatePlaylist(false, null, () -> notifyData());
                break;
            default:
                break;
        }
    }


    @Override
    public void notifyData() {
        if (mContext != null && mPlaylistAdapter != null) {
            mPlaylistAdapter.notifyDataSetChanged();
        }
    }


}
