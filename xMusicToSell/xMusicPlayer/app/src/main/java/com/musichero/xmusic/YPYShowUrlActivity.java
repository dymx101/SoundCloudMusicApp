package com.musichero.xmusic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.DBLog;
import com.musichero.xmusic.utils.StringUtils;

import butterknife.BindView;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:MusicPlayer
 * @Date:Jan 13, 2015
 */
public class YPYShowUrlActivity extends YPYFragmentActivity{

    public static final String TAG = YPYShowUrlActivity.class.getSimpleName();

    @BindView(R.id.progressBar1)
    ProgressBar mProgressBar;

    @BindView(R.id.webview)
    WebView mWebViewShowPage;

    private String mUrl;

    private String mNameHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_show_url);

        setUpCustomizeActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setColorForActionBar(Color.TRANSPARENT);
        getSupportActionBar().setHomeAsUpIndicator(mBackDrawable);

        Intent args = getIntent();
        if (args != null) {
            mUrl = args.getStringExtra(KEY_SHOW_URL);
            mNameHeader = args.getStringExtra(KEY_HEADER);
            DBLog.d(TAG, "===========>url=" + mUrl);
        }
        if (!StringUtils.isEmpty(mNameHeader)) {
            setActionBarTitle(mNameHeader);
        }

        this.mProgressBar.setVisibility(View.VISIBLE);
        this.mWebViewShowPage.getSettings().setJavaScriptEnabled(true);
        this.mWebViewShowPage.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }
        });
        this.mWebViewShowPage.loadUrl(mUrl);

        if(!ApplicationUtils.isOnline(this)){
            registerNetworkBroadcastReceiver(isNetworkOn -> {
                if(isNetworkOn){
                    setUpLayoutAdmob();
                }
            });
        }
        setUpLayoutAdmob();
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

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWebViewShowPage != null) {
            mWebViewShowPage.destroy();
        }
    }

    private void backToHome() {
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebViewShowPage.canGoBack()) {
                mWebViewShowPage.goBack();
            }
            else {
                backToHome();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
