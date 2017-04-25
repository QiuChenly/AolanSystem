package MuYuan;

import android.content.Context;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.PersistentCookieStore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by cheny on 2017/4/21.
 */

public class AolanOkHttp {

    //本类基于OKHttp3框架封装  TM一顿问题
    //已废弃


    OkHttpClient https = null;
    CookieJarImpl cookie = null;//持久化Cookie管理
    public String _viewstate = "";
    public String _viewStategenerator = "";
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
    public String GetSubText(String AllString, String left, String Right, int StartIndex) {
        int index = AllString.indexOf(left, StartIndex) + left.length();
        return AllString.substring(index, AllString.indexOf(Right, index));
    }

    /**
     * 更新必须参数
     *
     * @param ResponseBody 返回的网页数据
     */
    public void UpdataViewState(String ResponseBody) {
        _viewstate = GetSubText(ResponseBody, "id=\"__VIEWSTATE\" value=\"", "\"", 0);
        _viewStategenerator = GetSubText(ResponseBody, "id=\"__VIEWSTATEGENERATOR\" value=\"", "\"", 0);
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public int login(Context cts, String userid, String PassWord) {

        ct = cts;
        cookie = new CookieJarImpl(new PersistentCookieStore(ct));
        //此处设置客户端初始化数据,包括Cookie自动处理
        https = new OkHttpClient.Builder()
                .cookieJar(cookie)
                //.addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        //使设置生效
        OkHttpUtils.initClient(https);

        //不多BB,直接干
        OkHttpUtils.post()
                .url("http://alst.jsahvc.edu.cn/login.aspx")
                .addParams("__VIEWSTATE", "")
                .addParams("__VIEWSTATEGENERATOR", "")
                .addParams("userbh", userid)
                .addParams("pass", md5(PassWord))
                .addParams("vcode", "")
                .addParams("xzbz", "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {

                    }

                    @Override
                    public void onResponse(String s, int i) {
                        UpdataViewState(s);//更新参数
                        //是否为班干部
                        if (s.indexOf("注销登录") != -1) {
                            LoginInfo.mUserData.ClassLeader = true;
                        } else {
                            LoginInfo.mUserData.ClassLeader = false;
                        }
                        int val = s.indexOf("><b>欢迎你:");
                        String Temps = "";
                        if (val != -1) {
                            Temps = GetSubText(s, "><b>欢迎你:", "（", 0).trim();
                            LoginInfo.mUserData.Name = Temps;
                            LoginInfo.ErrCode = 1;
                        } else {
                            Temps = s;
                            LoginInfo.ErrCode = -1;
                        }
                        LoginInfo.Result = Temps;
                    }
                });
        return LoginInfo.ErrCode;
    }

    /**
     * 获取个人简要信息参数 (由于GetFullMyInfo()的关系,本方法即将废弃)
     */
    public void GetMyInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/default.aspx?dfldm=01";
        LoginInfo.ErrCode = 0;
        OkHttpUtils.get().url(url).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int i) {
                LoginInfo.ErrCode = -1;
            }

            @Override
            public void onResponse(String s, int i) {
                String Res = s;
                //获取参数 nd 和 学期
                LoginInfo.mUserData.nd = GetSubText(Res, "type=\"hidden\" id=\"nd\" value=\"", "\"", 0);
                LoginInfo.mUserData.Term = GetSubText(Res, "<option value=\"", "\">", 0);
                LoginInfo.ErrCode = 1;
            }
        });
    }


    /**
     * 查询完全的个人数据
     * 不返回数据
     */
    public void GetFullMyInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jbxg.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd;
        LoginInfo.ErrCode = 0;
        OkHttpUtils.get().url(url).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int i) {
                LoginInfo.ErrCode = -1;
            }

            @Override
            public void onResponse(String s, int i) {
                String Res = s;
                LoginInfo.mUserData.xdm = GetSubText(Res, "type=\"hidden\" id=\"xdm\" value=\"", "\"", 0);
                LoginInfo.mUserData.ClassName = GetSubText(Res, "type=\"hidden\" id=\"bjhm\" value=\"", "\"", 0);
                LoginInfo.mUserData.y_byzx = GetSubText(Res, "id=\"y_byzx\">", "</span>", 0);
                LoginInfo.mUserData.y_cell = GetSubText(Res, "id=\"y_cell\">", "</span>", 0);
                LoginInfo.mUserData.y_cwh = GetSubText(Res, "id=\"y_cwh\">", "</span>", 0);
                LoginInfo.mUserData.y_email = GetSubText(Res, "id=\"y_email\">", "</span>", 0);
                LoginInfo.mUserData.y_hkxzdm = GetSubText(Res, "id=\"y_hkxzdm\">", "</span>", 0);
                LoginInfo.mUserData.y_jtdz = GetSubText(Res, "id=\"y_jtdz\">", "</span>", 0);
                LoginInfo.mUserData.y_ksh = GetSubText(Res, "id=\"y_ksh\">", "</span>", 0);
                LoginInfo.mUserData.y_mzdm = GetSubText(Res, "id=\"y_mzdm\">", "</span>", 0);
                LoginInfo.mUserData.y_qq = GetSubText(Res, "id=\"y_qq\">", "</span>", 0);
                LoginInfo.mUserData.y_sshm = GetSubText(Res, "id=\"y_sshm\">", "</span>", 0);
                LoginInfo.mUserData.y_syszddm = GetSubText(Res, "id=\"y_syszddm\">", "</span>", 0);
                LoginInfo.mUserData.y_xbdm = GetSubText(Res, "id=\"y_xbdm\">", "</span>", 0);
                LoginInfo.mUserData.y_xdm = GetSubText(Res, "id=\"y_xdm\">", "</span>", 0);
                LoginInfo.mUserData.y_xh = GetSubText(Res, "id=\"y_xh\">", "</span>", 0);
                LoginInfo.mUserData.y_xz = GetSubText(Res, "id=\"y_xz\">", "</span>", 0);
                LoginInfo.mUserData.y_yhzh = GetSubText(Res, "id=\"y_yhzh\">", "</span>", 0);
                LoginInfo.mUserData.y_zzmmdm = GetSubText(Res, "id=\"y_zzmmdm\">", "</span>", 0);
                LoginInfo.mUserData.y_zzmmsj = GetSubText(Res, "id=\"y_zzmmsj\">", "</span>", 0);
                UpdataViewState(Res);
                LoginInfo.ErrCode = 1;
            }
        });
    }

    public void getClassMatesInfo() {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq="
                + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        OkHttpUtils.get().url(url).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int i) {
                LoginInfo.ErrCode = -1;
            }

            @Override
            public void onResponse(String s, int i) {
                String Res = s;
                UpdataViewState(Res);
                Pattern p = Pattern.compile("<option value=\"(.*?)\">(.*?)</option>");
                Matcher m = p.matcher(Res);
                LoginInfo.mUserData.ClassMates = new ArrayList<String>();//初始化班级同学数据
                //System.out.print(Res);
                //当前学生:201513043 陈玉奇
                String Temps = GetSubText(Res, "当前学生:", " ", 0);//获取自己的数据
                int ii = 0;
                while (m.find()) {
                    String Temp = m.group(2).toString();
                    if (Temp.indexOf(Temps) != -1) {//顺带寻找当前个人位置
                        LoginInfo.mUserData.ItemSelection = ii;//为了自适应Spinner控件的表项数据
                    }
                    LoginInfo.mUserData.ClassMates.add(Temp);//加入成员数据
                    ii++;//数组从0开始,故i++放在最后
                }
                LoginInfo.mUserData.HolidaysEume = UpdataHolidaysEume(Res);
                LoginInfo.ErrCode = 1;
            }
        });
    }

    public void getStudentHolidaysInfo(String StudentID, String StudentName) {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq="
                + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        RequestBody body = builder
                .addFormDataPart("__VIEWSTATE", _viewstate)
                .addFormDataPart("__VIEWSTATEGENERATOR", _viewStategenerator)
                .addFormDataPart("sele1", StudentID)
                .addFormDataPart("km", "st_xsqj")
                .addFormDataPart("y_km", "st_xsqj")
                .addFormDataPart("pzd", "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck")
                .addFormDataPart("pzd_c", "sqpzck,")
                .addFormDataPart("pzd_lock", "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,")
                .addFormDataPart("xdm", LoginInfo.mUserData.xdm)
                .addFormDataPart("bjhm", LoginInfo.mUserData.ClassName)
                .addFormDataPart("xh", StudentID)
                .addFormDataPart("xm", StudentName)
                .addFormDataPart("qx_i", "1")
                .addFormDataPart("qx_u", "1")
                .addFormDataPart("qx_d", "0")
                .addFormDataPart("qx_r", "1")
                .addFormDataPart("qx2_i", "0")
                .addFormDataPart("qx2_u", "0")
                .addFormDataPart("qx2_d", "0")
                .addFormDataPart("qx2_r", "1")
                .addFormDataPart("xzbz", "t")
                .addFormDataPart("hjzd", ",NHXSJ,QJSJ,QJTS,")
                .addFormDataPart("st_xq", LoginInfo.mUserData.Term)
                .addFormDataPart("msie", "1")
                .addFormDataPart("txxmxs", StudentID + " " + StudentName)
                .addFormDataPart("tkey", "qjsj")
                .addFormDataPart("xzbz", "1")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        final Call call = https.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.e("QiuChen", "response ----->" + string);
            }

        });

        //SB 框架  他妈的上传未知失败


//        OkHttpUtils.post()
//                . ("__VIEWSTATE", _viewstate)
//                .addFormDataPart("__VIEWSTATEGENERATOR", _viewStategenerator)
//                .addFormDataPart("sele1", StudentID)
//                .addFormDataPart("km", "st_xsqj")
//                .addFormDataPart("y_km", "st_xsqj")
//                .addFormDataPart("pzd", "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck")
//                .addFormDataPart("pzd_c", "sqpzck,")
//                .addFormDataPart("pzd_lock", "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,")
//                .addFormDataPart("xdm", LoginInfo.mUserData.xdm)
//                .addFormDataPart("bjhm", LoginInfo.mUserData.ClassName)
//                .addFormDataPart("xh", StudentID)
//                .addFormDataPart("xm", StudentName)
//                .addFormDataPart("qx_i", "1")
//                .addFormDataPart("qx_u", "1")
//                .addFormDataPart("qx_d", "0")
//                .addFormDataPart("qx_r", "1")
//                .addFormDataPart("qx2_i", "0")
//                .addFormDataPart("qx2_u", "0")
//                .addFormDataPart("qx2_d", "0")
//                .addFormDataPart("qx2_r", "1")
//                .addFormDataPart("xzbz", "t")
//                .addFormDataPart("hjzd", ",NHXSJ,QJSJ,QJTS,")
//                .addFormDataPart("st_xq", LoginInfo.mUserData.Term)
//                .addFormDataPart("msie", "1")
//                .addFormDataPart("txxmxs", StudentID + " " + StudentName)
//                .addFormDataPart("tkey", "qjsj")
//                .addFormDataPart("xzbz", "1")


        //client.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryk7VcDEiWQ6aOVhpl");


    }


    /**
     * 上传文件
     */
    public void upLoadFile() {
        //补全请求地址
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //设置类型
        builder.setType(MultipartBody.FORM);
        //追加参数
        builder.addFormDataPart("", "");

        //创建RequestBody
        RequestBody body = builder.build();
        //创建Request
        final Request request = new Request.Builder().url("").post(body).build();
        //单独设置参数 比如读取超时时间

        final Call call = https.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Log.e("QiuChen", "response ----->" + string);
            }

        });
    }

    /**
     * 更新获取当前页面请假数据
     *
     * @param string 网页数据
     * @return 返回适配好的ListMap
     */
    public List<Map<String, Object>> UpdataHolidaysEume(String string) {
        List<Map<String, Object>> mList1 = new ArrayList<Map<String, Object>>();
        Pattern p = Pattern.compile
                ("<span>(.*?)</span>[\\s]*" +
                        "</td><td nowrap=\"nowrap\">" +
                        "[\\s]*<span>(.*?)[\\s]*</span>" +
                        "[\\s]*</td><td nowrap=\"nowrap\">" +
                        "[\\s]*<span>(.*?)[\\s]*</span>[\\s]*" +
                        "</td><td nowrap=\"nowrap\">[\\s]*" +
                        "<span>(.*?)[\\s]*</span>[\\s]*" +
                        "</td><td nowrap=\"nowrap\">[\\s]*" +
                        "<span>(.*?)[\\s]*</span>[\\s]*" +
                        "</td><td nowrap=\"nowrap\">[\\s]*" +
                        "<span>(.*?)[\\s]*</span>[\\s]*</td>" +
                        "<td nowrap=\"nowrap\">[\\s]*<span>" +
                        "(.*?)[\\s]*</span>[\\s]*</td><td n" +
                        "owrap=\"nowrap\">[\\s]*<span>(.*?)" +
                        "[\\s]*</span>[\\s]*</td><td nowrap" +
                        "=\"nowrap\">[\\s]*<span>(.*?)[\\s]*" +
                        "</span>[\\s]*</td><td nowrap=\"nowr" +
                        "ap\">[\\s]*<span>(.*?)[\\s]*</span>" +
                        "[\\s]*</td><td nowrap=\"nowrap\">" +
                        "[\\s]*<span>(.*?)[\\s]*</span>[\\" +
                        "s]*</td><td nowrap=\"nowrap\">[\\s" +
                        "]*<span>(.*?)[\\s]*</span>[\\s]*</" +
                        "td><td nowrap=\"nowrap\">[\\s]*<sp" +
                        "an>(.*?)[\\s]*</span>[\\s]*</td>");
        Matcher m = p.matcher(string);
        int i = 0;//重设计数器
        while (m.find()) {
            String Temp = "未通过";
            if (m.group(13).indexOf("√") != -1)//发现一个怪现象,他不能做文本间的对比,比如说m.group(13)=="√"这样就会返回不一致的信息
            {
                Temp = "通过";
            }
            mList1.add(HashMapEx(String.valueOf(i + 1), m.group(3), "请假时间:" + m.group(1), "外出地址:" + m.group(4), Temp));
            i++;
        }
        //实现倒序排列,让最近的数据优先显示
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Log.d("QiuChen", String.valueOf(mList1.size()));
        for (int a = 1; a <= mList1.size(); a++) {
            Map<String, Object> map = mList1.get(mList1.size() - a);
            map.put("mItemIndex", a);
            mList.add(map);
        }
        return mList;
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
    private Map<String, Object> HashMapEx(
            String Index,
            String mItem_HolidayBecause,
            String mItem_HolidayTime,
            String mItem_WhereOutSide,
            String mItemAcceptState) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("mItemIndex", Index);
        map.put("mItem_HolidayBecause", mItem_HolidayBecause);
        map.put("mItem_HolidayTime", mItem_HolidayTime);
        map.put("mItem_WhereOutSide", mItem_WhereOutSide);
        map.put("mItemAcceptState", mItemAcceptState);
        return map;
    }
}
