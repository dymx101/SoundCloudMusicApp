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
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.utils.StringUtils;
import com.mihwapp.crazymusic.view.MaterialIconView;

import java.util.ArrayList;
import java.util.Locale;

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

public class TrackAdapter extends DBRecyclerViewAdapter implements IXMusicConstants {

    public static final String TAG = TrackAdapter.class.getSimpleName();
    private int mType;

    private OnTrackListener onTrackListener;
    private LayoutInflater mInflater;

    public TrackAdapter(YPYFragmentActivity mContext, ArrayList<TrackModel> mListObjects, int type) {
        super(mContext, mListObjects);
        this.mListObjects = mListObjects;
        this.mType = type;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setOnTrackListener(OnTrackListener onTrackListener) {
        this.onTrackListener = onTrackListener;
    }


    public interface OnTrackListener {
        public void onListenTrack(TrackModel mTrackObject);
        public void onShowMenu(View mView, TrackModel mTrackObject);
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position) {
        final TrackModel mTrackObject = (TrackModel) mListObjects.get(position);
        final TrackHolder mTrackHolder = (TrackHolder) holder;
        mTrackHolder.mTvSongName.setText(mTrackObject.getTitle());
        String author = mTrackObject.getAuthor();
        if (StringUtils.isEmpty(author) || author.toLowerCase(Locale.US).contains(PREFIX_UNKNOWN)) {
            author = mContext.getString(R.string.title_unknown);
        }
        mTrackHolder.mTvSinger.setText(author);

        String artwork = mTrackObject.getArtworkUrl();
        if (!TextUtils.isEmpty(artwork)) {
            GlideImageLoader.displayImage(mContext, mTrackHolder.mImgSongs, artwork, R.drawable.ic_rect_music_default);
        }
        else {
            Uri mUri = mTrackObject.getURI();
            if (mUri != null) {
                GlideImageLoader.displayImageFromMediaStore(mContext, mTrackHolder.mImgSongs, mUri, R.drawable.ic_rect_music_default);
            }
            else {
                mTrackHolder.mImgSongs.setImageResource(R.drawable.ic_rect_music_default);
            }
        }

        if (mTrackHolder.mCardView != null) {
            mTrackHolder.mCardView.setOnClickListener(view -> {
                if (onTrackListener != null) {
                    onTrackListener.onListenTrack(mTrackObject);
                }
            });
        }
        else {
            mTrackHolder.mLayoutRoot.setOnClickListener(view -> {
                if (onTrackListener != null) {
                    onTrackListener.onListenTrack(mTrackObject);
                }
            });
        }
        mTrackHolder.mImgMenu.setOnClickListener(view -> {
            if (onTrackListener != null) {
                onTrackListener.onShowMenu(mTrackHolder.mImgMenu, mTrackObject);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        View mView = mInflater.inflate(mType == TYPE_UI_GRID ? R.layout.item_grid_track : R.layout.item_list_track, v, false);
        RecyclerView.ViewHolder mHolder = new TrackHolder(mView);
        return mHolder;
    }


    public class TrackHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_songs)
        public ImageView mImgSongs;

        @BindView(R.id.img_menu)
        public MaterialIconView mImgMenu;

        @BindView(R.id.tv_song)
        public TextView mTvSongName;

        @BindView(R.id.tv_singer)
        public TextView mTvSinger;

        @BindView(R.id.layout_root)
        public View mLayoutRoot;

        @BindView(R.id.card_view)
        @Nullable
        public CardView mCardView;

        public TrackHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);

        }
    }


}
