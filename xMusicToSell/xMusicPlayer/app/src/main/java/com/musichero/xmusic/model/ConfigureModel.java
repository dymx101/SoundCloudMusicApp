package com.musichero.xmusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: newmusicapp_codecayon
 * Created by dotrungbao on 8/14/17.
 */

public class ConfigureModel {

    @SerializedName("bg")
    private String bg;

    @SerializedName("ui_top_chart")
    private int typeTopChart;

    @SerializedName("ui_genre")
    private int typeGenre;

    @SerializedName("ui_my_music")
    private int typeMyMusic;

    @SerializedName("top_chart_genre")
    private String topChartGenre;

    @SerializedName("top_chart_kind")
    private String topChartKind;

    @SerializedName("ui_playlist")
    private int typePlaylist;

    @SerializedName("ui_detail")
    private int typeDetail;

    @SerializedName("ui_search")
    private int typeSearch;

    @SerializedName("filter")
    private ArrayList<String> filters;


    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public int getTypeTopChart() {
        return typeTopChart;
    }

    public void setTypeTopChart(int typeTopChart) {
        this.typeTopChart = typeTopChart;
    }

    public int getTypeGenre() {
        return typeGenre;
    }

    public void setTypeGenre(int typeGenre) {
        this.typeGenre = typeGenre;
    }

    public int getTypeMyMusic() {
        return typeMyMusic;
    }

    public void setTypeMyMusic(int typeMyMusic) {
        this.typeMyMusic = typeMyMusic;
    }

    public String getTopChartGenre() {
        return topChartGenre;
    }

    public void setTopChartGenre(String topChartGenre) {
        this.topChartGenre = topChartGenre;
    }

    public String getTopChartKind() {
        return topChartKind;
    }

    public void setTopChartKind(String topChartKind) {
        this.topChartKind = topChartKind;
    }

    public int getTypePlaylist() {
        return typePlaylist;
    }

    public void setTypePlaylist(int typePlaylist) {
        this.typePlaylist = typePlaylist;
    }

    public int getTypeDetail() {
        return typeDetail;
    }

    public void setTypeDetail(int typeDetail) {
        this.typeDetail = typeDetail;
    }

    public int getTypeSearch() {
        return typeSearch;
    }

    public void setTypeSearch(int typeSearch) {
        this.typeSearch = typeSearch;
    }

    public ArrayList<String> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<String> filters) {
        this.filters = filters;
    }
}
