package com.musichero.xmusic.playservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.musichero.xmusic.R;
import com.musichero.xmusic.YPYMainActivity;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.dataMng.MusicDataMng;
import com.musichero.xmusic.dataMng.TotalDataManager;
import com.musichero.xmusic.executor.DBExecutorSupplier;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.setting.YPYSettingManager;
import com.musichero.xmusic.utils.DBLog;
import com.musichero.xmusic.utils.DownloadUtils;
import com.musichero.xmusic.utils.IOUtils;
import com.musichero.xmusic.utils.ImageProcessingUtils;
import com.musichero.xmusic.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: ColorPlayer
 * Created by dotrungbao on 9/19/16.
 */
public class YPYMusicService extends Service implements IYPYMusicConstant,IXMusicConstants, IMusicFocusableListener {

    public static final String TAG = YPYMusicService.class.getSimpleName();

    public static final int STATE_PREPAIRING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_STOP = 4;
    public static final int STATE_ERROR = 5;
    public static final int NOTIFICATION_LOCK_ID = 512;

    private int mCurrentState = STATE_STOP;

    private YPYAudioFocusHelper mAudioFocusHelper = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean isStartLoading;
    private Notification notificationLockScreen;
    private int mMinuteCount;

    private enum AudioFocus {
        NO_FOCUS_NO_DUCK, // we don't have audio focus, and can't duck
        NO_FOCUS_CAN_DUCK, // we don't have focus, but can play at a low volume
        FOCUSED // we have full audio focus
    }

    private AudioFocus mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;

    private NotificationManager mNotificationManager;

    private Notification mNotification = null;

    private TrackModel mCurrentTrack;
    private Handler mHandler = new Handler();
    private Handler mHandlerSleep = new Handler();

    //private RemoteViews notificationView;

    private Bitmap mBitmapTrack;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioFocusHelper = new YPYAudioFocusHelper(getApplicationContext(), this);
    }

    private void startSleepMode() {
        int minute = YPYSettingManager.getSleepMode(this);
        mHandlerSleep.removeCallbacksAndMessages(null);
        if (minute > 0) {
            this.mMinuteCount = minute*ONE_MINUTE;
            startCountSleep();
        }

    }

    private void startCountSleep(){
        if(mMinuteCount>0){
            mHandlerSleep.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMinuteCount=mMinuteCount-1000;
                    if(mMinuteCount<=0){
                        try {
                            processStopRequest(true);
                            if (!YPYSettingManager.getOnline(YPYMusicService.this)) {
                                MusicDataMng.getInstance().onDestroy();
                                TotalDataManager.getInstance().onDestroy();
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        startCountSleep();
                    }

                }
            }, 1000);
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String packageName = getPackageName();
        String action = intent.getAction();
        if (StringUtils.isEmpty(action)) {
            return START_NOT_STICKY;
        }
        if (action.equals(packageName + ACTION_TOGGLE_PLAYBACK)) {
            processTogglePlaybackRequest();
        }
        else if (action.equals(packageName + ACTION_PLAY)) {
            startSleepMode();
            processPlayRequest(true);
        }
        else if (action.equals(packageName + ACTION_PAUSE)) {
            processPauseRequest();
        }
        else if (action.equals(packageName + ACTION_NEXT)) {
            processNextRequest(false);
        }
        else if (action.equals(packageName + ACTION_STOP)) {
            processStopRequest(true);
        }
        else if (action.equals(packageName + ACTION_PREVIOUS)) {
            processPreviousRequest();
        }
        else if (action.equals(packageName + ACTION_SEEK)) {
            int mCurrentPos = intent.getIntExtra(KEY_VALUE, -1);
            processSeekBar(mCurrentPos);
        }
        else if (action.equals(packageName + ACTION_UPDATE_SLEEP_MODE)) {
            startSleepMode();
        }
        else if (action.equals(packageName + ACTION_SHUFFLE)) {
            boolean mCurrentPos = intent.getBooleanExtra(KEY_VALUE, false);
            YPYSettingManager.setShuffle(this, mCurrentPos);
            sendMusicBroadcast(ACTION_UPDATE_STATUS);
        }
        else if (action.equalsIgnoreCase(packageName + ACTION_UPDATE_STATUS)) {
            updateNotification();
        }
        return START_NOT_STICKY;
    }

    private void processPreviousRequest() {
        try {
            mCurrentTrack = MusicDataMng.getInstance().getPrevTrackObject(this);
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                processStopRequest(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTogglePlaybackRequest() {
        try {
            if (mCurrentState == STATE_PAUSE || mCurrentState == STATE_STOP) {
                processPlayRequest(false);
            }
            else {
                processPauseRequest();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processPlayRequest(boolean isForces) {
        ArrayList<TrackModel> mListTrack = MusicDataMng.getInstance().getListPlayingTrackObjects();
        if (mListTrack == null) {
            mCurrentState = STATE_ERROR;
            processStopRequest(true);
            return;
        }
        mCurrentTrack = MusicDataMng.getInstance().getCurrentTrackObject();
        if (mCurrentTrack == null) {
            mCurrentState = STATE_ERROR;
            processStopRequest(true);
            return;
        }
        if (mCurrentState == STATE_STOP || mCurrentState == STATE_PLAYING || isForces) {
            startPlayNewSong();
            sendMusicBroadcast(ACTION_NEXT);
        }
        else if (mCurrentState == STATE_PAUSE) {
            mCurrentState = STATE_PLAYING;
            configAndStartMediaPlayer();
            setUpNotification();
        }

    }


    private synchronized void startPlayNewSong() {
        tryToGetAudioFocus();
        if (!isStartLoading) {
            mCurrentState = STATE_STOP;
            isStartLoading = true;
            if (mCurrentTrack == null) {
                mCurrentState = STATE_ERROR;
                processStopRequest(true);
                return;
            }
            if(MusicDataMng.getInstance().isPlayingMusic()){
                releaseMedia();
            }
            startStreamMusic();
        }

    }

    private synchronized void startStreamMusic() {
        if (mCurrentTrack != null) {
            releaseMedia();
            onDestroyBitmap();
            setUpNotification();
            sendMusicBroadcast(ACTION_LOADING);
            MusicDataMng.getInstance().setLoading(true);
            DBExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    String artWork = mCurrentTrack.getArtworkUrl();
                    if (!StringUtils.isEmpty(artWork)) {
                        startGetHttpBitmap();
                    }
                    else {
                        startGetLocalBitmap();
                    }
                    setUpNotification();
                    final String uriStream = mCurrentTrack.startGetLinkStream(YPYMusicService.this);
                    DBLog.d(TAG, "=========>uriStream=" + uriStream);
                    DBExecutorSupplier.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            isStartLoading = false;
                            if (!StringUtils.isEmpty(uriStream)) {
                                setUpMediaForStream(uriStream);
                            }
                            else {
                                sendMusicBroadcast(ACTION_DIMISS_LOADING);
                                mCurrentState = STATE_ERROR;
                                MusicDataMng.getInstance().setLoading(false);
                                processStopRequest(true);
                            }

                        }
                    });
                }
            });
        }
    }

    private boolean setUpMediaForStream(final String path) {
        createMediaPlayer();
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDataSource(path);
                mCurrentState = STATE_PREPAIRING;
                mMediaPlayer.prepareAsync();
                return true;
            }
        }
        catch (Exception ex) {
            Log.d(TAG, "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
            processStopRequest(true);
        }
        return false;
    }


    private void startGetHttpBitmap() {
        String artWork = mCurrentTrack.getArtworkUrl();
        try {
            if (artWork.startsWith("http")) {
                try {
                    InputStream mInputStream = DownloadUtils.download(artWork);
                    if (mInputStream != null) {
                        mBitmapTrack = ImageProcessingUtils.decodePortraitBitmap(mInputStream, 100, 100);
                        mInputStream.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                String path = artWork.replace("file://", "");
                File mFile = new File(path);
                if (mFile.exists() && mFile.isFile()) {
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(path, mOptions);
                    ImageProcessingUtils.calculateInSampleSize(mOptions, 100, 100);
                    mOptions.inJustDecodeBounds = false;
                    mBitmapTrack = BitmapFactory.decodeFile(path, mOptions);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGetLocalBitmap() {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(YPYMusicService.this, mCurrentTrack.getURI());
            byte[] rawArt = mmr.getEmbeddedPicture();
            ByteArrayInputStream mInputStream = null;
            if (rawArt != null && rawArt.length > 0) {
                mInputStream = new ByteArrayInputStream(rawArt);
                mmr.release();
            }
            else {
                mmr.release();
            }
            if (mInputStream != null) {
                mBitmapTrack = ImageProcessingUtils.decodePortraitBitmap(mInputStream, 100, 100);
            }
            else {
                mBitmapTrack = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rect_music_default);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                mBitmapTrack = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rect_music_default);
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }


    private void processPauseRequest() {
        if (mCurrentTrack == null || mMediaPlayer == null) {
            mCurrentState = STATE_ERROR;
            processStopRequest(true);
            return;
        }
        try {
            if (mCurrentState == STATE_PLAYING) {
                mCurrentState = STATE_PAUSE;
                mMediaPlayer.pause();
                setUpNotification();
                sendMusicBroadcast(ACTION_PAUSE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            processNextRequest(false);
        }
    }

    private void processNextRequest(boolean isComplete) {
        try {
            mCurrentTrack = MusicDataMng.getInstance().getNextTrackObject(this, isComplete);
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                processStopRequest(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processStopRequest(boolean isDestroyAll) {
        isStartLoading = false;
        try {
            releaseData(isDestroyAll);
            sendMusicBroadcast(ACTION_STOP);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseData(true);
        try {
            giveUpAudioFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mNotification = null;

    }

    private void releaseData(final boolean isDestroyAll) {
        mHandler.removeCallbacksAndMessages(null);
        onDestroyBitmap();
        mHandlerSleep.removeCallbacksAndMessages(null);
        releaseMedia();
        try {
            if (isDestroyAll) {
                stopForeground(true);
                MusicDataMng.getInstance().onDestroy();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (isDestroyAll) {
            try {
                if (notificationLockScreen != null) {
                    mNotificationManager.cancel(NOTIFICATION_LOCK_ID);
                    notificationLockScreen = null;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void onDestroyBitmap() {
        try{
            if (mBitmapTrack != null) {
                mBitmapTrack.recycle();
                mBitmapTrack = null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void releaseMedia() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                MusicDataMng.getInstance().setPlayer(null);
                MusicDataMng.getInstance().releaseEffect();
                mMediaPlayer = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mCurrentState = STATE_STOP;

    }

    private void createMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    sendMusicBroadcast(ACTION_DIMISS_LOADING);
                    MusicDataMng.getInstance().setLoading(false);
                    mCurrentState = STATE_PLAYING;
                    initAudioEffect();
                    configAndStartMediaPlayer();
                    setUpNotification();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = STATE_STOP;
                    processNextRequest(true);
                    sendMusicBroadcast(ACTION_NEXT);
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    try {
                        MusicDataMng.getInstance().setLoading(false);
                        DBLog.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));
                        sendMusicBroadcast(ACTION_DIMISS_LOADING);
                        mCurrentState = STATE_ERROR;
                        processStopRequest(true);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            MusicDataMng.getInstance().setPlayer(mMediaPlayer);
        }
        catch (Exception e) {
            e.printStackTrace();
            mCurrentState = STATE_ERROR;
            processStopRequest(true);
        }

    }


    @Override
    public void onGainedAudioFocus() {
        try {
            mAudioFocus = AudioFocus.FOCUSED;
            if (mCurrentState == STATE_PLAYING) {
                configAndStartMediaPlayer();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        try {
            mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                configAndStartMediaPlayer();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void tryToGetAudioFocus() {
        try {
            if (mAudioFocus != null && mAudioFocus != AudioFocus.FOCUSED
                    && mAudioFocusHelper != null
                    && mAudioFocusHelper.requestFocus())
                mAudioFocus = AudioFocus.FOCUSED;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void giveUpAudioFocus() {
        try {
            if (mAudioFocus != null && mAudioFocus == AudioFocus.FOCUSED &&
                    mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) {
                mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mMediaPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    private void configAndStartMediaPlayer() {
        try {
            if (mMediaPlayer != null && (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE)) {
                if (mAudioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mHandler.removeCallbacksAndMessages(null);
                        sendMusicBroadcast(ACTION_PAUSE);
                    }
                    return;
                }
                else if (mAudioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
                    mMediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
                }
                else {
                    mMediaPlayer.setVolume(MAX_VOLUME, MAX_VOLUME);
                }
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    startUpdatePosition();
                    sendMusicBroadcast(ACTION_PLAY);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startUpdatePosition() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mMediaPlayer != null && mCurrentTrack != null) {
                    try {
                        int current = mMediaPlayer.getCurrentPosition();
                        sendMusicBroadcast(ACTION_UPDATE_POS, current);
                        if (current < mCurrentTrack.getDuration()) {
                            startUpdatePosition();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }, 1000);
    }

    public void sendMusicBroadcast(String action, int value) {
        try{
            Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
            if (value != -1) {
                mIntent.putExtra(KEY_VALUE, value);
            }
            mIntent.putExtra(KEY_ACTION, getPackageName() + action);
            sendBroadcast(mIntent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendMusicBroadcast(String action) {
        sendMusicBroadcast(action, -1);
    }


    private void updateNotification() {
        try {
            if (mNotification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpNotification() {
        if (mCurrentTrack == null) {
            return;
        }
        try {
            String packageName = getPackageName();
            Intent mIntent = new Intent(this.getApplicationContext(), YPYMainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            String CHANNEL_ONE_ID = getPackageName()+".N1";
            String CHANNEL_ONE_NAME = ANDROID8_CHANNEL_ONE_NAME;
            if(IOUtils.hasAndroid80()){
                try{
                    Log.e("DCM","======>put channel id for notification on android 8.0");
                    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                            CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            mBuilder.setSmallIcon(R.drawable.ic_notification_24dp);
            mBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
            mBuilder.setShowWhen(false);
            if(IOUtils.hasAndroid80()){
                Log.e("DCM","======>put channel id for notification on android 8.0");
                mBuilder.setChannelId(CHANNEL_ONE_ID);
            }

            String artist = mCurrentTrack.getAuthor();
            if (StringUtils.isEmpty(artist) || artist.equalsIgnoreCase(PREFIX_UNKNOWN)) {
                artist = getString(R.string.title_unknown);
            }

            Intent nextIntent = new Intent(this, YPYMusicIntentReceiver.class);
            nextIntent.setAction(packageName + ACTION_NEXT);
            PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 100, nextIntent, 0);

            Intent stopIntent = new Intent(this, YPYMusicIntentReceiver.class);
            stopIntent.setAction(packageName + ACTION_STOP);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 100, stopIntent, 0);

            Intent toggleIntent = new Intent(this, YPYMusicIntentReceiver.class);
            toggleIntent.setAction(packageName + ACTION_TOGGLE_PLAYBACK);
            PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 100, toggleIntent, 0);

            boolean isPlay = MusicDataMng.getInstance().isPlayingMusic();
            mBuilder.addAction(isPlay ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp, "Pause", pendingToggleIntent);
            mBuilder.addAction(R.drawable.ic_skip_next_white_36dp, "Next", pendingNextIntent);
            mBuilder.addAction(R.drawable.ic_close_white_36dp, "Close", stopPendingIntent);

            androidx.media.app.NotificationCompat.MediaStyle mStyle = new androidx.media.app.NotificationCompat.MediaStyle();
            mStyle.setShowActionsInCompactView(0, 1, 2);
            mBuilder.setStyle(mStyle);
            mBuilder.setContentTitle(getString(R.string.app_name));
            mBuilder.setContentText(mCurrentTrack.getTitle());
            mBuilder.setSubText(artist);
            if(mBitmapTrack!=null){
                mBuilder.setLargeIcon(mBitmapTrack);
            }
            mBuilder.setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT);
            mNotification = mBuilder.build();
            mNotification.contentIntent = pi;

            mNotification.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(NOTIFICATION_ID, mNotification);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void processSeekBar(int currentPos) {
        try {
            if ((mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE)
                    && currentPos > 0) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(currentPos);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initAudioEffect() {
        boolean b = YPYSettingManager.getEqualizer(this);
        try {
            Equalizer mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
            mEqualizer.setEnabled(b);
            setUpParams(mEqualizer);
            MusicDataMng.getInstance().setEqualizer(mEqualizer);

            setUpBassBoostVir();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setUpBassBoostVir() {
        try {
            boolean b = YPYSettingManager.getEqualizer(this);
            BassBoost mBassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
            Virtualizer mVirtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
            if (mBassBoost.getStrengthSupported() && mVirtualizer.getStrengthSupported()) {
                short bass = YPYSettingManager.getBassBoost(this);
                short vir = YPYSettingManager.getVirtualizer(this);
                mBassBoost.setEnabled(b);
                mVirtualizer.setEnabled(b);
                mBassBoost.setStrength((short) (bass * RATE_EFFECT));
                mVirtualizer.setStrength((short) (vir * RATE_EFFECT));

                MusicDataMng.getInstance().setBassBoost(mBassBoost);
                MusicDataMng.getInstance().setVirtualizer(mVirtualizer);
            }
            else {
                try {
                    mBassBoost.release();
                    mVirtualizer.release();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setUpParams(Equalizer mEqualizer) {
        if (mEqualizer != null) {
            String presetStr = YPYSettingManager.getEqualizerPreset(this);
            if (!StringUtils.isEmpty(presetStr)) {
                if (StringUtils.isNumber(presetStr)) {
                    short preset = Short.parseShort(presetStr);
                    short numberPreset = mEqualizer.getNumberOfPresets();
                    if (numberPreset > 0) {
                        if (preset < numberPreset - 1 && preset >= 0) {
                            try {
                                mEqualizer.usePreset(preset);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                }
            }
            setUpEqualizerCustom(mEqualizer);
        }
    }

    private void setUpEqualizerCustom(Equalizer mEqualizer) {
        if (mEqualizer != null) {
            String params = YPYSettingManager.getEqualizerParams(this);
            if (!StringUtils.isEmpty(params)) {
                String[] mEqualizerParams = params.split(":");
                if (mEqualizerParams != null && mEqualizerParams.length > 0) {
                    int size = mEqualizerParams.length;
                    for (int i = 0; i < size; i++) {
                        mEqualizer.setBandLevel((short) i, Short.parseShort(mEqualizerParams[i]));
                    }
                    short numberPreset = mEqualizer.getNumberOfPresets();
                    YPYSettingManager.setEqualizerPreset(this, String.valueOf(numberPreset));
                }
            }
        }
    }


}
