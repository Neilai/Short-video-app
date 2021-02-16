package me.laijingzhi.short_video_app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.laijingzhi.short_video_app.api.ApiHelper;
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

import static android.content.Context.MODE_PRIVATE;

public class VideoUploader {

    private VideoService mService;
    String idStr;
    String nameStr;
    boolean isSuccess = true;

    public boolean uploadVideo(Uri uri) {
        // 获得当前用户的信息供后续使用
        SharedPreferences share = MyApplication.getContext().getSharedPreferences("myshare", MODE_PRIVATE);
        idStr = share.getString("id", "");
        nameStr = share.getString("name", "");

        InputStream is = null;
        ContentResolver contentResolver = MyApplication.getContext().getContentResolver();
        try {
            is = contentResolver.openInputStream(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建网络通信API实例
        mService = ApiHelper.getInstance().buildRetrofit(ApiHelper.BASE_URL)
                .createService(VideoService.class);

        Context mContext = MyApplication.getContext();
        File file = uriToFileApiQ(uri, mContext);
        //实现上传进度监听
        ProgressRequestBody requestFile = new ProgressRequestBody(file, "video/*", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.d("TAG", "onProgressUpdate: " + percentage);
            }

            @Override
            public void onError() {

            }

            @Override
            public void onFinish() {
            }
        });

        MultipartBody.Part body = getMultipartFromStream("video", "video.mp4", is);

        mService.addVideo("", idStr, nameStr
                , getMultipartFromBitMap("cover_image", "pic.png", createVideoThumbnail(MyApplication.getContext(), uri))
                , body).enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                PostResponse resp = response.body();
                if (resp != null) {
                    Log.d("TAG", "图片上传成功");
                    // 上传成功跳转
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Log.d("TAG", "图片上传失败");
            }
        });
        return isSuccess;
    }

    public static File uriToFileApiQ(Uri uri, Context context) {
        File file = null;
        if(uri == null) return file;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis()+ Math.round((Math.random() + 1) * 1000)
                    +"."+ MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));

            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getExternalCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // 将位图和输入流转换成字节数组，再生成复合部分
    private byte[] fileNameToByte(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    private static byte[] BitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, os);
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

    // 以视频第一帧缩略图创建视频封面
    public Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }
}
