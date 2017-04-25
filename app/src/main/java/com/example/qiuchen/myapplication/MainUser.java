package com.example.qiuchen.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostNfcFService;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.LogHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import MuYuan.AolanOkHttpEx;
import MuYuan.LoginInfo;
import MuYuan.UserData;

public class MainUser extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ImageView mPersonImage = null;
    TextView mPersonName = null;
    TextView mPersonLink = null;
    Toolbar toolbar = null;
    long BackTime = 0;

    public void initiation() {
        mPersonImage = (ImageView) findViewById(R.id.PersonPic);
        mPersonName = (TextView) findViewById(R.id.PersonName);
        mPersonLink = (TextView) findViewById(R.id.PersonLink);
        mPersonLink.setText(LoginInfo.mUserData.ClassName + "," + LoginInfo.mUserData.nd + "级学生");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        LoginInfo.Dialog = new ProgressDialog(MainUser.this);
        LoginInfo.Dialog.setTitle("页面加载中...");
        LoginInfo.Dialog.setMessage("初始化页面数据中...");
        LoginInfo.Dialog.setCancelable(false);
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
        if (id == R.id.Logout) {
            //init All State
            LoginInfo.ErrCode = 0;
            LoginInfo.Result = "";
            LoginInfo.mUserData = new UserData();
            LoginInfo.aolanEx = new AolanOkHttpEx();
            LoginInfo.IsRequestViews = false;

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
            toolbar.setTitle("节假日请假");
            SwitchViews.sendMessage(SwitchView(3));
        } else if (id == R.id.nav_manage) {
            toolbar.setTitle("啊啦,这是一个空页面哦~");
            SwitchViews.sendMessage(SwitchView(4));
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, "你好,我是秋城落叶,想联系我?请点击下方邮箱.", Toast.LENGTH_SHORT).show();
            //toolbar.setTitle("你有完没完?");
            //SwitchViews.sendMessage(SwitchView(5));
        } else if (id == R.id.nav_send) {
            Toast.makeText(this, "作者QQ:963084062,你有什么想说的吗?", Toast.LENGTH_SHORT).show();
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
            final LayoutInflater inflater = LayoutInflater.from(MainUser.this);
            final int view = msg.getData().getInt("View");
            LinearLayout i;
            final LinearLayout linearLayout;
            switch (view) {
                case 1:
                    LoginInfo.Dialog.show();
                    i = (LinearLayout) inflater.inflate(R.layout.mfullinfo, null).findViewById(R.id.mFullInfo);
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    linearLayout.addView(i);
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
                                t.setText(LoginInfo.mUserData.Name + "," + LoginInfo.mUserData.y_xbdm + "\n" + LoginInfo.mUserData.y_xdm + LoginInfo.mUserData.nd + "级学生\n学号:" + LoginInfo.mUserData
                                        .y_xh + "\n" + LoginInfo.mUserData.ClassName + Leader + "\n" + "当前处于" + LoginInfo.mUserData.Term + "学期.\n政治面貌:" + LoginInfo.mUserData.y_zzmmdm + ",加入时间:" +
                                        LoginInfo.mUserData.y_zzmmsj + "\n所在地区:" + LoginInfo.mUserData.y_syszddm + "\n高中毕业于" + LoginInfo.mUserData.y_byzx);
                            }
                            LoginInfo.Dialog.cancel();
                        }
                    };
                    Hand1.sendEmptyMessage(0);
                    break;
                case 2:
                    LoginInfo.Dialog.show();
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    //移除所有控件,并准备加入新的控件
                    i = (LinearLayout) inflater.inflate(R.layout.mdaysview, null).findViewById(R.id.mDaysListViewLinearLayout);
                    linearLayout.addView(i);
                    final ListView ls = (ListView) findViewById(R.id.mDaysListView);
                    final Handler UpdateView = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            //判斷是否為空,若为空则初始化一下防止抛异常
                            if (LoginInfo.mUserData.HolidaysEume == null) {
                                LoginInfo.mUserData.HolidaysEume = new ArrayList<Map<String, Object>>();
                            }
                            ls.setAdapter(new SimpleAdapter(MainUser.this, LoginInfo.mUserData.HolidaysEume, R.layout.listviewadapteritem, new String[]{"mItemIndex", "mItem_HolidayBecause",
                                    "mItem_HolidayTime", "mItem_WhereOutSide", "mItemAcceptState"}, new int[]{R.id.mItemIndex, R.id.mItem_HolidayBecause, R.id.mItem_HolidayTime, R.id
                                    .mItem_WhereOutSide, R.id.mItemAcceptState}));
                            ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    //TODO:写下点击事件
                                    //似乎不需要写,因为这个只是起到一个查阅的作用.
                                }
                            });
                        }
                    };
                    final Handler hand2 = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            Spinner spinner = (Spinner) findViewById(R.id.mStudentSelect);
                            spinner.setVisibility(View.GONE);
                            //判斷是否為空,若为空则初始化一下防止抛异常
                            if (LoginInfo.mUserData.ClassMates == null) {
                                LoginInfo.mUserData.ClassMates = new ArrayList<String>();
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(MainUser.this, android.R.layout.simple_spinner_item, LoginInfo.mUserData.ClassMates);
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
                                    final String[] mTemps = t.split(" ");


                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                LoginInfo.aolanEx.getStudentHolidaysInfo(mTemps[0], mTemps[1]);
                                                UpdateView.sendEmptyMessage(0);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    Toast.makeText(MainUser.this, "你点击的表项是不存在的.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            UpdateView.sendEmptyMessage(0);//无论什么情况都更新请假数据
                            //TODO:这里设置默认选项,仅针对班级管理员
                            if (LoginInfo.mUserData.ClassLeader != null && LoginInfo.mUserData.ClassMates.size() > 0) {
                                spinner.setSelection(LoginInfo.mUserData.ItemSelection);
                                spinner.setVisibility(View.VISIBLE);
                            } else {
                                ((TextView) findViewById(R.id.PleaseChoiceInfo)).setText("点击下方按钮即可申请请假");
                            }
                            Button RequestButton = (Button) findViewById(R.id.mRequestHoliday);
                            RequestButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LoginInfo.Dialog.show();
                                    LoginInfo.IsRequestViews = true;//设置返回键返回上一页
                                    linearLayout.removeAllViews();
                                    LinearLayout newLinearLayout = (LinearLayout) inflater.inflate(R.layout.holidaysrequest, null).findViewById(R.id.MainXML);
                                    linearLayout.addView(newLinearLayout);
                                    final Handler TempsHandle = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            super.handleMessage(msg);
                                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(MainUser.this, android.R.layout.simple_spinner_item, LoginInfo.mUserData.CategoryHolidays);
                                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            final Spinner Request_HolidaysCategory = (Spinner) findViewById(R.id.Request_HolidaysCategory);
                                            Request_HolidaysCategory.setAdapter(arrayAdapter);
                                            Request_HolidaysCategory.setSelection(1);//设置默认为事假


                                            //处理编辑框事件
                                            final Calendar calendar = Calendar.getInstance();
                                            final String Temps_Time = String.valueOf(calendar.get(Calendar.YEAR)) + "" + "." + String.valueOf(calendar.get(Calendar.MONTH)) + "." + String.valueOf
                                                    (calendar.get(Calendar.DAY_OF_MONTH));

                                            //计算天数
                                            final EditText HolidaysDay = (EditText) findViewById(R.id.Request_HolidaysDays);
                                            final EditText BackToSchoolTime = (EditText) findViewById(R.id.Request_BackToSchoolTime);
                                            final EditText RequestTime = (EditText) findViewById(R.id.Request_HolidaysTime);


                                            RequestTime.setText(Temps_Time);
                                            BackToSchoolTime.setText(Temps_Time);
                                            HolidaysDay.setText("1");

                                            RequestTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                                @Override
                                                public void onFocusChange(View v, boolean hasFocus) {
                                                    if (hasFocus) {

                                                    } else {
                                                        String Res = LoginInfo.CalculationHoliday_Days(RequestTime.getText().toString(), BackToSchoolTime.getText().toString());
                                                        if (Res == null) {
                                                            HolidaysDay.setText("1");
                                                            RequestTime.setText(BackToSchoolTime.getText().toString());
                                                            Toast.makeText(MainUser.this, "时间可能是负数吗?!你不要搞事情我和你讲.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            HolidaysDay.setText(Res);
                                                        }

                                                    }
                                                }
                                            });
                                            BackToSchoolTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                                @Override
                                                public void onFocusChange(View v, boolean hasFocus) {
                                                    if (hasFocus) {

                                                    } else {
                                                        String Res = LoginInfo.CalculationHoliday_Days(RequestTime.getText().toString(), BackToSchoolTime.getText().toString());
                                                        if (Res == null) {
                                                            HolidaysDay.setText("1");
                                                            BackToSchoolTime.setText(RequestTime.getText().toString());
                                                            Toast.makeText(MainUser.this, "时间可能是负数吗?!你不要搞事情我和你讲.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            HolidaysDay.setText(Res);
                                                        }
                                                    }
                                                }
                                            });


                                            //设置原因编辑框
                                            EditText Reasons = (EditText) findViewById(R.id.Request_Reason);
                                            Reasons.setFocusable(true);//设置焦点
                                            Reasons.requestFocus();
                                            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.Request_HolidaysOK_progressBar);
                                            final Button RequestHolidaysOK = (Button) findViewById(R.id.Request_HolidaysOK);
                                            final LinearLayout linearLayouts = (LinearLayout) findViewById(R.id.ButtonControl);

                                            RequestHolidaysOK.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    progressBar.setVisibility(View.VISIBLE);
                                                    linearLayouts.setVisibility(View.GONE);

                                                    final Handler Temps_Hand = new Handler() {
                                                        @Override
                                                        public void handleMessage(Message msg) {
                                                            super.handleMessage(msg);
                                                            //TODO:这里写UI事件
                                                            progressBar.setVisibility(View.GONE);
                                                            linearLayouts.setVisibility(View.VISIBLE);
                                                            if (LoginInfo.ErrCode == 1) {
                                                                Toast.makeText(MainUser.this, "请假记录提交成功!请等待老师审核!", Toast.LENGTH_LONG).show();
                                                                SwitchViews.sendMessage(SwitchView(2));
                                                                LoginInfo.IsRequestViews = false;
                                                            } else {
                                                                Toast.makeText(MainUser.this, "请假记录提交失败!" + LoginInfo.Result, Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    };
                                                    new Thread() {
                                                        @Override
                                                        public void run() {
                                                            //TODO:这里写网络请求
                                                            String[] Temp_Str = LoginInfo.mUserData.CategoryHolidays.get(Request_HolidaysCategory.getSelectedItemPosition()).split("\\|");

                                                            String Reasons = ((EditText) findViewById(R.id.Request_Reason)).getText().toString();
                                                            String OutAdress = ((EditText) findViewById(R.id.OutAddress)).getText().toString();
                                                            String ContactInformation = ((EditText) findViewById(R.id.Request_ContactInformation)).getText().toString();
                                                            String BackToSchoolTime = ((EditText) findViewById(R.id.Request_BackToSchoolTime)).getText().toString();
                                                            String HolidaysDay = ((EditText) findViewById(R.id.Request_HolidaysDays)).getText().toString();
                                                            String CaoZuoTime = String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH)) + "-" + String
                                                                    .valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf
                                                                    (calendar.get(Calendar.MINUTE)) + ":" + String.valueOf(calendar.get(Calendar.SECOND));//
                                                            // "2017-4-23 16:21:11"
                                                            Map<String, String> map = LoginInfo.aolanEx.BundleData(LoginInfo.mUserData.y_xh, LoginInfo.mUserData.Name, RequestTime.getText().toString
                                                                    (), Temp_Str[1], Temp_Str[0], Reasons, OutAdress, ContactInformation, BackToSchoolTime, HolidaysDay, CaoZuoTime);
                                                            try {
                                                                LoginInfo.ErrCode = LoginInfo.aolanEx.Request_Holidays(map);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            Temps_Hand.sendEmptyMessage(0);
                                                        }
                                                    }.start();
                                                }
                                            });
                                            LoginInfo.Dialog.cancel();
                                        }
                                    };
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
//                                                //优化性能,加载过一次就不需要再次加载数据
//                                                if (LoginInfo.mUserData.CategoryHolidays.isEmpty() == true) {
                                                LoginInfo.aolanEx.Init_Holidays_xzdm();
//                                                }
                                                while (LoginInfo.ErrCode == 0) {
                                                    try {
                                                        Thread.sleep(100);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                TempsHandle.sendEmptyMessage(0);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            });
                            LoginInfo.Dialog.cancel();
                        }
                    };
                    LoginInfo.aolanEx.getClassMatesInfo();
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
                    InitLongHolidaysView(inflater);
                    break;
                case 4:
                    linearLayout = (LinearLayout) findViewById(R.id.mViews);
                    linearLayout.removeAllViews();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 子程序.初始化长假请假视图数据
     *
     * @param inflater
     */
    private void InitLongHolidaysView(final LayoutInflater inflater) {
        LoginInfo.mUserData.HolidaysEume = new ArrayList<Map<String, Object>>();
        LoginInfo.mUserData.ClassMates = new ArrayList<>();
        LoginInfo.Dialog.show();
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mViews);
        linearLayout.removeAllViews();
        //移除所有控件,并准备加入新的控件
        LinearLayout i = (LinearLayout) inflater.inflate(R.layout.holidays_long, null).findViewById(R.id.holidasLong);
        linearLayout.addView(i);

        //预定义时间
        final Calendar calendar = Calendar.getInstance();

        //更新视图数据
        final Handler UpdataView = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //判斷是否為空,若为空则初始化一下防止抛异常
                if (LoginInfo.mUserData.HolidaysEume == null) {
                    LoginInfo.mUserData.HolidaysEume = new ArrayList<Map<String, Object>>();
                }
                ((ListView) findViewById(R.id.mDaysListView_holidays_long)).setAdapter(new SimpleAdapter(MainUser
                        .this, LoginInfo.mUserData.HolidaysEume, R.layout.listviewadapteritem, new String[]{"mItemIndex", "mItem_HolidayBecause", "mItem_HolidayTime", "mItem_WhereOutSide",
                        "mItemAcceptState"}, new int[]{R.id.mItemIndex, R.id.mItem_HolidayBecause, R.id.mItem_HolidayTime, R.id.mItem_WhereOutSide, R.id.mItemAcceptState}));
                //下面开始设置Spiner控件
                Spinner spinner = (Spinner) findViewById(R.id.mStudentSelect_holidays_long);
                spinner.setVisibility(View.GONE);
                //判斷是否為空,若为空则初始化一下防止抛异常

                final Handler Temp_hand = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        //判斷是否為空,若为空则初始化一下防止抛异常
                        if (LoginInfo.mUserData.HolidaysEume == null) {
                            LoginInfo.mUserData.HolidaysEume = new ArrayList<Map<String, Object>>();
                        }
                        ((ListView) findViewById(R.id.mDaysListView_holidays_long)).setAdapter(new SimpleAdapter(MainUser
                                .this, LoginInfo.mUserData.HolidaysEume, R.layout.listviewadapteritem, new String[]{"mItemIndex", "mItem_HolidayBecause", "mItem_HolidayTime", "mItem_WhereOutSide",
                                "mItemAcceptState"}, new int[]{R.id.mItemIndex, R.id.mItem_HolidayBecause, R.id.mItem_HolidayTime, R.id.mItem_WhereOutSide, R.id.mItemAcceptState}));

                        ((EditText) findViewById(R.id.long_ClassMatesContactInfomation)).setText("家庭电话:" + LoginInfo.mUserData.HousePhoneNum + "\n个人电话:" + LoginInfo.mUserData.MySelfPhoneNum);
                    }
                };

                if (LoginInfo.mUserData.ClassMates != null && LoginInfo.mUserData.ClassMates.size() > 0) {
                    ArrayAdapter arrayAdapter = new ArrayAdapter<String>(MainUser.this, android.R.layout.simple_spinner_item, LoginInfo.mUserData.ClassMates);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdapter);
                    //TODO:因为是管理员嘛,所以显示这些额外的东西
                    spinner.setVisibility(View.VISIBLE);
                    findViewById(R.id.long_ClassMatesContactInfomation).setVisibility(View.VISIBLE);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String t;
                            t = LoginInfo.mUserData.ClassMates.get(position);
                            Toast.makeText(MainUser.this, t, Toast.LENGTH_SHORT).show();
                            final String[] mTemps = t.split(" ");
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        LoginInfo.aolanEx.QueryHolidaysLongClassMatesInfomation(mTemps[0], mTemps[1], "五一", "04");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    while (LoginInfo.ErrCode == 0) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Temp_hand.sendEmptyMessage(0);
                                }
                            }.start();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    spinner.setSelection(LoginInfo.mUserData.ItemSelection);
                    //上方是Spinner控件的简要设置
                } else {
                    ((TextView) findViewById(R.id.PleaseChoiceInfo_holidays_long)).setText("点击下方按钮即可申请请假");
                }
                //TODO:这里直接执行一遍数据
                Temp_hand.sendEmptyMessage(0);

                //设置申请请假按钮被点击Handler事件
                final Handler Temp_HandUpdataView = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(MainUser.this, android.R.layout.simple_spinner_item, LoginInfo.mUserData.CategoryHolidays);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ((Spinner) findViewById(R.id.long_holidays)).setAdapter(arrayAdapter);
                        arrayAdapter = new ArrayAdapter<String>(MainUser.this, android.R.layout.simple_spinner_item, LoginInfo.mUserData.WithOutCategory);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ((Spinner) findViewById(R.id.long_holidaysToGo)).setAdapter(arrayAdapter);

                        //数据获取完毕,开始设置一些信息
                        ((TextView) findViewById(R.id.long_TextView_RoomsNum)).setText(LoginInfo.mUserData.RoomsID);
                        ((TextView) findViewById(R.id.long_TextView_HousePhoneNum)).setText(LoginInfo.mUserData.HousePhoneNum);
                        ((TextView) findViewById(R.id.long_TextView_YouSelfCallNum)).setText(LoginInfo.mUserData.MySelfPhoneNum);

                        // TODO: 2017/4/24  设置节假日三个时间编辑框默认值
                        ((EditText) findViewById(R.id.long_OutOfSchoolTime)).setText(String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH)) + "-" + String
                                .valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                        ((EditText) findViewById(R.id.long_WillBackOfSchool)).setText(String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH)) + "-几号?");
                        ((EditText) findViewById(R.id.long_RealBackToSchoolTime)).setText(String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH)) + "-几号?");

                        //提交按钮事件的实现
                        findViewById(R.id.long_HolidaysOK).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findViewById(R.id.long_HolidaysOK_progressBar).setVisibility(View.VISIBLE);
                                findViewById(R.id.long_HolidaysOK).setVisibility(View.GONE);
                                final Handler Temps_Hand = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        //TODO:这里写UI事件
                                        findViewById(R.id.long_HolidaysOK_progressBar).setVisibility(View.GONE);
                                        findViewById(R.id.long_HolidaysOK).setVisibility(View.VISIBLE);
                                        switch (LoginInfo.ErrCode) {
                                            case 1:
                                                Toast.makeText(MainUser.this, "请假记录提交成功!请等待老师审核!", Toast.LENGTH_LONG).show();
                                                SwitchViews.sendMessage(SwitchView(3));
                                                LoginInfo.IsLongRequestViews = false;
                                                break;
                                            case -1:
                                                Toast.makeText(MainUser.this, "请假记录提交失败!" + LoginInfo.Result, Toast.LENGTH_LONG).show();
                                                break;
                                            case 3:
                                                Toast.makeText(MainUser.this, "请假记录提交失败!没有发现有效的网络连接!", Toast.LENGTH_LONG).show();
                                                break;
                                        }
                                    }
                                };
                                new Thread() {
                                    @Override
                                    public void run() {
                                        //TODO:这里写网络请求
                                        Spinner long_Holidays = (Spinner) findViewById(R.id.long_holidays);
                                        Spinner long_GoCategory = (Spinner) findViewById(R.id.long_holidaysToGo);

                                        //获取请假类别
                                        String[] CategoryHolidays = LoginInfo.mUserData.CategoryHolidays.get(long_Holidays.getSelectedItemPosition()).split("\\|");

                                        //获取去向类别
                                        String[] WithOutCategory = LoginInfo.mUserData.WithOutCategory.get(long_GoCategory.getSelectedItemPosition()).split("\\|");

                                        String long_OutAddress = ((EditText) findViewById(R.id.long_OutAddress)).getText().toString();
                                        String long_OutOfSchoolTime = ((EditText) findViewById(R.id.long_OutOfSchoolTime)).getText().toString();
                                        String long_WillBackOfSchool = ((EditText) findViewById(R.id.long_WillBackOfSchool)).getText().toString();
                                        String long_RealBackToSchoolTime = ((EditText) findViewById(R.id.long_RealBackToSchoolTime)).getText().toString();

                                        String CaoZuoTime = String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH)) + "-" + String.valueOf(calendar.get
                                                (Calendar.DAY_OF_MONTH)) + " " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                                                String.valueOf(calendar.get(Calendar.SECOND));// "2017-4-23
                                        // 16:21:11"

                                        try {
                                            LoginInfo.ErrCode = LoginInfo.aolanEx.HolidaysDays_Long(CategoryHolidays[1], CategoryHolidays[0], WithOutCategory[1], WithOutCategory[0],
                                                    long_OutAddress, long_OutOfSchoolTime, long_WillBackOfSchool, long_RealBackToSchoolTime, "操作时间:" + CaoZuoTime);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        //TODO:这里把请长假的逻辑搞定了
                                        Temps_Hand.sendEmptyMessage(0);
                                    }
                                }.start();
                            }
                        });

                        LoginInfo.Dialog.cancel();
                    }
                };
                //下面高能
                findViewById(R.id.mRequestHoliday_holidays_long).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginInfo.Dialog.show();
                        LoginInfo.IsLongRequestViews = true;//设置返回键返回上一页
                        linearLayout.removeAllViews();
                        LinearLayout newLinearLayout = (LinearLayout) inflater.inflate(R.layout.long_holidaysrequests, null).findViewById(R.id.long_HolidaysView);
                        linearLayout.addView(newLinearLayout);
                        //TODO:这里是线程了
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    LoginInfo.aolanEx.Init_HolidaysLong(1);
                                    LoginInfo.aolanEx.Init_HolidaysLong(2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                while (LoginInfo.ErrCode == 0) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Temp_HandUpdataView.sendEmptyMessage(0);
                            }
                        }.start();
                    }
                });
                LoginInfo.Dialog.cancel();
            }
        };

        //上方Handle定义完毕,下方线程启动,开始判断并呼叫上方Handle
        new Thread() {
            @Override
            public void run() {
                try {
                    LoginInfo.aolanEx.get_Holidays_LongTime();//获取数据
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (LoginInfo.ErrCode == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                UpdataView.sendEmptyMessage(0);
            }
        }.start();
    }


    //实现双击返回建退出的方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (LoginInfo.IsRequestViews) {
                    SwitchViews.sendMessage(SwitchView(2));
                    LoginInfo.IsRequestViews = false;
                } else if (LoginInfo.IsLongRequestViews) {
                    SwitchViews.sendMessage(SwitchView(3));
                    LoginInfo.IsLongRequestViews = false;
                } else {
                    long secondT = System.currentTimeMillis();
                    if (secondT - BackTime > 2000) {
                        Toast.makeText(this, "再次点击返回键退出", Toast.LENGTH_SHORT).show();
                        BackTime = secondT;
                    } else {
                        finish();
                        System.exit(0);
                    }
                }
                return true;
            default:
                return true;
        }
    }
}
