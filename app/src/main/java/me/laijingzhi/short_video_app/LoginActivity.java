package me.laijingzhi.short_video_app;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {

    EditText mId;
    EditText mName;
    Button mBtnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mId = findViewById(R.id.student_id);
        mName = findViewById(R.id.user_name);
        mBtnJoin = findViewById(R.id.btn_join);
        // 使用SharedPreferences读取用户信息
        SharedPreferences share = getSharedPreferences("myshare", MODE_PRIVATE);
        String idStr = share.getString("id", "");
        String nameStr = share.getString("name", "");
        mId.setText(idStr);
        mName.setText(nameStr);

        mBtnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = mId.getText().toString();
                String nameStr = mName.getText().toString();
                if(nameStr.equals("")){
                    Toast.makeText(view.getContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                }
                else if(idStr.equals("")){
                    Toast.makeText(view.getContext(), "请输入用户ID", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 使用SharedPreferences存储用户信息
                    SharedPreferences share = getSharedPreferences("myshare", MODE_PRIVATE);
                    SharedPreferences.Editor edt = share.edit();
                    edt.putString("id", idStr);
                    edt.putString("name", nameStr);
                    edt.commit();
                    // 成功登陆跳转主页
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}