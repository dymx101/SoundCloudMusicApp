package com.musichero.xmusic.task;

import android.os.AsyncTask;


public class YPYTask extends AsyncTask<Void, Void, Void> {
	
	private IYPYTaskListener mDownloadListener;
	
	public YPYTask(IYPYTaskListener mDownloadListener) {
		this.mDownloadListener = mDownloadListener;
	}
	
	@Override
	protected void onPreExecute() {
		if(mDownloadListener!=null){
			mDownloadListener.onPreExcute();
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(mDownloadListener!=null){
			mDownloadListener.onDoInBackground();
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void result) {
		if(mDownloadListener!=null){
			mDownloadListener.onPostExcute();
		}
	}

}
