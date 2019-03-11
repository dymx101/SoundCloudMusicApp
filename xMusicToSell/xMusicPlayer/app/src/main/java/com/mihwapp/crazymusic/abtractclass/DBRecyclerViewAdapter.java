package com.mihwapp.crazymusic.abtractclass;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: melocloud
 * Created by dotrungbao on 4/27/17.
 */

public abstract class DBRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG =DBRecyclerViewAdapter.class.getSimpleName();

    public static final int TYPE_HEADER_VIEW =-1;
    private View mHeaderView;

    public Context mContext;
    public ArrayList<? extends Object> mListObjects;
    public boolean isHasHeader;

    public DBRecyclerViewAdapter(Context mContext, ArrayList<? extends Object> listObjects) {
        this.mContext = mContext;
        this.mListObjects = listObjects;
    }
    public DBRecyclerViewAdapter(Context mContext, ArrayList<? extends Object> listObjects, View mHeaderView) {
        this.mContext = mContext;
        this.mListObjects = listObjects;
        this.isHasHeader=true;
        this.mHeaderView=mHeaderView;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(isHasHeader){
            int viewType = getItemViewType(position);
            if (viewType != TYPE_HEADER_VIEW) {
                onBindNormalViewHolder(holder,position-1);
            }
        }
        else{
            onBindNormalViewHolder(holder,position);
        }

    }

    public abstract void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position);
    public abstract RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType);


    @Override
    public int getItemCount() {
        int size = mListObjects!=null?mListObjects.size():0;
        if(isHasHeader){
            return size+1;
        }
        else{
            return size;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isHasHeader && position==0){
            return TYPE_HEADER_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup v, int viewType) {
        RecyclerView.ViewHolder mHolder;
        if (viewType == TYPE_HEADER_VIEW) {
            mHolder= new ViewHeaderHolder(mHeaderView);
        }
        else{
            mHolder=onCreateNormalViewHolder(v,viewType);
        }
        return mHolder;
    }


    public class ViewHeaderHolder extends RecyclerView.ViewHolder {
        public ViewHeaderHolder(View convertView) {
            super(convertView);
        }
    }

    public void setListObjects(ArrayList<? extends Object> mListObjects, boolean isDestroyOldData) {
        if (mListObjects != null) {
            if (this.mListObjects != null && isDestroyOldData) {
                this.mListObjects.clear();
                this.mListObjects = null;
            }
            this.mListObjects = mListObjects;
            this.notifyDataSetChanged();
        }
    }

    public ArrayList<? extends Object> getListObjects() {
        return mListObjects;
    }
}
