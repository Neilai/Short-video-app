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
    private AlertDialog alertDialog;

    private Uri videoUri;
    String idStr;
    String nameStr;
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
                //TODO: 删除本地文件
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

//        SharedPreferences share = getSharedPreferences("myshare", MODE_PRIVATE);
//        idStr = share.getString("id", "");
//        nameStr = share.getString("name", "");
    }



//    void uploadVideo(Uri uri) {
//        showLoadingDialog();
//        InputStream is = null;
//        ContentResolver contentResolver = getContentResolver();
//        try {
//            is = contentResolver.openInputStream(uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        VideoService service = retrofit.create(VideoService.class);
//        Call<PostResponse>
//                call = service.addVideo("", idStr, nameStr, getMultipartFromBitMap("cover_image", "pic.png", createVideoThumbnail(this, uri)), getMultipartFromStream("video", "video.mp4", is));
//        call.enqueue(new Callback<PostResponse>() {
//            @Override
//            public void onResponse(final Call<PostResponse> call, final Response<PostResponse> response) {
////                dismissLoadingDialog();
//            }
//
//            @Override
//            public void onFailure(final Call<PostResponse> call, final Throwable t) {
//                t.printStackTrace();
////                dismissLoadingDialog();
//            }
//        });
//    }
//
//    private static byte[] BitmapToBytes(Bitmap bm) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        return baos.toByteArray();
//    }
//
//    private byte[] fileNameToByte(InputStream is) throws IOException {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        byte[] buffer = new byte[0xFFFF];
//        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
//            os.write(buffer, 0, len);
//        }
//        return os.toByteArray();
//    }
//
//    private MultipartBody.Part getMultipartFromStream(String key, String name, InputStream is) {
//        RequestBody requestFile = null;
//        try {
//            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), fileNameToByte(is));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return MultipartBody.Part.createFormData(key, name, requestFile);
//    }
//
//    private MultipartBody.Part getMultipartFromBitMap(String key, String name, Bitmap map) {
//        RequestBody requestFile = null;
//        requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), BitmapToBytes(map));
//        return MultipartBody.Part.createFormData(key, name, requestFile);
//    }
//
//    public Bitmap createVideoThumbnail(Context context, Uri uri) {
//        Bitmap bitmap = null;
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            retriever.setDataSource(context, uri);
//            bitmap = retriever.getFrameAtTime(-1);
//        } catch (RuntimeException ex) {
//        } finally {
//            try {
//                retriever.release();
//            } catch (RuntimeException ex) {
//            }
//        }
//        return bitmap;
//    }
//
    public void showLoadingDialog() {
        alertDialog = new AlertDialog.Builder(this).create();
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
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        dismissLoadingDialog();
//    }
}
