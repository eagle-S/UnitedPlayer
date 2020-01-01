package com.haha.record;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.haha.record.camera.CameraPreviewView;
import com.haha.record.encodec.BaseMediaEncoder;
import com.haha.record.encodec.MediaEncoder;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

/**
 * 视频录制界面
 */
public class VideoActivity extends AppCompatActivity {
    private static String TAG = VideoActivity.class.getSimpleName();

    private CameraPreviewView cameraView;
    private Button btnRecord;

    private MediaEncoder mediaEncodec;
    //提供音乐
    private WlMusic wlMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        cameraView = findViewById(R.id.cameraview);
        btnRecord = findViewById(R.id.btn_record);

        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);

        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(39, 60);
            }
        });

        wlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                mediaEncodec.stopRecord();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnRecord.setText("开始录制");
                    }
                });
                mediaEncodec = null;
            }
        });

        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                if (mediaEncodec == null) {
                    Log.d("ywl5320", "textureid is " + cameraView.getTextureId());
                    mediaEncodec = new MediaEncoder(VideoActivity.this, cameraView.getTextureId());
                    mediaEncodec.initEncoder(cameraView.getEglContext(),
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/manlian.mp4"
                            , 720, 1280, samplerate);
                    mediaEncodec.setOnMediaInfoListener(new BaseMediaEncoder.OnMediaInfoListener() {
                        @Override
                        public void onMediaTime(long times) {
                            Log.d(TAG, "time is : " + times);
                        }
                    });

                    mediaEncodec.startRecord();
                }


            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (mediaEncodec != null) {
                    mediaEncodec.putPcmData(pcmdata, size);
                }
            }

        });
    }

    public void record(View view) {
        //因为停止录制的时候会将wlMediaEncodec置为空，所以可以根据wlMediaEncodec是否为空判断是否是正在录制状态
        if (mediaEncodec == null) {
            wlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/haikuotiankong.mp3");
            wlMusic.prePared();
            btnRecord.setText("正在录制");
        } else {
            mediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            mediaEncodec = null;
            wlMusic.stop();
        }

    }
}
