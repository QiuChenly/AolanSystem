package com.example.qiuchen.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lzy.okgo.OkGo;

import MuYuan.LoginInfo;

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
        OkGo.init(getApplication());//TM这....
        btn = (Button) findViewById(R.id.mLogin);
        mUser = (EditText) findViewById(R.id.mUser);
        mPass = (EditText) findViewById(R.id.mPass);
        mLoginBar = (ProgressBar) findViewById(R.id.mLoginProgressBar);

        Share = MainActivity.this.getSharedPreferences("QiuChenSet", MODE_PRIVATE);
        String Temp;
        Temp = Share.getString("user", "");
        mUser.setText(Temp);
        Temp = Share.getString("pass", "");
        mPass.setText(Temp);
        final Handler hand = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
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
                    edit.apply();
                    Toast.makeText(MainActivity.this,
                            "登录成功,欢迎你:" + LoginInfo.mUserData.Name + "!",
                            Toast.LENGTH_SHORT
                    ).show();
                    LoginInfo.mUserData.UserNum = mUser.getText().toString();
                    LoginInfo.mUserData.Password = mPass.getText().toString();
                    //下面是登录前的获取简要个人信息的操作.
                    LoginInfo.aolanEx.GetMyInfo();//获取个人信息
                    new Thread() {
                        @Override
                        public void run() {
                            //循环判断
                            //默认0为初始值,1或-1为执行成功或失败
                            while (LoginInfo.ErrCode == 0) {
                                Log.d("QiuChen", "Gays" + LoginInfo.Result);
                                try {
                                    Thread.sleep(100);//休眠100毫秒等待
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            LoginInfo.aolanEx.GetFullMyInfo();
                            while (LoginInfo.ErrCode == 0) {
                                Log.d("QiuChen", "Gays" + LoginInfo.Result);
                                try {
                                    Thread.sleep(100);//休眠100毫秒等待
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //跳出循环,发送Handler消息通知UI更新
                            Intent i = new Intent(MainActivity.this, MainUser.class);
                            startActivity(i);
                            finish();
                        }
                    }.start();

                }
            }
        };
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
                        LoginInfo.aolanEx.login(MainActivity.this, mUser.getText().toString(), mPass.getText().toString());
                        //循环判断
                        //默认0为初始值,1或-1为执行成功或失败
                        while (LoginInfo.ErrCode == 0) {
                            Log.d("QiuChen", "Gays" + LoginInfo.Result);
                            try {
                                Thread.sleep(100);//休眠100毫秒等待
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //跳出循环,发送Handler消息通知UI更新
                        hand.sendEmptyMessage(0);
                    }
                }.start();
            }
        });
        if (mUser.getText().toString().length() > 0 && mPass.getText().toString().length() > 0) {
            Toast.makeText(MainActivity.this,
                    "自动登录中...",
                    Toast.LENGTH_SHORT
            ).show();
            btn.callOnClick();
        }
    }
}