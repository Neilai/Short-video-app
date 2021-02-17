package me.laijingzhi.short_video_app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.laijingzhi.short_video_app.api.VideoService;
import me.laijingzhi.short_video_app.model.PostResponse;
import me.laijingzhi.short_video_app.util.FileUriPathUtils;
import me.laijingzhi.short_video_app.util.MyApplication;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PreviewActivity extends AppCompatActivity {
    private ImageView buttonCancel;
    private ImageView buttonConfrim;
    private VideoView videoView;

    private Uri videoUri;
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api-sjtu-camp.bytedance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_preview);

        Intent intent = getIntent();
        videoUri = Uri.parse(intent.getStringExtra("videoUri"));

        // 使用VideoView对拍摄视频进行预览
        videoView = findViewById(R.id.video_look);
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        buttonCancel = findViewById(R.id.cancel_button);
        buttonConfrim = findViewById(R.id.confirm_button);
        // 选择取消发布删除预览文件
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(new FileUriPathUtils().getRealFilePath(MyApplication.getContext(), videoUri));
                if (file.exists()&& file.isFile()){
                    file.delete();
                }
                finish();
            }
        });
        // 选择发布视频后跳转回上传页
        buttonConfrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UploadActivity.class);
                intent.putExtra("videoUri", videoUri.toString());
                startActivity(intent);
            }
        });
    }
}
