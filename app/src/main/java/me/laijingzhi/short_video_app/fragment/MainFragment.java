package me.laijingzhi.short_video_app.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.laijingzhi.short_video_app.CameraActivity;
import me.laijingzhi.short_video_app.MainActivity;
import me.laijingzhi.short_video_app.MyApplication;
import me.laijingzhi.short_video_app.R;
import me.laijingzhi.short_video_app.UploadActivity;
import me.laijingzhi.short_video_app.VideoList.VideoAdapter;
import me.laijingzhi.short_video_app.VideoUploader;
import me.laijingzhi.short_video_app.api.ApiHelper;
import me.laijingzhi.short_video_app.api.VideoService;
import me.laijingzhi.short_video_app.model.PostResponse;
import me.laijingzhi.short_video_app.model.VideoListResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private static final int CHOOSE_VIDEO = 1;
    private static final int RECORD_VIDEO = 2;

    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;

    private RecyclerView mRecyclerView;
    private VideoAdapter videoAdapter = new VideoAdapter();
    private SwipeRefreshLayout mSwipeView;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private Spinner spinner;
    private AdapterView.OnItemSelectedListener selectListener;
    private AlertDialog alertDialog;

    private VideoService mService;
    String idStr;
    String nameStr;
    boolean showMe = false;
    Uri chooseVideoUri;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // 在初次显示主页时对所需要的权限进行申请
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(view.getContext(), permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        // 有权限没有通过，需要申请后回调进行操作
        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(getActivity(), permissions, mRequestCode);
        }

        // 获得当前用户的信息供后续使用
        SharedPreferences share = view.getContext().getSharedPreferences("myshare", MODE_PRIVATE);
        idStr = share.getString("id", "");
        nameStr = share.getString("name", "");

        mRecyclerView = view.findViewById(R.id.rv);
        mSwipeView = view.findViewById(R.id.swipe);
        spinner = view.findViewById(R.id.selector);
        // 设置选视频和拍视频的按键
        view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 从文件中读取视频文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, CHOOSE_VIDEO);
            }
        });

        view.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 调用系统相机进行视频拍摄，后回调进行预览
//                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                startActivityForResult(intent, RECORD_VIDEO);
                // 跳转进入拍摄视频界面
                startActivity(new Intent(getActivity(), CameraActivity.class));
            }
        });



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 设置下拉刷新布局和滚动控件
        GridLayoutManager layoutManager = new GridLayoutManager(this.getContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        // 刷新时重新获取视频列表
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mService = ApiHelper.getInstance().buildRetrofit(ApiHelper.BASE_URL)
                        .createService(VideoService.class);
                Call<VideoListResponse> call;
                if (showMe) {
                    call = mService.getVideo(idStr);
                } else {
                    call = mService.getVideo(null);
                }
                call.enqueue(new Callback<VideoListResponse>() {
                    @Override
                    public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                        mRecyclerView.setAdapter(videoAdapter);
                        videoAdapter.notifyItems(response.body().getFeeds());
                    }

                    @Override
                    public void onFailure(Call<VideoListResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
                mSwipeView.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeView.setRefreshing(false);
                    }
                });
            }
        };
        mSwipeView.setOnRefreshListener(refreshListener);

        // 设置选择过滤下拉栏
        String[] list = new String[]{"所有", "我的"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MyApplication.getContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        selectListener = new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        showMe = false;
                        break;
                    case 1:
                        showMe = true;
                        break;
                    default:
                        break;
                }
                requestAllVideos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
        spinner.setOnItemSelectedListener(selectListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 如果有权限没有被允许，不让他继续访问
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this.getContext(), "You denied the permission", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOOSE_VIDEO:
                if (resultCode == RESULT_OK) {
                    // 获得视频文件uri进行上传确认
                    chooseVideoUri = data.getData();
                    if (chooseVideoUri != null) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle("上传确认");
                        dialog.setMessage("是否开始上传");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 跳转至上传界面
                                Intent intent = new Intent(MyApplication.getContext(), UploadActivity.class);
                                intent.putExtra("videoUri", chooseVideoUri.toString());
                                startActivity(intent);
                            }
                        });
                        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.show();
                    }
                }
                break;
            case RECORD_VIDEO:
                if(resultCode == RESULT_OK){
                    //TODO: doSomething
                }
                break;
            default:
                break;
        }
    }

    private void requestAllVideos() {

        mSwipeView.post(new Runnable() {
            @Override
            public void run() {
                mSwipeView.setRefreshing(true);
            }
        });
        refreshListener.onRefresh();

    }

    // 设置加载对话框的显示和消失
    public void showLoadingDialog() {
        Log.d(TAG, "showLoadingDialog");
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    public void dismissLoadingDialog() {
        Log.d(TAG, "dismissLoadingDialog");
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
