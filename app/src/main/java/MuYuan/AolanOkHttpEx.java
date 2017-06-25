package MuYuan;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

import com.example.qiuchen.myapplication.MainUser;
import com.example.qiuchen.myapplication.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.lzy.okgo.model.HttpHeaders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.MediaType;
import okhttp3.Response;

import static com.lzy.okgo.utils.OkLogger.debug;

/**
 * This Code Is Created by QiuChenly on 2017/4/21 16:00.
 */

public class AolanOkHttpEx {

    public String _viewstate = "";
    public String _viewStategenerator = "";
    private PersistentCookieStore CookieStore = new PersistentCookieStore ();
    Context ct = null;

    /**
     * 取文本中间方法
     *
     * @param AllString  所有文本
     * @param left       左边文本
     * @param Right      右边文本
     * @param StartIndex 可空,起始值
     * @return中间文本
     */
    public String GetSubText (String AllString, String left, String Right, int StartIndex) {
        int index = AllString.indexOf (left, StartIndex) + left.length ();
        return AllString.substring (index, AllString.indexOf (Right, index));
    }

    /**
     * 更新必须参数
     *
     * @param ResponseBody 返回的网页数据
     */
    public void UpdataViewState (String ResponseBody) {
        _viewstate = GetSubText (ResponseBody, "id=\"__VIEWSTATE\" value=\"", "\"", 0);
        _viewStategenerator = GetSubText (ResponseBody, "id=\"__VIEWSTATEGENERATOR\" value=\"", "\"", 0);
    }

    public static String md5 (String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance ("MD5").digest (string.getBytes ("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException ("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException ("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder (hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append ("0");
            hex.append (Integer.toHexString (b & 0xFF));
        }
        return hex.toString ();
    }


    /**
     * 直接登录系统
     *
     * @param userid   学号
     * @param PassWord 密码
     * @return 参考返回值, 直接回调 LoginInfo.ErrCode 即可
     */
    public int login (Context cts, String userid, String PassWord) {
        ct = cts;
        OkGo.getInstance ()
                //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
                .setCacheTime (CacheEntity.CACHE_NEVER_EXPIRE)

                //可以全局统一设置超时重连次数,默认为三次,那么最差的情况会请求4次(一次原始请求,三次重连请求),不需要可以设置为0
                .setRetryCount (3)

                //如果不想让框架管理cookie（或者叫session的保持）,以下不需要
                //.setCookieStore(memoryCookieStore)            //cookie使用内存缓存（app退出后，cookie消失）
                //memoryCookieStore=内存保持  persistentCookieStore=持久化存储  建议使用后者
                .setCookieStore (CookieStore)        //cookie持久化存储，如果cookie不过期，则一直有效

                //可以设置https的证书,以下几种方案根据需要自己设置
                .setCertificates ();
        LoginInfo.ErrCode = 0;
        OkGo.post ("http://alst.jsahvc.edu.cn/login.aspx")     // 请求方式和请求url
                .cacheKey ("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode (CacheMode.DEFAULT)    // 缓存模式，详细请看缓存介绍
                .params ("__VIEWSTATE", "").params ("__VIEWSTATEGENERATOR", "").params ("userbh", userid).params ("pass", md5 (PassWord)).params ("vcode", "").params ("xzbz", "1").execute (new StringCallback () {
            @Override
            public void onSuccess (String s, Call call, Response response) {
                System.out.print (s);
                // s 即为所需要的结果
                UpdataViewState (s);//更新参数
                //是否为班干部
                if (s.indexOf ("班干部") != - 1) {
                    LoginInfo.mUserData.ClassLeader = true;
                } else {
                    LoginInfo.mUserData.ClassLeader = false;
                }
                int val = s.indexOf ("><b>欢迎你:");
                String Temps = "";
                if (val != - 1) {
                    Temps = GetSubText (s, "><b>欢迎你:", "\n", 0).trim ();
                    LoginInfo.mUserData.Name = Temps;
                    LoginInfo.ErrCode = 1;
                } else {
                    Temps = s;
                    LoginInfo.ErrCode = - 1;
                }
                LoginInfo.Result = Temps;
            }
        });
        return LoginInfo.ErrCode;
    }

    /**
     * 获取个人简要信息参数 (由于GetFullMyInfo()的关系,本方法即将废弃)
     */
    public void GetMyInfo () {
        String url = "http://alst.jsahvc.edu.cn/txxm/default.aspx?dfldm=01";
        LoginInfo.ErrCode = 0;
        OkGo.get (url)     // 请求方式和请求url
                //.tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .cacheKey ("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode (CacheMode.DEFAULT)    // 缓存模式，详细请看缓存介绍
                .execute (new StringCallback () {
                    @Override
                    public void onSuccess (String s, Call call, Response response) {
                        // s 即为所需要的结果
                        String Res = s;
                        //获取参数 nd 和 学期
                        LoginInfo.mUserData.nd = GetSubText (Res, "type=\"hidden\" id=\"nd\" value=\"", "\"", 0);
                        LoginInfo.mUserData.Term = GetSubText (Res, "<option value=\"", "\">", 0);
                        LoginInfo.ErrCode = 1;
                    }

                    @Override
                    public void onError (Call call, Response response, Exception e) {
                        super.onError (call, response, e);
                        LoginInfo.ErrCode = - 1;
                    }
                });
    }


    /**
     * 查询完全的个人数据
     * 不返回数据
     */
    public void GetFullMyInfo () {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jbxg.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd;
        LoginInfo.ErrCode = 0;
        OkGo.get (url)     // 请求方式和请求url
                //.tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .cacheKey ("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode (CacheMode.DEFAULT)    // 缓存模式，详细请看缓存介绍
                .execute (new StringCallback () {
                    @Override
                    public void onSuccess (String s, Call call, Response response) {
                        // s 即为所需要的结果
                        String Res = s;
                        LoginInfo.mUserData.xdm = GetSubText (Res, "type=\"hidden\" id=\"xdm\" value=\"", "\"", 0);
                        LoginInfo.mUserData.ClassName = GetSubText (Res, "type=\"hidden\" id=\"bjhm\" value=\"", "\"", 0);
                        LoginInfo.mUserData.y_byzx = GetSubText (Res, "id=\"y_byzx\">", "</span>", 0);
                        LoginInfo.mUserData.y_cell = GetSubText (Res, "id=\"y_cell\">", "</span>", 0);
                        LoginInfo.mUserData.y_cwh = GetSubText (Res, "id=\"y_cwh\">", "</span>", 0);
                        LoginInfo.mUserData.y_email = GetSubText (Res, "id=\"y_email\">", "</span>", 0);
                        LoginInfo.mUserData.y_hkxzdm = GetSubText (Res, "id=\"y_hkxzdm\">", "</span>", 0);
                        LoginInfo.mUserData.y_jtdz = GetSubText (Res, "id=\"y_jtdz\">", "</span>", 0);
                        LoginInfo.mUserData.y_ksh = GetSubText (Res, "id=\"y_ksh\">", "</span>", 0);
                        LoginInfo.mUserData.y_mzdm = GetSubText (Res, "id=\"y_mzdm\">", "</span>", 0);
                        LoginInfo.mUserData.y_qq = GetSubText (Res, "id=\"y_qq\">", "</span>", 0);
                        LoginInfo.mUserData.y_sshm = GetSubText (Res, "id=\"y_sshm\">", "</span>", 0);
                        LoginInfo.mUserData.y_syszddm = GetSubText (Res, "id=\"y_syszddm\">", "</span>", 0);
                        LoginInfo.mUserData.y_xbdm = GetSubText (Res, "id=\"y_xbdm\">", "</span>", 0);
                        LoginInfo.mUserData.y_xdm = GetSubText (Res, "id=\"y_xdm\">", "</span>", 0);
                        LoginInfo.mUserData.y_xh = GetSubText (Res, "id=\"y_xh\">", "</span>", 0);
                        LoginInfo.mUserData.y_xz = GetSubText (Res, "id=\"y_xz\">", "</span>", 0);
                        LoginInfo.mUserData.y_yhzh = GetSubText (Res, "id=\"y_yhzh\">", "</span>", 0);
                        LoginInfo.mUserData.y_zzmmdm = GetSubText (Res, "id=\"y_zzmmdm\">", "</span>", 0);
                        LoginInfo.mUserData.y_zzmmsj = GetSubText (Res, "id=\"y_zzmmsj\">", "</span>", 0);
                        UpdataViewState (Res);
                        LoginInfo.ErrCode = 1;
                    }

                    @Override
                    public void onError (Call call, Response response, Exception e) {
                        super.onError (call, response, e);
                        LoginInfo.ErrCode = - 1;
                    }
                });
    }


    /**
     * 获取学生和我自己的日常请假数据
     */
    public void getClassMatesInfo () {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";

        OkGo.get (url)     // 请求方式和请求url
                //.tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .cacheKey ("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode (CacheMode.DEFAULT)    // 缓存模式，详细请看缓存介绍
                .execute (new StringCallback () {
                    @Override
                    public void onSuccess (String s, Call call, Response response) {
                        if (s.indexOf ("初始密码为身份证号后六位") != - 1) {
                            login (ct, LoginInfo.mUserData.UserNum, LoginInfo.mUserData.Password);
                            getClassMatesInfo ();
                            return;
                        }
                        // s 即为所需要的结果
                        String Res = s;
                        UpdataViewState (Res);
                        Pattern p = Pattern.compile ("<option value=\"(.*?)\">(.*?)</option>");
                        Matcher m = p.matcher (Res);
                        LoginInfo.mUserData.ClassMates = new ArrayList<String> ();//初始化班级同学数据
                        System.out.print (Res);
                        //当前学生:201513043 陈玉奇
                        String Temps = GetSubText (Res, "当前学生:", " ", 0);//获取自己的数据
                        int ii = 0;
                        while (m.find ()) {
                            String Temp = m.group (2).toString ();
                            if (Temp.indexOf (Temps) != - 1) {//顺带寻找当前个人位置
                                LoginInfo.mUserData.ItemSelection = ii;//为了自适应Spinner控件的表项数据
                            }
                            LoginInfo.mUserData.ClassMates.add (Temp);//加入成员数据
                            ii++;//数组从0开始,故i++放在最后
                        }
                        LoginInfo.mUserData.HolidaysEume = UpdataHolidaysEume (Res);
                        LoginInfo.ErrCode = 1;
                    }

                    @Override
                    public void onError (Call call, Response response, Exception e) {
                        super.onError (call, response, e);
                        LoginInfo.ErrCode = - 1;
                    }
                });
    }

    /**
     * 获取学生日常请假信息
     *
     * @param StudentID   学号
     * @param StudentName 姓名
     * @throws IOException IO异常捕捉
     */
    public void getStudentHolidaysInfo (String StudentID, String StudentName) throws IOException {
        String url;
        url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=2016-2017-2&nd=2015&msie=1";
        String datas = "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"__EVENTTARGET\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"__EVENTARGUMENT\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"__VIEWSTATE\"\n"
                + "\n" + "{__VIEWSTATE}\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"__VIEWSTATEGENERATOR\"\n" + "\n" + "{__VIEWSTATEGENERATOR}\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"__VIEWSTATEENCRYPTED\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"sele1\"\n" + "\n" + StudentID + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qjsj\"\n" +
                "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qjsy\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"qjsydm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qjjtyy\"\n" + "\n" + "\n"
                + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"File1\"; filename=\"\"\n" + "Content-Type: application/octet-stream\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"fjm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: " +
                "" + "" + "" + "" + "" + "" + "" + "" + "form-data; name=\"wcdz\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; " +
                "name=\"jzxmlxfs\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"nhxsj\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qjts\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition:" +
                " form-data; name=\"xjrq\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"czsj\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition:" + " form-data; name=\"fdyspyj\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"fdyspyjdm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"yxspyj\"\n" + "\n" +
                "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"yxspyjdm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xjspyj\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xjspyjdm\"\n" + "\n" +
                "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"km\"\n" + "\n" + "st_xsqj\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"y_km\"\n" + "\n" + "st_xsqj\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pzd\"\n" + "\n" +
                "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj," + "fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pzd_c\"\n" + "\n" + "sqpzck,\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"pzd_lock\"\n" + "\n" + "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr," + "" + "" + "" + "" + "" + "" + "" + "yxspsj,xjspyjdm,xjspr," +
                "xjspsj,\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pzd_lock2\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"pzd_lock3\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pzd_lock4\"\n" + "\n" +
                "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pzd_y\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xdm\"\n" + "\n" + LoginInfo.mUserData.xdm + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; " +
                "name=\"bjhm\"\n" + "\n" + LoginInfo.mUserData.ClassName + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xh\"\n" + "\n" + StudentID +
                "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xm\"\n" + "\n" + StudentName + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"qx_i\"\n" + "\n" + "1\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qx_u\"\n" + "\n" + "1\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qx_d\"\n" + "\n" + "0\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"qx2_r\"\n" + "\n" + "1\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qx2_i\"\n" + "\n" + "0\n"
                + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"qx2_u\"\n" + "\n" + "0\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"qx2_d\"\n" + "\n" + "0\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"databcxs\"\n" + "\n" +
                "1\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"databcdel\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xzbz\"\n" + "\n" + "t\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pkey\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pkey4\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xs_bj\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"bdbz\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition:" + " form-data; name=\"cw\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"hjzd\"\n" + "\n" + "," + "NHXSJ,QJSJ,QJTS,\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; " +
                "name=\"st_xq\"\n" + "\n" + LoginInfo.mUserData.Term + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"st_nd\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"mc\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: " +
                "form-data; name=\"smbz\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"fjmf\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition:" + " form-data; name=\"psrc\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"pa\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pb\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: " + "form-data; name=\"pc\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"pd\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"pe\"\n" + "\n" + "\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: " + "form-data; name=\"pf\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"msie\"\n" + "\n" + "1\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"txxmxs\"\n" + "\n" +
                StudentID + " " + StudentName + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"tkey\"\n" + "\n" + "qjsj\n" +
                "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"tkey4\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xp_pmc\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xp_pval\"\n" + "\n" + "\n"
                + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xp_plx\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xp_pkm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xp_pzd\"\n" + "\n" + "\n"
                + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xp_pjxjdm\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" +
                "Content-Disposition: form-data; name=\"xp_ipbz\"\n" + "\n" + "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f\n" + "Content-Disposition: form-data; name=\"xp_pjxjdm2\"\n" + "\n" +
                "\n" + "------WebKitFormBoundarymAMsOSk5Z6IB3N2f--";
        datas = datas.replace ("{__VIEWSTATE}", _viewstate).replace ("{__VIEWSTATEGENERATOR}", _viewStategenerator);
        String Cookie = getCookies ();
        ResponseData r = HttpUtils.POST (url, datas, Cookie, "multipart/form-data; boundary=----WebKitFormBoundarymAMsOSk5Z6IB3N2f");
        UpdataViewState (r.ResponseText);
        LoginInfo.mUserData.HolidaysEume = UpdataHolidaysEume (r.ResponseText);
    }

    /**
     * 序列化Cookie为文本型数据
     *
     * @return
     */
    private String getCookies () {
        List<Cookie> Cookielist = CookieStore.getAllCookie ();
        String Cook = "";
        for (Cookie m : Cookielist) {
            Cook = Cook + m.name () + "=" + m.value () + ";";
        }
        return Cook;
    }


    /**
     * 更新获取当前页面请假数据
     *
     * @param string 网页数据
     * @return 返回适配好的ListMap
     */
    public List<Map<String, Object>> UpdataHolidaysEume (String string) {
        List<Map<String, Object>> mList1 = new ArrayList<Map<String, Object>> ();
        Pattern p = Pattern.compile ("</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>");
        Matcher m = p.matcher (string);
        int i = 0;//重设计数器
        while (m.find ()) {
            String Temp = "等待通过";
            if (m.group (12).indexOf ("√") != - 1)//发现一个怪现象,他不能做文本间的对比,比如说m.group(13)=="√"这样就会返回不一致的信息
            {
                Temp = "通过";
            }
            Log.d ("QiuChen", "QiuChen:" + m.group (1).trim ());
            mList1.add (HashMapEx (String.valueOf (i + 1), CheckNull (m.group (2).trim ()), "类型:" + m.group (1).trim () + " 请假时间:" + CheckNull (m.group (5)), "外出地址:" + CheckNull (m.group (3)), Temp));
            i++;
        }
        return listUnder (mList1);
    }

    /**
     * 倒序排列数据
     *
     * @param list 数据源
     * @return 排序好的数据
     */
    private List<Map<String, Object>> listUnder (List<Map<String, Object>> list) {
        //实现倒序排列,让最近的数据优先显示
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>> ();
        Log.d ("QiuChen", String.valueOf (list.size ()));
        for (int a = 1; a <= list.size (); a++) {
            Map<String, Object> map = list.get (list.size () - a);
            map.put ("mItemIndex", a);
            mList.add (map);
        }
        return mList;
    }

    private String CheckNull (String Data) {
        if (Data.length () <= 0) {
            return "无";
        } else {
            return Data;
        }
    }

    /**
     * 封装Map数据
     *
     * @param Index                序号
     * @param mItem_HolidayBecause 请假原因
     * @param mItem_HolidayTime    请假时间
     * @param mItem_WhereOutSide   外出地址
     * @param mItemAcceptState     是否同意
     * @return 封装好的Map数据
     */
    private Map<String, Object> HashMapEx (String Index, String mItem_HolidayBecause, String mItem_HolidayTime, String mItem_WhereOutSide, String mItemAcceptState) {
        Map<String, Object> map = new HashMap<String, Object> ();
        map.put ("mItemIndex", Index);
        map.put ("mItem_HolidayBecause", mItem_HolidayBecause);
        map.put ("mItem_HolidayTime", mItem_HolidayTime);
        map.put ("mItem_WhereOutSide", mItem_WhereOutSide);
        map.put ("mItemAcceptState", mItemAcceptState);
        return map;
    }

    /**
     * 初始化请假选项数据 如 病假 其他 事假 等
     *
     * @throws IOException
     */
    public void Init_Holidays_xzdm () throws IOException {
        LoginInfo.mUserData.CategoryHolidays = new ArrayList<> ();
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
//        String data = "__EVENTTARGET=" +
//                "&__EVENTARGUMENT=" +
//                "&__LASTFOCUS=" +
//                "&__VIEWSTATE=" + _viewstate +
//                "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
//                "&mh=&tj=&pdm=&pmc=qjsy&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=QJSYDM&pzd=qjjtyy&xzbz=0&ipbz=1&cwbz=&xz1v=";
        OkGo.post (url).params ("__VIEWSTATE", _viewstate).params ("__VIEWSTATEGENERATOR", _viewStategenerator).params ("pmc", "qjsy").params ("pkm", "QJSYDM").params ("pzd", "qjjtyy").params
                ("xzbz", "0").params ("ipbz", "1").execute (new StringCallback () {
            @Override
            public void onSuccess (String s, Call call, Response response) {
                Pattern p = Pattern.compile ("<span id=\".*?\">(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span id=\".*?\">(.*?)[\\s]*</span>");
                Matcher m = p.matcher (s);
                while (m.find ()) {
                    String Line = m.group (1) + "|" + m.group (2);
                    LoginInfo.mUserData.CategoryHolidays.add (Line);
                    LoginInfo.ErrCode = 1;
                }
            }
        });
    }


    /**
     * 日常请假
     *
     * @param data 学生数据
     * @return 返回结果, 1=OK -1=FAILED
     * @throws IOException IO异常捕捉
     */
    public int Request_Holidays (Map<String, String> data) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String Data = "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"__EVENTTARGET\"\n" + "\n" + "databc\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"__EVENTARGUMENT\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"__VIEWSTATE\"\n"
                + "\n" + _viewstate + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"__VIEWSTATEGENERATOR\"\n" + "\n" + _viewStategenerator + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"__VIEWSTATEENCRYPTED\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"sele1\"\n" + "\n" + data.get ("StudentID") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; " +
                "name=\"qjsj\"\n" + "\n" + data.get ("Request_Time") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qjsy\"\n" + "\n" + data.get
                ("Request_Categroy_str") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qjsydm\"\n" + "\n" + data.get ("Request_Categroy_int") +
                "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qjjtyy\"\n" + "\n" + data.get ("Request_Reason") + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"File1\"; filename=\"\"\n" + "Content-Type: application/octet-stream\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"fjm\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: " +
                "" + "" + "" + "" + "" + "" + "" + "" + "form-data; name=\"wcdz\"\n" + "\n" + data.get ("Request_OutAddress") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: " + "form-data; " + "name=\"jzxmlxfs\"\n" + "\n" + data.get ("Request_ContactInformation") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; " + "name=\"nhxsj\"\n" + "\n" + data.get ("Request_BackSchoolTime") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: "
                + "form-data; name=\"qjts\"\n" + "\n" + data.get ("Request_HolidaysDay") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xjrq\"\n" +
                "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"czsj\"\n" + "\n" + data.get ("Request_CaoZhuoTime") + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; " + "name=\"fdyspyj\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"fdyspyjdm\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"yxspyj\"\n" + "\n" +
                "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"yxspyjdm\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"xjspyj\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xjspyjdm\"\n" + "\n" +
                "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"km\"\n" + "\n" + "st_xsqj\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"y_km\"\n" + "\n" + "st_xsqj\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pzd\"\n" + "\n" +
                "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj," + "fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pzd_c\"\n" + "\n" + "sqpzck,\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"pzd_lock\"\n" + "\n" + "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr," + "yxspsj,xjspyjdm,xjspr,xjspsj,\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pzd_lock2\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"pzd_lock3\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pzd_lock4\"\n" + "\n" +
                "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pzd_y\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"xdm\"\n" + "\n" + LoginInfo.mUserData.xdm + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; " +
                "name=\"bjhm\"\n" + "\n" + LoginInfo.mUserData.ClassName + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xh\"\n" + "\n" + data.get
                ("StudentID") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xm\"\n" + "\n" + data.get ("StudentName") + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; " + "name=\"qx_i\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"qx_u\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qx_d\"\n" + "\n" + "0\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qx2_r\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"qx2_i\"\n" + "\n" + "0\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qx2_u\"\n" + "\n" + "0\n"
                + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"qx2_d\"\n" + "\n" + "0\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"databcxs\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"databcdel\"\n" + "\n" +
                "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xzbz\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"pkey\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pkey4\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xs_bj\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"bdbz\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"cw\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: " + "form-data; name=\"hjzd\"\n" + "\n" + ",NHXSJ,QJSJ,QJTS,\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"st_xq\"\n" + "\n" + LoginInfo.mUserData.Term + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; " +
                "name=\"st_nd\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"mc\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: " + "form-data; name=\"smbz\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"fjmf\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"psrc\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition:" + " form-data; name=\"pa\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"pb\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pc\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: " + "form-data; name=\"pd\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"pe\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"pf\"\n" + "\n" + "\n" +
                "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: " + "form-data; name=\"msie\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"txxmxs\"\n" + "\n" + data.get ("StudentID") + " " + data.get ("StudentName") + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"tkey\"\n" + "\n" + "qjsj\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"tkey4\"\n" + "\n" + "\n"
                + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xp_pmc\"\n" + "\n" + "qjsy\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"xp_pval\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xp_plx\"\n" + "\n" + "\n"
                + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xp_pkm\"\n" + "\n" + "QJSYDM\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"xp_pzd\"\n" + "\n" + "qjjtyy\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xp_pjxjdm\"\n" +
                "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" + "Content-Disposition: form-data; name=\"xp_ipbz\"\n" + "\n" + "1\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG\n" +
                "Content-Disposition: form-data; name=\"xp_pjxjdm2\"\n" + "\n" + "\n" + "------WebKitFormBoundary4MkGaZgofSEEJeRG--";
        String Cookie = getCookies ();
        ResponseData r = HttpUtils.POST (url, Data, Cookie, "multipart/form-data; boundary=----WebKitFormBoundary4MkGaZgofSEEJeRG");
        if (r.ResponseText.contains("增加记录成功")) {
            return 1;
        } else {
            LoginInfo.Result = GetSubText (r.ResponseText , "<input name=\"cw\" type=\"hidden\" id=\"cw\" value=\"", "\"", 0);
            return - 1;
        }
    }


    /**
     * 封装数据为Map,直接传
     *
     * @param StudentID                  学号
     * @param StudentName                学生姓名
     * @param Request_Time               请假时间
     * @param Request_Categroy_str       请假类别 其他
     * @param Request_Categroy_int       请假类别代号 99
     * @param Request_Reason             请假原因
     * @param Request_OutAddress         外出地址
     * @param Request_ContactInformation 联系方式
     * @param Request_BackSchoolTime     返校时间
     * @param Request_HolidaysDay        请假天数
     * @param Request_CaoZhuoTime        操作时间 2017-4-23 16:21:11
     * @return 返回封装好的数据
     */
    public Map<String, String> BundleData (
            String StudentID, String StudentName, String Request_Time, String Request_Categroy_str, String Request_Categroy_int, String Request_Reason, String Request_OutAddress, String
            Request_ContactInformation, String Request_BackSchoolTime, String Request_HolidaysDay, String Request_CaoZhuoTime
    ) {
        Map<String, String> data = new HashMap<String, String> ();
        data.put ("StudentID", StudentID);
        data.put ("Request_Time", Request_Time);
        data.put ("Request_Categroy_str", Request_Categroy_str);
        data.put ("Request_Categroy_int", Request_Categroy_int);
        data.put ("Request_Reason", Request_Reason);
        data.put ("Request_OutAddress", Request_OutAddress);
        data.put ("Request_ContactInformation", Request_ContactInformation);
        data.put ("Request_BackSchoolTime", Request_BackSchoolTime);
        data.put ("Request_HolidaysDay", Request_HolidaysDay);
        data.put ("Request_CaoZhuoTime", Request_CaoZhuoTime);
        data.put ("StudentName", StudentName);
        return data;
    }

    public void get_Holidays_LongTime () throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        LoginInfo.ErrCode = 0;
        OkGo.get (url).execute (new StringCallback () {
            @Override
            public void onSuccess (String s, Call call, Response response) {
                UpdataViewState (s);
                Pattern p = Pattern.compile ("<option value=\"(.*?)\">(.*?)</option>");
                Matcher m = p.matcher (s);
                LoginInfo.mUserData.ClassMates = new ArrayList<String> ();//初始化班级同学数据
                System.out.print (s);
                //当前学生:201513043 陈玉奇
                String Temps = GetSubText (s, "当前学生:", " ", 0);//获取自己的数据
                int ii = 0;
                while (m.find ()) {
                    String Temp = m.group (2);
                    if (Temp.indexOf (Temps) != - 1) {//顺带寻找当前个人位置
                        LoginInfo.mUserData.ItemSelection = ii;//为了自适应Spinner控件的表项数据
                    }
                    LoginInfo.mUserData.ClassMates.add (Temp);//加入成员数据
                    ii++;//数组从0开始,故i++放在最后
                }
                LoginInfo.mUserData.HolidaysEume = getViewByHolidaysLongInfomation (s);
                LoginInfo.mUserData.RoomsID = GetSubText (s, "<span id=\"l_sy_jbgr_sshm\">", "</span>", 0);
                LoginInfo.mUserData.HousePhoneNum = GetSubText (s, "<span id=\"l_sy_jbgr_jtdh\">", "</span>", 0);
                LoginInfo.mUserData.MySelfPhoneNum = GetSubText (s, "<span id=\"l_sy_jbgr_cell\">", "</span>", 0);
                LoginInfo.ErrCode = 1;
            }

            @Override
            public void onError (Call call, Response response, Exception e) {
                super.onError (call, response, e);
                LoginInfo.ErrCode = - 1;
            }
        });
    }


    /**
     * 查询每个学生对应的节假日请假数据
     *
     * @param ID    学号
     * @param Name  姓名
     * @param jjr   节假日名
     * @param jjrdm 代码
     * @throws IOException
     */
    public void QueryHolidaysLongClassMatesInfomation (String ID, String Name, String jjr, String jjrdm) throws IOException {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String Datas = "__EVENTTARGET=" + "&__EVENTARGUMENT=" + "&__VIEWSTATE=" + EncodeStr (_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator + "&__VIEWSTATEENCRYPTED=&sele1=" + ID +
                "&jjr=" + EncodeStr ("") + "&jjrdm=" + "" + "&qxlb=&qxlbdm=&wcdz=&lxrq=&nhxsj=&fxrq=&ptbz=&km=st_jjrqx&y_km=st_jjrqx&pzd=jjrdm%2Cqxlbdm%2Cwcdz%2Clxrq%2Cnhxsj%2Cfxrq%2Cptbz&pzd_c" +
                "=&pzd_lock=&pzd_lock2=&pzd_lock3=&pzd_lock4=&pzd_y=&xdm=" + LoginInfo.mUserData.xdm + "&bjhm=" + EncodeStr (LoginInfo.mUserData.ClassName) + "&xh=" + ID + "&xm=" + EncodeStr (Name)
                + "&qx_i=1&qx_u=1&qx_d=0&qx2_r=1&qx2_i=0&qx2_u=0&qx2_d=0&databcxs=&databcdel=&xzbz=t&pkey=04&pkey4=&xs_bj=&bdbz=&cw=&hjzd=%2CCP10%2CCP1%2CCP2%2CCP3%2CCP4%2CCP5%2CCP6%2CCP7%2CCP8" +
                "%2CCP9" + "" + "" + "" + "" + "" + "" + "%2CCPZF%2C&st_xq=" + LoginInfo.mUserData.Term + "&st_nd=&mc=&smbz=&fjmf=&psrc=&pa=&pb=&pc=&pd=&pe=&pf=&msie=1&txxmxs=" + EncodeStr (ID + " " +
                "" + "" + Name) + "&tkey=jjrdm&tkey4=&xp_pmc=jjr&xp_pval=&xp_plx=&xp_pkm=JJRDM&xp_pzd=qxlbdm&xp_pjxjdm=&xp_ipbz=1&xp_pjxjdm2=";
        String Cookie = getCookies ();
        ResponseData r = HttpUtils.POST (url, Datas, Cookie, "application/x-www-form-urlencoded");
        LoginInfo.mUserData.HolidaysEume = getViewByHolidaysLongInfomation (r.ResponseText );
        LoginInfo.mUserData.RoomsID = GetSubText (r.ResponseText , "<span id=\"l_sy_jbgr_sshm\">", "</span>", 0);
        LoginInfo.mUserData.HousePhoneNum = GetSubText (r.ResponseText , "<span id=\"l_sy_jbgr_jtdh\">", "</span>", 0);
        LoginInfo.mUserData.MySelfPhoneNum = GetSubText (r.ResponseText , "<span id=\"l_sy_jbgr_cell\">", "</span>", 0);
        LoginInfo.ErrCode = 1;
    }

    /**
     * 把汉字编码为UTF-8编码,解决报错问题
     *
     * @param str 原字符
     * @return 转换后的UTF-8字符
     * @throws UnsupportedEncodingException 异常捕捉
     */
    public String EncodeStr (String str) throws UnsupportedEncodingException {
        return URLEncoder.encode (str, "UTF-8");
    }

    /**
     * 获取请假数据 请假分类  去向类别
     *
     * @param Category 获取的信息类型,1=请假分类,如 五一节  2=去向类别,如  回家 留校 等.
     * @throws IOException
     */
    public void Init_HolidaysLong (int Category) throws IOException {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        String Data;
        if (Category == 1) {
            LoginInfo.mUserData.CategoryHolidays_Long = new ArrayList<> ();
            Data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" + EncodeStr (_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
                    "&mh=&tj=&pdm=&pmc=jjr&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=JJRDM&pzd=qxlbdm&xzbz=0&ipbz=1&cwbz=&xz1v=";
        } else {
            LoginInfo.mUserData.WithOutCategory = new ArrayList<> ();
            Data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" + EncodeStr (_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
                    "&mh=&tj=&pdm=&pmc=qxlb&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=QXLBDM&pzd=wcdz&xzbz=0&ipbz=1&cwbz=&xz1v=";
        }
        String Cookie = getCookies ();
        ResponseData r = HttpUtils.POST (url, Data, Cookie, "application/x-www-form-urlencoded");
        Pattern p = Pattern.compile ("<span id=\".*?\">(.*?)[\\s]*</span>[\\s]*</font></td><td nowrap=\"nowrap\"><font color=\"Black\">[\\s]*<span id=\".*?\">(.*?)[\\s]*</span>");
        Matcher m = p.matcher (r.ResponseText);
        while (m.find ()) {
            String Line = m.group (1) + "|" + m.group (2);
            if (Category == 1) {
                LoginInfo.mUserData.CategoryHolidays_Long.add (Line);
                System.out.print (Line);
            } else {
                LoginInfo.mUserData.WithOutCategory.add (Line);
            }
        }
        LoginInfo.ErrCode = 1;
    }

    /**
     * 封装查询到的同学们请假数据
     *
     * @param str 网页数据
     * @return 封装好的List
     */
    private List<Map<String, Object>> getViewByHolidaysLongInfomation (String str) {
        List<Map<String, Object>> mlist = new ArrayList<Map<String, Object>> ();
        Pattern p = Pattern.compile ("<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td>");
        Matcher m = p.matcher (str);
        int i = 0;
        while (m.find ()) {
            //String res = "节假日:" + m.group(1)
            //        + "去向类别:" + m.group(2)
            //        + "外出地址:" + m.group(3)
            //        + "离校日期:" + m.group(4)
            //        + "拟回校日期:" + m.group(5)
            //        + "返校日期:" + m.group(6)
            //        + "备注:" + m.group(7);
            mlist.add (HashMapEx (String.valueOf (i), "节假日:" + m.group (1) + " 去向类别:" + m.group (2), "离校日期:" + m.group (4), "外出地址:" + CheckNull (m.group (3)), ""));
        }
        return listUnder (mlist);
    }


    /**
     * 节假日请假
     *
     * @param y_jjr    节假日
     * @param y_jjrdm  节假日代码
     * @param y_qxlb   去向类别
     * @param y_qxlbdm 去向类别代码
     * @param y_wcdz   外出地址
     * @param y_lxrq   离校日期
     * @param y_nhxsj  拟回校日期
     * @param y_fxrq   返校日期
     * @param y_ptbz   备注信息
     * @return 1=OK -1=增加失败,并在全局 LoginInfo.Result 中提交错误信息 3=未知错误
     * @throws IOException 异常捕捉
     */
    public int HolidaysDays_Long (String y_jjr, String y_jjrdm, String y_qxlb, String y_qxlbdm, String y_wcdz, String y_lxrq, String y_nhxsj, String y_fxrq, String y_ptbz) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String Data = "__EVENTTARGET=databc&__EVENTARGUMENT=&__VIEWSTATE=" + EncodeStr (_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator + "&__VIEWSTATEENCRYPTED=&jjr=" + EncodeStr
                (y_jjr) + "&jjrdm=" + y_jjrdm + "&qxlb=" + EncodeStr (y_qxlb) + "&qxlbdm=" + y_qxlbdm + "&wcdz=" + EncodeStr (y_wcdz) + "&lxrq=" + y_lxrq + "&nhxsj=" + y_nhxsj + "&fxrq=" + y_fxrq +
                "&ptbz=" + EncodeStr (y_ptbz) + "&km=st_jjrqx&y_km=st_jjrqx&pzd=jjrdm%2Cqxlbdm%2Cwcdz" +
                "%2Clxrq%2Cnhxsj%2Cfxrq%2Cptbz&pzd_c=&pzd_lock=&pzd_lock2=&pzd_lock3=&pzd_lock4=&pzd_y=&xdm=" + LoginInfo.mUserData.xdm + "&bjhm=" + EncodeStr (LoginInfo.mUserData.ClassName) +
                "&xh=" + LoginInfo.mUserData.UserNum + "&xm=" + EncodeStr (LoginInfo.mUserData.Name) + "&qx_i=1&qx_u=1&qx_d=0&qx2_r=1&qx2_i=0&qx2_u=0&qx2_d=0&databcxs=1&databcdel=&" +
                "xzbz=&pkey=04&pkey4=&xs_bj=&bdbz=&cw=&hjzd=%2CCP10%2CCP1%2CCP2%2CCP3%2CCP4%2CC" + "P5%2CCP6%2CCP7%2CCP8%2CCP9%2CCPZF%2C&st_xq=" + LoginInfo.mUserData.Term +
                "&st_nd=&mc=&smbz=&fjmf=&psrc=&pa=&pb=&pc=&pd=&pe=&pf=&msie=1&txxmxs=" + LoginInfo.mUserData.UserNum + EncodeStr (" " + LoginInfo.mUserData.Name) +
                "&tkey=jjrdm&tkey4=&xp_pmc=qxlb&xp_pval=&xp_plx=&xp_pkm=QXLBDM&xp_pzd=wcdz" + "&xp_pjxjdm=&xp_ipbz=1&xp_pjxjdm2=";
        ResponseData Result = HttpUtils.POST (url, Data, getCookies (), "application/x-www-form-urlencoded");
        System.out.print (Result.ResponseText);
        if (Result == null) {
            return 3;
        }
        if (Result.ResponseText.contains("增加记录成功")) {
            return 1;
        } else {
            LoginInfo.Result = GetSubText (Result.ResponseText, "<input name=\"cw\" type=\"hidden\" id=\"cw\" value=\"", "\"", 0);
            return - 1;
        }
    }

    public String getOICQName (String Uin) throws IOException {
        String url = "http://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + Uin;
        url = HttpUtils.Get_s (url);
        url = GetSubText (url, "-1,0,0,0,\"", "\"", 0);
        return url;
    }

    public Bitmap getOICQBitMap (String Uin) {
        String url = "http://q2.qlogo.cn/headimg_dl?bs=" + Uin + "&dst_uin=" + Uin + "&dst_uin=" + Uin + "&dst_uin=" + Uin + "&spec=100&url_enc=0&referer=bu_interface&term_type=PC";
        return HttpUtils.getImageBitmap (url);
    }
}
