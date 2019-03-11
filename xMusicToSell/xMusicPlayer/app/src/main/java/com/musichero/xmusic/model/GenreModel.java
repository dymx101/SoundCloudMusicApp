package com.musichero.xmusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GenreModel {

	@SerializedName("id")
	private String id;

	@SerializedName("name")
	private String name;

	@SerializedName("keyword")
	private String keyword;

	@SerializedName("img")
	private String img;

	private transient ArrayList<TrackModel> listTrackObjects;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<TrackModel> getListTrackObjects() {
		return listTrackObjects;
	}

	public void setListTrackObjects(ArrayList<TrackModel> listTrackObjects) {
		this.listTrackObjects = listTrackObjects;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
