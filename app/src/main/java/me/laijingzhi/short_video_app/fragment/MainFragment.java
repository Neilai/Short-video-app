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
import me.laijingzhi.short_video_app.R;
import me.laijingzhi.short_video_app.VideoList.Adapter;
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
    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private RecyclerView mRecyclerView;
    private Adapter videoAdapter = new Adapter();
    private SwipeRefreshLayout mSwipeView;
    private Spinner spinner;
    private AlertDialog alertDialog;
    boolean showMe = false;
    String idStr;
    String nameStr;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = view.findViewById(R.id.rv);
        mSwipeView = view.findViewById(R.id.swipe);
        GridLayoutManager layoutManager = new GridLayoutManager(this.getContext(), 2);

        mRecyclerView.setLayoutManager(layoutManager);

        mSwipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAllVideos();
            }
        });

        mSwipeView.post(new Runnable() {
            @Override
            public void run() {
                requestAllVideos();
            }
        });


        SharedPreferences share = view.getContext().getSharedPreferences("myshare", MODE_PRIVATE);
        idStr = share.getString("id", "");
        nameStr = share.getString("name", "");
        view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        view.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                startActivityForResult(intent, 2);
                startActivity(new Intent(getActivity(), CameraActivity.class));
            }
        });



//        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{
//                    Manifest.permission.CAMERA}, 0
//            );
//        }

        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(view.getContext(), permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(getActivity(), permissions, mRequestCode);
        }

        spinner = view.findViewById(R.id.selector);
        String[] list = new String[]{"所有", "我的"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    showMe = false;
                } else {
                    showMe = true;
                }
                requestAllVideos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                return; // 直接关闭页面，不让他继续访问
            }
        }
    }

    void uploadVideo(Uri uri) {
        showLoadingDialog();
        InputStream is = null;
        ContentResolver contentResolver = this.getContext().getContentResolver();
        try {
            is = contentResolver.openInputStream(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        VideoService service = retrofit.create(VideoService.class);
        Call<PostResponse>
                call = service.addVideo("", idStr, nameStr, getMultipartFromBitMap("cover_image", "pic.png", createVideoThumbnail(this.getContext(), uri)), getMultipartFromStream("video", "video.mp4", is));
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(final Call<PostResponse> call, final Response<PostResponse> response) {
                requestAllVideos();
                dismissLoadingDialog();
            }

            @Override
            public void onFailure(final Call<PostResponse> call, final Throwable t) {
                t.printStackTrace();
                dismissLoadingDialog();
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null)
                uploadVideo(uri);
        }
    }

    private static byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void requestAllVideos() {
        mSwipeView.setRefreshing(true);
        VideoService service = retrofit.create(VideoService.class);
        Call<VideoListResponse> call;
        if (showMe) {
            call = service.getVideo(idStr);
        } else {
            call = service.getVideo(null);

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
        mSwipeView.setRefreshing(false);
    }

    private byte[] fileNameToByte(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    private MultipartBody.Part getMultipartFromStream(String key, String name, InputStream is) {
        RequestBody requestFile = null;
        try {
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileNameToByte(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MultipartBody.Part.createFormData(key, name, requestFile);
    }

    private MultipartBody.Part getMultipartFromBitMap(String key, String name, Bitmap map) {
        RequestBody requestFile = null;
        requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), BitmapToBytes(map));
        return MultipartBody.Part.createFormData(key, name, requestFile);
    }

    public Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (RuntimeException ex) {
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return bitmap;
    }

    public void showLoadingDialog() {
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
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
