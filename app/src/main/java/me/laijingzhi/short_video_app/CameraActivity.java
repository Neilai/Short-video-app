package me.laijingzhi.short_video_app;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.laijingzhi.short_video_app.util.BothWayProgressBar;

public class CameraActivity extends AppCompatActivity implements BothWayProgressBar.OnProgressEndListener {

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int mPreviewSurfaceWidth;
    private int mPreviewSurfaceHeight;

    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording = false;
    private File mTargetFile;
    private Uri videoUri;

    private BothWayProgressBar mProgressBar;
    private int mProgress;
    private MyHandler mHandler;
    private Thread mProgressThread;
    private boolean isRunning;

    private ImageView btn_flash;
    private ImageView btn_turn;
    private ImageView btn_control;
    private int flag_control = 0;
    private boolean flag_back = false;
    private boolean flag_lighton = false;

    private int mCameraCnt;
    private Camera.CameraInfo mFrontCameraInfo = null;
    private int mFrontCameraIndex = -1;
    private Camera.CameraInfo mBackCameraInfo = null;
    private int mBackCameraIndex = -1;
    private int mOrientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_camera);

        // 设置计时进度条
        mProgressBar = (BothWayProgressBar) findViewById(R.id.main_progress_bar);
        mProgressBar.setOnProgressEndListener(this);
        mHandler = new MyHandler(this);

        // 监听闪光灯切换按钮
        btn_flash = findViewById(R.id.camera_light);
        btn_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Camera.Parameters parameters = mCamera.getParameters();
                if(flag_lighton){
                    btn_flash.setImageResource(R.drawable.btn_camera_lighting_off);
                    flag_lighton = false;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                else{
                    btn_flash.setImageResource(R.drawable.btn_camera_lighting_on);
                    flag_lighton = true;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
                try {
                    mCamera.setParameters(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 监听镜头转换按钮
        btn_turn = findViewById(R.id.camera_turn);
        btn_turn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }

                if (flag_back) {
                    flag_back = false;
                    mCamera = Camera.open(mFrontCameraIndex);
                    mCamera.setDisplayOrientation(getCameraDisplayOrientation(mFrontCameraInfo));
                } else {
                    flag_back = true;
                    mCamera = Camera.open(mBackCameraIndex);
                    mCamera.setDisplayOrientation(getCameraDisplayOrientation(mBackCameraInfo));
                }

                setCamera();
                try {
                    mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
        });

        // 监听拍摄按钮
        btn_control = findViewById(R.id.camera_control);
        btn_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag_control == 0) {    // 手动开始录制
                    Log.d("TAG", "start recording");
                    btn_control.setImageResource(R.drawable.btn_camera_recording);
                    flag_control = 1;
                    mProgressBar.setVisibility(View.VISIBLE);
                    startMediaRecorder();

                    // 异步重画进度条
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
                                    Thread.sleep(40000 / mPreviewSurfaceWidth);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mProgressThread.start();

                } else {    // 手动结束录制
                    Log.d("TAG", "stop recording");
                    btn_control.setImageResource(R.drawable.btn_camera_taking);
                    flag_control = 0;
                    mProgressBar.setVisibility(View.INVISIBLE);

                    // 判断是否为成功录制(时间过短删除文件)
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

    @Override
    protected void onStart() {
        super.onStart();
        // 设置摄像头，初始打开后摄
        getCameraInfo();
        try {
            mCamera = Camera.open(mBackCameraIndex);
            flag_back = true;
            mCamera.setDisplayOrientation(getCameraDisplayOrientation(mBackCameraInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置画幅
        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
                mPreviewSurfaceWidth = width;
                mPreviewSurfaceHeight = height;

                setCamera();
                try {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceHolder = null;
                mPreviewSurfaceWidth = 0;
                mPreviewSurfaceHeight = 0;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    // 设置摄像头参数
    private void setCamera() {
        int longSide = mPreviewSurfaceHeight;
        int shortSide = mPreviewSurfaceWidth;
        float aspectRatio = (float) longSide / shortSide;
        Camera.Parameters parameters = mCamera.getParameters();
        // 设置预览尺寸
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            if ((float) previewSize.width / previewSize.height == aspectRatio && previewSize.height <= shortSide && previewSize.width <= longSide) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                break;
            }
        }
        // 设置照片尺寸
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        for (Camera.Size pictureSize : supportedPictureSizes) {
            if ((float) pictureSize.width / pictureSize.height == aspectRatio) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                break;
            }
        }
        //设置持续聚焦
        if (flag_back) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setOrientationHint(mOrientation);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3 : 设置输出视频文件的格式和编码
        CamcorderProfile mCamcorderProfile;
        if(flag_back == true){
            mCamcorderProfile = CamcorderProfile.get(mBackCameraIndex, CamcorderProfile.QUALITY_HIGH);
        }
        else{
            mCamcorderProfile = CamcorderProfile.get(mFrontCameraIndex, CamcorderProfile.QUALITY_HIGH);
        }

        mMediaRecorder.setProfile(mCamcorderProfile);
        mMediaRecorder.setVideoSize(mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);
        mMediaRecorder.setAudioSamplingRate(44100);

        //
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

        //
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mMediaRecorder.prepare();
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
                    // 防止拍摄时长过短导致程序崩溃
                    mMediaRecorder.setOnErrorListener(null);
                    mMediaRecorder.setOnInfoListener(null);
                    mMediaRecorder.setPreviewDisplay(null);
                    mMediaRecorder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mMediaRecorder.reset();
                try {
                    mMediaRecorder.stop();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mMediaRecorder.release();
                mCamera.lock();
                mMediaRecorder = null;
                mIsRecording = false;
                isRunning = false;
//                }
            }
        }
    }

    // 拍摄完成跳转预览
    private void jumpToPreview() {
        videoUri = Uri.fromFile(mTargetFile);
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("videoUri", videoUri.toString());
        startActivity(intent);
    }

    @Override
    public void onProgressEndListener() {
        //视频停止录制
        Log.d("TAG", "expire");
        btn_control.setBackgroundResource(R.drawable.btn_camera_taking);
        flag_control = 0;
        mProgressBar.setVisibility(View.INVISIBLE);
        stopMediaRecorder();
        jumpToPreview();
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

    // 矫正预览画面方向
    private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (flag_back == false) {
            result = (270 + 180 - degrees) % 360;
            mOrientation = result + 180;
        } else {  // back-facing
            result = (90 - degrees + 360) % 360;
            mOrientation = result;
        }
        return result;
    }

    // 获取设备摄像头信息
    protected void getCameraInfo() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        mCameraCnt = Camera.getNumberOfCameras();

        for (int i = 0; i < mCameraCnt; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            // 记录前置和后置摄像头的序号
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraIndex = i;
                mFrontCameraInfo = cameraInfo;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraIndex = i;
                mBackCameraInfo = cameraInfo;
            }
        }
    }
}