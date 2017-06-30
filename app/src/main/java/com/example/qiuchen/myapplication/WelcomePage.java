package com.example.qiuchen.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import MuYuan.HttpUtils;
import MuYuan.LoginInfo;
import MuYuan.httpClient;

public class WelcomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        new Thread() {
            @Override
            public void run() {
                Bitmap mSave = null;
                try {
                    mSave = httpClient.getBingImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LoginInfo.BackGroundPic = httpClient.BitmapBlur(mSave,
                        getApplicationContext(), 25f);

                SharedPreferences share = WelcomePage.this.getSharedPreferences("QiuChenSet", MODE_PRIVATE);
                boolean state = share.getBoolean("isLogin", false);
                if (state) {
                    int STATE = LoginInfo.aolanEx.login(share.getString("user", ""),
                            share.getString("pass", ""));
                    if (STATE == 1) {
                        LoginInfo.aolanEx.GetMyInfo();//获取个人信息
                        LoginInfo.aolanEx.GetFullMyInfo();
                        Intent i = new Intent(WelcomePage.this, MainUser.class);
                        startActivity(i);
                    } else {
                        startActivity(new Intent(WelcomePage.this, MainActivity.class));
                    }

                } else {
                    startActivity(new Intent(WelcomePage.this, MainActivity.class));
                }
                WelcomePage.this.finish();
            }
        }.start();
    }
}
