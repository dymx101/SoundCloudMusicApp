package com.mihwapp.crazymusic;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.mihwapp.crazymusic.abtractclass.fragment.DBFragment;
import com.mihwapp.crazymusic.abtractclass.fragment.DBFragmentAdapter;
import com.mihwapp.crazymusic.adapter.SuggestionAdapter;
import com.mihwapp.crazymusic.dataMng.MusicDataMng;
import com.mihwapp.crazymusic.dataMng.TotalDataManager;
import com.mihwapp.crazymusic.dataMng.XMLParsingData;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.fragment.FragmentYPYDetailTracks;
import com.mihwapp.crazymusic.fragment.FragmentYPYGenre;
import com.mihwapp.crazymusic.fragment.FragmentYPYMyMusic;
import com.mihwapp.crazymusic.fragment.FragmentYPYPlayerListenMusic;
import com.mihwapp.crazymusic.fragment.FragmentYPYPlaylist;
import com.mihwapp.crazymusic.fragment.FragmentYPYSearchTrack;
import com.mihwapp.crazymusic.fragment.FragmentYPYTopChart;
import com.mihwapp.crazymusic.imageloader.GlideImageLoader;
import com.mihwapp.crazymusic.listener.IDBMusicPlayerListener;
import com.mihwapp.crazymusic.listener.IDBSearchViewInterface;
import com.mihwapp.crazymusic.model.ConfigureModel;
import com.mihwapp.crazymusic.model.GenreModel;
import com.mihwapp.crazymusic.model.PlaylistModel;
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.setting.YPYSettingManager;
import com.mihwapp.crazymusic.utils.AdsManager;
import com.mihwapp.crazymusic.utils.ApplicationUtils;
import com.mihwapp.crazymusic.utils.DownloadUtils;
import com.mihwapp.crazymusic.utils.ShareActionUtils;
import com.mihwapp.crazymusic.utils.StringUtils;
import com.mihwapp.crazymusic.view.CircularProgressBar;
import com.mihwapp.crazymusic.view.DBViewPager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: NewMusicApp
 * Created by dotrungbao on 1/5/17.
 */

public class YPYMainActivity extends YPYFragmentActivity implements IDBMusicPlayerListener, View.OnClickListener {

    public static final String TAG = YPYMainActivity.class.getSimpleName();

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.view_pager)
    DBViewPager mViewpager;

    @BindView(R.id.container)
    FrameLayout mLayoutContainer;

    @BindView(R.id.btn_play)
    ImageView mBtnSmallPlay;

    @BindView(R.id.btn_prev)
    ImageView mBtnSmallPrev;

    @BindView(R.id.layout_listen_music)
    View mLayoutListenMusic;

    @BindView(R.id.btn_next)
    ImageView mBtnSmallNext;

    @BindView(R.id.img_song)
    ImageView mImgSmallSong;

    @BindView(R.id.tv_song)
    TextView mTvSmallSong;

    @BindView(R.id.tv_singer)
    TextView mTvSmallSinger;

    @BindView(R.id.img_status_loading)
    CircularProgressBar mProgressLoadingMusic;

    @BindView(R.id.layout_music)
    RelativeLayout mLayoutControlMusic;

    @BindView(R.id.play_container)
    FrameLayout mLayoutDetailListenMusic;

    private FragmentYPYTopChart mFragmentTopChart;
    private FragmentYPYGenre mFragmentGenre;
    private FragmentYPYMyMusic mFragmentMyMusic;
    private FragmentYPYPlaylist mFragmentPlaylist;

    private DBFragmentAdapter mTabAdapters;

    private ArrayList<Fragment> mListHomeFragments = new ArrayList<>();

    private Menu mMenu;

    private ArrayList<String> mListSuggestionStr;
    private String[] mColumns;
    private Object[] mTempData;
    private MatrixCursor mCursor;
    private int mStartHeight;

    private BottomSheetBehavior<View> mBottomSheetBehavior;

    private FragmentYPYPlayerListenMusic mFragmentListenMusic;
    private Drawable mHomeIconDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_app_bar_main);

        mHomeIconDrawable =getResources().getDrawable(R.drawable.ic_home_24dp);
        mHomeIconDrawable.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);

        setUpActionBar();

        setIsAllowPressMoreToExit(true);

        YPYSettingManager.setOnline(this, true);

        createArrayFragment();

        mFragmentListenMusic = (FragmentYPYPlayerListenMusic) getSupportFragmentManager().findFragmentById(R.id.fragment_listen_music);

        mTabLayout.setTabTextColors(getResources().getColor(R.color.tab_text_normal_color), getResources().getColor(R.color.tab_text_focus_color));
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        ViewCompat.setElevation(mTabLayout, 0f);

        mViewpager.setPagingEnabled(true);

        findViewById(R.id.img_touch).setOnTouchListener((view, motionEvent) -> true);

        this.isNeedProcessOther = true;

        setUpSmallMusicLayout();
        registerMusicPlayerBroadCastReceiver(this);

        if (!ApplicationUtils.isOnline(this)) {
            registerNetworkBroadcastReceiver(isNetworkOn -> {
                if (mListHomeFragments != null && mListHomeFragments.size() > 0) {
                    for (Fragment mFragment : mListHomeFragments) {
                        ((DBFragment) mFragment).onNetworkChange(isNetworkOn);
                    }
                }

            });
        }
        boolean b = SHOW_BANNER_ADS_IN_HOME;

        checkConfigure();

        if (!YPYSettingManager.getHasShownDisclaimer(this)) {
            showDisclaimer();
            YPYSettingManager.setHasShownDisclaimer(this, true);
        }

        ViewGroup vg = findViewById(R.id.layout_ads);
        if (vg != null) {
            AdsManager.Companion.getInstance().installBanner(vg);
        }

    }

    private void showDisclaimer() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dlg_disclaimer, null);
        dialogBuilder.setView(dialogView)
                .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    private void checkConfigure(){
        ConfigureModel configureModel= mTotalMng.getConfigureModel();
        if(configureModel==null){
            showProgressDialog();
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                mTotalMng.readConfigure(YPYMainActivity.this);
                mTotalMng.readGenreData(YPYMainActivity.this);
                mTotalMng.readCached(TYPE_FILTER_SAVED);
                mTotalMng.readPlaylistCached();
                mTotalMng.readLibraryTrack(YPYMainActivity.this);
                runOnUiThread(() -> {
                    dimissProgressDialog();
                    setUpTab();
                    showAppRate();
                });
            });
        }
        else{
            setUpTab();
            showAppRate();
        }
    }


    private void setUpActionBar() {
        setUpCustomizeActionBar();
        removeEvalationActionBar();
        setColorForActionBar(Color.TRANSPARENT);
        setActionBarTitle("");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setIcon(mHomeIconDrawable);
    }

    @Override
    public void onDestroyData() {
        super.onDestroyData();
        try {
            MediaPlayer mMediaPlayer = MusicDataMng.getInstance().getPlayer();
            if (mMediaPlayer == null) {
                MusicDataMng.getInstance().onDestroy();
                TotalDataManager.getInstance().onDestroy();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        boolean b = STOP_MUSIC_WHEN_EXITS_APP;
        if (b) {
            startMusicService(ACTION_STOP);
        }

    }

    private void setUpSmallMusicLayout() {
        mBtnSmallPlay.setOnClickListener(this);

        mBtnSmallPrev.setOnClickListener(this);
        setUpImageViewBaseOnColor(mBtnSmallPrev,mIconColor,R.drawable.ic_skip_previous_white_36dp,false);
        mBtnSmallNext.setOnClickListener(this);

        setUpImageViewBaseOnColor(mBtnSmallNext,mIconColor,R.drawable.ic_skip_next_white_36dp,false);

        mTvSmallSong.setSelected(true);

        mStartHeight = getResources().getDimensionPixelOffset(R.dimen.size_img_big);

        mLayoutControlMusic.setOnClickListener(view -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        mBottomSheetBehavior = BottomSheetBehavior.from(mLayoutListenMusic);
        mBottomSheetBehavior.setPeekHeight(mStartHeight);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            public boolean isHidden;
            public float mSlideOffset;

            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    showAppBar(false);
                    showHeaderMusicPlayer(true);
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    isHidden = false;
                    showAppBar(true);
                    showHeaderMusicPlayer(false);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                if (mSlideOffset > 0 && slideOffset > mSlideOffset && !isHidden) {
                    showAppBar(false);
                    isHidden = true;
                }
                mSlideOffset = slideOffset;
                mLayoutControlMusic.setVisibility(View.VISIBLE);
                mLayoutDetailListenMusic.setVisibility(View.VISIBLE);
                mLayoutControlMusic.setAlpha(1f - slideOffset);
                mLayoutDetailListenMusic.setAlpha(slideOffset);
            }
        });
        if (MusicDataMng.getInstance().isPrepaireDone()) {
            showLayoutListenMusic(true);
            TrackModel mCurrentTrack = MusicDataMng.getInstance().getCurrentTrackObject();
            if (mCurrentTrack != null) {
                updateInfoOfPlayTrack(mCurrentTrack);
            }
            int playId=MusicDataMng.getInstance().isPlayingMusic()?R.drawable.ic_pause_white_36dp:R.drawable.ic_play_arrow_white_36dp;
            setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,playId,false);
        }
        else {
            showLayoutListenMusic(false);
            setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,R.drawable.ic_play_arrow_white_36dp,false);
            if(mFragmentListenMusic !=null){
                mFragmentListenMusic.setUpBackground();
            }
        }

    }

    private void showHeaderMusicPlayer(boolean b) {
        mLayoutControlMusic.setVisibility(!b ? View.VISIBLE : View.GONE);
        mLayoutDetailListenMusic.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        if(b && mFragmentListenMusic !=null){
            mFragmentListenMusic.setUpBackground();
        }
    }

    public void showAppBar(boolean b) {
        mAppBarLayout.setExpanded(b);
    }

    private void setUpTab() {
        boolean b = SHOW_SOUND_CLOUD_TAB;
        if (b) {
            mTabLayout.addTab(mTabLayout.newTab().setText(R.string.title_tab_top_charts));
            mTabLayout.addTab(mTabLayout.newTab().setText(R.string.title_tab_discover));
        }

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.title_tab_my_music));

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.title_tab_my_playlist));

        setTypefaceForTab(mTabLayout, mTypefaceBold);

        if (b) {
            mFragmentTopChart = (FragmentYPYTopChart) Fragment.instantiate(this, FragmentYPYTopChart.class.getName(), new Bundle());
            mListHomeFragments.add(mFragmentTopChart);

            mFragmentGenre = (FragmentYPYGenre) Fragment.instantiate(this, FragmentYPYGenre.class.getName(), new Bundle());
            mListHomeFragments.add(mFragmentGenre);
        }

        mFragmentMyMusic = (FragmentYPYMyMusic) Fragment.instantiate(this, FragmentYPYMyMusic.class.getName(), new Bundle());
        mListHomeFragments.add(mFragmentMyMusic);

        if (!ApplicationUtils.isOnline(this) || mFragmentTopChart == null) {
            mFragmentMyMusic.setFirstInTab(true);
        }
        else {
            mFragmentTopChart.setFirstInTab(true);
        }
        mFragmentPlaylist = (FragmentYPYPlaylist) Fragment.instantiate(this, FragmentYPYPlaylist.class.getName(), new Bundle());
        mListHomeFragments.add(mFragmentPlaylist);

        mTabAdapters = new DBFragmentAdapter(getSupportFragmentManager(), mListHomeFragments);
        mViewpager.setAdapter(mTabAdapters);
        mViewpager.setOffscreenPageLimit(mListHomeFragments.size());

        mViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                hiddenKeyBoardForSearchView();
                mViewpager.setCurrentItem(tab.getPosition());
                ((DBFragment) mListHomeFragments.get(tab.getPosition())).startLoadData();

                AdsManager.Companion.getInstance().showInterstitial();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (!ApplicationUtils.isOnline(this)) {
            mViewpager.setCurrentItem(mListHomeFragments.indexOf(mFragmentMyMusic));
            registerNetworkBroadcastReceiver(isNetworkOn -> {

            });
        }
    }

    private void showLayoutListenMusic(boolean b) {
        mLayoutListenMusic.setVisibility(b ? View.VISIBLE : View.GONE);
        mViewpager.setPadding(0, 0, 0, b ? mStartHeight : 0);
        mLayoutContainer.setPadding(0, 0, 0, b ? mStartHeight : 0);
        if (!b) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mColumns = null;
        mTempData = null;
        try {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (mListHomeFragments != null) {
            mListHomeFragments.clear();
            mListHomeFragments = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.mMenu = menu;
        initSetupForSearchView(menu, R.id.action_search, new IDBSearchViewInterface() {
            @Override
            public void onStartSuggestion(String keyword) {
                if (mFragmentMyMusic != null && mViewpager.getCurrentItem() == mListHomeFragments.indexOf(mFragmentMyMusic)) {
                    mFragmentMyMusic.startSearchData(keyword);
                }
                else {
                    if (!StringUtils.isEmpty(keyword)) {
                        startSuggestion(keyword);
                    }
                }
            }

            @Override
            public void onProcessSearchData(String keyword) {
                if (mFragmentMyMusic != null && mViewpager.getCurrentItem() == mListHomeFragments.indexOf(mFragmentMyMusic)) {
                    mFragmentMyMusic.startSearchData(keyword);
                }
                else {
                    if (!StringUtils.isEmpty(keyword)) {
                        goToSearchMusic(keyword);
                    }
                }
            }

            @Override
            public void onClickSearchView() {
                if (mFragmentMyMusic != null && mViewpager.getCurrentItem() != mListHomeFragments.indexOf(mFragmentMyMusic)) {
                    searchView.setQuery("", false);
                }
            }

            @Override
            public void onCloseSearchView() {

            }
        });
        searchView.setOnSuggestionListener(new androidx.appcompat.widget.SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                if (mListSuggestionStr != null && mListSuggestionStr.size() > 0) {
                    searchView.setQuery(mListSuggestionStr.get(position), false);
                    goToSearchMusic(mListSuggestionStr.get(position));
                }
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sleep_mode:
                showDialogSleepMode();
                break;
            case R.id.action_equalizer:
                goToEqualizer();
                break;
            case android.R.id.home:
                backToHome();
                break;
            case R.id.action_rate_me:
                String urlApp = String.format(URL_FORMAT_LINK_APP, getPackageName());
                ShareActionUtils.goToUrl(this, urlApp);
                YPYSettingManager.setRateApp(this, true);
                break;
            case R.id.action_share:
                String urlApp1 = String.format(URL_FORMAT_LINK_APP, getPackageName());
                String msg = String.format(getString(R.string.info_share_app), getString(R.string.app_name), urlApp1);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/*");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.info_share)));
                break;
            case R.id.action_contact_us:
                ShareActionUtils.shareViaEmail(this, YOUR_CONTACT_EMAIL, "Feedback for TTPod Music Player", "");
                break;
            case R.id.action_visit_website:
                goToUrl(getString(R.string.info_visit_website), URL_WEBSITE);
                break;
            case R.id.action_disclaimer:
                showDisclaimer();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHideSearchView(boolean b) {
        if (mMenu != null) {
            mMenu.findItem(R.id.action_search).setVisible(b);
        }
    }

    public void goToDetailPlaylist(PlaylistModel mPlaylistObject, int type) {
        mTotalMng.setPlaylistObject(mPlaylistObject);
        setActionBarTitle(mPlaylistObject.getName());
        goToDetailTracks(type);
    }

    private void goToDetailTracks(int type) {
        showHideLayoutContainer(true);
        Bundle mBundle = new Bundle();
        mBundle.putInt(KEY_TYPE, type);
        goToFragment(getFragmentTag(type), R.id.container, FragmentYPYDetailTracks.class.getName(), 0, mBundle);
    }


    public void goToGenre(GenreModel mGenreObject) {
        mTotalMng.setGenreObject(mGenreObject);
        setActionBarTitle(mGenreObject.getName());
        showHideLayoutContainer(true);
        goToDetailTracks(TYPE_DETAIL_GENRE);
    }


    private String getFragmentTag(int type) {
        if (type == TYPE_DETAIL_PLAYLIST) {
            return TAG_FRAGMENT_DETAIL_PLAYLIST;
        }
        else if (type == TYPE_DETAIL_TOP_PLAYLIST) {
            return TAG_FRAGMENT_TOP_PLAYLIST;
        }
        else if (type == TYPE_DETAIL_GENRE) {
            return TAG_FRAGMENT_DETAIL_GENRE;
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchView != null && !searchView.isIconified()) {
                hiddenKeyBoardForSearchView();
                return true;
            }
            if (collapseListenMusic()) {
                return true;
            }
            boolean b = backToHome();
            if (b) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean backToHome() {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof DBFragment) {
                    boolean isBack = ((DBFragment) mFragment).isCheckBack();
                    if (isBack) {
                        return true;
                    }
                }
            }
        }
        boolean b = backStack(null);
        if (b) {
            showHideLayoutContainer(false);
            return true;
        }
        return false;
    }

    public void showHideLayoutContainer(boolean b) {
        mLayoutContainer.setVisibility(b ? View.VISIBLE : View.GONE);
        mTabLayout.setVisibility(b ? View.GONE : View.VISIBLE);
        mViewpager.setVisibility(b ? View.GONE : View.VISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(b);
        getSupportActionBar().setHomeButtonEnabled(b);
        getSupportActionBar().setDisplayUseLogoEnabled(!b);
        getSupportActionBar().setDisplayShowHomeEnabled(!b);
        showHideSearchView(!b);
        if (b) {
            mAppBarLayout.setExpanded(true);
            getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);
        }
        else {
            getSupportActionBar().setIcon(mHomeIconDrawable);
            setActionBarTitle("");
        }

    }

    @Override
    public void notifyData(int type) {
        switch (type) {
            case TYPE_PLAYLIST:
                if (mFragmentPlaylist != null) {
                    mFragmentPlaylist.notifyData();
                    if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAIL_PLAYLIST) != null) {
                        ((FragmentYPYDetailTracks) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAIL_PLAYLIST)).notifyData();
                    }
                }
                break;
            case TYPE_EDIT_SONG:
                if(mFragmentMyMusic!=null){
                    mFragmentMyMusic.notifyData();
                }
                if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAIL_PLAYLIST) != null) {
                    ((FragmentYPYDetailTracks) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_DETAIL_PLAYLIST)).notifyData();
                }
                if(MusicDataMng.getInstance().isPrepaireDone()){
                    TrackModel model = MusicDataMng.getInstance().getCurrentTrackObject();
                    if(model!=null){
                        updateInfoOfPlayTrack(model);
                        if(mFragmentListenMusic !=null){
                            mFragmentListenMusic.updateInformation();
                        }
                    }
                }
                break;
        }
    }


    @Override
    public void notifyData(int type, long id) {
        if (type == TYPE_DELETE) {
            long mCurrentId = MusicDataMng.getInstance().getCurrentPlayingId();
            if (mCurrentId == id) {
                startMusicService(ACTION_NEXT);
            }
            notifyFragment();
        }
    }

    @Override
    public void notifyFragment() {
        super.notifyFragment();
        if (mListHomeFragments != null && mListHomeFragments.size() > 0) {
            mFragmentPlaylist.notifyData();
            mFragmentMyMusic.notifyData();
        }
    }

    public void startPlayingList(TrackModel mTrackObject, ArrayList<TrackModel> listTrackObjects) {
        updateInfoOfPlayTrack(mTrackObject);
        if (listTrackObjects != null && listTrackObjects.size() > 0) {
            ArrayList<TrackModel> mListTracks = (ArrayList<TrackModel>) listTrackObjects.clone();

            MusicDataMng.getInstance().setListPlayingTrackObjects(mListTracks);
            if (mFragmentListenMusic != null) {
                mFragmentListenMusic.setUpInfo(mListTracks);
            }
            TrackModel mCurrentTrack = MusicDataMng.getInstance().getCurrentTrackObject();
            boolean isPlayingTrack = (mCurrentTrack != null && mCurrentTrack.getId() == mTrackObject.getId());
            if (!isPlayingTrack) {
                boolean b = MusicDataMng.getInstance().setCurrentIndex(mTrackObject);
                if (b) {
                    startMusicService(ACTION_PLAY);
                }
            }
            else {
                try {
                    MediaPlayer mMediaPlayer = MusicDataMng.getInstance().getPlayer();
                    if (mMediaPlayer != null) {
                        int playId=MusicDataMng.getInstance().isPlayingMusic()?R.drawable.ic_pause_white_36dp:R.drawable.ic_play_arrow_white_36dp;
                        setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,playId,false);
                    }
                    else {
                        setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,R.drawable.ic_play_arrow_white_36dp,false);
                        boolean b = MusicDataMng.getInstance().setCurrentIndex(mTrackObject);
                        if (b) {
                            startMusicService(ACTION_PLAY);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,R.drawable.ic_play_arrow_white_36dp,false);
                    startMusicService(ACTION_STOP);
                }

            }
        }
    }

    private void updateInfoOfPlayTrack(TrackModel mTrackObject) {
        if (mTrackObject != null) {
            showLayoutListenMusic(true);
            String artwork = mTrackObject.getArtworkUrl();
            if (!TextUtils.isEmpty(artwork)) {
                GlideImageLoader.displayImage(this, mImgSmallSong, artwork, R.drawable.ic_rect_music_default);
            }
            else {
                Uri mUri = mTrackObject.getURI();
                if (mUri != null) {
                    GlideImageLoader.displayImageFromMediaStore(this, mImgSmallSong, mUri, R.drawable.ic_rect_music_default);
                }
                else {
                    mImgSmallSong.setImageResource(R.drawable.ic_rect_music_default);
                }
            }

            mTvSmallSong.setText(Html.fromHtml(mTrackObject.getTitle()));
            String artist = mTrackObject.getAuthor();
            if (!StringUtils.isEmpty(artist) && !artist.equalsIgnoreCase(PREFIX_UNKNOWN)) {
                mTvSmallSinger.setText(artist);
            }
            else {
                mTvSmallSinger.setText(R.string.title_unknown);
            }
        }
    }

    @Override
    public void onPlayerUpdateState(boolean isPlay) {
        int playId=MusicDataMng.getInstance().isPlayingMusic()?R.drawable.ic_pause_white_36dp:R.drawable.ic_play_arrow_white_36dp;
        setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,playId,false);
        if (mFragmentListenMusic != null) {
            mFragmentListenMusic.onPlayerUpdateState(isPlay);
        }
    }

    @Override
    public void onPlayerStop() {
        showLayoutListenMusic(false);
        int playId=MusicDataMng.getInstance().isPlayingMusic()?R.drawable.ic_pause_white_36dp:R.drawable.ic_play_arrow_white_36dp;
        setUpImageViewBaseOnColor(mBtnSmallPlay,mIconColor,playId,false);
        if (mFragmentListenMusic != null) {
            mFragmentListenMusic.onPlayerStop();
        }
    }

    @Override
    public void onPlayerLoading() {
        showLoading(true);
        TrackModel mTrackObject = MusicDataMng.getInstance().getCurrentTrackObject();
        updateInfoOfPlayTrack(mTrackObject);
    }

    @Override
    public void onPlayerStopLoading() {
        showLoading(false);
    }

    @Override
    public void onPlayerUpdatePos(int currentPos) {
        if (mFragmentListenMusic != null) {
            mFragmentListenMusic.onUpdatePos(currentPos);
        }
    }

    @Override
    public void onPlayerError() {

    }

    @Override
    public void onPlayerUpdateStatus() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                startMusicService(ACTION_NEXT);
                break;
            case R.id.btn_prev:
                startMusicService(ACTION_PREVIOUS);
                break;
            case R.id.btn_play:
                startMusicService(ACTION_TOGGLE_PLAYBACK);
                break;
        }
    }

    private void showLoading(boolean b) {
        mBtnSmallPlay.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        mBtnSmallNext.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        mBtnSmallPrev.setVisibility(!b ? View.VISIBLE : View.INVISIBLE);
        mProgressLoadingMusic.setVisibility(b ? View.VISIBLE : View.GONE);
        if (mFragmentListenMusic != null) {
            mFragmentListenMusic.showLoading(b);
        }
    }

    private void startSuggestion(final String search) {
        DBExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(() -> {
            String countryCode = Locale.getDefault().getCountry();
            String url = String.format(URL_FORMAT_SUGESSTION, countryCode, StringUtils.urlEncodeString(search));
            InputStream mInputStream = DownloadUtils.download(url);
            if (mInputStream != null) {
                final ArrayList<String> mListSuggestionStr = XMLParsingData.parsingSuggestion(mInputStream);
                ConfigureModel configureModel=TotalDataManager.getInstance().getConfigureModel();
                ArrayList<String> mListFilters= configureModel!=null?configureModel.getFilters():null;
                if(mListSuggestionStr!=null && mListSuggestionStr.size()>0){
                    if(mListFilters!=null && mListFilters.size()>0){
                        Iterator mIterator = mListSuggestionStr.iterator();
                        while (mIterator.hasNext()){
                            String model= (String) mIterator.next();
                            for(String mStr:mListFilters){
                                if(model!=null && model.toLowerCase().contains(mStr)){
                                    mIterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (mListSuggestionStr != null) {
                    runOnUiThread(() -> {
                        searchView.setSuggestionsAdapter(null);
                        if (YPYMainActivity.this.mListSuggestionStr != null) {
                            YPYMainActivity.this.mListSuggestionStr.clear();
                            YPYMainActivity.this.mListSuggestionStr = null;
                        }
                        YPYMainActivity.this.mListSuggestionStr = mListSuggestionStr;
                        try {
                            mTempData = null;
                            mColumns = null;
                            if (mCursor != null) {
                                mCursor.close();
                                mCursor = null;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        mColumns = new String[]{"_id", "text"};
                        mTempData = new Object[]{0, "default"};
                        mCursor = new MatrixCursor(mColumns);
                        int size = mListSuggestionStr.size();
                        mCursor.close();
                        for (int i = 0; i < size; i++) {
                            mTempData[0] = i;
                            mTempData[1] = mListSuggestionStr.get(i);
                            mCursor.addRow(mTempData);
                        }
                        SuggestionAdapter mSuggestAdapter = new SuggestionAdapter(YPYMainActivity.this, mCursor, mListSuggestionStr);
                        searchView.setSuggestionsAdapter(mSuggestAdapter);
                    });
                }
            }
        });
    }


    private void goToSearchMusic(String keyword) {
        hiddenKeyBoardForSearchView();
        if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SEARCH) != null) {
            FragmentYPYSearchTrack mFragmentSearch = (FragmentYPYSearchTrack) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_SEARCH);
            mFragmentSearch.startSearch(keyword);
        }
        else {
            backStack(null);
            Bundle mBundle = new Bundle();
            mBundle.putString(KEY_BONUS, keyword);
            String tag = getCurrentFragmentTag();
            setActionBarTitle(R.string.title_search_music);
            showHideLayoutContainer(true);
            showHideSearchView(true);
            if (StringUtils.isEmpty(tag)) {
                goToFragment(TAG_FRAGMENT_SEARCH, R.id.container, FragmentYPYSearchTrack.class.getName(), 0, mBundle);
            }
            else {
                goToFragment(TAG_FRAGMENT_SEARCH, R.id.container, FragmentYPYSearchTrack.class.getName(), tag, mBundle);
            }
        }
    }

    public boolean collapseListenMusic() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }
        return false;
    }


}
