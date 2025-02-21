package com.yuyh.library.imgsel.bean;

import com.yuyh.library.imgsel.utils.LogUtils;

import java.io.Serializable;

/**
 * Image bean
 * Created by Yancy on 2015/12/2.
 */
public class Image implements Serializable {

    public String path;
    public String name;

    public Image(String path, String name) {
        this.path = path;
        this.name = name;
        LogUtils.d("yuyh", "Image: ");
    }

    public Image() {

    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}