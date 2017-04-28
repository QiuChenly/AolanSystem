package MuYuan;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by QiuChen on 2017/4/15.
 */

public class AoLan {

    //原生AysncHttpClient类库封装
    //已废弃项目

    private AsyncHttpClient client = null;
    //private OkHttpClient client = null;
    public String _viewstate = "";
    public String _viewStategenerator = "";
    Context mc;

    /**
     * 更新必须参数
     *
     * @param ResponseBody 返回的网页数据
     */
    public void UpdataViewState(String ResponseBody) {
        _viewstate = GetSubText(ResponseBody, "id=\"__VIEWSTATE\" value=\"", "\"", 0);
        _viewStategenerator = GetSubText(ResponseBody, "id=\"__VIEWSTATEGENERATOR\" value=\"", "\"", 0);
    }


    public void init(Context c) {
        mc = c;
        client = new AsyncHttpClient();//initiation
        client.setConnectTimeout(5000);//设置超时
        client.setResponseTimeout(5000);
        client.get("http://alst.jsahvc.edu.cn/login.aspx", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Temp = null;
                Temp = new String(responseBody);
                UpdataViewState(Temp);//更新数据
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("QiuChen", error.getMessage());
            }
        });
    }

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
     * MD5加密
     *
     * @param string 加密的数据
     * @return 返回加密文本
     */
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

    /**
     * Login For MuYuanSystem
     *
     * @param PersonId YouSelfStudyID
     * @param Password usually is Your ID Card Lasted six Num
     * @param Vcode    Hum,Is No need.
     */
    public void LoginUser(final String PersonId, final String Password, String Vcode) {
        //init
        LoginInfo.ErrCode = 0;
        LoginInfo.Result = null;

        RequestParams params = new RequestParams();
        params.add("__VIEWSTATE", _viewstate);
        params.add("__VIEWSTATEGENERATOR", _viewStategenerator);
        params.add("userbh", PersonId);
        params.add("pass", md5(Password));
        params.add("vcode", "");
        params.add("xzbz", "1");


        client.post("http://alst.jsahvc.edu.cn/login.aspx", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                String Ress = new String(bytes);
                //更新必须数据
                UpdataViewState(Ress);
                //是否为班干部
                if (Ress.indexOf("注销登录") != -1) {
                    LoginInfo.mUserData.ClassLeader = true;
                } else {
                    LoginInfo.mUserData.ClassLeader = false;
                }
                int val = Ress.indexOf("><b>欢迎你:");
                String Temps = "";
                if (val != -1) {
                    Temps = GetSubText(Ress, "><b>欢迎你:", "（", 0).trim();
                    LoginInfo.mUserData.Name = Temps;
                    LoginInfo.ErrCode = 1;
                } else {
                    Temps = Ress;
                    LoginInfo.ErrCode = -1;
                }
                LoginInfo.Result = Temps;
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.d("QiuChen", throwable.toString());
            }
        });
    }

    /**
     * 获取个人简要信息参数 (由于GetFullMyInfo()的关系,本方法即将废弃)
     */
    public void GetMyInfo() {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/default.aspx?dfldm=01";
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Res = new String(responseBody);
                //获取参数 nd 和 学期
                LoginInfo.mUserData.nd = GetSubText(Res, "type=\"hidden\" id=\"nd\" value=\"", "\"", 0);
                LoginInfo.mUserData.Term = GetSubText(Res, "<option value=\"", "\">", 0);
                LoginInfo.ErrCode = 1;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                LoginInfo.ErrCode = -1;
            }
        });
        //当前学期:<select name="xq_xz" id="xq_xz" class="xq" onchange="load1()">
        //<option value="2016-2017-2">2016-2017-2</option>
        //<option value="2016-2017-1">2016-2017-1</option>
        //<option value="2015-2016-2">2015-2016-2</option>
        //<option value="2015-2016-1">2015-2016-1</option>
    }


    /**
     * 查询完全的个人数据
     * 不返回数据
     */
    public void GetFullMyInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jbxg.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd;
        LoginInfo.ErrCode = 0;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Res = new String(responseBody);
                System.out.print(Res);
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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                LoginInfo.ErrCode = -1;
            }
        });
    }

    public void getClassMatesInfo() {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq="
                + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Res = new String(responseBody);
                UpdataViewState(Res);
                Pattern p = Pattern.compile("<option value=\"(.*?)\">(.*?)</option>");
                Matcher m = p.matcher(Res);
                LoginInfo.mUserData.ClassMates = new ArrayList<String>();//初始化班级同学数据
                //System.out.print(Res);
                //当前学生:201513043 陈玉奇
                String Temps = GetSubText(Res, "当前学生:", " ", 0);//获取自己的数据
                int i = 0;
                while (m.find()) {
                    String Temp = m.group(2).toString();
                    if (Temp.indexOf(Temps) != -1) {//顺带寻找当前个人位置
                        LoginInfo.mUserData.ItemSelection = i;//为了自适应Spinner控件的表项数据
                    }
                    LoginInfo.mUserData.ClassMates.add(Temp);//加入成员数据
                    i++;//数组从0开始,故i++放在最后
                }
                LoginInfo.mUserData.HolidaysEume = UpdataHolidaysEume(Res);
                LoginInfo.ErrCode = 1;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                LoginInfo.ErrCode = -1;
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

    public void getStudentHolidaysInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=2016-2017-2&nd=2015&msie=1";
        String data = "";

       client.addHeader
               ("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryk7VcDEiWQ6aOVhpl");


    }


}

