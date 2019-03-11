package com.musichero.xmusic.dataMng;

import android.text.TextUtils;
import android.util.JsonReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.musichero.xmusic.constants.IXMusicConstants;
import com.musichero.xmusic.model.ConfigureModel;
import com.musichero.xmusic.model.GenreModel;
import com.musichero.xmusic.model.PlaylistModel;
import com.musichero.xmusic.model.ResultCollectionModel;
import com.musichero.xmusic.model.TrackModel;
import com.musichero.xmusic.utils.DBLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.musichero.com
 * @Project:MusicPlayer
 */

public class JsonParsingUtils implements IXMusicConstants {

    public static final String TAG = JsonParsingUtils.class.getSimpleName();

    public static ArrayList<GenreModel> parsingGenreObject(String data) {
        if (!TextUtils.isEmpty(data)) {
            try {
                Gson mGson = new GsonBuilder().create();
                Type listType = new TypeToken<ArrayList<GenreModel>>(){}.getType();
                ArrayList<GenreModel> listDatas = mGson.fromJson(data,listType);
                return listDatas;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static ConfigureModel parsingConfigureModel(String data) {
        if (!TextUtils.isEmpty(data)) {
            try {
                Gson mGson = new GsonBuilder().create();
                ConfigureModel configureModel = mGson.fromJson(data,ConfigureModel.class);
                return configureModel;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static ArrayList<PlaylistModel> parsingPlaylistObject(String mReader) {
        if (mReader!=null) {
            try {
                Gson mGson = new GsonBuilder().create();
                Type listType = new TypeToken<ArrayList<PlaylistModel>>(){}.getType();
                ArrayList<PlaylistModel> listPlaylist = mGson.fromJson(mReader,listType);
                if(listPlaylist!=null && listPlaylist.size()>0){
                    for(PlaylistModel mPlaylistObject:listPlaylist){
                        ArrayList<Long> mListIds = mPlaylistObject.getListTrackIds();
                        if(mListIds==null){
                            mPlaylistObject.setListTrackIds(new ArrayList<Long>());
                        }
                    }
                }
                return listPlaylist;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<TrackModel> parsingListTrackObjects(InputStream in) {
        if (in == null) {
            new Exception(TAG + " data can not null").printStackTrace();
            return null;
        }
        try {
            Gson mGson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<TrackModel>>(){}.getType();
            ArrayList<TrackModel> listVideo = mGson.fromJson(new InputStreamReader(in),listType);
            if(listVideo!=null && listVideo.size()>0){
                ConfigureModel configureModel=TotalDataManager.getInstance().getConfigureModel();
                ArrayList<String> mListFilters= configureModel!=null?configureModel.getFilters():null;
                if(mListFilters!=null && mListFilters.size()>0){
                    Iterator mIterator = listVideo.iterator();
                    while (mIterator.hasNext()){
                        TrackModel model= (TrackModel) mIterator.next();
                        String name=model.getTitle()!=null?model.getTitle().toLowerCase():null;
                        String author=model.getAuthor()!=null?model.getAuthor().toLowerCase():null;
                        for(String mStr:mListFilters){
                            if(name!=null && name.contains(mStr)){
                                mIterator.remove();
                                break;
                            }
                            if(author!=null && author.contains(mStr)){
                                mIterator.remove();
                                break;
                            }
                        }
                    }
                }

            }
            return listVideo;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static ArrayList<PlaylistModel> parsingListTopMusicObject(InputStream in) {
        if (in == null) {
            new Exception(TAG + " data can not null").printStackTrace();
            return null;
        }
        else {
            try {
                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                ArrayList<PlaylistModel> listTopObjects = new ArrayList<>();
                reader.beginObject();
                while (reader.hasNext()) {
                    String nameTag = reader.nextName();
                    if (nameTag.equals("feed")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            nameTag = reader.nextName();
                            if (nameTag.equals("entry")) {
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginObject();
                                    PlaylistModel mTrackObject = parsingTopMusicObject(reader);
                                    if (mTrackObject != null) {
                                        listTopObjects.add(mTrackObject);
                                    }
                                    reader.endObject();
                                }
                                reader.endArray();
                            }
                            else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    else {
                        reader.skipValue();
                    }

                }
                reader.endObject();
                reader.close();

                DBLog.d(TAG, "================>listTopObjects size=" + listTopObjects.size());

                return listTopObjects;

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private static PlaylistModel parsingTopMusicObject(JsonReader reader) {
        if (reader != null) {
            try {
                PlaylistModel mTopMusicObject = null;
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("im:name")) {
                        mTopMusicObject = new PlaylistModel();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String nameTagName = reader.nextName();
                            if (nameTagName.equals("label")) {
                                mTopMusicObject.setName(reader.nextString());
                            }
                            else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    else if (name.equals("im:image")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String nameTagImg = reader.nextName();
                                if (nameTagImg.equals("label")) {
                                    mTopMusicObject.setArtwork(reader.nextString());
                                }
                                else {
                                    reader.skipValue();
                                }
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                    }
                    else if (name.equals("im:artist")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String nameArtist = reader.nextName();
                            if (nameArtist.equals("label")) {
                                mTopMusicObject.setArtist(reader.nextString());
                            }
                            else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    else {
                        reader.skipValue();
                    }
                }
                return mTopMusicObject;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<TrackModel> parsingListHotTrackObjects(InputStream in) {
        if (in == null) {
            new Exception(TAG + " data can not null").printStackTrace();
            return null;
        }
        try {
            Gson mGson = new GsonBuilder().create();
            ResultCollectionModel mResultCollections = mGson.fromJson(new InputStreamReader(in), ResultCollectionModel.class);
            if(mResultCollections!=null){
                ArrayList<TrackModel> mTrackObjects = mResultCollections.getListTrackObjects();
                DBLog.d(TAG,"=========>parsingListHotTrackObjects="+(mTrackObjects!=null?mTrackObjects.size():0));
                return mTrackObjects;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


}
