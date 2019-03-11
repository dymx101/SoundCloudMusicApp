package com.mihwapp.crazymusic.dataMng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mihwapp.crazymusic.R;
import com.mihwapp.crazymusic.YPYFragmentActivity;
import com.mihwapp.crazymusic.constants.IXMusicConstants;
import com.mihwapp.crazymusic.executor.DBExecutorSupplier;
import com.mihwapp.crazymusic.model.ConfigureModel;
import com.mihwapp.crazymusic.model.GenreModel;
import com.mihwapp.crazymusic.model.PlaylistModel;
import com.mihwapp.crazymusic.model.TrackModel;
import com.mihwapp.crazymusic.model.UserModel;
import com.mihwapp.crazymusic.setting.IYPYSettingConstants;
import com.mihwapp.crazymusic.setting.YPYSettingManager;
import com.mihwapp.crazymusic.task.IYPYCallback;
import com.mihwapp.crazymusic.utils.ApplicationUtils;
import com.mihwapp.crazymusic.utils.DBLog;
import com.mihwapp.crazymusic.utils.IOUtils;
import com.mihwapp.crazymusic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class TotalDataManager implements IXMusicConstants, IYPYSettingConstants {

    public static final String TAG = TotalDataManager.class.getSimpleName();

    private static TotalDataManager totalDataManager;
    private ArrayList<GenreModel> listGenreObjects;

    private ArrayList<TrackModel> listSavedTrackObjects;

    private ArrayList<PlaylistModel> listPlaylistObjects;

    private PlaylistModel playlistObject;
    private GenreModel genreObject;
    private ArrayList<TrackModel> listLibraryTrackObjects;
    private ConfigureModel configureModel;

    public static TotalDataManager getInstance() {
        if (totalDataManager == null) {
            totalDataManager = new TotalDataManager();
        }
        return totalDataManager;
    }

    private TotalDataManager() {

    }


    public void onDestroy() {
        if (listGenreObjects != null) {
            listGenreObjects.clear();
            listGenreObjects = null;
        }
        if (listPlaylistObjects != null) {
            listPlaylistObjects.clear();
            listPlaylistObjects = null;
        }
        if (listLibraryTrackObjects != null) {
            listLibraryTrackObjects.clear();
            listLibraryTrackObjects = null;
        }
        totalDataManager = null;
    }

    public ArrayList<GenreModel> getListGenreObjects() {
        return listGenreObjects;
    }


    public void setListGenreObjects(ArrayList<GenreModel> listGenreObjects) {
        this.listGenreObjects = listGenreObjects;
    }


    public ArrayList<PlaylistModel> getListPlaylistObjects() {
        return listPlaylistObjects;
    }

    public void setListPlaylistObjects(ArrayList<PlaylistModel> listPlaylistObjects) {
        this.listPlaylistObjects = listPlaylistObjects;
    }

    public void addPlaylistObject(PlaylistModel mPlaylistObject) {
        if (listPlaylistObjects != null && mPlaylistObject != null) {
            synchronized (listPlaylistObjects) {
                listPlaylistObjects.add(mPlaylistObject);
            }
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                }
            });
        }
    }

    public boolean isPlaylistNameExisted(String name) {
        if (!StringUtils.isEmpty(name)) {
            if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
                for (PlaylistModel mPlaylistObject : listPlaylistObjects) {
                    if (mPlaylistObject.getName().equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void editPlaylistObject(PlaylistModel mPlaylistObject, String newName) {
        if (listPlaylistObjects != null && mPlaylistObject != null && !StringUtils.isEmpty(newName)) {
            mPlaylistObject.setName(newName);
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                }
            });
        }
    }

    public void removePlaylistObject(PlaylistModel mPlaylistObject) {
        if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
            listPlaylistObjects.remove(mPlaylistObject);
            ArrayList<TrackModel> mListTrack = mPlaylistObject.getListTrackObjects();
            boolean isNeedToSaveTrack = false;
            if (mListTrack != null && mListTrack.size() > 0) {
                for (TrackModel mTrackObject : mListTrack) {
                    boolean isAllowRemoveToList = true;
                    for (PlaylistModel mPlaylist : listPlaylistObjects) {
                        if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
                            isAllowRemoveToList = false;
                            break;
                        }
                    }
                    if (isAllowRemoveToList) {
                        listSavedTrackObjects.remove(mTrackObject);
                        isNeedToSaveTrack = true;
                    }
                }
                mListTrack.clear();
            }
            final boolean isGlobal = isNeedToSaveTrack;
            DBExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    savePlaylistObjects();
                    if (isGlobal) {
                        saveDataInCached(TYPE_FILTER_SAVED);
                    }
                }
            });
        }
    }

    public synchronized void savePlaylistObjects() {
        File mFile = getDirectoryTemp();
        if (mFile != null && listPlaylistObjects != null) {
            Gson mGson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<PlaylistModel>>() {}.getType();
            String data = mGson.toJson(listPlaylistObjects, listType);
            DBLog.d(TAG, "=============>savePlaylistObjects=" + data + "==>path=" + mFile.getAbsolutePath());
            IOUtils.writeString(mFile.getAbsolutePath(), FILE_PLAYLIST, data);
        }

    }


    public void deleteSong(final TrackModel mTrackObject,
                           final IYPYCallback mCallback) {
        try {
            File mFile = null;
            String path = mTrackObject.getPath();
            if (!StringUtils.isEmpty(path)) {
                mFile = new File(path);
            }
            if (mFile != null && mFile.exists() && mFile.isFile()) {
                try {
                    boolean b = mFile.delete();
                    if (b) {
                        removeSongFromList(MusicDataMng.getInstance().getListPlayingTrackObjects(), mTrackObject.getId());
                        removeSongFromList(listLibraryTrackObjects, mTrackObject.getId());
                        if (listPlaylistObjects != null && listPlaylistObjects.size() > 0) {
                            for (PlaylistModel mPlaylistObject : listPlaylistObjects) {
                                boolean remove = removeTrackToPlaylistNoThread(mTrackObject, mPlaylistObject, null, true);
                                if (remove) {
                                    break;
                                }
                            }
                        }
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TrackModel> startSearchSong(String keyword) {
        ArrayList<TrackModel> mListTracks = listLibraryTrackObjects;
        if (!TextUtils.isEmpty(keyword)) {
            if (mListTracks != null && mListTracks.size() > 0) {
                ArrayList<TrackModel> mListTrackObjects = new ArrayList<TrackModel>();
                synchronized (mListTracks) {
                    int size = mListTracks.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            TrackModel mTrackObject = mListTracks.get(i);
                            if (mTrackObject.getTitle().toLowerCase(Locale.US).contains(keyword)) {
                                mListTrackObjects.add(mTrackObject.clone());
                            }
                        }
                    }
                }
                return mListTrackObjects;
            }
        }
        else {
            if (mListTracks != null && mListTracks.size() > 0) {
                return (ArrayList<TrackModel>) mListTracks.clone();
            }
        }
        return null;
    }



    public boolean isLibraryTracks(TrackModel mTrackObject) {
        if (listLibraryTrackObjects != null && listLibraryTrackObjects.size() > 0) {
            for (TrackModel mTrackObject1 : listLibraryTrackObjects) {
                if (mTrackObject1.getId() == mTrackObject.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeSongFromList(ArrayList<TrackModel> listTrackObjects, long id) {
        if (listTrackObjects != null && listTrackObjects.size() > 0) {
            synchronized (listTrackObjects) {
                Iterator<TrackModel> mIterator = listTrackObjects.iterator();
                while (mIterator.hasNext()) {
                    TrackModel mTrackObject = mIterator.next();
                    if (mTrackObject.getId() == id) {
                        mIterator.remove();
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void readPlaylistCached() {
        File mFile = getDirectoryTemp();
        if (mFile != null) {
            String data = IOUtils.readString(mFile.getAbsolutePath(), FILE_PLAYLIST);
            ArrayList<PlaylistModel> mListPlaylist = JsonParsingUtils.parsingPlaylistObject(data);
            if (mListPlaylist != null && mListPlaylist.size() > 0) {
                setListPlaylistObjects(mListPlaylist);
            }
            else {
                mListPlaylist = new ArrayList<>();
                setListPlaylistObjects(mListPlaylist);
            }
            if (mListPlaylist.size() > 0) {
                for (PlaylistModel mPlaylistObject : mListPlaylist) {
                    filterSongOfPlaylistId(mPlaylistObject);
                }
            }
        }
    }

    public File getDirectoryCached() {
        if (!ApplicationUtils.hasSDcard()) {
            return null;
        }
        try {
            final File mFile = new File(Environment.getExternalStorageDirectory(), DIR_CACHE);
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            return mFile;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private File getDirectoryTemp() {
        File mRoot = getDirectoryCached();
        if (mRoot != null) {
            final File mFile = new File(mRoot, DIR_TEMP);
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            return mFile;
        }
        return null;

    }



    public ArrayList<TrackModel> getListTracks(int type) {
        if (type == TYPE_FILTER_SAVED) {
            return listSavedTrackObjects;
        }
        return null;
    }

    public String getFileNameSaved(int type) {
        if (type == TYPE_FILTER_SAVED) {
            return FILE_SAVED_TRACK;
        }
        return null;
    }

    public void saveListTrack(int type, ArrayList<TrackModel> listTrack) {
        if (type == TYPE_FILTER_SAVED) {
            listSavedTrackObjects = listTrack;
        }
    }

    public void readCached(int type) {
        final ArrayList<TrackModel> mListTrackObject = getListTracks(type);
        if (mListTrackObject != null && mListTrackObject.size() > 0) {
            return;
        }
        File mFileCache = getDirectoryTemp();
        if (mFileCache != null) {
            File mFileData = new File(mFileCache, getFileNameSaved(type));
            if (mFileData.exists() && mFileData.isFile()) {
                try {
                    FileInputStream mFileInputStream = new FileInputStream(mFileData);
                    ArrayList<TrackModel> mListTracks = JsonParsingUtils.parsingListTrackObjects(mFileInputStream);
                    DBLog.d(TAG, "=========>readCached=" + (mListTracks != null ? mListTracks.size() : 0));
                    if (mListTracks != null && mListTracks.size() > 0) {
                        saveListTrack(type, mListTracks);
                        return;
                    }
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            saveListTrack(type, new ArrayList<>());
        }

    }

    private void filterSongOfPlaylistId(PlaylistModel mPlaylistObject) {
        if (listSavedTrackObjects != null && listSavedTrackObjects.size() > 0) {
            ArrayList<Long> mListId = mPlaylistObject.getListTrackIds();
            if (mListId != null && mListId.size() > 0) {
                for (Long mId : mListId) {
                    for (TrackModel mTrackObject : listSavedTrackObjects) {
                        if (mTrackObject.getId() == mId) {
                            mPlaylistObject.addTrackObject(mTrackObject, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void addTrackToPlaylist(final YPYFragmentActivity mContext, final TrackModel mParentTrackObject,
                                                final PlaylistModel mPlaylistObject, boolean isShowMsg, final IYPYCallback mCallback) {
        if (mParentTrackObject != null && mPlaylistObject != null) {
            if (!mPlaylistObject.isSongAlreadyExited(mParentTrackObject.getId())) {
                TrackModel mTrackObject = mParentTrackObject.clone();
                mPlaylistObject.addTrackObject(mTrackObject, true);
                boolean isAllowAddToList = true;
                for (TrackModel mTrackObject1 : listSavedTrackObjects) {
                    if (mTrackObject1.getId() == mTrackObject.getId()) {
                        isAllowAddToList = false;
                        break;
                    }
                }
                if (isAllowAddToList) {
                    listSavedTrackObjects.add(mTrackObject);
                }
                if (mCallback != null) {
                    mCallback.onAction();
                }
                mContext.runOnUiThread(() -> {
                    mContext.showToast(String.format(mContext.getString(R.string.info_add_playlist), mParentTrackObject.getTitle()
                            , mPlaylistObject.getName()));
                });
                DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                    savePlaylistObjects();
                    saveDataInCached(TYPE_FILTER_SAVED);
                });
            }
            else {
                if (isShowMsg) {
                    mContext.runOnUiThread(() -> mContext.showToast(R.string.info_song_already_playlist));
                }

            }
        }
    }

    public synchronized boolean removeTrackToPlaylistNoThread(TrackModel mTrackObject
            , PlaylistModel mPlaylistObject, IYPYCallback mCallback, boolean isNeedSave) {
        if (mTrackObject != null && mPlaylistObject != null) {
            mPlaylistObject.removeTrackObject(mTrackObject);
            boolean isAllowRemoveToList = true;
            for (PlaylistModel mPlaylist : listPlaylistObjects) {
                if (mPlaylist.isSongAlreadyExited(mTrackObject.getId())) {
                    isAllowRemoveToList = false;
                    break;
                }
            }
            if (mCallback != null) {
                mCallback.onAction();
            }
            DBLog.d(TAG, "============>removeTrackToPlaylist=" + isAllowRemoveToList);
            if (isAllowRemoveToList) {
                listSavedTrackObjects.remove(mTrackObject);
                if (isNeedSave) {
                    savePlaylistObjects();
                    saveDataInCached(TYPE_FILTER_SAVED);
                }
            }
            return isAllowRemoveToList;

        }
        return false;
    }

    public synchronized void removeTrackToPlaylist(final TrackModel mTrackObject
            , final PlaylistModel mPlaylistObject, final IYPYCallback mCallback) {
        DBExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            removeTrackToPlaylistNoThread(mTrackObject, mPlaylistObject, mCallback, true);
        });
    }

    public void readGenreData(Context mContext) {
        String data = IOUtils.readStringFromAssets(mContext, FILE_GENRE);
        ArrayList<GenreModel> mListGenres = JsonParsingUtils.parsingGenreObject(data);
        DBLog.d(TAG, "==========>size genres=" + (mListGenres != null ? mListGenres.size() : 0));
        if (mListGenres != null) {
            setListGenreObjects(mListGenres);
        }
    }
    public void readConfigure(Context mContext) {
        String data = IOUtils.readStringFromAssets(mContext, FILE_CONFIGURE);
        configureModel = JsonParsingUtils.parsingConfigureModel(data);
        if(configureModel!=null){
            YPYSettingManager.setBackground(mContext,configureModel.getBg());
        }
    }

    public synchronized void saveDataInCached(int type) {
        File mFile = getDirectoryTemp();
        if (mFile != null) {
            ArrayList<TrackModel> mListTracks = getListTracks(type);
            String data = "[]";
            if (mListTracks != null && mListTracks.size() > 0) {
                Gson mGson = new GsonBuilder().create();
                Type listType = new TypeToken<ArrayList<TrackModel>>() {}.getType();
                data = mGson.toJson(mListTracks, listType);
            }
            IOUtils.writeString(mFile.getAbsolutePath(), getFileNameSaved(type), data);
        }

    }

    public PlaylistModel getPlaylistObject() {
        return playlistObject;
    }

    public void setPlaylistObject(PlaylistModel playlistObject) {
        this.playlistObject = playlistObject;
    }

    public GenreModel getGenreObject() {
        return genreObject;
    }

    public void setGenreObject(GenreModel genreObject) {
        this.genreObject = genreObject;
    }


    public void readLibraryTrack(Context mContext) {
        if (listLibraryTrackObjects != null && listLibraryTrackObjects.size() > 0) {
            return;
        }
        this.listLibraryTrackObjects = getListMusicsFromLibrary(mContext);
        sortListSongs(listLibraryTrackObjects);
    }

    private ArrayList<TrackModel> getListMusicsFromLibrary(Context mContext) {
        Cursor cur=null;
        try{
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cur = mContext.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
            DBLog.d(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
            if (cur == null) {
                Log.e(TAG, "===>Failed to retrieve music: cursor is null :-(");
                return null;
            }
            if (!cur.moveToFirst()) {
                Log.e(TAG, "===>Failed to move cursor to first row (no query results).");
                return null;
            }
            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
            int dataColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dateColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            ArrayList<TrackModel> listTrackObjects = new ArrayList<TrackModel>();
            while (cur.moveToNext()){
                long id = cur.getLong(idColumn);
                String singer = cur.getString(artistColumn);
                String title = cur.getString(titleColumn);
                long duration = cur.getLong(durationColumn);
                String path = cur.getString(dataColumn);
                Date mDate = new Date(cur.getLong(dateColumn) * 1000);
                if (!StringUtils.isEmpty(path)) {
                    File mFile = new File(path);
                    if (mFile.exists() && mFile.isFile()) {
                        UserModel userObject = new UserModel(singer);
                        TrackModel mTrackObject = new TrackModel(path, title);
                        mTrackObject.setId(id);
                        mTrackObject.setUserObject(userObject);
                        mTrackObject.setDateCreated(mDate);
                        mTrackObject.setDuration(duration);
                        listTrackObjects.add(mTrackObject);
                    }
                }
            }
            return listTrackObjects;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if(cur!=null){
                    cur.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        return null;
    }

    public boolean sortListSongs(ArrayList<TrackModel> mListLibrary) {
        if (mListLibrary != null && mListLibrary.size() > 0) {
            try {
                Collections.sort(mListLibrary, (lhs, rhs) -> {
                    Date date1 = lhs.getDateCreated();
                    Date date2 = rhs.getDateCreated();
                    return date2.compareTo(date1);
                });
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public ArrayList<TrackModel> getListLibraryTrackObjects() {
        return listLibraryTrackObjects;
    }


    public ConfigureModel getConfigureModel() {
        return configureModel;
    }

    public boolean renameOfSong(Context mContext, long id, String artistName, String titleName) {
        try {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Media.TITLE, titleName);
            values.put(MediaStore.Audio.Media.ARTIST, artistName);
            int i = mContext.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    values, MediaStore.Audio.Media._ID + "=" + id, null);
            if (i > 0) {
                updateAllMusicData(id, artistName, titleName);
            }
            return i >= 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateAllMusicData(long id, String artistName, String titleName) {
        try {
            boolean b = updateNameForTrack(listSavedTrackObjects, id, artistName, titleName);
            if (b) {
                saveDataInCached(TYPE_FILTER_SAVED);
            }
            updateNameForTrack(listLibraryTrackObjects, id, artistName, titleName);
            updateNameForTrack(MusicDataMng.getInstance().getListPlayingTrackObjects(), id, artistName, titleName);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean updateNameForTrack(ArrayList<TrackModel> listTrackObjects, long id, final String artistName, final String titleName) {
        if (listTrackObjects != null && listTrackObjects.size() > 0) {
            for (TrackModel mTrackObject : listTrackObjects) {
                if (mTrackObject.getId() == id) {
                    mTrackObject.setAuthor(artistName);
                    mTrackObject.setTitle(titleName);
                    return true;
                }
            }
        }
        return false;
    }

}
