package com.musichero.xmusic.abtractclass.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.musichero.xmusic.R;
import com.musichero.xmusic.utils.ApplicationUtils;
import com.musichero.xmusic.utils.StringUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class DBFragment extends Fragment implements IDBFragmentConstants{
	
	public static final String TAG = DBFragment.class.getSimpleName();
	public static final String TEST_DEVICE = "51F0A3F4C13F05DD49DE0D71F2B369FB";
	
	public View mRootView;
	private boolean isExtractData;

	public String mNameFragment;
	public int mIdFragment;
	private boolean isAllowFindViewContinous;
	private boolean isCreated;
	private boolean isFirstInTab;

	public ArrayList<Fragment> mListFragments;
	private boolean isLoadingData;
	private RelativeLayout mLayoutAds;
	private Unbinder mBinder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = onInflateLayout(inflater,container,savedInstanceState);
		if(mRootView!=null){
			mBinder= ButterKnife.bind(this,mRootView);
		}
		return mRootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (!isExtractData) {
			isExtractData = true;
			onExtractData();
			findView();
		}
		else{
			if(isAllowFindViewContinous){
				findView();
			}
		}
		isCreated=true;
	}

	public void createArrayFragment(){
		mListFragments= new ArrayList<>();
	}

	@Override
	public void onStart() {
		super.onStart();
		if(isAllowFindViewContinous && isCreated){
			findView();
		}

	}

	public abstract View onInflateLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
	public abstract void findView();
	
	public void onExtractData(){
		Bundle args = getArguments();
		if (args != null) {
			mNameFragment = args.getString(KEY_NAME_FRAGMENT);
			mIdFragment = args.getInt(KEY_ID_FRAGMENT);
		}
	}
	
	public void backToHome(FragmentActivity mContext) {
		FragmentTransaction mFragmentTransaction = null;
		FragmentManager mFragmentManager = mContext.getSupportFragmentManager();
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.remove(this);

		Fragment mFragmentHome = getFragmentHome(mContext);
		if(mFragmentHome!=null){
			mFragmentTransaction.show(mFragmentHome);
		}
		mFragmentTransaction.commit();
	}

	public void setAllowFindViewContinous(boolean isAllowFindViewContinous) {
		this.isAllowFindViewContinous = isAllowFindViewContinous;
	}

	public Fragment getFragmentHome(FragmentActivity mContext){
		Fragment mFragmentHome=null;
		if(mIdFragment>0){
			mFragmentHome = mContext.getSupportFragmentManager().findFragmentById(mIdFragment);
		}
		else{
			if(!StringUtils.isEmpty(mNameFragment)){
				mFragmentHome = mContext.getSupportFragmentManager().findFragmentByTag(mNameFragment);
			}
		}
		return mFragmentHome;
	}

	public void notifyData(){

	}

	public void startLoadData(){

	}

	public void onNetworkChange(boolean isNetworkOn){

	}

	public boolean isLoadingData() {
		return isLoadingData;
	}

	public void setLoadingData(boolean loadingData) {
		isLoadingData = loadingData;
	}
	public boolean isFirstInTab() {
		return isFirstInTab;
	}

	public void setFirstInTab(boolean firstInTab) {
		isFirstInTab = firstInTab;
	}

	public void hideBannerAds() {
		if (mLayoutAds != null) {
			mLayoutAds.setVisibility(View.GONE);
		}
	}

	public void setUpLayoutAdmob(){

	}
	public void setUpLayoutAdmob(final Context mContext, AdSize adSize,String id) {
		if (mRootView != null) {
			mLayoutAds = (RelativeLayout) mRootView.findViewById(R.id.layout_ads);
			if (ApplicationUtils.isOnline(mContext) && mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
				AdView adView = new AdView(mContext);
				adView.setAdUnitId(id);
				adView.setAdSize(adSize);
				mLayoutAds.addView(adView);

				AdRequest mAdRequest = new AdRequest.Builder().addTestDevice(TEST_DEVICE).build();
				adView.setAdListener(new com.google.android.gms.ads.AdListener() {
					@Override
					public void onAdLoaded() {
						super.onAdLoaded();
						showBannerAds();

					}
				});
				adView.loadAd(mAdRequest);
				hideBannerAds();
				return;

			}
			hideBannerAds();
		}
	}

	public void showBannerAds() {
		if (mLayoutAds != null) {
			mLayoutAds.setVisibility(View.VISIBLE);
		}
	}

	public boolean isCheckBack(){
		return false;
	}
	public void justNotifyData(){

	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mBinder!=null){
			mBinder.unbind();
		}
	}
}
