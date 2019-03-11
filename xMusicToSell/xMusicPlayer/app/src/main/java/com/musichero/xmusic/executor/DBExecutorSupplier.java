package com.musichero.xmusic.executor;

import android.os.Process;

import com.musichero.xmusic.utils.DBLog;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: androidbox3
 * Created by dotrungbao on 9/10/16.
 */
public class DBExecutorSupplier {
    public static final String TAG =DBExecutorSupplier.class.getSimpleName();
    /*
    * Number of cores to decide the number of threads
    */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    /*
    * thread pool com.makingmixes.app.executor for background tasks
    */
    private ThreadPoolExecutor mForBackgroundTasks;
    /*
    * thread pool com.makingmixes.app.executor for light weight background tasks
    */
    private ThreadPoolExecutor mForLightWeightBackgroundTasks;
    /*
    * thread pool com.makingmixes.app.executor for main thread tasks
    */
    private MainThreadExecutor mMainThreadExecutor;
    /*
    * an instance of DBExecutorSupplier
    */
    private static DBExecutorSupplier sInstance;
    private PriorityThreadFactory mBgThreadPiority;

    /*
    * returns the instance of DBExecutorSupplier
    */
    public static DBExecutorSupplier getInstance() {
        if (sInstance == null) {
            synchronized (DBExecutorSupplier.class) {
                sInstance = new DBExecutorSupplier();
            }
        }
        return sInstance;
    }

    /*
    * constructor for  DBExecutorSupplier
    */
    private DBExecutorSupplier() {
        DBLog.d(TAG,"===================>number core="+NUMBER_OF_CORES);
        // setting the thread factory
        mBgThreadPiority = new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);
        // setting the thread pool com.makingmixes.app.executor for mForBackgroundTasks;
        mForBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mBgThreadPiority
        );

        // setting the thread pool com.makingmixes.app.executor for mForLightWeightBackgroundTasks;
        mForLightWeightBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mBgThreadPiority
        );

        // setting the thread pool com.makingmixes.app.executor for mMainThreadExecutor;
        mMainThreadExecutor = new MainThreadExecutor();
    }

    /*
    * returns the thread pool com.makingmixes.app.executor for background task
    */

    public ThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    /*
    * returns the thread pool com.makingmixes.app.executor for light weight background task
    */
    public ThreadPoolExecutor forLightWeightBackgroundTasks() {
        return mForLightWeightBackgroundTasks;
    }

    /*
    * returns the thread pool com.makingmixes.app.executor for main thread task
    */
    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }

    public void onDestroy() {
        try {
            if (mBgThreadPiority != null) {
                mBgThreadPiority.onDestroy();
                mBgThreadPiority=null;
            }
            if (mMainThreadExecutor != null) {
                mMainThreadExecutor.onDestroy();
                mMainThreadExecutor=null;
            }
            mForLightWeightBackgroundTasks=null;
            mForBackgroundTasks=null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        sInstance=null;

    }
}
