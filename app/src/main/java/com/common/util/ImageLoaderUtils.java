package com.common.util;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class ImageLoaderUtils {

    /**
     * display local image
     * @param uri
     * @param imageView
     * @param options
     */
    public static void displayLocalImage(String uri, ImageView imageView, DisplayImageOptions options) {
        ImageLoader.getInstance().displayImage("file://" + uri, new ImageViewAware(imageView), options, null, null);
    }

    /**
     * display Drawable image
     * @param uri
     * @param imageView
     * @param options
     */
    public static void displayDrawableImage(String uri, ImageView imageView, DisplayImageOptions options) {
        ImageLoader.getInstance().displayImage("drawable://" + uri, new ImageViewAware(imageView), options, null, null);
    }



}
