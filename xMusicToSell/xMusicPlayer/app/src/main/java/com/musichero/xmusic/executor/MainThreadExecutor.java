package com.musichero.xmusic.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: androidbox3
 * Created by dotrungbao on 9/10/16.
 */
public class MainThreadExecutor implements Executor {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }

    public void onDestroy(){
        handler.removeCallbacksAndMessages(null);
    }
}
