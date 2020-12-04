package me.laijingzhi.short_video_app.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private RecyclerView mRecyclerView;
    private Adapter videoAdapter = new Adapter();
    String idStr;
    String nameStr;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = view.findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        requestAllVideos();

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
        return view;
    }

    //
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            Uri uri = data.getData();
            InputStream is = null;
            try {
                is = new FileInputStream(uri.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            VideoService service = retrofit.create(VideoService.class);
            Call<PostResponse>
                    call = service.addVideo("weqweqwesa", idStr, nameStr, getMultipartFromBitMap("cover_image", "pic.png", getVideoThumbnail(uri.getPath(), 96, 96, MediaStore.Video.Thumbnails.MINI_KIND)), getMultipartFromStream("video", "video.mp4", is));
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
        }
    }

    public static byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind); //調用ThumbnailUtils類的靜態方法createVideoThumbnail獲取視頻的截圖；
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//調用ThumbnailUtils類的靜態方法extractThumbnail將原圖片（即上方截取的圖片）轉化為指定大小；
        }
        return bitmap;
    }


    private void requestAllVideos() {
        VideoService service = retrofit.create(VideoService.class);
        Call<VideoListResponse> call = service.getVideo();
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
    }

    byte[] fileNameToByte(InputStream is) throws IOException {
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
}
