package me.laijingzhi.short_video_app.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.laijingzhi.short_video_app.LoginActivity;
import me.laijingzhi.short_video_app.MainActivity;
import me.laijingzhi.short_video_app.R;

import static android.content.Context.MODE_PRIVATE;

public class MeFragment extends Fragment {
    String idStr;
    String nameStr;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_me, container, false);
        SharedPreferences share = view.getContext().getSharedPreferences("myshare", MODE_PRIVATE);
        idStr = share.getString("id", "");
        nameStr = share.getString("name", "");
        TextView idTxt = view.findViewById(R.id.txt_id);
        TextView nameTxt = view.findViewById(R.id.txt_name);
        nameTxt.setText(nameStr);
        idTxt.setText("ID:" + idStr);

        Button btn = view.findViewById(R.id.log_out);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
