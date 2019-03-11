package com.musichero.xmusic.imageloader.target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

/**
 * @author:dotrungbao
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: MJStoreVideo
 * Created by dotrungbao on 8/1/17.
 */

public class GlideViewGroupTarget extends GlideGroupTarget<Bitmap>  {

    private Context context;

    public GlideViewGroupTarget(Context context, ViewGroup view) {
        super(view);
        this.context = context;
    }

    @Override
    protected void setResource(Bitmap resource) {
        view.setBackground(new BitmapDrawable(context.getResources(), resource));
    }
}
