package com.musichero.xmusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: PrimeMusicPlayer
 * Created by dotrungbao on 2/3/17.
 */

public class CollectionModel {

    @SerializedName("track")
    private TrackModel trackObject;

    public TrackModel getTrackObject() {
        return trackObject;
    }

    public void setTrackObject(TrackModel trackObject) {
        this.trackObject = trackObject;
    }
}
