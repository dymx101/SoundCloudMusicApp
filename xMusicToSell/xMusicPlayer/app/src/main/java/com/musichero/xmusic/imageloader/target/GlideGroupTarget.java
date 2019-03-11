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

package com.musichero.xmusic.imageloader.target;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * @author:YPY Global
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public abstract class GlideGroupTarget<Z> extends ViewTarget<ViewGroup,Z> implements  Transition.ViewAdapter {

    public GlideGroupTarget(ViewGroup view) {
        super(view);
    }

    /**
     * Returns the current {@link Drawable} being displayed in the view using
     * {@link android.widget.ImageView#getDrawable()}.
     */
    @Override
    public Drawable getCurrentDrawable() {
        return view.getBackground();
    }

    /**
     * Sets the given {@link Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(Drawable)}.
     *
     * @param drawable {@inheritDoc}
     */
    @Override
    public void setDrawable(Drawable drawable) {
        view.setBackground(drawable);
    }


    /**
     * Sets the given {@link Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(Drawable)}.
     *
     * @param placeholder {@inheritDoc}
     */
    @Override
    public void onLoadStarted(Drawable placeholder) {
        super.onLoadStarted(placeholder);
        view.setBackground(placeholder);
    }


    /**
     * Sets the given {@link Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(Drawable)}.
     *
     * @param errorDrawable {@inheritDoc}
     */
    @Override
    public void onLoadFailed(Drawable errorDrawable) {
        view.setBackground(errorDrawable);
    }

    /**
     * Sets the given {@link Drawable} on the view using
     * {@link android.widget.ImageView#setImageDrawable(Drawable)}.
     *
     * @param placeholder {@inheritDoc}
     */
    @Override
    public void onLoadCleared(Drawable placeholder) {
        super.onLoadCleared(placeholder);
        view.setBackground(placeholder);
    }

    @Override
    public void onResourceReady(@NonNull Z resource, @Nullable Transition<? super Z> transition) {
        this.setResource(resource);
    }

    protected abstract void setResource(Z resource);
}
