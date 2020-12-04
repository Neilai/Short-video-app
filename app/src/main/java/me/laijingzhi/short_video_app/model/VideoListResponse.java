package me.laijingzhi.short_video_app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoListResponse {

    @SerializedName("feeds")
    List<Video> feeds;

    public List<Video> getFeeds() {
        return feeds;
    }
}
