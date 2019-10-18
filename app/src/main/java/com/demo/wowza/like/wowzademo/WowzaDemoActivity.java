package com.demo.wowza.like.wowzademo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.graphics.WOWZColor;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;

import static android.content.ContentValues.TAG;

/**
 * @author like
 * @date :2019/10/18
 * @description:
 */
public class WowzaDemoActivity  extends AppCompatActivity{
    private WOWZCameraView mWZCameraView;
    private WOWZBroadcastConfig wowzBroadcastConfig;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wowzademo);
        mWZCameraView=findViewById(R.id.cameraPreview);
        wowzBroadcastConfig=new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1280x720);
        WowzaGoCoder mWowzaGoCoder = WowzaGoCoder.init(this, "GOSK-0B47-010C-EE83-871C-3140");
//
//        if (mWowzaGoCoder == null) {
//            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
////            mView.showToast("GoCoder SDK error: " + goCoderInitError.getErrorDescription());
//          //  postError(goCoderInitError.getException());
//            return;
//        }
        boolean isinit = WowzaGoCoder.isInitialized();
        Log.e(TAG, "init: is =" + isinit);
        Log.i("info", "onCreate: "+mWZCameraView);

            if (mWZCameraView != null) {
                WOWZCamera availableCameras[] = mWZCameraView.getCameras();
                // Ensure we can access to at least one camera
                    Log.i(TAG, "onCreate: 相机数"+availableCameras.length);
                if (availableCameras.length > 0) {
                    // Set the video broadcaster in the broadcast config
                    wowzBroadcastConfig.setVideoBroadcaster(mWZCameraView);

                }

        }


        //Broadcast
        WOWZDataMap streamMetadata = new WOWZDataMap();
        streamMetadata.put("androidRelease", Build.VERSION.RELEASE);
        streamMetadata.put("androidSDK", Build.VERSION.SDK_INT);
        streamMetadata.put("deviceProductName", Build.PRODUCT);
        streamMetadata.put("deviceManufacturer", Build.MANUFACTURER);
        streamMetadata.put("deviceModel", Build.MODEL);
        wowzBroadcastConfig.setStreamMetadata(streamMetadata);
        //
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            // Add query string parameters describing the current app
            WOWZDataMap connectionParameters = new WOWZDataMap();
            connectionParameters.put("appPackageName", pInfo.packageName);
            connectionParameters.put("appVersionName", pInfo.versionName);
            connectionParameters.put("appVersionCode", pInfo.versionCode);

            wowzBroadcastConfig.setConnectionParameters(connectionParameters);

        } catch (PackageManager.NameNotFoundException e) {
        }

        WOWZMediaConfig mediaConfig = wowzBroadcastConfig.getVideoSourceConfig();
        mediaConfig.setVideoFramerate(wowzBroadcastConfig.getVideoFramerate());
        mediaConfig.setVideoFrameHeight(wowzBroadcastConfig.getVideoFrameHeight());
        mediaConfig.setVideoFrameWidth(wowzBroadcastConfig.getVideoFrameWidth());
        wowzBroadcastConfig.setVideoSourceConfig(mediaConfig);
        //

        mWZCameraView.setCameraConfig(wowzBroadcastConfig);
        //mWZCameraView.setScaleMode(GoCoderSDKPrefs.getScaleMode(sharedPrefs));
       // mWZCameraView.setVideoBackgroundColor(WOWZColor.DARKGREY);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (this.hasDevicePermissionToAccess() && sGoCoderSDK != null && mWZCameraView != null) {
//            if (mAutoFocusDetector == null)
//                mAutoFocusDetector = new GestureDetectorCompat(this, new AutoFocusListener(this, mWZCameraView));

//            WOWZCamera activeCamera = mWZCameraView.getCamera();
//            if (activeCamera != null && activeCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
//                activeCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
            mWZCameraView.startPreview();
        }
   // }
}
