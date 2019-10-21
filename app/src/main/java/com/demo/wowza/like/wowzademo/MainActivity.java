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

public class MainActivity extends AppCompatActivity {
    private WOWZCameraView wowzCameraView;
    private Size liveSize = new Size(1280, 720);
    private int liveBitrate = 1500;
    private WowzaGoCoder mWowzaGoCoder;
    private WOWZAudioDevice goCoderAudioDevice;
    private WOWZBroadcast mWZBroadcast;
    private WOWZBroadcastConfig mWZBroadcastConfig;

    private Button change_cammer;
    private Button btn_start_boardcase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wowza);
        wowzCameraView = findViewById(R.id.cameraPreview);
        initWowza();
        change_cammer = findViewById(R.id.change_cammer);
        btn_start_boardcase = findViewById(R.id.btn_start_boardcase);
        change_cammer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WOWZCamera newCamera = wowzCameraView.switchCamera();//切换摄像头
            }
        });

        btn_start_boardcase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWZBroadcast.startBroadcast(mWZBroadcastConfig);//开始推流
            }
        });

    }

    private void initWowza() {

        mWowzaGoCoder = WowzaGoCoder.init(this, "GOSK-0B47-010C-EE83-871C-3140");//初始化SDK
        //初始化音频
        goCoderAudioDevice = new WOWZAudioDevice();
        goCoderAudioDevice.setAudioEnabled(true);
        goCoderAudioDevice.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//设置活动配置的输入设备以捕获音频。
        goCoderAudioDevice.setMuted(true);//设置静音（推荐使用setAudioPaused暂停）
        //推流和播放
        mWZBroadcast = new WOWZBroadcast();
        mWZBroadcastConfig = new WOWZBroadcastConfig();
        //流媒体设置
        mWZBroadcastConfig.setHostAddress("stream.morecanai.com");//设置流服务器的主机名或IP地址。
        mWZBroadcastConfig.setPortNumber(1935);//设置服务器连接端口号。
        mWZBroadcastConfig.setApplicationName("live");//设置实时流应用程序的名称。
        mWZBroadcastConfig.setStreamName("123456");//设置目标流名称。
        mWZBroadcastConfig.setUsername("admin");//设置用于源身份验证的用户名。
        mWZBroadcastConfig.setPassword("admin");
        //配置视频和音频，捕获和编码的属性。
        mWZBroadcastConfig.setVideoFrameSize(liveSize.getWidth(), liveSize.getHeight());//使用指定的值设置视频帧的大小。
        mWZBroadcastConfig.setOrientationBehavior(WOWZMediaConfig.ALWAYS_LANDSCAPE);//设置流广播的方向。
        mWZBroadcastConfig.setVideoEnabled(true);//启用视频流。
        mWZBroadcastConfig.setABREnabled(true);//启用自适应比特率（ABR）流。
        mWZBroadcastConfig.setHLSEnabled(true);//指定通过Apple HLS的主要播放。
        mWZBroadcastConfig.setVideoFramerate(30);//设置视频帧率。默认30
        mWZBroadcastConfig.setVideoKeyFrameInterval(30);//设置关键帧间隔，即关键帧之间的帧数,默认30
        mWZBroadcastConfig.setVideoBitRate(liveBitrate);//设置视频比特率。//默认1500
        mWZBroadcastConfig.setAudioEnabled(true);//启用音频流

        mWZBroadcastConfig.setAudioSampleRate(WOWZMediaConfig.DEFAULT_AUDIO_SAMPLE_RATE);//设置音频采样率
        mWZBroadcastConfig.setAudioChannels(WOWZMediaConfig.AUDIO_CHANNELS_STEREO);//设置音频通道的数量。
        mWZBroadcastConfig.setAudioBitRate(WOWZMediaConfig.DEFAULT_AUDIO_BITRATE);//设置音频比特率

        mWZBroadcastConfig.setVideoBroadcaster(wowzCameraView);//设置视频播放器。
        mWZBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);//设置音频播放器
        //摄像机配置
        wowzCameraView.setCameraConfig(mWZBroadcastConfig);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (wowzCameraView != null) {
                        if (wowzCameraView.isPreviewPaused()) {
                            wowzCameraView.onResume();
                        } else {
                            wowzCameraView.startPreview();//启动预览
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }
}
