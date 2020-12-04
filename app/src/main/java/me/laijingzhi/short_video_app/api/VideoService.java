package me.laijingzhi.short_video_app.api;

import me.laijingzhi.short_video_app.model.PostResponse;
import me.laijingzhi.short_video_app.model.VideoListResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface VideoService {

    @POST("invoke/video")
    @Multipart
    Call<PostResponse> addVideo(@Query("extra_value") String extra_value, @Query("student_id") String student_id, @Query("user_name") String user_name, @Part MultipartBody.Part cover_image, @Part  MultipartBody.Part video);

    @GET("/invoke/video")
    Call<VideoListResponse> getVideo();
}
