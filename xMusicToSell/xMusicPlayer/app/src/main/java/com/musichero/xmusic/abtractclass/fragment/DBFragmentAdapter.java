package com.musichero.xmusic.abtractclass.fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.util.ArrayList;

/**
 * DesignSeriesFragmentAdapter
 * 
 * @author DOBAO
 * @Email dotrungbao@gmail.com
 * @Skype baopfiev_k50
 * @Date Sep 19, 2013
 * @Packagename com.greenandcompanycreative.adapter
 */
public class DBFragmentAdapter extends FragmentPagerAdapter {

	public static final String TAG = DBFragmentAdapter.class.getSimpleName();

	private ArrayList<Fragment> listFragments;

	public DBFragmentAdapter(FragmentManager fm, ArrayList<Fragment> listFragments) {
		super(fm);
		this.listFragments = listFragments;
	}

	@Override
	public Fragment getItem(int position) {
		return listFragments.get(position);
	}

	@Override
	public int getCount() {
		return listFragments.size();
	}

	@Override
	public void destroyItem(View pView, int pIndex, Object pObject) {
		try {
			((ViewPager) pView).removeView((View) pObject);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
