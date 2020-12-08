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
    private ImageView coverImage;
    private String videoSrc;

    public Viewholder(@NonNull View itemView) {
        super(itemView);
        videoName = itemView.findViewById(R.id.video_name);
        coverImage = itemView.findViewById(R.id.cover_image);
        itemView.setOnClickListener(this);
    }

    public void bind(String text, String src, String url) {
        videoName.setText(text);
        Glide.with(coverImage.getContext()).load(src).into(coverImage);
        videoSrc = url;
    }


    @Override
    public void onClick(View v) {
        Log.d("viewHolder", "clicked");
        Intent intent = new Intent(v.getContext(), VideoActivity.class);
        intent.putExtra("src", videoSrc);
        v.getContext().startActivity(intent);
    }
}