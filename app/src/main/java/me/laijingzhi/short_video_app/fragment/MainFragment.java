package me.laijingzhi.short_video_app.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import me.laijingzhi.short_video_app.PathUtil;
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

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private RecyclerView mRecyclerView;
    private Adapter videoAdapter = new Adapter();
    private SwipeRefreshLayout mSwipeView;
    private Spinner spinner;
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
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, 2);
            }
        });

        if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.CAMERA}, 0
            );
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
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InputStream is = null;
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filePath = "";
//            try {
//                filePath = PathUtil.getPath(this.getContext(), uri);
//                is = new FileInputStream(filePath);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            ContentResolver contentResolver = this.getContext().getContentResolver();
            try {
                is = contentResolver.openInputStream(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            VideoService service = retrofit.create(VideoService.class);
            Call<PostResponse>
//                    call = service.addVideo("", idStr, nameStr, getMultipartFromBitMap("cover_image", "pic.png", getVideoThumbnail(filePath, 96, 96, MediaStore.Video.Thumbnails.MINI_KIND)), getMultipartFromStream("video", "video.mp4", is));
                    call = service.addVideo("", idStr, nameStr, getMultipartFromBitMap("cover_image", "pic.png", createVideoThumbnail(this.getContext(), uri)), getMultipartFromStream("video", "video.mp4", is));
            call.enqueue(new Callback<PostResponse>() {
                @Override
                public void onResponse(final Call<PostResponse> call, final Response<PostResponse> response) {
                    requestAllVideos();
                }

                @Override
                public void onFailure(final Call<PostResponse> call, final Throwable t) {
                    t.printStackTrace();
                }
            });
        } else if (requestCode == 2 && resultCode == RESULT_OK) {

        }
    }

    private static byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind); //調用ThumbnailUtils類的靜態方法createVideoThumbnail獲取視頻的截圖；
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//調用ThumbnailUtils類的靜態方法extractThumbnail將原圖片（即上方截取的圖片）轉化為指定大小；
        }
        return bitmap;
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
}
