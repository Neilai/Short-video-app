package me.laijingzhi.short_video_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements BothWayProgressBar.OnProgressEndListener {
    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private File mTargetFile;
    private Uri videoUri;

    private BothWayProgressBar mProgressBar;
    private int mProgress;
    private MyHandler mHandler;
    private Thread mProgressThread;
    private boolean isRunning;
    private int mTime;

    //屏幕分辨率
    private int videoWidth = 720, videoHeight = 1280;

    private ImageView btn_flash;
    private ImageView btn_turn;
    private ImageView btn_control;
    private int flag_control = 0;

    private int iCameraCnt;
    private int iFrontCameraIndex;
    private int iBackCameraIndex;
    private boolean flag_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_camera);

        checkPermissionAndStartRecord();
    }

    private void checkPermissionAndStartRecord() {
        getCameraInfo();

        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            flag_back = true;
            setCamera();
        } catch (Exception e) {

        }

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });


        mProgressBar = (BothWayProgressBar) findViewById(R.
                id.main_progress_bar);
        mProgressBar.setOnProgressEndListener(this);
        mHandler = new MyHandler(this);

        btn_turn = findViewById(R.id.camera_turn);
        btn_turn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                if (flag_back) {
                    flag_back = false;
                    mCamera = Camera.open(iFrontCameraIndex);
                    setCamera();
                    try {
                        mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                } else {
                    flag_back = true;
                    mCamera = Camera.open(iBackCameraIndex);
                    setCamera();
                    try {
                        mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                }
            }
        });

        btn_control = findViewById(R.id.camera_control);
        btn_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag_control == 0) {    // 手动开始录制
                    Log.d("TAG", "start");
                    btn_control.setBackgroundResource(R.drawable.btn_camera_recording);
                    flag_control = 1;
                    mProgressBar.setVisibility(View.VISIBLE);

                    startMediaRecorder();

                    mProgressThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                mProgress = 0;
                                isRunning = true;
                                while (isRunning) {
                                    mProgress++;
                                    mHandler.obtainMessage(0).sendToTarget();
                                    Thread.sleep(20);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mProgressThread.start();
                } else {    // 手动结束录制
                    Log.d("TAG", "stop");
                    btn_control.setBackgroundResource(R.drawable.btn_camera_taking);
                    flag_control = 0;
                    mProgressBar.setVisibility(View.INVISIBLE);

                    //判断是否为成功录制(时间过短)
                    // TODO: 时间过短判断只会进行一次
                    if (mProgress < 50) {
                        Toast.makeText(CameraActivity.this, "录制时长过短", Toast.LENGTH_SHORT).show();
                        if (mTargetFile.exists()) {
                            mTargetFile.delete();
                        }
                        stopMediaRecorder();
                    } else {
                        stopMediaRecorder();
                        jumpToPreview();
                    }
                }
            }
        });
    }

    private void setCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> listSize = parameters.getSupportedPictureSizes();
        parameters.setPictureSize(listSize.get(0).width, listSize.get(0).height);
        mCamera.setDisplayOrientation(90);
        //设置不断聚焦
//        if (flag_back) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        }
        mCamera.setParameters(parameters);
    }

    protected void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void startMediaRecorder() {
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOrientationHint(90);//78后置摄像头选择90度，前置摄像头旋转270度

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//VOICE_RECOGNITION
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a Camera Parameters
        mMediaRecorder.setOutputFormat(mProfile.fileFormat);
        /* 设置分辨率*/
        mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        //视频文件的流畅度主要跟VideoFrameRate有关，参数越大视频画面越流畅，但实际中跟你的摄像头质量有很大关系
        mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
        /* Encoding bit rate: 1 * 1024 * 1024*/
        //设置帧频率，然后就清晰了 清晰度和录制文件大小主要和EncodingBitRate有关，参数越大越清晰，同时录制的文件也越大
        mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);

        mMediaRecorder.setAudioChannels(mProfile.audioChannels);
        mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
        // 视频录制格式
        mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
        mMediaRecorder.setAudioEncoder(mProfile.audioCodec);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        try {
            //准备录制
            mMediaRecorder.prepare();
            // 开始录制
            mMediaRecorder.start();
            mIsRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMediaRecorder() {
        if (mMediaRecorder != null) {
            if (mIsRecording) {
                try {
                    mMediaRecorder.setOnErrorListener(null);
                    mMediaRecorder.setOnInfoListener(null);
                    mMediaRecorder.setPreviewDisplay(null);
                    mMediaRecorder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                isRunning = false;

                //mCamera.lock();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mIsRecording = false;
                try {
                    mCamera.reconnect();
                } catch (Exception e) {
                    Log.e("TAG", "reconect fail" + e.toString());
                }
            }
        }
    }

    private void jumpToPreview() {
        videoUri = Uri.fromFile(mTargetFile);
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("videoUri", videoUri.toString());
        startActivity(intent);
    }

    // TODO：超时过一次后再次拍摄就会崩掉
    // 崩掉就崩掉吧，反正录制完就跳转了
    @Override
    public void onProgressEndListener() {
        //视频停止录制
        Log.d("TAG", "expire");
        btn_control.setBackgroundResource(R.drawable.btn_camera_taking);
        flag_control = 0;
        mProgressBar.setVisibility(View.INVISIBLE);
        stopMediaRecorder();
    }

    // 文件保存在/Android/data/#package#/file/..
    private File getOutputMediaFile() {
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mTargetFile = new File(mediaStorageDir.getAbsolutePath() +
                File.separator + "VID_" + timeStamp + ".mp4");
        return mTargetFile;
    }

    private static class MyHandler extends Handler {
        private WeakReference<CameraActivity> mReference;
        private CameraActivity mActivity;

        public MyHandler(CameraActivity activity) {
            mReference = new WeakReference<CameraActivity>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mActivity.mProgressBar.setProgress(mActivity.mProgress);
                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    protected void getCameraInfo() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        iCameraCnt = Camera.getNumberOfCameras();

        for (int i = 0; i < iCameraCnt; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                iFrontCameraIndex = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                iBackCameraIndex = i;
            }
        }
    }
}