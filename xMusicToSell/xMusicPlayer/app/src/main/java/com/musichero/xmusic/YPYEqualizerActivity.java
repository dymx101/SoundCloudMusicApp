package com.musichero.xmusic;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.musichero.xmusic.adapter.PresetAdapter;
import com.musichero.xmusic.dataMng.MusicDataMng;
import com.musichero.xmusic.listener.IDBMusicPlayerListener;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.utils.StringUtils;
import com.musichero.xmusic.view.SwitchView;
import com.musichero.xmusic.view.VerticalSeekBar;
import com.triggertrap.seekarc.SeekArc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.BindView;


/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:YPYMusicPlayer
 * @Date:Apr 18, 2015
 */
public class YPYEqualizerActivity extends YPYFragmentActivity implements IDBMusicPlayerListener {

    public static final String TAG = YPYEqualizerActivity.class.getSimpleName();

    @BindView(R.id.layout_bands)
    LinearLayout mLayoutBands;

    @BindView(R.id.list_preset)
    Spinner mSpinnerPresents;

    @BindView(R.id.switch1)
    SwitchView mSwitchBtn;

    @BindView(R.id.tv_info_virtualizer)
    TextView mTvInfoVirtualizer;

    @BindView(R.id.tv_info_bass)
    TextView mTvInfoBass;

    @BindView(R.id.layout_bass_vir)
    LinearLayout mLayoutBassVir;

    @BindView(R.id.seekVir)
    SeekArc mCircularVir;

    @BindView(R.id.seekBass)
    SeekArc mCircularBass;

    private MediaPlayer mMediaPlayer;

    private Equalizer mEqualizer;

    private String[] mLists;
    private ArrayList<VerticalSeekBar> listSeekBars = new ArrayList<VerticalSeekBar>();

    private short bands;

    private short minEQLevel;

    private short maxEQLevel;

    private String[] mEqualizerParams;

    private boolean isCreateLocal;

    private BassBoost mBassBoost;
    private Virtualizer mVirtualizer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_equalizer);

        setUpCustomizeActionBar();
        setActionBarTitle(R.string.title_equalizer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setColorForActionBar(Color.TRANSPARENT);
        getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);

        mSwitchBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mCircularBass.setProgressColor(getResources().getColor(R.color.colorAccent));
        mCircularBass.setArcColor(getResources().getColor(R.color.main_color_hint_text));

        mCircularBass.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                try{
                    mTvInfoBass.setText(String.valueOf(progress));
                    if (fromUser) {
                        if (mBassBoost != null) {
                            YPYSettingManager.setBassBoost(YPYEqualizerActivity.this, (short) progress);
                            mBassBoost.setStrength((short) (progress * RATE_EFFECT));
                        }
                    }
                }
                catch (Exception e){
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
        mCircularVir.setProgressColor(getResources().getColor(R.color.colorAccent));
        mCircularVir.setArcColor(getResources().getColor(R.color.main_color_hint_text));
        mCircularVir.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                try{
                    mTvInfoVirtualizer.setText(String.valueOf(progress));
                    if (fromUser) {
                        if (mVirtualizer != null) {
                            YPYSettingManager.setVirtualizer(YPYEqualizerActivity.this, (short) progress);
                            mVirtualizer.setStrength((short) (progress * RATE_EFFECT));
                        }
                    }
                }
                catch (Exception e){
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
        mSwitchBtn.setOncheckListener(check -> {
            YPYSettingManager.setEqualizer(YPYEqualizerActivity.this, check);
            startCheckEqualizer();
        });
        registerMusicPlayerBroadCastReceiver(this);
        setUpEffects(true);
    }

    private void setUpEqualizerParams() {
        if (mEqualizer != null) {
            String presetStr = YPYSettingManager.getEqualizerPreset(this);
            if (!StringUtils.isEmpty(presetStr)) {
                if (StringUtils.isNumber(presetStr)) {
                    short preset = Short.parseShort(presetStr);
                    short numberPreset = mEqualizer.getNumberOfPresets();
                    if (numberPreset > 0) {
                        if (preset < numberPreset - 1 && preset >= 0) {
                            mEqualizer.usePreset(preset);
                            mSpinnerPresents.setSelection(preset);
                            return;
                        }
                    }
                }
            }
            setUpEqualizerCustom();
        }
    }

    private void setUpEqualizerCustom() {
        try{
            if (mEqualizer != null) {
                String params = YPYSettingManager.getEqualizerParams(this);
                if (!StringUtils.isEmpty(params)) {
                    mEqualizerParams = params.split(":");
                    if (mEqualizerParams != null && mEqualizerParams.length > 0) {
                        int size = mEqualizerParams.length;
                        for (int i = 0; i < size; i++) {
                            mEqualizer.setBandLevel((short) i, Short.parseShort(mEqualizerParams[i]));
                            listSeekBars.get(i).setProgress(Short.parseShort(mEqualizerParams[i]) - minEQLevel);
                        }
                        mSpinnerPresents.setSelection(mLists.length - 1);
                        YPYSettingManager.setEqualizerPreset(this, String.valueOf(mLists.length - 1));
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void saveEqualizerParams() {
        try{
            if (mEqualizer != null) {
                if (bands > 0) {
                    String data = "";
                    for (short i = 0; i < bands; i++) {
                        if (i < bands - 1) {
                            data = data + mEqualizer.getBandLevel(i) + ":";
                        }
                    }
                    YPYSettingManager.setEqualizerPreset(this, String.valueOf(mLists.length - 1));
                    YPYSettingManager.setEqualizerParams(this, data);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void startCheckEqualizer() {
        try {
            boolean b = YPYSettingManager.getEqualizer(this);
            mSpinnerPresents.setEnabled(b);

            if (mEqualizer != null) {
                mEqualizer.setEnabled(b);
            }
            if (listSeekBars.size() > 0) {
                for (int i = 0; i < listSeekBars.size(); i++) {
                    listSeekBars.get(i).setEnabled(b);
                }
            }
            mCircularBass.setEnabled(b);
            mCircularVir.setEnabled(b);
            mSwitchBtn.setChecked(b);
            if (mBassBoost != null) {
                mBassBoost.setEnabled(b);
            }

            if (mVirtualizer != null) {
                mVirtualizer.setEnabled(b);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setupEqualizerFxAndUI(boolean isFirstTime) {
        long pivotTime = System.currentTimeMillis();
        mEqualizer = MusicDataMng.getInstance().getEqualizer();
        if (mEqualizer == null) {
            mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
            mEqualizer.setEnabled(YPYSettingManager.getEqualizer(this));
        }
        try {
            bands = mEqualizer.getNumberOfBands();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (bands == 0) {
            backToHome();
            return;
        }
        short[] bandRange = null;
        try {
            bandRange = mEqualizer.getBandLevelRange();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (bandRange == null || bandRange.length < 2) {
            backToHome();
            return;
        }
        minEQLevel = bandRange[0];
        maxEQLevel = bandRange[1];

        if(isFirstTime){
            for (short i = 0; i < bands; i++) {
                final short band = i;
                View mView = LayoutInflater.from(this).inflate(R.layout.item_equalizer, null);
                TextView minDbTextView = mView.findViewById(R.id.tv_min_db);
                minDbTextView.setText((minEQLevel / 100) + " dB");
                minDbTextView.setTypeface(mTypefaceNormal);

                TextView maxDbTextView = mView.findViewById(R.id.tv_max_db);
                maxDbTextView.setText((maxEQLevel / 100) + " dB");
                maxDbTextView.setTypeface(mTypefaceNormal);

                VerticalSeekBar mSliderView = mView.findViewById(R.id.mySeekBar);
                mSliderView.setMax(maxEQLevel - minEQLevel);
                mSliderView.setProgress(mEqualizer.getBandLevel(band) - minEQLevel);
                Drawable mDrawable = mSliderView.getProgressDrawable();
                if (mDrawable != null) {
                    if (mDrawable instanceof LayerDrawable) {
                        LayerDrawable mLayerDrawable = (LayerDrawable) mDrawable;
                        Drawable mDrawableBg = mLayerDrawable.findDrawableByLayerId(android.R.id.background);
                        if (mDrawableBg != null) {
                            mDrawable.setColorFilter(getResources().getColor(R.color.main_color_hint_text), PorterDuff.Mode.SRC_ATOP);
                        }
                        Drawable mDrawableProgress = mLayerDrawable.findDrawableByLayerId(android.R.id.progress);
                        if (mDrawableProgress != null) {
                            mDrawableProgress.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                        }
                        mSliderView.postInvalidate();

                    }
                    else if (mDrawable instanceof StateListDrawable) {
                        StateListDrawable mStateListDrawable = (StateListDrawable) mDrawable;
                        try {
                            int[] currentState = new int[]{android.R.attr.state_enabled};
                            int[] currentState1 = new int[]{-android.R.attr.state_enabled};
                            Method getStateDrawableIndex = StateListDrawable.class.getMethod("getStateDrawableIndex", int[].class);
                            Method getStateDrawable = StateListDrawable.class.getMethod("getStateDrawable", int.class);

                            int index = (int) getStateDrawableIndex.invoke(mStateListDrawable, currentState);
                            int index1 = (int) getStateDrawableIndex.invoke(mStateListDrawable, currentState1);
                            Drawable drawable = (Drawable) getStateDrawable.invoke(mStateListDrawable, index);
                            drawable.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

                            Drawable drawable1 = (Drawable) getStateDrawable.invoke(mStateListDrawable, index1);
                            drawable1.setColorFilter(getResources().getColor(R.color.main_color_hint_text), PorterDuff.Mode.SRC_ATOP);
                        }
                        catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        mSliderView.postInvalidate();
                    }
                    Drawable mThumb = getResources().getDrawable(R.drawable.thumb_default);
                    mThumb.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                    mSliderView.setThumb(mThumb);
                }

                mSliderView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            try{
                                mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
                                saveEqualizerParams();
                                mSpinnerPresents.setSelection(mLists.length - 1);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                listSeekBars.add(mSliderView);
                LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mLayoutBands.addView(mView, mLayoutParams);
            }
        }

    }


    private void setUpBassVirtualizer() {
        BassBoost mBassBoost = MusicDataMng.getInstance().getBassBoost();
        if (mBassBoost == null) {
            mBassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
        }
        try {
            if (mBassBoost.getStrengthSupported()) {
                Virtualizer mVirtualizer = MusicDataMng.getInstance().getVirtualizer();
                if (mVirtualizer == null) {
                    mVirtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
                }
                if (mVirtualizer.getStrengthSupported()) {
                    short mCurrentShort = YPYSettingManager.getBassBoost(this);
                    mBassBoost.setStrength((short) (mCurrentShort * RATE_EFFECT));
                    mBassBoost.setEnabled(YPYSettingManager.getEqualizer(this));

                    short mCurrentVir = YPYSettingManager.getVirtualizer(this);
                    mVirtualizer.setStrength((short) (mCurrentVir * RATE_EFFECT));
                    mVirtualizer.setEnabled(YPYSettingManager.getEqualizer(this));

                    mCircularBass.setProgress(mCurrentShort);
                    mCircularVir.setProgress(mCurrentVir);

                    this.mBassBoost = mBassBoost;
                    this.mVirtualizer = mVirtualizer;
                }
                else {
                    mLayoutBassVir.setVisibility(View.GONE);
                }

            }
            else {
                mLayoutBassVir.setVisibility(View.GONE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            mLayoutBassVir.setVisibility(View.GONE);
        }
    }

    private void setUpPresetName() {
        if(mLists!=null){
            return;
        }
        if (mEqualizer != null) {
            short numberPreset = mEqualizer.getNumberOfPresets();
            if (numberPreset > 0) {
                mLists = new String[numberPreset + 1];
                for (short i = 0; i < numberPreset; i++) {
                    mLists[i] = mEqualizer.getPresetName(i);
                }
                mLists[numberPreset] = getString(R.string.title_custom);

                PresetAdapter dataAdapter = new PresetAdapter(this, R.layout.item_preset_name, mLists);
                mSpinnerPresents.setAdapter(dataAdapter);
                dataAdapter.setPresetListener(position -> mSpinnerPresents.setSelection(position));
                mSpinnerPresents.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        YPYSettingManager.setEqualizerPreset(YPYEqualizerActivity.this, String.valueOf(position));
                        try{
                            if (position < mLists.length - 1) {
                                mEqualizer.usePreset((short) position);
                            }
                            else {
                                setUpEqualizerCustom();
                            }
                            for (short i = 0; i < bands; i++) {
                                VerticalSeekBar bar = listSeekBars.get(i);
                                bar.setProgress(mEqualizer.getBandLevel(i) - minEQLevel);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            else {
                mSpinnerPresents.setVisibility(View.INVISIBLE);
            }
        }
        else {
            mSpinnerPresents.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToHome();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backToHome() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listSeekBars != null) {
            listSeekBars.clear();
            listSeekBars = null;
        }
        if (isCreateLocal) {
            try {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                if (mEqualizer != null) {
                    mEqualizer.release();
                    mEqualizer = null;
                }
                if (mBassBoost != null) {
                    mBassBoost.release();
                    mBassBoost = null;
                }
                if (mVirtualizer != null) {
                    mVirtualizer.release();
                    mVirtualizer = null;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onPlayerUpdateState(boolean isPlay) {
        if(isPlay){
            setUpEffects(false);
        }
    }

    @Override
    public void onPlayerStop() {

    }

    @Override
    public void onPlayerLoading() {
        showProgressDialog();
    }

    @Override
    public void onPlayerStopLoading() {
        dimissProgressDialog();
    }

    @Override
    public void onPlayerUpdatePos(int currentPos) {

    }

    @Override
    public void onPlayerError() {
        dimissProgressDialog();
        backToHome();
    }

    @Override
    public void onPlayerUpdateStatus() {

    }

    private synchronized void setUpEffects(boolean isFirstTime){
        try{
            mMediaPlayer = MusicDataMng.getInstance().getPlayer();
            if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                isCreateLocal = true;
                mMediaPlayer = new MediaPlayer();
            }
            setupEqualizerFxAndUI(isFirstTime);
            setUpBassVirtualizer();

            setUpPresetName();

            if(isFirstTime){
                startCheckEqualizer();
                setUpEqualizerParams();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
