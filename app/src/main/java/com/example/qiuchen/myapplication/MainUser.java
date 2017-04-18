package com.example.qiuchen.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import MuYuan.AoLan;
import MuYuan.LoginInfo;
import MuYuan.UserData;

public class MainUser extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ImageView mPersonImage = null;
    TextView mPersonName = null;
    TextView mPersonLink = null;
    Toolbar toolbar = null;
    long BackTime = 0;

    public void initiation() {
        mPersonImage = (ImageView) findViewById(R.id.PersonPic);
        mPersonName = (TextView) findViewById(R.id.PersonName);
        mPersonLink = (TextView) findViewById(R.id.PersonLink);
        mPersonLink.setText(LoginInfo.mUserData.nd + "级学生,当前" + LoginInfo.mUserData.Term + "学期.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    //右端菜单创建的数据初始化
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        initiation();
        String Leader;
        if (LoginInfo.mUserData.ClassLeader == true) {
            Leader = "班级负责人";
        } else {
            Leader = "班级群众";
        }
        mPersonName.setText(LoginInfo.mUserData.Name + "," + Leader);
        mPersonImage.setImageDrawable(getResources().getDrawable(R.mipmap.userimg));
        getMenuInflater().inflate(R.menu.main_user, menu);
        return true;
    }


    //这里是普通菜单设置 没什么软用的菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //init All State
            LoginInfo.ErrCode = 0;
            LoginInfo.mUserData = new UserData();
            LoginInfo.aolan = new AoLan();
            LoginInfo.Result = "";

            SharedPreferences s = MainUser.this.getSharedPreferences("QiuChenSet", MODE_PRIVATE);
            SharedPreferences.Editor e = s.edit();
            e.putString("pass", "");
            e.apply();
            Intent i = new Intent(MainUser.this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //这里是右边侧边导航栏 实现点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.myInfo) {
            // Handle the camera action
            toolbar.setTitle("我的基本信息");
            SwitchViews.sendMessage(SwitchView(1));
        } else if (id == R.id.mDaysHoliday) {
            toolbar.setTitle("日常请假");
            SwitchViews.sendMessage(SwitchView(2));
        } else if (id == R.id.mLongHoliday) {
            toolbar.setTitle("寒暑假请假");
            SwitchViews.sendMessage(SwitchView(3));
        } else if (id == R.id.nav_manage) {
            toolbar.setTitle("啊啦,这是一个空页面哦~");
            SwitchViews.sendMessage(SwitchView(4));
        } else if (id == R.id.nav_share) {
            //toolbar.setTitle("你有完没完?");
            //SwitchViews.sendMessage(SwitchView(5));
        } else if (id == R.id.nav_send) {
            //toolbar.setTitle("可以 I 服了 You!");
            //SwitchViews.sendMessage(SwitchView(6));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 封装整数型消息,以便传递数据
     *
     * @param Views 欲切换的View编号
     * @return 返回封装好的消息
     */
    private Message SwitchView(int Views) {
        Bundle b = new Bundle();
        b.putInt("View", Views);
        Message m = new Message();
        m.setData(b);
        return m;
    }


    //切换视图的Handle
    Handler SwitchViews = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LayoutInflater inflater = LayoutInflater.from(MainUser.this);
            int view = msg.getData().getInt("View");
            LinearLayout i;
            LinearLayout linearLayout;
            switch (view) {
                case 1:
                    i = (LinearLayout) inflater.inflate(R.layout.mfullinfo, null).findViewById(R.id.mFullInfo);
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    linearLayout.addView(i);
                    LoginInfo.aolan.GetFullMyInfo();//获取消息
                    //需要在View加入后组建创建完成再挂接事件,否则将会闪退
                    final Handler Hand1 = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            TextView t = (TextView) findViewById(R.id.m_TextView_UserInfo);
                            if (LoginInfo.ErrCode == -1) {
                                t.setText("Error:网络超时...请检查手机网络是否畅通...");
                            } else if (LoginInfo.ErrCode == 1) {
                                String Leader;
                                if (LoginInfo.mUserData.ClassLeader == true) {
                                    Leader = "班级负责人";
                                } else {
                                    Leader = "班级群众";
                                }
                                t.setText(LoginInfo.mUserData.Name + ","
                                        + LoginInfo.mUserData.y_xbdm
                                        + "\n" + LoginInfo.mUserData.y_xdm +
                                        LoginInfo.mUserData.nd
                                        + "级学生\n学号:" + LoginInfo.mUserData.y_xh + "\n"
                                        + LoginInfo.mUserData.ClassName
                                        + Leader
                                        + "\n"
                                        + "当前处于"
                                        + LoginInfo.mUserData.Term
                                        + "学期.\n政治面貌:"
                                        + LoginInfo.mUserData.y_zzmmdm + ",加入时间:" + LoginInfo.mUserData.y_zzmmsj
                                        + "\n所在地区:" + LoginInfo.mUserData.y_syszddm + "\n高中毕业于" + LoginInfo.mUserData.y_byzx
                                );
                            }
                        }
                    };
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            while (LoginInfo.ErrCode == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Hand1.sendEmptyMessage(0);
                        }
                    }.start();
                    break;
                case 2:
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    //移除所有控件,并准备加入新的控件
                    i = (LinearLayout) inflater.inflate(R.layout.mdaysview, null)
                            .findViewById(R.id.mDaysListViewLinearLayout);
                    linearLayout.addView(i);
                    final Handler hand2 = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            ListView ls = (ListView) findViewById(R.id.mDaysListView);
                            //判斷是否為空,若为空则初始化一下防止抛异常
                            if (LoginInfo.mUserData.HolidaysEume == null) {
                                LoginInfo.mUserData.HolidaysEume = new ArrayList<Map<String, Object>>();
                            }
                            ls.setAdapter(new SimpleAdapter(MainUser.this, LoginInfo.mUserData.HolidaysEume, R.layout.listviewadapteritem,
                                    new String[]{"mItemIndex", "mItem_HolidayBecause", "mItem_HolidayTime", "mItem_WhereOutSide", "mItemAcceptState"},
                                    new int[]{R.id.mItemIndex, R.id.mItem_HolidayBecause, R.id.mItem_HolidayTime, R.id.mItem_WhereOutSide, R.id.mItemAcceptState}
                            ));
                            ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    //TODO:写下点击事件
                                    //似乎不需要写,因为这个只是起到一个查阅的作用.
                                }
                            });


                            Spinner spinner = (Spinner) findViewById(R.id.mStudentSelect);
                            //判斷是否為空,若为空则初始化一下防止抛异常
                            if (LoginInfo.mUserData.ClassMates == null) {
                                LoginInfo.mUserData.ClassMates = new ArrayList<String>();
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(MainUser.this,
                                    android.R.layout.simple_spinner_item,
                                    LoginInfo.mUserData.ClassMates);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(arrayAdapter);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String t = null;
                                    if (LoginInfo.mUserData.ClassMates != null) {
                                        t = LoginInfo.mUserData.ClassMates.get(position);
                                    }
                                    Toast.makeText(MainUser.this, t, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            spinner.setSelection(LoginInfo.mUserData.ItemSelection);
                            Button RequestButton = (Button) findViewById(R.id.mRequestHoliday);
                            RequestButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LoginInfo.aolan.getStudentHolidaysInfo();
                                }
                            });
                        }
                    };
                    LoginInfo.aolan.getClassMatesInfo();
                    new Thread() {
                        @Override
                        public void run() {
                            while (LoginInfo.ErrCode == 0) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (LoginInfo.ErrCode == 1) {
                                hand2.sendEmptyMessage(0);
                            } else {
                                Toast.makeText(MainUser.this, "网络超时啦!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.start();
                    break;
                case 3:
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    //移除所有控件,并准备加入新的控件
                    //i = (LinearLayout) inflater.inflate(R.layout.mfullinfo, null).findViewById(R.id.mFullInfo);
                    //linearLayout.addView(i);
                    break;
                default:
                    break;
            }
        }
    };

    //实现双击返回建退出的方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondT = System.currentTimeMillis();
                if (secondT - BackTime > 2000) {
                    Toast.makeText(this, "再次点击返回键退出", Toast.LENGTH_SHORT).show();
                    BackTime = secondT;
                } else {
                    finish();
                    System.exit(0);
                }
                return true;
            default:
                return true;
        }
    }
}
