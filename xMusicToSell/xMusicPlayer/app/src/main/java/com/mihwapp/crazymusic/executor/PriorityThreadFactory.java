package com.mihwapp.crazymusic.executor;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: androidbox3
 * Created by dotrungbao on 9/10/16.
 */
public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;
    private Thread mThread;

    public PriorityThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                }
                catch (Throwable t) {
                    t.printStackTrace();

                }
                runnable.run();
            }
        };
        mThread=new Thread(wrapperRunnable);
        return mThread;

    }
    public void onDestroy(){
        try{
            if(mThread!=null){
                mThread.interrupt();
                mThread=null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
