/*
 * Copyright (c) 2017. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.musichero.xmusic.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;


import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * @author:YPY Global
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public class GlideImageLoader {

    public static final int DEFAULT_ANIM_TIME=200;
    public static final String  HTTP_PREFIX="http";
    public static final String PREFIX_ASSETS = "assets://";
    public static final String PREFIX_NEW_ASSETS = "file:///android_asset/";

    public static void displayImage(Context mContext, ImageView mImageView, String artwork, int resId){
        displayImage(mContext,mImageView,artwork,null,resId);
    }
    public static void displayImage(Context mContext, ImageView mImageView, String artwork,
                                    Transformation<Bitmap> mTransform, int resId){
        if(!TextUtils.isEmpty(artwork)){
            if (artwork.startsWith(PREFIX_ASSETS)) {
                artwork = artwork.replace(PREFIX_ASSETS, PREFIX_NEW_ASSETS);
            }
            Uri mUri;
            if(artwork.startsWith(HTTP_PREFIX)){
                mUri=Uri.parse(artwork);
            }
            else{
                File mFile = new File(artwork);
                if(mFile.exists() && mFile.isFile()){
                    mUri=Uri.fromFile(mFile);
                }
                else{
                    mUri=Uri.parse(artwork);
                }
            }
            if(mUri!=null){
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(resId)
                        .priority(Priority.HIGH);
                if(mTransform!=null){
                    options.transform(mTransform);
                }
                Glide.with(mContext).load(mUri).apply(options).transition(withCrossFade(DEFAULT_ANIM_TIME)).into(mImageView);
            }

        }
    }
    public static void displayImage(Context mContext, ImageView mImageView, int resId,
                                    Transformation<Bitmap> mTransform){
        if(resId!=0){
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .priority(Priority.HIGH);
            if(mTransform!=null){
                options.transform(mTransform);
            }
            Glide.with(mContext).load(resId).apply(options).transition(withCrossFade(DEFAULT_ANIM_TIME)).into(mImageView);
        }
    }
    public static void displayImage(Context mContext, ImageView mImageView, int resId){
        displayImage(mContext,mImageView,resId,null);
    }

    public static boolean displayImageFromMediaStore(Context mContext, ImageView mImageView, final Uri imgUrl,int resId) {
        return displayImageFromMediaStore(mContext,mImageView,imgUrl,null,resId);

    }
    public static boolean displayImageFromMediaStore(Context mContext, ImageView mImageView, final Uri imgUrl, Transformation<Bitmap> mTransformation, int resId) {
        MediaMetadataRetriever mmr=null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(mContext, imgUrl);
            byte[] rawArt = mmr.getEmbeddedPicture();
            if (rawArt != null && rawArt.length > 0) {
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(resId)
                        .priority(Priority.HIGH);
                if(mTransformation!=null){
                    options.transform(mTransformation);
                }
                Glide.with(mContext)
                        .load(rawArt).apply(options).transition(withCrossFade(DEFAULT_ANIM_TIME)).into(mImageView);
                return true;
            }
            mImageView.setImageResource(resId);
        }
        catch (Exception e) {
            mImageView.setImageResource(resId);
            e.printStackTrace();
        }
        finally {
            try{
                if(mmr!=null){
                    mmr.release();
                    mmr=null;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
        return false;

    }
}
