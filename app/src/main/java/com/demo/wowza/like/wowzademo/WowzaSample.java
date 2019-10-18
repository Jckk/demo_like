package com.demo.wowza.like.wowzademo;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.graphics.WOWZColor;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.util.Arrays;

public class WowzaSample extends AppCompatActivity implements WOWZCameraView.PreviewStatusListener, WOWZStatusCallback {
    protected WOWZCameraView mWZCameraView = null;
    protected WOWZAudioDevice mWZAudioDevice = null;
    protected static WowzaGoCoder sGoCoderSDK = null;
    protected WOWZBroadcastConfig mWZBroadcastConfig = null;
    protected WOWZBroadcast mWZBroadcast = null;
    protected String[] mRequiredPermissions = {};
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean hasRequestedPermissions = false;
    private boolean mDevicesInitialized = false;
    private boolean mUIInitialized = false;
    protected boolean mPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wowza_sample);
        mWZCameraView = findViewById(R.id.cameraPreview);

        //init
        if (sGoCoderSDK == null) {
            // Enable detailed logging from the GoCoder SDK
            WOWZLog.LOGGING_ENABLED = true;

            // Initialize the GoCoder SDK
            //  sGoCoderSDK = WowzaGoCoder.init(this, "GOSK-A144-010C-9E08-5FE6-6AA7");//demo的
            sGoCoderSDK = WowzaGoCoder.init(this, "GOSK-0B47-010C-EE83-871C-3140");

            if (sGoCoderSDK == null) {
            }
        }

        boolean inits=WowzaGoCoder.isInitialized();
        Log.i("info", "onCreate: "+inits);

        if (sGoCoderSDK != null) {
            // Create a new instance of the preferences mgr

            // Create an instance for the broadcast configuration
            mWZBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1280x720);

            // Create a broadcaster instance
            mWZBroadcast = new WOWZBroadcast();
            mWZBroadcast.setLogLevel(WOWZLog.LOG_LEVEL_DEBUG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initGoCoderDevices();
        this.hasDevicePermissionToAccess(new PermissionCallbackInterface() {

            @Override
            public void onPermissionResult(boolean result) {
                if (!mDevicesInitialized || result) {
                    initGoCoderDevices();
                }
            }
        });

        if (sGoCoderSDK != null && this.hasDevicePermissionToAccess(Manifest.permission.CAMERA)) {
            final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            /// Set mirror capability.
            //mWZCameraView.setSurfaceExtension(mWZCameraView.EXTENSION_MIRROR);

            // Update the camera preview display config based on the stored shared preferences

            mWZCameraView.setCameraConfig(getBroadcastConfig());
            mWZCameraView.setScaleMode(WOWZMediaConfig.FILL_VIEW);//这里可以改
            mWZCameraView.setVideoBackgroundColor(WOWZColor.DARKGREY);

            final WowzaSample ref = this;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mWZBroadcastConfig.isVideoEnabled() && !mWZCameraView.isPreviewing()) {
                        mWZCameraView.startPreview(getBroadcastConfig(), ref);

                    } else {
                        mWZCameraView.stopPreview();
                    }
                }
            }, 300);
        }

        mPermissionsGranted = this.hasDevicePermissionToAccess();
        if (mPermissionsGranted) {
            //syncPreferences();
        }

        mWZCameraView.startPreview();
    }

    protected void initGoCoderDevices() {
        if (sGoCoderSDK != null) {
            boolean videoIsInitialized = false;
            boolean audioIsInitialized = false;

            // Initialize the camera preview
            if (this.hasDevicePermissionToAccess(Manifest.permission.CAMERA)) {
                if (mWZCameraView != null) {
                    WOWZCamera availableCameras[] = mWZCameraView.getCameras();
                    // Ensure we can access to at least one camera
                    if (availableCameras.length > 0) {
                        // Set the video broadcaster in the broadcast config
                        mWZBroadcastConfig.setVideoBroadcaster(mWZCameraView);
                        videoIsInitialized = true;
                    }
                }
            }

            if (this.hasDevicePermissionToAccess(Manifest.permission.RECORD_AUDIO)) {
                // Initialize the audio input device interface
                mWZAudioDevice = new WOWZAudioDevice();

                // Set the audio broadcaster in the broadcast config
                getBroadcastConfig().setAudioBroadcaster(mWZAudioDevice);
                audioIsInitialized = true;
            }
        }
    }

    public WOWZBroadcast getBroadcast() {
        return mWZBroadcast;
    }

    public WOWZBroadcastConfig getBroadcastConfig() {
        return mWZBroadcastConfig;
    }

    protected boolean hasDevicePermissionToAccess(String source) {

        String[] permissionRequestArr = new String[]{
                source
        };
        boolean result = false;
        if (mWZBroadcast != null) {
            result = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = (mRequiredPermissions.length > 0 ? WowzaGoCoder.hasPermissions(this, permissionRequestArr) : true);
            }
        }
        return result;
    }

    private PermissionCallbackInterface callbackFunction = null;

    protected void hasDevicePermissionToAccess(PermissionCallbackInterface callback) {
        this.callbackFunction = callback;
        if (mWZBroadcast != null) {
            boolean result = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = (mRequiredPermissions.length > 0 ? WowzaGoCoder.hasPermissions(this, mRequiredPermissions) : true);
                if (!result && !hasRequestedPermissions) {
                    ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
                    hasRequestedPermissions = true;
                } else {
                    this.callbackFunction.onPermissionResult(result);
                }
            } else {
                this.callbackFunction.onPermissionResult(result);
            }
        }
    }

    protected boolean hasDevicePermissionToAccess() {
        boolean result = false;
        if (mWZBroadcast != null) {
            result = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = (mRequiredPermissions.length > 0 ? WowzaGoCoder.hasPermissions(this, mRequiredPermissions) : true);
                if (!result && !hasRequestedPermissions) {
                    ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
                    hasRequestedPermissions = true;
                }
            }
        }
        return result;
    }

    @Override
    public void onWZCameraPreviewStarted(WOWZCamera wowzCamera, WOWZSize wowzSize, int i) {

    }

    @Override
    public void onWZCameraPreviewStopped(int i) {

    }

    @Override
    public void onWZCameraPreviewError(WOWZCamera wowzCamera, WOWZError wowzError) {

    }

    @Override
    public void onWZStatus(WOWZStatus wowzStatus) {

    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {

    }

    //define callback interface
    interface PermissionCallbackInterface {

        void onPermissionResult(boolean result);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
        if (this.callbackFunction != null)
            this.callbackFunction.onPermissionResult(mPermissionsGranted);
    }
}
