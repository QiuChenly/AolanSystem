package com.example.qiuchen.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import MuYuan.HttpUtils;
import MuYuan.LoginInfo;
import MuYuan.httpClient;

public class MainActivity extends AppCompatActivity {
    Button btn = null;
    EditText mUser = null;
    EditText mPass = null;
    SharedPreferences Share;
    ProgressBar mLoginBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //如果需要透明导航栏，请加入标记

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        btn = (Button) findViewById(R.id.mLogin);
        mUser = (EditText) findViewById(R.id.mUser);
        mPass = (EditText) findViewById(R.id.mPass);
        mLoginBar = (ProgressBar) findViewById(R.id.mLoginProgressBar);

        Share = this.getSharedPreferences("QiuChenSet", MODE_PRIVATE);
        String Temp;
        Temp = Share.getString("user", "");
        mUser.setText(Temp);
        Temp = Share.getString("pass", "");
        mPass.setText(Temp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginBar.setVisibility(View.VISIBLE);
                btn.setVisibility(View.GONE);
                //线程启动
                new Thread() {
                    @Override
                    public void run() {
                        //LoginInfo.aolanEx.init(MainActivity.this);
                        LoginInfo.ErrCode = LoginInfo.aolanEx.login(mUser.getText().toString(), mPass.getText().toString());
                        //发送Handler消息通知UI更新
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (LoginInfo.ErrCode == -1) {
                                    Toast.makeText(MainActivity.this,
                                            "登录失败!可能是账号或密码错误!请重新输入!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    mLoginBar.setVisibility(View.GONE);
                                    btn.setVisibility(View.VISIBLE);
                                    mPass.setText("");
                                } else if (LoginInfo.ErrCode == 1) {
                                    SharedPreferences.Editor edit = Share.edit();
                                    edit.putString("user", mUser.getText().toString());
                                    edit.putString("pass", mPass.getText().toString());
                                    edit.putBoolean("isLogin",true);
                                    edit.apply();
                                    Toast.makeText(MainActivity.this,
                                            "登录成功,欢迎你:" + LoginInfo.mUserData.Name + "!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    LoginInfo.mUserData.UserNum = mUser.getText().toString();
                                    LoginInfo.mUserData.Password = mPass.getText().toString();
                                    //下面是登录前的获取简要个人信息的操作.

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            LoginInfo.aolanEx.GetMyInfo();//获取个人信息
                                            LoginInfo.aolanEx.GetFullMyInfo();
                                            Intent i = new Intent(MainActivity.this, MainUser.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }.start();
                                }
                            }
                        });
                    }
                }.start();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.mLoginBackGround);
        imageView.setImageBitmap(LoginInfo.BackGroundPic);
        if (mUser.getText().toString().length() > 0 && mPass.getText().toString().length() > 0) {
            Toast.makeText(MainActivity.this,
                    "自动登录中...",
                    Toast.LENGTH_SHORT
            ).show();
            btn.callOnClick();
        }
    }
}