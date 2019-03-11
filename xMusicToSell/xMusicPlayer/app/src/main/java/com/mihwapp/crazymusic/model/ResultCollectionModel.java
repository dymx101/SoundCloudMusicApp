package com.mihwapp.crazymusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: PrimeMusicPlayer
 * Created by dotrungbao on 2/3/17.
 */

public class ResultCollectionModel {

    @SerializedName("collection")
    private ArrayList<CollectionModel> listCollectionObjects;

    private transient ArrayList<TrackModel> listTrackObjects;

    public ArrayList<CollectionModel> getListCollectionObjects() {
        return listCollectionObjects;
    }

    public void setListCollectionObjects(ArrayList<CollectionModel> listCollectionObjects) {
        this.listCollectionObjects = listCollectionObjects;
    }

    public ArrayList<TrackModel> getListTrackObjects(){
        if(listCollectionObjects!=null && listCollectionObjects.size()>0
                && (listTrackObjects==null || listTrackObjects.size()==0)){
            listTrackObjects = new ArrayList<>();
            for(CollectionModel mCollectionObject:listCollectionObjects){
                if(mCollectionObject.getTrackObject()!=null){
                    listTrackObjects.add(mCollectionObject.getTrackObject());
                }
            }
        }
        return listTrackObjects;
    }
}
