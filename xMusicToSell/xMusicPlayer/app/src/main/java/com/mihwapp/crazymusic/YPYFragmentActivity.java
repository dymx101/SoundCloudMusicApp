package com.mihwapp.crazymusic;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import com.mihwapp.crazymusic.abtractclass.fragment.DBFragment;
import com.mihwapp.crazymusic.abtractclass.fragment.IDBFragmentConstants;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.dataMng.MusicDataMng;
import com.mihwapp.crazymusic.dataMng.TotalDataManager;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.imageloader.target.GlideViewGroupTarget;
import com.mihwapp.crazymusic.listener.IDBMusicPlayerListener;
import com.mihwapp.crazymusic.listener.IDBSearchViewInterface;
import com.mihwapp.crazymusic.model.PlaylistModel;
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.playservice.IYPYMusicConstant;
import com.mihwapp.crazymusic.playservice.YPYMusicService;
import com.mihwapp.crazymusic.setting.YPYSettingManager;
import com.mihwapp.crazymusic.task.IYPYCallback;
import com.mihwapp.crazymusic.utils.ApplicationUtils;
import com.mihwapp.crazymusic.utils.DBLog;
import com.mihwapp.crazymusic.utils.IOUtils;
import com.mihwapp.crazymusic.utils.ResolutionUtils;
import com.mihwapp.crazymusic.utils.ShareActionUtils;
import com.mihwapp.crazymusic.utils.StringUtils;
import com.mihwapp.crazymusic.view.DividerItemDecoration;
import com.triggertrap.seekarc.SeekArc;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class YPYFragmentActivity extends AppCompatActivity implements IXMusicConstants, IYPYMusicConstant, IDBFragmentConstants {

    public static final String TAG = YPYFragmentActivity.class.getSimpleName();

    private long adShowInterval = 300;

    private Dialog mProgressDialog;

    private int screenWidth;
    private int screenHeight;

    public ArrayList<Fragment> mListFragments;

    public Typeface mTypefaceNormal;
    public Typeface mTypefaceLight;
    public Typeface mTypefaceBold;
    public Typeface mTypefaceItalic;

    private boolean isAllowPressMoreToExit;
    private int countToExit;
    private long pivotTime;

    public TotalDataManager mTotalMng;
    private IDBMusicPlayerListener musicPlayerListener;
    private MusicPlayerBroadcast mPlayerBroadcast;

    private ConnectionChangeReceiver mNetworkBroadcast;
    private INetworkListener mNetworkListener;

    private RelativeLayout mLayoutAds;
    public SearchView searchView;
    private String[] mListStr;

    //process favorite, playlist
    public boolean isNeedProcessOther;

    private boolean isLoadingBg;
    public BlurTransformation mBlurBgTranform;
    private GlideViewGroupTarget mTarget;
    private boolean isPausing;

    public Drawable mBackDrawable;
    public int mContentActionColor;
    public int mIconColor;
    private Unbinder mBind;

    private Handler mHandlerAds = new Handler();
    private InterstitialAd interstitialAd;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFormat(PixelFormat.RGBA_8888);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        this.createProgressDialog();

        this.mTypefaceNormal = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        this.mTypefaceLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        this.mTypefaceBold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        this.mTypefaceItalic = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Italic.ttf");

        this.mTotalMng = TotalDataManager.getInstance();
        setStatusBarTranslucent(true);

        MobileAds.initialize(this,ADMOB_APP_ID);

        this.mBlurBgTranform = new BlurTransformation();

        this.mContentActionColor =getResources().getColor(R.color.icon_action_bar_color);
        this.mIconColor =getResources().getColor(R.color.icon_color);

        this.mBackDrawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        this.mBackDrawable.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);

        int[] mRes = ResolutionUtils.getDeviceResolution(this);
        if (mRes != null && mRes.length == 2) {
            screenWidth = mRes[0];
            screenHeight = mRes[1];
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mBind=ButterKnife.bind(this);
    }

    public void setStatusBarTranslucent(boolean makeTranslucent) {
        if (IOUtils.hasKitKat()) {
            if (makeTranslucent) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

    }
    public void setUpImageViewBaseOnColor(int id, int color, int idDrawabe, boolean isReset) {
        setUpImageViewBaseOnColor(findViewById(id), color, idDrawabe, isReset);
    }

    public void setUpImageViewBaseOnColor(View mView, int color, int idDrawabe, boolean isReset) {
        Drawable mDrawable = getResources().getDrawable(idDrawabe);
        if (isReset) {
            mDrawable.clearColorFilter();
        }
        else {
            mDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        if (mView instanceof Button) {
            mView.setBackgroundDrawable(mDrawable);
        }
        else if (mView instanceof ImageView) {
            ((ImageView) mView).setImageDrawable(mDrawable);
        }
        else if (mView instanceof ImageButton) {
            ((ImageView) mView).setImageDrawable(mDrawable);
        }
    }

    public void setUpBackground(){
        try{
            RelativeLayout mLayoutBg = findViewById(R.id.layout_bg);
            if(mLayoutBg!=null){
                mTarget =new GlideViewGroupTarget(this,mLayoutBg){
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                    }
                };
                String imgBg = YPYSettingManager.getBackground(this);
                Log.e("DCM","=============>getBackground="+imgBg);
                if(!TextUtils.isEmpty(imgBg)){
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.default_bg_app)
                            .transform(mBlurBgTranform)
                            .priority(Priority.HIGH);
                    Glide.with(this).asBitmap().apply(options).load(Uri.parse(imgBg)).into(mTarget);
                }
                else{
                    if(this instanceof YPYSplashActivity){
                        mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    else{
                        mLayoutBg.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPausing=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPausing || !isLoadingBg){
            isPausing=false;
            isLoadingBg=true;
            setUpBackground();
        }
    }



    public void showAppRate() {
        if(!YPYSettingManager.getRateApp(this)){
            AppRate.with(this).setInstallDays(NUMBER_INSTALL_DAYS) // default 10, 0 means install day.
                    .setLaunchTimes(NUMBER_LAUNCH_TIMES) // default 10
                    .setRemindInterval(REMIND_TIME_INTERVAL) // default 1
                    .setShowLaterButton(true) // default true
                    .setShowNeverButton(false) // default true
                    .setDebug(false).setOnClickButtonListener(new OnClickButtonListener() {
                @Override
                public void onClickButton(int which) {
                    if (which == -1) {
                        YPYSettingManager.setRateApp(YPYFragmentActivity.this,true);
                        ShareActionUtils.goToUrl(YPYFragmentActivity.this, String.format(URL_FORMAT_LINK_APP, getPackageName()));
                    }
                }
            }).monitor();

            AppRate.showRateDialogIfMeetsConditions(this);
        }

    }

    public void setUpLayoutAdmob() {
        mLayoutAds = findViewById(R.id.layout_ads);
        boolean b = SHOW_ADS;
        if (b) {
            if (ApplicationUtils.isOnline(this)
                    && mLayoutAds != null && mLayoutAds.getChildCount()==0) {
                adView = new AdView(this);
                adView.setAdUnitId(ADMOB_BANNER_ID);
                adView.setAdSize(AdSize.SMART_BANNER);
                mLayoutAds.addView(adView);
                AdRequest mAdRequest = new AdRequest.Builder().addTestDevice(ADMOB_TEST_DEVICE).build();
                adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        DBLog.d(TAG, "===========>Add loaded");
                        showAds();

                    }
                });
                adView.loadAd(mAdRequest);
                hideAds();
                return;

            }
        }
        if(mLayoutAds.getChildCount()==0){
            hideAds();
        }

    }

    void saveAdShowTime(Date showTime) {
        YPYSettingManager.setShowAdsTime(this, showTime.getTime());
    }

    Date adShowTime() {
        long milliseconds = YPYSettingManager.getShowAdsTime(this);
        return new Date(milliseconds);
    }

    Boolean goodTimeToShowAds() {
        Date adShowTime = adShowTime();
        Date now = new Date();

        if (adShowTime.getTime() <= 0) {
            saveAdShowTime(now);
            return false;
        }

        long interval = now.getTime() - adShowTime.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(interval);
        return seconds > adShowInterval;
    }

    public void showInterstitial(final IYPYCallback mCallback) {
        boolean b = SHOW_ADS && goodTimeToShowAds();
        if (ApplicationUtils.isOnline(this) && b) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(ADMOB_INTERSTITIAL_ID);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(ADMOB_TEST_DEVICE).build();
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mHandlerAds.removeCallbacksAndMessages(null);
                    try{
                        if(interstitialAd !=null){
                            interstitialAd.show();
                            saveAdShowTime(new Date());
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }
            });
            interstitialAd.loadAd(adRequest);
            mHandlerAds.postDelayed(() -> {
                interstitialAd=null;
                if (mCallback != null) {
                    mCallback.onAction();
                }
            },TIME_OUT_LOAD_ADS);
            return;
        }
        if (mCallback != null) {
            mCallback.onAction();
        }

    }

    public void showAds() {
        mLayoutAds.setVisibility(View.VISIBLE);
    }

    public void hideAds() {
        mLayoutAds.setVisibility(View.GONE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView!=null){
            adView.destroy();
        }
        mHandlerAds.removeCallbacksAndMessages(null);
        if(mBind!=null){
            mBind.unbind();
        }
        if (mPlayerBroadcast != null) {
            unregisterReceiver(mPlayerBroadcast);
            mPlayerBroadcast = null;
        }
        if (mNetworkBroadcast != null) {
            unregisterReceiver(mNetworkBroadcast);
            mNetworkBroadcast = null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isAllowPressMoreToExit) {
                showQuitDialog();
            }
            else {
                pressMoreToExitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setIsAllowPressMoreToExit(boolean isAllowPressMoreToExit) {
        this.isAllowPressMoreToExit = isAllowPressMoreToExit;
    }

    private void pressMoreToExitApp() {
        if (countToExit >= 1) {
            long delaTime = System.currentTimeMillis() - pivotTime;
            if (delaTime <= 2000) {
                onDestroyData();
                finish();
                return;
            }
            else {
                countToExit = 0;
            }
        }
        pivotTime = System.currentTimeMillis();
        showToast(R.string.info_press_again_to_exit);
        countToExit++;
    }

    public MaterialDialog createFullDialog(int iconId, int mTitleId, int mYesId, int mNoId, String messageId, final IYPYCallback mCallback, final IYPYCallback mNeCallback) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title(mTitleId);
        if (iconId != -1) {
            mBuilder.iconRes(iconId);
        }
        mBuilder.content(messageId);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_text));
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.negativeColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.negativeText(mNoId);
        mBuilder.positiveText(mYesId);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceLight);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (mCallback != null) {
                    mCallback.onAction();
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                if (mNeCallback != null) {
                    mNeCallback.onAction();
                }
            }
        });
        return mBuilder.build();
    }


    public void showFullDialog(int titleId, String message, int idPositive, int idNegative, final IYPYCallback mDBCallback) {
        createFullDialog(-1, titleId, idPositive, idNegative, message, mDBCallback, null).show();
    }
    public void showFullDialog(int titleId, String message, int idPositive, int idNegative, final IYPYCallback mDBCallback, final IYPYCallback mNegative) {
        createFullDialog(-1, titleId, idPositive, idNegative, message, mDBCallback, mNegative).show();
    }

    public void showQuitDialog() {
        int mNoId = R.string.title_no;
        int mTitleId = R.string.title_confirm;
        int mYesId = R.string.title_yes;
        int iconId = R.mipmap.ic_launcher;
        int messageId = R.string.info_close_app;

        createFullDialog(iconId, mTitleId, mYesId, mNoId, getString(messageId), () -> {
            onDestroyData();
            finish();
        }, null).show();

    }

    private void createProgressDialog() {
        this.mProgressDialog = new Dialog(this);
        this.mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mProgressDialog.setContentView(R.layout.item_progress_bar);
        TextView mTvMessage = mProgressDialog.findViewById(R.id.tv_message);
        mTvMessage.setTypeface(mTypefaceLight);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
            return false;
        });
    }

    public void showProgressDialog() {
        showProgressDialog(R.string.info_loading);
    }

    public void showProgressDialog(int messageId) {
        showProgressDialog(getString(messageId));
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog != null) {
            TextView mTvMessage = mProgressDialog.findViewById(R.id.tv_message);
            mTvMessage.setText(message);
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    public void dimissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showToastWithLongTime(int resId) {
        showToastWithLongTime(getString(resId));
    }

    public void showToastWithLongTime(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public void onDestroyData() {
        YPYSettingManager.setOnline(this, false);
        mTotalMng.onDestroy();
    }

    public void createArrayFragment() {
        mListFragments = new ArrayList<>();
    }

    public void addFragment(Fragment mFragment) {
        if (mFragment != null && mListFragments != null) {
            synchronized (mListFragments) {
                mListFragments.add(mFragment);
            }
        }
    }

    public Fragment getFragmentHome(String nameFragment, int idFragment) {
        Fragment mFragmentHome = null;
        if (idFragment > 0) {
            mFragmentHome = getSupportFragmentManager().findFragmentById(idFragment);
        }
        else {
            if (!StringUtils.isEmpty(nameFragment)) {
                mFragmentHome = getSupportFragmentManager().findFragmentByTag(nameFragment);
            }
        }
        return mFragmentHome;
    }

    public String getStringDuration(long duration) {
        String minute = String.valueOf((int) (duration / 60));
        String seconds = String.valueOf((int) (duration % 60));
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        if (seconds.length() < 2) {
            seconds = "0" + seconds;
        }
        return minute + ":" + seconds;
    }

    public boolean backStack(IYPYCallback mCallback) {
        if (mListFragments != null && mListFragments.size() > 0) {
            int count = mListFragments.size();
            if (count > 0) {
                synchronized (mListFragments) {
                    Fragment mFragment = mListFragments.remove(count - 1);
                    if (mFragment != null) {
                        if (mFragment instanceof DBFragment) {
                            ((DBFragment) mFragment).backToHome(this);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public void goToFragment(String tag, int idContainer, String fragmentName, String parentTag, Bundle mBundle) {
        goToFragment(tag, idContainer, fragmentName, 0, parentTag, mBundle);
    }

    public void goToFragment(String tag, int idContainer, String fragmentName, int parentId, Bundle mBundle) {
        goToFragment(tag, idContainer, fragmentName, parentId, null, mBundle);
    }

    public void goToFragment(String tag, int idContainer, String fragmentName, int parentId, String parentTag, Bundle mBundle) {
        if (!StringUtils.isEmpty(tag) && getSupportFragmentManager().findFragmentByTag(tag) != null) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mBundle != null) {
            if (parentId != 0) {
                mBundle.putInt(KEY_ID_FRAGMENT, parentId);
            }
            if (!StringUtils.isEmpty(parentTag)) {
                mBundle.putString(KEY_NAME_FRAGMENT, parentTag);
            }
        }
        Fragment mFragment = Fragment.instantiate(this, fragmentName, mBundle);
        addFragment(mFragment);
        transaction.add(idContainer, mFragment, tag);
        if (parentId != 0) {
            Fragment mFragmentParent = getSupportFragmentManager().findFragmentById(parentId);
            if (mFragmentParent != null) {
                transaction.hide(mFragmentParent);
            }
        }
        if (!StringUtils.isEmpty(parentTag)) {
            Fragment mFragmentParent = getSupportFragmentManager().findFragmentByTag(parentTag);
            if (mFragmentParent != null) {
                transaction.hide(mFragmentParent);
            }
        }
        transaction.commit();
    }

    public void showDialogPlaylist(final TrackModel mTrackObject, final IYPYCallback mCallback) {
        final ArrayList<PlaylistModel> mListPlaylist = mTotalMng.getListPlaylistObjects();
        if (mListPlaylist != null && mListPlaylist.size() > 0) {
            int size = mListPlaylist.size() + 1;
            mListStr = new String[size];
            mListStr[0] = getResources().getStringArray(R.array.list_create_playlist)[0];
            for (int i = 1; i < size; i++) {
                PlaylistModel mPlaylistObject = mListPlaylist.get(i - 1);
                mListStr[i] = mPlaylistObject.getName();
            }
        }
        else {
            mListStr = getResources().getStringArray(R.array.list_create_playlist);
        }
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_select_playlist);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.items(mListStr);
        mBuilder.itemColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(R.string.title_cancel);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                mListStr = null;
            }
        });
        mBuilder.itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                if (mListPlaylist != null && mListPlaylist.size() > 0 && which > 0) {
                    mTotalMng.addTrackToPlaylist(YPYFragmentActivity.this, mTrackObject,
                            mListPlaylist.get(which - 1), true, mCallback);
                }
                else {
                    showDialogCreatePlaylist(false, null, new IYPYCallback() {
                        @Override
                        public void onAction() {
                            final ArrayList<PlaylistModel> mListPlaylist = mTotalMng.getListPlaylistObjects();
                            mTotalMng.addTrackToPlaylist(YPYFragmentActivity.this, mTrackObject, mListPlaylist.get(mListPlaylist.size() - 1), true, mCallback);
                            if(isNeedProcessOther){
                                notifyData(TYPE_PLAYLIST);
                            }
                            else{
                                sendBroadcastPlayer(ACTION_PLAYLIST);
                            }

                        }
                    });
                }
                mListStr = null;
            }
        });
        mBuilder.build().show();
    }

    public void notifyData(int type) {

    }

    public void notifyData(int type, long value) {

    }

    public void notifyFragment() {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof DBFragment) {
                    ((DBFragment) mFragment).notifyData();
                }
            }
        }
    }
    public void justNotifyFragment() {
        if (mListFragments != null && mListFragments.size() > 0) {
            for (Fragment mFragment : mListFragments) {
                if (mFragment instanceof DBFragment) {
                    ((DBFragment) mFragment).justNotifyData();
                }
            }
        }
    }


    public void showDialogCreatePlaylist(final boolean isEdit, final PlaylistModel mPlaylistObject, final IYPYCallback mCallback) {
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final EditText mEdPlaylistName = mView.findViewById(R.id.ed_name);
        mEdPlaylistName.setTextColor(getResources().getColor(R.color.main_color_text));
        mEdPlaylistName.setHighlightColor(getResources().getColor(R.color.main_color_secondary_text));
        mEdPlaylistName.setHint(R.string.title_playlist_name);
        if (isEdit) {
            mEdPlaylistName.setText(mPlaylistObject.getName());
        }
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_playlist_name);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_text));
        mBuilder.customView(mView, false);
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(isEdit ? R.string.title_save : R.string.title_create);
        mBuilder.negativeText(R.string.title_cancel);
        mBuilder.negativeColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                ApplicationUtils.hiddenVirtualKeyboard(YPYFragmentActivity.this, mEdPlaylistName);
                String mPlaylistName = mEdPlaylistName.getText().toString();
                checkCreatePlaylist(isEdit, mPlaylistObject, mPlaylistName, mCallback);
            }
        });
        final MaterialDialog mDialog = mBuilder.build();
        mEdPlaylistName.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String mPlaylistName = mEdPlaylistName.getText().toString();
                checkCreatePlaylist(isEdit, mPlaylistObject, mPlaylistName, mCallback);
                mDialog.dismiss();
                return true;
            }
            return false;
        });
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mDialog.show();
    }

    private void checkCreatePlaylist(boolean isEdit, PlaylistModel mPlaylistObject, String mPlaylistName, IYPYCallback mCallback) {
        if (StringUtils.isEmpty(mPlaylistName)) {
            showToast(R.string.info_playlist_error);
            return;
        }
        if (mTotalMng.isPlaylistNameExisted(mPlaylistName)) {
            showToast(R.string.info_playlist_name_existed);
            return;
        }
        if (!isEdit) {
            mPlaylistObject = new PlaylistModel(System.currentTimeMillis(), mPlaylistName);
            mPlaylistObject.setListTrackObjects(new ArrayList<>());
            mPlaylistObject.setListTrackIds(new ArrayList<>());
            mTotalMng.addPlaylistObject(mPlaylistObject);
        }
        else {
            mTotalMng.editPlaylistObject(mPlaylistObject, mPlaylistName);
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    public void shareFile(TrackModel mTrackObject) {
        try{
            String path = mTrackObject.getPath();
            if (TextUtils.isEmpty(path)) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mTrackObject.getTitle() + "\n" + mTrackObject.getPermalinkUrl());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share Via"));
            }
            else{
                File mFile=new File(path);
                Uri uri=null;
                if(mFile.exists() && mFile.isFile()){
                    if(!IOUtils.hasNougat()){
                       uri= Uri.fromFile(mFile);
                    }
                    else{
                        uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider",mFile);
                    }
                }
                if(uri!=null){
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("*/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(shareIntent, "Share Via"));
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onProcessSeekAudio(int currentPos) {
        startMusicService(ACTION_SEEK, currentPos);
    }

    public void startMusicService(String action) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(getPackageName() + action);
        startService(mIntent1);
    }

    public void startMusicService(String action, boolean data) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(getPackageName() + action);
        mIntent1.putExtra(KEY_VALUE, data);
        startService(mIntent1);
    }

    public void startMusicService(String action, int data) {
        Intent mIntent1 = new Intent(this, YPYMusicService.class);
        mIntent1.setAction(getPackageName() + action);
        mIntent1.putExtra(KEY_VALUE, data);
        startService(mIntent1);
    }

    private class MusicPlayerBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    if (!StringUtils.isEmpty(action)) {
                        String packageName = getPackageName();
                        if (action.equals(packageName + ACTION_BROADCAST_PLAYER)) {
                            String actionPlay = intent.getStringExtra(KEY_ACTION);
                            if (!StringUtils.isEmpty(actionPlay)) {
                                if (actionPlay.equals(packageName + ACTION_NEXT)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(false);
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_LOADING)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerLoading();
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_DIMISS_LOADING)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerStopLoading();
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_ERROR)) {
                                    showToast(R.string.info_play_song_error);
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerError();
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_PAUSE)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(false);
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_STOP)) {
                                    MusicDataMng.getInstance().onResetData();
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerStop();
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_PLAY)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateState(true);
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_UPDATE_POS)) {
                                    int currentPos = intent.getIntExtra(KEY_VALUE, -1);
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdatePos(currentPos);
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_UPDATE_STATUS)) {
                                    if (musicPlayerListener != null) {
                                        musicPlayerListener.onPlayerUpdateStatus();
                                    }
                                }
                                else if (actionPlay.equals(packageName + ACTION_FAVORITE) && isNeedProcessOther) {
                                    int type = intent.getIntExtra(KEY_TYPE, -1);
                                    notifyData(type);
                                }
                                else if (actionPlay.equals(packageName + ACTION_PLAYLIST) && isNeedProcessOther) {
                                    notifyData(TYPE_PLAYLIST);
                                }
                                else if (actionPlay.equals(packageName + ACTION_DELETE_SONG) && isNeedProcessOther) {
                                    long idSong = intent.getLongExtra(KEY_SONG_ID, -1);
                                    notifyData(TYPE_DELETE, idSong);
                                }
                            }
                        }

                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void registerMusicPlayerBroadCastReceiver(IDBMusicPlayerListener musicPlayerListener) {
        if (mPlayerBroadcast != null) {
            return;
        }
        this.musicPlayerListener = musicPlayerListener;
        mPlayerBroadcast = new MusicPlayerBroadcast();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + ACTION_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcast, mIntentFilter);
    }

    public void sendBroadcastPlayer(String action) {
        Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
        mIntent.putExtra(KEY_ACTION, getPackageName() + action);
        sendBroadcast(mIntent);
    }

    public void sendBroadcastPlayer(String action, int type) {
        Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
        mIntent.putExtra(KEY_ACTION, getPackageName() + action);
        mIntent.putExtra(KEY_TYPE, type);
        sendBroadcast(mIntent);
    }

    public void showPopupMenu(View v, final TrackModel mTrackObject) {
        showPopupMenu(v, mTrackObject, null);
    }

    public void showPopupMenu(View v, final TrackModel mTrackObject, final PlaylistModel mPlaylistObject) {
        boolean isOffline = mTotalMng.isLibraryTracks(mTrackObject);
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_track, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_add_playlist:
                    showDialogPlaylist(mTrackObject, () -> notifyData(TYPE_PLAYLIST));
                    break;
                case R.id.action_remove_from_playlist:
                    mTotalMng.removeTrackToPlaylist(mTrackObject, mPlaylistObject, () -> {
                        runOnUiThread(() ->  notifyData(TYPE_PLAYLIST));
                    });
                    break;
                case R.id.action_delete_song:
                    showDialogDelete(mTrackObject);
                    break;
                case R.id.action_share:
                    shareFile(mTrackObject);
                    break;
                case R.id.action_set_notification:
                    saveAsNotification(mTrackObject);
                    break;
                case R.id.action_set_ringtone:
                    saveAsRingtone(mTrackObject);
                    break;
                case R.id.action_edit_song:
                    showDialogEditTrack(mTrackObject, () -> notifyData(TYPE_EDIT_SONG));
                    break;
            }
            return true;
        });
        if (!isOffline) {
            popupMenu.getMenu().findItem(R.id.action_delete_song).setVisible(false);
            popupMenu.getMenu().findItem(R.id.action_set_ringtone).setVisible(false);
            popupMenu.getMenu().findItem(R.id.action_set_notification).setVisible(false);
            popupMenu.getMenu().findItem(R.id.action_edit_song).setVisible(false);
        }

        if (mPlaylistObject == null) {
            popupMenu.getMenu().findItem(R.id.action_remove_from_playlist).setVisible(false);
        }
        else {
            popupMenu.getMenu().findItem(R.id.action_add_playlist).setVisible(false);
        }
        popupMenu.show();
    }

    public void showDialogDelete(final TrackModel mTrackObject) {
        showFullDialog(R.string.title_confirm, getString(R.string.info_delete_song), R.string.title_ok, R.string.title_cancel, () -> {
            showProgressDialog();
            mTotalMng.deleteSong(mTrackObject, () -> {
                showToast(R.string.info_delete_song_done);
                dimissProgressDialog();
                notifyData(TYPE_DELETE, mTrackObject.getId());
            });
        });
    }


    public void registerNetworkBroadcastReceiver(INetworkListener networkListener) {
        if (mNetworkBroadcast != null) {
            return;
        }
        mNetworkBroadcast = new ConnectionChangeReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkBroadcast, mIntentFilter);
        mNetworkListener = networkListener;
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNetworkListener != null) {
                mNetworkListener.onNetworkState(ApplicationUtils.isOnline(YPYFragmentActivity.this));
            }
        }
    }

    public interface INetworkListener {
        public void onNetworkState(boolean isNetworkOn);
    }

    public void goToUrl(String name, String url) {
        Intent mIntent = new Intent(this, YPYShowUrlActivity.class);
        mIntent.putExtra(KEY_HEADER, name);
        mIntent.putExtra(KEY_SHOW_URL, url);
        startActivity(mIntent);
    }

    public void setTypefaceForTab(TabLayout mTabLayout, Typeface sMaterialDesignIcons) {
        try {
            ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof AppCompatTextView || tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTypeface(sMaterialDesignIcons);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setActionBarTitle(String title) {
        ActionBar mAb = getSupportActionBar();
        if (mAb != null) {
            mAb.setTitle(title);
        }
    }

    public void setActionBarTitle(int titleId) {
        setActionBarTitle(getResources().getString(titleId));
    }

    public void setUpCustomizeActionBar() {
        Toolbar mToolbar = findViewById(R.id.my_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setTitleTextColor(mContentActionColor);

            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert_white_24dp);
            drawable.setColorFilter(mContentActionColor, PorterDuff.Mode.SRC_ATOP);
            mToolbar.setOverflowIcon(drawable);
        }
    }

    public void setUpEvalationActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(getResources().getDimensionPixelOffset(R.dimen.card_elevation));
        }
    }

    public void removeEvalationActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(0);
        }
    }

    public void setColorForActionBar(int color) {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    public void initSetupForSearchView(Menu menu, int idSearch, final IDBSearchViewInterface mListener) {
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(idSearch));
        ImageView searchBtn = searchView.findViewById(R.id.search_button);
        setUpImageViewBaseOnColor(searchBtn, mContentActionColor, R.drawable.ic_search_white_24dp, false);

        ImageView closeBtn = searchView.findViewById(R.id.search_close_btn);
        setUpImageViewBaseOnColor(closeBtn, mContentActionColor, R.drawable.ic_close_white_24dp, false);

        EditText searchEditText = searchView.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(mContentActionColor);
        searchEditText.setHintTextColor(mContentActionColor);

        try{
            ImageView searchSubmit = searchView.findViewById (R.id.search_go_btn);
            if(searchSubmit!=null){
                searchSubmit.setColorFilter (mContentActionColor, PorterDuff.Mode.SRC_ATOP);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                hiddenKeyBoardForSearchView();
                if (mListener != null) {
                    mListener.onProcessSearchData(keyword);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                if (mListener != null) {
                    mListener.onStartSuggestion(keyword);
                }
                return true;
            }
        });
        searchView.setOnSearchClickListener(view -> {
            if (mListener != null) {
                mListener.onClickSearchView();
            }
        });
        searchView.setOnCloseListener(() -> {
            if (mListener != null) {
                mListener.onCloseSearchView();
            }
            return false;
        });
        searchView.setQueryHint(getString(R.string.title_search_music));
        searchView.setSubmitButtonEnabled(true);
    }

    public void hiddenKeyBoardForSearchView() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
            ApplicationUtils.hiddenVirtualKeyboard(this, searchView);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            hiddenKeyBoardForSearchView();
        }
        else {
            super.onBackPressed();
        }

    }
    public String getCurrentFragmentTag() {
        if (mListFragments != null && mListFragments.size() > 0) {
            Fragment mFragment = mListFragments.get(0);
            if (mFragment instanceof DBFragment) {
                return mFragment.getTag();
            }
        }
        return null;
    }

    public void showDialogSleepMode() {
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_time, null);
        final TextView mTvInfo = mView.findViewById(R.id.tv_info);
        mTvInfo.setTypeface(mTypefaceNormal);
        if(YPYSettingManager.getSleepMode(this)>0){
            mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(YPYSettingManager.getSleepMode(YPYFragmentActivity.this))));
        }
        else{
            mTvInfo.setText(R.string.title_off);
        }

        SeekArc mCircularVir = mView.findViewById(R.id.seek_sleep);
        mCircularVir.setProgressColor(getResources().getColor(R.color.colorAccent));
        mCircularVir.setArcColor(getResources().getColor(R.color.main_color_secondary_text));
        mCircularVir.setMax((MAX_SLEEP_MODE - MIN_SLEEP_MODE) / STEP_SLEEP_MODE + 1);
        mCircularVir.setProgressWidth(getResources().getDimensionPixelOffset(R.dimen.tiny_margin));
        mCircularVir.setProgress(YPYSettingManager.getSleepMode(this) / STEP_SLEEP_MODE);
        mCircularVir.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                try {
                    YPYSettingManager.setSleepMode(YPYFragmentActivity.this, progress * STEP_SLEEP_MODE);
                    if (progress == 0) {
                        mTvInfo.setText(R.string.title_off);
                    }
                    else {
                        mTvInfo.setText(String.format(getString(R.string.format_minutes), String.valueOf(YPYSettingManager.getSleepMode(YPYFragmentActivity.this))));
                    }
                    ArrayList<TrackModel> mListSongs = MusicDataMng.getInstance().getListPlayingTrackObjects();
                    if(mListSongs!=null && mListSongs.size()>0){
                        startMusicService(ACTION_UPDATE_SLEEP_MODE);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.title(R.string.title_sleep_mode);
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.customView(mView, false);
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(R.string.title_done);
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
            }
        });
        final MaterialDialog mDialog = mBuilder.build();
        mDialog.show();
    }

    public void goToEqualizer(){
        Intent mIntent = new Intent(this,YPYEqualizerActivity.class);
        startActivity(mIntent);
    }

    public void setUpRecyclerViewAsListView(RecyclerView mListViewTrack, Drawable mDivider){
        if(mDivider!=null){
            mListViewTrack.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, mDivider));
        }
        mListViewTrack.setHasFixedSize(true);
        LinearLayoutManager mLayoutMngList = new LinearLayoutManager(this);
        mLayoutMngList.setOrientation(RecyclerView.VERTICAL);
        mListViewTrack.setLayoutManager(mLayoutMngList);
    }

    public void setUpRecyclerViewAsGridView(RecyclerView mGridView,int numberColumn) {
        mGridView.setHasFixedSize(false);
        GridLayoutManager layoutManager = new GridLayoutManager(this,numberColumn);
        mGridView.setLayoutManager(layoutManager);

        DividerItemDecoration mItemDecorVerti = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, this.getResources().getDrawable(R.drawable.alpha_divider_verti));
        mGridView.addItemDecoration(mItemDecorVerti);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNeedGrantSettingPermission() {
        try {
            if (IOUtils.hasMarsallow()) {
                boolean retVal = Settings.System.canWrite(this);
                if(!retVal){
                    showFullDialog(R.string.title_confirm,getString(R.string.info_write_setting_permission), R.string.title_ok, R.string.title_cancel, () -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    });
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public void saveAsRingtone(TrackModel mSongObject) {
        try{
            if(isNeedGrantSettingPermission()){
                return;
            }
            final File mOutPutFile = new File(mSongObject.getPath());
            if (mOutPutFile != null && mOutPutFile.isFile()) {
                Uri mUri = null;
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, mOutPutFile.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, mSongObject.getTitle());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "com/*");
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);

                String id = getIdFromContentUri(mOutPutFile.getAbsolutePath());
                if (StringUtils.isEmpty(id)) {
                    mUri = getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(mOutPutFile.getAbsolutePath()), values);
                }
                else {
                    getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.MediaColumns._ID + " = ?", new String[]{id});
                    mUri = Uri.parse(String.format(FORMAT_URI, id));
                }
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, mUri);
                showToast(R.string.info_set_ringtone_successfully);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void saveAsNotification(TrackModel mSongObject) {
        try{
            if(isNeedGrantSettingPermission()){
                return;
            }
            final File mOutPutFile = new File(mSongObject.getPath());
            if (mOutPutFile != null && mOutPutFile.isFile()) {
                Uri mUri = null;
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, mOutPutFile.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, mSongObject.getTitle());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "com/*");
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);

                String id = getIdFromContentUri(mOutPutFile.getAbsolutePath());
                if (StringUtils.isEmpty(id)) {
                    mUri = getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(mOutPutFile.getAbsolutePath()), values);
                }
                else {
                    getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.MediaColumns._ID + " = ?", new String[]{id});
                    mUri = Uri.parse(String.format(FORMAT_URI, id));
                }

                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, mUri);
                showToast(R.string.info_set_notification_successfully);

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getIdFromContentUri(String path) {
        try {
            if (path != null) {
                String id;
                String[] filePathColumn = {MediaStore.MediaColumns._ID};
                String[] selectionArgs = {path};
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, filePathColumn, MediaStore.MediaColumns.DATA + " = ?", selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    id = cursor.getString(columnIndex);
                    cursor.close();
                    return id;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showDialogEditTrack(final TrackModel mTrackObject, final IYPYCallback mCallback) {
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_song, null);

        final EditText mEdSongName = mView.findViewById(R.id.ed_song);
        mEdSongName.setText(mTrackObject.getTitle());

        final EditText mEdSongArtist = mView.findViewById(R.id.ed_artist);
        mEdSongArtist.setText(mTrackObject.getAuthor());

        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title(R.string.title_edit_song);
        mBuilder.backgroundColor(getResources().getColor(R.color.dialog_bg_color));
        mBuilder.titleColor(getResources().getColor(R.color.main_color_text));
        mBuilder.contentColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.customView(mView, false);
        mBuilder.positiveColor(getResources().getColor(R.color.colorAccent));
        mBuilder.positiveText(R.string.title_save);
        mBuilder.negativeText(R.string.title_cancel);
        mBuilder.negativeColor(getResources().getColor(R.color.main_color_secondary_text));
        mBuilder.autoDismiss(true);
        mBuilder.typeface(mTypefaceBold, mTypefaceNormal);
        mBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                String mNewName = mEdSongName.getText().toString();
                String mNewArtist = mEdSongArtist.getText().toString();
                editSong(mNewName, mNewArtist, mTrackObject, mCallback);

            }
        });
        final MaterialDialog mDialog = mBuilder.build();
        mEdSongArtist.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mDialog.dismiss();
                String mNewName = mEdSongName.getText().toString();
                String mNewArtist = mEdSongArtist.getText().toString();
                editSong(mNewName, mNewArtist, mTrackObject, mCallback);
                return true;
            }
            return false;
        });
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mDialog.show();
    }

    private void editSong(final String mNewName, String mArtist, final TrackModel mTrackObject, final IYPYCallback mCallback) {
        if (TextUtils.isEmpty(mNewName)) {
            showToast(R.string.info_empty);
            return;
        }
        if (!mNewName.equalsIgnoreCase(mTrackObject.getTitle()) || mArtist != null && !mArtist.equalsIgnoreCase(mTrackObject.getAuthor())) {
            if (TextUtils.isEmpty(mArtist) || mArtist.equalsIgnoreCase(PREFIX_UNKNOWN)) {
                mArtist = PREFIX_UNKNOWN;
            }
            final String finalArtist = mArtist;
            showProgressDialog();
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                boolean b = mTotalMng.renameOfSong(this, mTrackObject.getId(), finalArtist, mNewName);
                if (b) {
                    mTrackObject.setTitle(mNewName);
                    mTrackObject.setAuthor(finalArtist);
                    runOnUiThread(() -> {
                        dimissProgressDialog();
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    });

                }
                else {
                    runOnUiThread(() ->  dimissProgressDialog());
                }
            });


        }
    }


}
