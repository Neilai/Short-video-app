package me.laijingzhi.short_video_app;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video);

        videoView = findViewById(R.id.videoView);
        videoView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        videoView.setZOrderOnTop(true);
        Intent intent = getIntent();
        String  src = intent.getStringExtra("src");
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoPath(src);
        videoView.start();
    }
}
