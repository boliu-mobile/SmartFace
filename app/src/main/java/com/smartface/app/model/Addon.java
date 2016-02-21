package com.smartface.app.model;


/**
 * 本地简单使用,实际项目中与贴纸相关的属性可以添加到此类中
 */
public class Addon  {
    private int    id;

    //JSON用到
    public Addon() {

    }

    public Addon(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
