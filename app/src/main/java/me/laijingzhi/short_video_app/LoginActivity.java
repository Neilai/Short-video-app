package me.laijingzhi.short_video_app;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


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
        SharedPreferences share = getSharedPreferences("myshare",MODE_PRIVATE);
        String idStr = share.getString("id","");
        String nameStr = share.getString("name","");
        mId.setText(idStr);
        mName.setText(nameStr);
        mBtnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = mId.getText().toString();
                String nameStr = mName.getText().toString();
                SharedPreferences share = getSharedPreferences("myshare",MODE_PRIVATE);
                SharedPreferences.Editor edt = share.edit();
                edt.putString("id",idStr);
                edt.putString("name",nameStr);
                edt.commit();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}