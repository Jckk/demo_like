package com.demo.wowza.like.wowzademo;

import android.app.Application;
import android.util.Log;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;

/**
 * @author like
 * @date :2019/10/18
 * @description:
 */
public class WowzaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isinit = WowzaGoCoder.isInitialized();
      //  Log.i("info", "onCreate: 初始化"+isinit);

    }
}
