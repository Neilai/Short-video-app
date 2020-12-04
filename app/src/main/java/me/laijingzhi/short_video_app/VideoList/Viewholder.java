package me.laijingzhi.short_video_app.VideoList;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import me.laijingzhi.short_video_app.R;

public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView videoName;
    private ImageView coverImage;

    public Viewholder(@NonNull View itemView) {
        super(itemView);
        videoName = itemView.findViewById(R.id.video_name);
        coverImage = itemView.findViewById(R.id.cover_image);
        itemView.setOnClickListener(this);
    }

    public void bind(String text, String src) {
        videoName.setText(text);
        Glide.with(coverImage.getContext()).load(src).into(coverImage);
    }

    @Override
    public void onClick(View v) {
        Log.d("viewHolder", "clicked");
    }
}