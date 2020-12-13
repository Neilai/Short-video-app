package me.laijingzhi.short_video_app.model;

import com.google.gson.annotations.SerializedName;


public class Video{

    @SerializedName("_id")
    private String id;

    @SerializedName("student_id")
    private String studentId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("image_w")
    private int imageW;

    @SerializedName("image_h")
    private int  imageH;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getUserName() {
        return userName;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getImageW() {
        return imageW;
    }

    public int getImageH() {
        return imageH;
    }

    public String getCreatedAt() { return createdAt; }

    public String getTime() { return createdAt.substring(0, 10); }
}
