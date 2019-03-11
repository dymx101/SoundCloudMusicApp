package com.mihwapp.crazymusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: NewMusicApp
 * Created by dotrungbao on 1/3/17.
 */

public class UserModel {

    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar_url")
    private String avatar;

    public UserModel(String username) {
        this.username = username;
    }

    public UserModel(String id, String avatar, String username) {
        this.id = id;
        this.avatar = avatar;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public UserModel cloneObject(){
        return new UserModel(id,avatar,username);
    }
}
