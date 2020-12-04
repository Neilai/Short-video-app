package me.laijingzhi.short_video_app.VideoList;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.laijingzhi.short_video_app.R;
import me.laijingzhi.short_video_app.model.Video;


public class Adapter extends RecyclerView.Adapter<Viewholder> {

    private List<Video> mItems;

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.bind(mItems.get(position).getUserName(),mItems.get(position).getVideoUrl());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void notifyItems(@NonNull List<Video> items) {
//        mItems.clear();
//        mItems.addAll(items);
        mItems=items;
        notifyDataSetChanged();
    }
}
