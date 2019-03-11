package com.mihwapp.crazymusic.adapter;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mihwapp.crazymusic.R;
import com.mihwapp.crazymusic.YPYFragmentActivity;
import com.mihwapp.crazymusic.abtractclass.DBRecyclerViewAdapter;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.imageloader.GlideImageLoader;
import com.mihwapp.crazymusic.model.PlaylistModel;
import com.mihwapp.crazymusic.view.MaterialIconView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.freemusic.com
 * @Project:NhacVui
 * @Date:Jul 14, 2015
 */

public class PlaylistAdapter extends DBRecyclerViewAdapter implements IXMusicConstants {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();
    private int mTypeUI;

    private OnPlaylistListener onPlaylistListener;
    private LayoutInflater mInflater;

    public PlaylistAdapter(YPYFragmentActivity mContext, ArrayList<PlaylistModel> mListObjects, View mHeaderView, int typeUI) {
        super(mContext, mListObjects, mHeaderView);
        this.mContext = mContext;
        this.mTypeUI = typeUI;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnPlaylistListener(OnPlaylistListener onPlaylistListener) {
        this.onPlaylistListener = onPlaylistListener;
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder1, int position) {
        final PlaylistModel mPlaylistModel = (PlaylistModel) mListObjects.get(position);
        final PlaylistHolder mHolder = ((PlaylistHolder) holder1);

        mHolder.mTvPlaylistName.setText(mPlaylistModel.getName());
        long size = mPlaylistModel.getNumberVideo();
        String data;
        if (size <= 1) {
            data = String.format(mContext.getString(R.string.format_number_music), String.valueOf(size));
        }
        else {
            data = String.format(mContext.getString(R.string.format_number_musics), String.valueOf(size));
        }
        String artwork = mPlaylistModel.getArtwork();
        if (!TextUtils.isEmpty(artwork)) {
            GlideImageLoader.displayImage(mContext, mHolder.mImgPlaylist, artwork, R.drawable.ic_rect_music_default);
        }
        else {
            Uri mUri = mPlaylistModel.getURI();
            if (mUri != null) {
                GlideImageLoader.displayImageFromMediaStore(mContext, mHolder.mImgPlaylist, mUri, R.drawable.ic_rect_music_default);
            }
            else {
                mHolder.mImgPlaylist.setImageResource(R.drawable.ic_rect_music_default);
            }
        }

        mHolder.mTvNumberMusic.setText(data);
        if (mHolder.mCardView != null) {
            mHolder.mCardView.setOnClickListener(view -> {
                if (onPlaylistListener != null) {
                    onPlaylistListener.onViewDetail(mPlaylistModel);
                }
            });
        }
        else {
            mHolder.mLayoutRoot.setOnClickListener(view -> {
                if (onPlaylistListener != null) {
                    onPlaylistListener.onViewDetail(mPlaylistModel);
                }
            });
        }

        mHolder.mImgMenu.setOnClickListener(view -> {
            if (onPlaylistListener != null) {
                onPlaylistListener.showPopUpMenu(mHolder.mImgMenu, mPlaylistModel);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        View mView = mInflater.inflate(mTypeUI == TYPE_UI_GRID ? R.layout.item_grid_playlist : R.layout.item_list_playlist, v, false);
        PlaylistHolder mHolder = new PlaylistHolder(mView);
        return mHolder;
    }

    public interface OnPlaylistListener {
        public void onViewDetail(PlaylistModel mPlaylistObject);
        public void showPopUpMenu(View v, PlaylistModel mPlaylistObject);
    }

    public class PlaylistHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        @Nullable
        public CardView mCardView;

        @BindView(R.id.tv_playlist_name)
        public TextView mTvPlaylistName;

        @BindView(R.id.tv_number_music)
        public TextView mTvNumberMusic;

        @BindView(R.id.img_menu)
        public MaterialIconView mImgMenu;

        @BindView(R.id.img_playlist)
        public ImageView mImgPlaylist;

        @BindView(R.id.layout_root)
        public View mLayoutRoot;

        public PlaylistHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this,convertView);

        }
    }

}
