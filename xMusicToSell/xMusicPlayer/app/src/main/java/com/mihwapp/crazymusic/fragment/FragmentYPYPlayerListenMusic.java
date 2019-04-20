package com.mihwapp.crazymusic.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.mihwapp.crazymusic.R;
import com.mihwapp.crazymusic.YPYMainActivity;
import com.mihwapp.crazymusic.abtractclass.fragment.DBFragment;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.dataMng.MusicDataMng;
import com.mihwapp.crazymusic.imageloader.GlideImageLoader;
import com.mihwapp.crazymusic.imageloader.target.GlideViewGroupTarget;
import com.mihwapp.crazymusic.lyrics.Lyrics;
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.setting.YPYSettingManager;
import com.mihwapp.crazymusic.utilities.DownloadThread;
import com.mihwapp.crazymusic.utils.AdsManager;
import com.mihwapp.crazymusic.view.CircularProgressBar;
import com.mihwapp.crazymusic.view.MaterialIconView;
import com.mihwapp.crazymusic.view.SliderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import butterknife.BindView;
import eu.gsottbauer.equalizerview.EqualizerView;


import static com.mihwapp.crazymusic.playservice.IYPYMusicConstant.ACTION_NEXT;
import static com.mihwapp.crazymusic.playservice.IYPYMusicConstant.ACTION_PLAY;
import static com.mihwapp.crazymusic.playservice.IYPYMusicConstant.ACTION_PREVIOUS;
import static com.mihwapp.crazymusic.playservice.IYPYMusicConstant.ACTION_TOGGLE_PLAYBACK;

public class FragmentYPYPlayerListenMusic extends DBFragment implements IXMusicConstants,View.OnClickListener, Lyrics.Callback {

    public static final String TAG = FragmentYPYPlayerListenMusic.class.getSimpleName();

    @BindView(R.id.layout_listen_bg)
    RelativeLayout mLayoutBg;

    @BindView(R.id.img_track)
    ImageView mImgTrack;

    @BindView(R.id.tv_current_song)
    TextView mTvSong;

    @BindView(R.id.tv_current_singer)
    TextView mTvSinger;

    @BindView(R.id.big_equalizer)
    EqualizerView mEqualizer;

    @BindView(R.id.fb_play)
    FloatingActionButton mFloatingActionButton;

    @BindView(R.id.progressBar1)
    CircularProgressBar mProgressBar;

    @BindView(R.id.layout_control)
    RelativeLayout mLayoutControl;

    @BindView(R.id.layout_content)
    LinearLayout mLayoutContent;

    @BindView(R.id.btn_play)
    MaterialIconView mBtnPlay;

    @BindView(R.id.tv_current_time)
    TextView mTvCurrentTime;

    @BindView(R.id.tv_duration)
    TextView mTvDuration;

    @BindView(R.id.seekBar1)
    SliderView mSeekbar;

    @BindView(R.id.cb_shuffle)
    ImageView mCbShuffe;

    @BindView(R.id.cb_repeat)
    ImageView mCbRepeat;

    @BindView(R.id.img_sound_cloud)
    ImageView mIconSoundCloud;

    @BindView(R.id.cover_container)
    RelativeLayout coverContainer;

    @BindView(R.id.lyrics_icon)
    ImageView lyricsIcon;

    @BindView(R.id.lyrics_container)
    RelativeLayout lyricsContainer;

    @BindView(R.id.lyrics_loading_indicator)
    AVLoadingIndicatorView lyricsLoadingIndicator;

    @BindView(R.id.lyrics_status_text)
    TextView lyricsStatus;

    @BindView(R.id.lyrics_content)
    TextView lyricsContent;

    @BindView(R.id.view_bg)
    View bgView;

    @BindView(R.id.et_artist)
    EditText etArtist;

    @BindView(R.id.et_title)
    EditText etTitle;

    @BindView(R.id.btn_search_lyrics)
    Button btnSearchLyrics;

    public static final int[] RES_ID_CLICKS = {R.id.btn_close,
            R.id.img_share,R.id.btn_next,R.id.btn_prev,R.id.img_add_playlist
            ,R.id.img_equalizer,R.id.img_sleep_mode};

    public static final int[] RES_IMAGE = {R.drawable.ic_arrow_down_white_36dp,
            R.drawable.ic_share_white_36dp,R.drawable.ic_skip_next_white_36dp,R.drawable.ic_skip_previous_white_36dp
            ,R.drawable.ic_add_to_playlist_white_36dp
            ,R.drawable.ic_equalizer_white_36dp,R.drawable.ic_sleep_mode_white_36dp};

    private YPYMainActivity mContext;

    private TrackModel mCurrentTrackObject;
    private ArrayList<TrackModel> mListSongs;
    private long mCurrentId;

    private GlideViewGroupTarget mTarget;

    public Boolean isLyricsVisisble = false;
    public Lyrics currentLyrics = null;
    public DownloadThread downloadThread;

    @Override
    public void onLyricsDownloaded(Lyrics lyrics) {
        currentLyrics = lyrics;
        lyricsLoadingIndicator.setVisibility(View.GONE);

        if (currentLyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
            lyricsContent.setText(Html.fromHtml(currentLyrics.getText()));
            lyricsStatus.setVisibility(View.GONE);

            etArtist.setVisibility(View.GONE);
            etTitle.setVisibility(View.GONE);
            btnSearchLyrics.setVisibility(View.GONE);

            AdsManager.Companion.getInstance().showInterstitial();
        } else {
            lyricsStatus.setText(R.string.lbl_lyrics_not_found);
            lyricsStatus.setVisibility(View.VISIBLE);

            etArtist.setVisibility(View.VISIBLE);
            etTitle.setVisibility(View.VISIBLE);
            btnSearchLyrics.setVisibility(View.VISIBLE);
        }
    }

    private String getTitle() {
        return mCurrentTrackObject.getTitle();
    }

    private String getArtist() {
        return mCurrentTrackObject.getAuthor();
    }

    @Override
    public View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_listen_music, container, false);
    }

    @Override
    public void findView() {
        this.mContext = (YPYMainActivity) getActivity();

        int len=RES_ID_CLICKS.length;
        for(int i=0;i<len;i++){
            ImageView mImgView = mRootView.findViewById(RES_ID_CLICKS[i]);
            mImgView.setOnClickListener(this);
            mContext.setUpImageViewBaseOnColor(mImgView,mContext.mIconColor,RES_IMAGE[i],false);
        }

        mFloatingActionButton = mRootView.findViewById(R.id.fb_play);
        mFloatingActionButton.setColorNormal(mContext.getResources().getColor(R.color.colorAccent));
        mFloatingActionButton.setColorPressed(mContext.getResources().getColor(R.color.colorAccent));
        mFloatingActionButton.setColorRipple(getResources().getColor(R.color.main_color_divider));
        mFloatingActionButton.setOnClickListener(this);

        this.mProgressBar.setVisibility(View.VISIBLE);

        mSeekbar.setProcessColor(getResources().getColor(R.color.colorAccent));
        mSeekbar.setBackgroundColor(getResources().getColor(R.color.default_image_color));
        this.mSeekbar.setOnValueChangedListener(value -> {
            if (mCurrentTrackObject != null) {
                int currentPos = (int) (value * mCurrentTrackObject.getDuration() / 100f);
                mContext.onProcessSeekAudio(currentPos);
            }
        });

        this.mCbShuffe.setOnClickListener(this);
        updateTypeShuffle();
        setUpBackground();

        this.mCbRepeat.setOnClickListener(this);
        updateTypeRepeat();

        mCurrentTrackObject=MusicDataMng.getInstance().getCurrentTrackObject();
        mCurrentId =mCurrentTrackObject!=null?mCurrentTrackObject.getId():0;

        boolean isLoading = MusicDataMng.getInstance().isLoading();
        showLoading(isLoading);

        if(MusicDataMng.getInstance().isPlayingMusic()){
            mEqualizer.animateBars();
        }
        else{
            mEqualizer.stopBars();
        }
        onPlayerUpdateState(MusicDataMng.getInstance().isPlayingMusic());

        updateInformation();

        lyricsIcon.setOnClickListener(view -> {
            showLyrics(!isLyricsVisisble);
        });

        bgView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

//        etArtist.setText("Michael Jackson");
//        etTitle.setText("Beat it");

        btnSearchLyrics.setOnClickListener(view -> {
            String artist = etArtist.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            if (artist == null || artist.isEmpty()) {
                Toast.makeText(mContext, "Please enter artist name", Toast.LENGTH_SHORT).show();
            } else if (title == null || title.isEmpty()) {
                Toast.makeText(mContext, "Please enter song title", Toast.LENGTH_SHORT).show();
            } else {
                doSearcLyrics(artist, title);
            }
        });
    }

    private void doSearcLyrics(String artist, String title) {

        if (downloadThread != null) {
            downloadThread.interrupt();
        }

        downloadThread = new DownloadThread(FragmentYPYPlayerListenMusic.this, false, artist, title);
        downloadThread.start();

        lyricsLoadingIndicator.setVisibility(View.VISIBLE);
        etArtist.setVisibility(View.GONE);
        etTitle.setVisibility(View.GONE);
        btnSearchLyrics.setVisibility(View.GONE);

        lyricsStatus.setText(getString(R.string.lbl_searching_lyrics));
        lyricsStatus.setVisibility(View.VISIBLE);
    }

    private void showLyrics(Boolean show) {
        if (show) {
            lyricsIcon.setAlpha(1.0f);
            lyricsContainer.setVisibility(View.VISIBLE);

            coverContainer.setVisibility(View.GONE);
            if (currentLyrics == null) {
                doSearcLyrics(getArtist(), getTitle());
            } else {
                onLyricsDownloaded(currentLyrics);
            }

        } else {
            lyricsIcon.setAlpha(0.5f);
            lyricsContent.setText("");
            lyricsContainer.setVisibility(View.GONE);
            coverContainer.setVisibility(View.VISIBLE);
        }
        isLyricsVisisble = show;
    }

    private void resetLyricsSearch() {
        currentLyrics = null;
        if (downloadThread != null) {
            downloadThread.interrupt();
        }
        if (isLyricsVisisble) {
            isLyricsVisisble = false;
            lyricsContent.setText("");
            lyricsContainer.setVisibility(View.GONE);
            lyricsIcon.setAlpha(0.5f);
            coverContainer.setVisibility(View.VISIBLE);
        }
    }


    private void updateTypeShuffle(){
        if(mCbShuffe!=null){
            int color =getResources().getColor(YPYSettingManager.getShuffle(mContext) ? R.color.colorAccent : R.color.icon_color);
            mContext.setUpImageViewBaseOnColor(mCbShuffe,color,R.drawable.ic_shuffle_white_36dp,false);
        }
    }

    private void updateTypeRepeat(){
        if(mCbRepeat!=null){
            int type= YPYSettingManager.getNewRepeat(mContext);
            if(type==0){
                mContext.setUpImageViewBaseOnColor(mCbRepeat,mContext.mIconColor,R.drawable.ic_repeat_white_36dp,false);
            }
            else if(type==1){
                mContext.setUpImageViewBaseOnColor(mCbRepeat,mContext.getResources().getColor(R.color.colorAccent),R.drawable.ic_repeat_one_white_36dp,false);
            }
            else if(type==2){
                mContext.setUpImageViewBaseOnColor(mCbRepeat,mContext.getResources().getColor(R.color.colorAccent),R.drawable.ic_repeat_white_36dp,false);
            }
        }

    }

    public void setUpInfo(ArrayList<TrackModel> mListSongs){
        if(this.mListSongs!=null){
            this.mListSongs.clear();
            this.mListSongs=null;
        }
        mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        if (mListSongs == null || mListSongs.size() == 0 || mCurrentTrackObject == null) {
            return;
        }
        this.mListSongs = (ArrayList<TrackModel>) mListSongs.clone();
        updateInformation();

    }


    public void updateInformation() {

        resetLyricsSearch();

        final TrackModel mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        if (mCurrentTrackObject != null) {
            this.mTvSong.setText(String.format(getString(R.string.format_current_song), mCurrentTrackObject.getTitle()));

            String path = mCurrentTrackObject.getPath();
            boolean isOnlineTrack = TextUtils.isEmpty(path);
            this.mIconSoundCloud.setVisibility(isOnlineTrack?View.VISIBLE:View.GONE);

            String artist = mCurrentTrackObject.getAuthor();
            if (!TextUtils.isEmpty(artist) && !artist.equalsIgnoreCase(PREFIX_UNKNOWN)) {
                mTvSinger.setText(String.format(getString(R.string.format_current_singer), mCurrentTrackObject.getAuthor()));
            }
            else {
                mTvSinger.setText(String.format(getString(R.string.format_current_singer), mContext.getString(R.string.title_unknown)));
            }
            String artworkUrl = mCurrentTrackObject.getArtworkUrl();
            if (!TextUtils.isEmpty(artworkUrl)) {
                GlideImageLoader.displayImage(mContext,mImgTrack,artworkUrl,R.drawable.ic_disk);
            }
            else {
                Uri mUri = mCurrentTrackObject.getURI();
                if (mUri!=null) {
                    GlideImageLoader.displayImageFromMediaStore(mContext,mImgTrack,mUri,R.drawable.ic_disk);
                }
                else {
                    mImgTrack.setImageResource(R.drawable.ic_disk);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mEqualizer!=null){
            mEqualizer.stopBars();
        }
        if(mListSongs!=null){
            mListSongs.clear();
            mListSongs=null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                mContext.startMusicService(ACTION_NEXT);

                AdsManager.Companion.getInstance().showInterstitial();
                break;
            case R.id.btn_prev:
                mContext.startMusicService(ACTION_PREVIOUS);

                AdsManager.Companion.getInstance().showInterstitial();
                break;
            case R.id.fb_play:
                onActionPlay();
                break;
            case R.id.cb_shuffle:
                boolean b = YPYSettingManager.getShuffle(mContext);
                YPYSettingManager.setShuffle(mContext, !b);
                updateTypeShuffle();
                break;
            case R.id.cb_repeat:
                int repeat = YPYSettingManager.getNewRepeat(mContext);
                repeat++;
                if(repeat>2){
                    repeat=0;
                }
                YPYSettingManager.setNewRepeat(mContext, repeat);
                updateTypeRepeat();
                break;
            case R.id.img_add_playlist:
                TrackModel mCurrentTrack = MusicDataMng.getInstance().getCurrentTrackObject();
                if (mCurrentTrack != null) {
                    mContext.showDialogPlaylist(mCurrentTrack, () -> mContext.notifyData(TYPE_PLAYLIST));
                }
                break;
            case R.id.img_share:
                TrackModel mCurrentTrack1 = MusicDataMng.getInstance().getCurrentTrackObject();
                if (mCurrentTrack1 != null) {
                    mContext.shareFile(mCurrentTrack1);
                }
                break;
            case R.id.btn_close:
                mContext.collapseListenMusic();
                break;
            case R.id.img_equalizer:
                mContext.goToEqualizer();
                break;
            case R.id.img_sleep_mode:
                mContext.showDialogSleepMode();
                break;
            default:
                break;
        }
    }
    public void onActionPlay(){
        ArrayList<TrackModel> mListMusics = MusicDataMng.getInstance().getListPlayingTrackObjects();
        int size = mListMusics!=null?mListMusics.size():0;
        if(size>0){
            if(MusicDataMng.getInstance().isPrepaireDone()){
                mContext.startMusicService(ACTION_TOGGLE_PLAYBACK);
                return;
            }
        }
        if(mListSongs!=null && mListSongs.size()>0){
            MusicDataMng.getInstance().setListPlayingTrackObjects((ArrayList<TrackModel>) mListSongs.clone());
            for(TrackModel mTrackObject:mListSongs){
                if(mTrackObject.getId()==mCurrentId){
                    MusicDataMng.getInstance().setCurrentIndex(mTrackObject);
                    mContext.startMusicService(ACTION_PLAY);
                    return;
                }
            }
            MusicDataMng.getInstance().setCurrentIndex(mListSongs.get(0));
            mContext.startMusicService(ACTION_PLAY);
        }

    }

    public void showLoading(boolean isShow) {
        if(mLayoutContent!=null){
            mLayoutContent.setVisibility(isShow ? View.INVISIBLE : View.VISIBLE);
            mProgressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
            mEqualizer.stopBars();
            if(isShow){
                updateInformation();
            }

        }
    }

    public void onUpdatePos(long currentPos){
        if (currentPos > 0 && mCurrentTrackObject != null && mTvCurrentTime!=null) {
            mTvCurrentTime.setText(mContext.getStringDuration(currentPos/1000));
            int percent = (int) (((float) currentPos / (float) mCurrentTrackObject.getDuration()) * 100f);
            mSeekbar.setValue(percent);
        }
    }

    public void onPlayerStop() {
        if(mBtnPlay!=null){
            mBtnPlay.setText(Html.fromHtml(getString(R.string.icon_play)));
            mSeekbar.setValue(0);
            mTvCurrentTime.setText(mContext.getStringDuration(0));
            mTvDuration.setText(mContext.getStringDuration(0));

            mEqualizer.stopBars();
            setUpInfo(null);
        }

    }
    public void onPlayerUpdateState(boolean isPlay) {
        if(mBtnPlay!=null){
            mBtnPlay.setText(Html.fromHtml(getString(isPlay ? R.string.icon_pause : R.string.icon_play)));
            mCurrentTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
            if(mCurrentTrackObject!=null){
                mCurrentId=mCurrentTrackObject.getId();
                if (mCurrentTrackObject != null) {
                    mTvDuration.setText(mContext.getStringDuration(mCurrentTrackObject.getDuration()/1000));
                }
            }
            if(isPlay){
                mEqualizer.animateBars();
            }
            else{
                mEqualizer.stopBars();
            }
        }

    }

    public void setUpBackground(){
        try{
            if(mLayoutBg!=null){
                mTarget =new GlideViewGroupTarget(mContext,mLayoutBg){
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                    }
                };
                String imgBg = YPYSettingManager.getBackground(mContext);
                Log.e("DCM","=============>getBackground="+imgBg);
                if(!TextUtils.isEmpty(imgBg)){
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.default_bg_app)
                            .transform(mContext.mBlurBgTranform)
                            .priority(Priority.HIGH);
                    Glide.with(this).asBitmap().apply(options).load(Uri.parse(imgBg)).into(mTarget);
                }
                else{
                    mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
