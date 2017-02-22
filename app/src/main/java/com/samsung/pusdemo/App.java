package com.samsung.pusdemo;

import android.graphics.drawable.Drawable;

/**
 * Created by samsung on 2017/2/22.
 */

public class App {
    private String info;
    private Drawable icon;

    public App(String info, Drawable icon) {
        this.info = info;
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
