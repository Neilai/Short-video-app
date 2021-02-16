package me.laijingzhi.short_video_app.VideoList;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import me.laijingzhi.short_video_app.MainActivity;
import me.laijingzhi.short_video_app.R;
import me.laijingzhi.short_video_app.VideoActivity;

public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView videoName;
    private TextView videoTime;
    private ImageView coverImage;
    private String videoSrc;

    public Viewholder(@NonNull View itemView) {
        super(itemView);
        videoName = itemView.findViewById(R.id.video_name);
        videoTime = itemView.findViewById(R.id.video_time);
        coverImage = itemView.findViewById(R.id.cover_image);
        itemView.setOnClickListener(this);
    }

    public void bind(String text1, String text2, String src, String url) {
        videoName.setText(text1);
        videoTime.setText(text2);
        Glide.with(coverImage.getContext()).load(src).into(coverImage);
        videoSrc = url;
    }


    @Override
    public void onClick(View v) {
        Log.d("viewHolder", "clicked");
        // 点击相应卡片跳转至视频播放界面
        Intent intent = new Intent(v.getContext(), VideoActivity.class);
        intent.putExtra("src", videoSrc);
        v.getContext().startActivity(intent);
    }
}