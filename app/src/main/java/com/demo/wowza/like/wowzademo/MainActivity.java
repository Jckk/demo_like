package com.demo.wowza.like.wowzademo;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;

import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.android.graphics.WOWZText;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

public class MainActivity extends AppCompatActivity implements WOWZStatusCallback,WOWZCameraView.PreviewStatusListener {
    private WOWZCameraView wowzCameraView;
    private Size liveSize = new Size(1280, 720);
    private int vodBitrate = 0;
    private int liveBitrate = 0;
    private WowzaGoCoder mWowzaGoCoder;
    private WOWZCamera mWZCamera;
    private WOWZAudioDevice goCoderAudioDevice;
    private WOWZBroadcast mWZBroadcast;
    private WOWZBroadcastConfig mWZBroadcastConfig;
    private WOWZSize mWOWZSize;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wowza);
        wowzCameraView = findViewById(R.id.cameraPreview);
        wowzCameraView.startPreview();
        initWowza();
        btn=findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,WowzaDemoActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initWowza() {

        mWowzaGoCoder = WowzaGoCoder.init(this, "GOSK-0B47-010C-EE83-871C-3140");
        mWZCamera = wowzCameraView.getCamera();
        if (mWowzaGoCoder == null) {
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
         //   return;
        }
        boolean isinit = WowzaGoCoder.isInitialized();
        goCoderAudioDevice = new WOWZAudioDevice();
        goCoderAudioDevice.setAudioEnabled(true);
        goCoderAudioDevice.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        Log.e("Test", "---");
        goCoderAudioDevice.setMuted(true);
        mWZBroadcast = new WOWZBroadcast();
        mWZBroadcastConfig = new WOWZBroadcastConfig();
//        mWZBroadcastConfig.set(WOWZMediaConfig.FRAME_SIZE_1280x720);
//        mWZBroadcastConfig.setVideoFrameSize(960, 540);
        mWZBroadcastConfig.setVideoFrameSize(liveSize.getWidth(), liveSize.getHeight());
        mWOWZSize = mWZBroadcastConfig.getVideoFrameSize();
        //设置广播方向设置的值，该设置指定视频在其中流媒体的方向  960-->ALWAYS_LANDSCAPE
        //WOWZMediaConfig.SAME_AS_SOURCE (default), WOWZMediaConfig.ALWAYS_LANDSCAPE, WOWZMediaConfig.ALWAYS_PORTRAIT
        mWZBroadcastConfig.setOrientationBehavior(WOWZMediaConfig.ALWAYS_LANDSCAPE);


        mWZBroadcastConfig.setHostAddress("stream.morecanai.com");
        mWZBroadcastConfig.setPortNumber(1935);
        mWZBroadcastConfig.setApplicationName("live");
        //
        mWZBroadcastConfig.setStreamName("123456");
        mWZBroadcastConfig.setUsername("admin");
        mWZBroadcastConfig.setPassword("admin");
        //--------------------------------------
        mWZBroadcastConfig.setVideoEnabled(true);
        mWZBroadcastConfig.setABREnabled(true);
        mWZBroadcastConfig.setHLSEnabled(true);
        //
//        mWZBroadcastConfig.setVideoFramerate(WOWZMediaConfig.DEFAULT_VIDEO_FRAME_RATE);
//        mWZBroadcastConfig.setVideoKeyFrameInterval(WOWZMediaConfig.DEFAULT_VIDEO_KEYFRAME_INTERVAL);

        mWZBroadcastConfig.setVideoFramerate(25);
        mWZBroadcastConfig.setVideoKeyFrameInterval(25);
        mWZBroadcastConfig.setVideoBitRate(liveBitrate);
        //

        mWZBroadcastConfig.setAudioEnabled(true);

        mWZBroadcastConfig.setAudioSampleRate(WOWZMediaConfig.DEFAULT_AUDIO_SAMPLE_RATE);
        mWZBroadcastConfig.setAudioChannels(WOWZMediaConfig.AUDIO_CHANNELS_STEREO);
        mWZBroadcastConfig.setAudioBitRate(WOWZMediaConfig.DEFAULT_AUDIO_BITRATE);
        //
        //
        mWZBroadcastConfig.setVideoBroadcaster(wowzCameraView);
        mWZBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        //
        final WOWZMediaConfig wowzMediaConfig = new WOWZMediaConfig();
        wowzMediaConfig.setVideoFrameSize(liveSize.getWidth(), liveSize.getHeight());
        wowzMediaConfig.setVideoBitRate(liveBitrate);
        wowzMediaConfig.setAudioBitRate(WOWZMediaConfig.DEFAULT_AUDIO_BITRATE);
        wowzMediaConfig.setAudioChannels(WOWZMediaConfig.AUDIO_CHANNELS_STEREO);
        wowzMediaConfig.setAudioSampleRate(WOWZMediaConfig.DEFAULT_AUDIO_SAMPLE_RATE);
        //
        wowzCameraView.setCameraConfig(wowzMediaConfig);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (wowzCameraView != null) {
                        if (wowzCameraView.isPreviewPaused()) {
                            wowzCameraView.onResume();
                        } else {
                            wowzCameraView.startPreview();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }
        }, 1000);
//


    }

    @Override
    protected void onResume() {
        super.onResume();
        wowzCameraView.startPreview();

        //    WOWZCamera activeCamera = wowzCameraView.getCamera();
           // if (activeCamera != null && activeCamera.hasCapability(WOWZCamera.FOCUS_MODE_CONTINUOUS))
         //       activeCamera.setFocusMode(WOWZCamera.FOCUS_MODE_CONTINUOUS);
            //    mWZBroadcast.startBroadcast(mWZBroadcastConfig);
//
//        mWZBroadcast.startBroadcast(mWZBroadcastConfig, new WOWZStatusCallback() {
//            @Override
//            public void onWZStatus(WOWZStatus wowzStatus) {
//
//            }
//
//            @Override
//            public void onWZError(WOWZStatus wowzStatus) {
//
//            }
//        });

    }

    @Override
    public void onWZStatus(WOWZStatus wowzStatus) {

    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {

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

}
