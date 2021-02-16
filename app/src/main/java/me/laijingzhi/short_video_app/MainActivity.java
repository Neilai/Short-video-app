package me.laijingzhi.short_video_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import me.laijingzhi.short_video_app.fragment.MainFragment;
import me.laijingzhi.short_video_app.fragment.MeFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // 主页建立两个Fragment(首页/我的)，设置点击监听切换Fragment的显示和隐藏

    protected MainFragment mMainFragment = new MainFragment();
    protected MeFragment mMeFragment = new MeFragment();
    protected LinearLayout mMenuMain;
    protected LinearLayout mMenuMe;
    private  ImageView MainIcon;
    private  ImageView MeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        this.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_content, mMainFragment)
                .add(R.id.container_content, mMeFragment)
                .hide(mMeFragment)
                .commit();
    }

    public void initView() {
        mMenuMain = (LinearLayout) this.findViewById(R.id.menu_main);
        mMenuMe = (LinearLayout) this.findViewById(R.id.menu_me);

        mMenuMain.setOnClickListener(this);
        mMenuMe.setOnClickListener(this);

        MainIcon = mMenuMain.findViewById(R.id.img_main);
        MainIcon.setImageResource(R.drawable.nav_main_click);
        MeIcon = mMenuMe.findViewById(R.id.img_me);
        MeIcon.setImageResource(R.drawable.nav_me_normal);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_main:
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .show(mMainFragment)
                        .hide(mMeFragment)
                        .commit();
                MainIcon.setImageResource(R.drawable.nav_main_click);
                MeIcon.setImageResource(R.drawable.nav_me_normal);
                break;
            case R.id.menu_me:
                this.getSupportFragmentManager()
                        .beginTransaction()
                        .hide(mMainFragment)
                        .show(mMeFragment)
                        .commit();
                MeIcon.setImageResource(R.drawable.nav_me_click);
                MainIcon.setImageResource(R.drawable.nav_main_normal);
                break;
        }
    }
}
