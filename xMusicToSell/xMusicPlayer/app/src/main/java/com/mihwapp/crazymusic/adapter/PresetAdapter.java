
package com.mihwapp.crazymusic.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mihwapp.crazymusic.R;


public class PresetAdapter extends ArrayAdapter<String>{

	private LayoutInflater mInflater;
	private Context mContext;
	private String[] mListString;
	private IPresetListener presetListener;


	public PresetAdapter(Context context, int resource, String[] objects) {
		super(context, resource, objects);
		this.mContext= context;
		this.mListString = objects;
		this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_preset_name, null);
			convertView.setTag(mHolder);

			mHolder.mTvName =convertView.findViewById(R.id.tv_name);
		}
		else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.mTvName.setText(mListString[position]);
		return convertView;
	}

	@Override
	public View getDropDownView(final int position, View convertView, ViewGroup parent) {
		ViewDropHolder mViewDropHolder;
		if(convertView==null){
			mViewDropHolder = new ViewDropHolder();
			convertView = mInflater.inflate(R.layout.item_preset_name, null);
			convertView.setTag(mViewDropHolder);
			mViewDropHolder.mTvName=convertView.findViewById(R.id.tv_name);

		}
		else{
			mViewDropHolder= (ViewDropHolder) convertView.getTag();
		}
		mViewDropHolder.mTvName.setText(mListString[position]);
		mViewDropHolder.mTvName.setOnClickListener(v -> {
			View root = v.getRootView();
			root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
			root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			if(presetListener!=null){
				presetListener.onSelectItem(position);
			}
		});
		return convertView;
	}

	private static class ViewDropHolder {
		public TextView mTvName;
	}
	

	private static class ViewHolder {
		public TextView mTvName;
	}

	public interface IPresetListener{
		public void onSelectItem(int position);
	}

	public void setPresetListener(IPresetListener presetListener) {
		this.presetListener = presetListener;
	}
	
	
}
