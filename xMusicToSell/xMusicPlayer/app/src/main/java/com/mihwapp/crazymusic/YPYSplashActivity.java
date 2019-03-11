package com.mihwapp.crazymusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.setting.YPYSettingManager;
import com.mihwapp.crazymusic.utils.DBLog;
import com.mihwapp.crazymusic.utils.IOUtils;
import com.mihwapp.crazymusic.view.CircularProgressBar;

import java.io.File;

import butterknife.BindView;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.mihwapp.com
 * @Project:MusicPlayer
 */
public class YPYSplashActivity extends YPYFragmentActivity {

    public static final String TAG = YPYSplashActivity.class.getSimpleName();
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1000;

    public static final int REQUEST_PERMISSION_CODE = 1001;
    public static final String[] REQUEST_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    private Handler mHandler = new Handler();

    private boolean isLoading;

    @BindView(R.id.progressBar1)
    CircularProgressBar mProgressBar;

    private boolean isCheckGoogle;
    private GoogleApiAvailability googleAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash);

        YPYSettingManager.setOnline(this, true);
        DBLog.setDebug(DEBUG);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!isCheckGoogle) {
            isCheckGoogle = true;
            checkGooglePlayService();
        }
    }


    private void startLoad() {
        File mFile = mTotalMng.getDirectoryCached();
        if (mFile == null) {
            createFullDialog(-1, R.string.title_info, R.string.title_settings, R.string.title_cancel,
                    getString(R.string.info_error_sdcard), () -> {
                        isCheckGoogle = false;
                        startActivityForResult(new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS), 0);
                    }, () -> {
                        onDestroyData();
                        finish();
                    }).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);

        boolean b = isNeedGrantPermission();
        if (!b) {
            startExecuteTask();
        }

    }
    private void startExecuteTask(){
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            mTotalMng.readConfigure(YPYSplashActivity.this);
            mTotalMng.readGenreData(YPYSplashActivity.this);
            mTotalMng.readCached(TYPE_FILTER_SAVED);
            mTotalMng.readPlaylistCached();
            mTotalMng.readLibraryTrack(YPYSplashActivity.this);
            runOnUiThread(() -> goToMainActivity());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void goToMainActivity() {
        showInterstitial(() -> {
            mProgressBar.setVisibility(View.INVISIBLE);
            Intent mIntent = new Intent(YPYSplashActivity.this, YPYMainActivity.class);
            startActivity(mIntent);
            finish();
        });
       }


    private void startInit() {
        if (!isLoading) {
            isLoading = true;
            mProgressBar.setVisibility(View.VISIBLE);
            startLoad();
        }
    }

    private void checkGooglePlayService() {
        googleAPI = GoogleApiAvailability.getInstance();
        try {
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            if (result == ConnectionResult.SUCCESS) {
                startInit();
            }
            else {
                if (googleAPI.isUserResolvableError(result)) {
                    isCheckGoogle = false;
                    googleAPI.showErrorDialogFragment(this, result, REQUEST_GOOGLE_PLAY_SERVICES);
                }
                else {
                    showToast(googleAPI.getErrorString(result));
                    startInit();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isNeedGrantPermission() {
        try {
            if (IOUtils.hasMarsallow()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_PERMISSION_CODE);
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUEST_PERMISSION_CODE) {
                if (grantResults != null && grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startExecuteTask();
                }
                else {
                    showToast(R.string.info_permission_denied);
                    onDestroyData();
                    finish();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.info_permission_denied);
            onDestroyData();
            finish();
        }

    }



}
