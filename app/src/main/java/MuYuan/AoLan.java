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
    private AsyncHttpClient client = null;
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
        //构造函数,初始化数据,无需传参
        client.get("http://alst.jsahvc.edu.cn/login.aspx", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Temp = new String(responseBody);
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
     * @return
     */
    public void LoginUser(String PersonId, String Password, String Vcode) {
        //init
        LoginInfo.ErrCode = 0;
        LoginInfo.Result = null;
        String url = "http://alst.jsahvc.edu.cn/login.aspx";
        RequestParams params = new RequestParams();
        params.add("__VIEWSTATE", _viewstate);
        params.add("__VIEWSTATEGENERATOR", _viewStategenerator);
        params.add("userbh", PersonId);
        params.add("pass", md5(Password));
        params.add("vcode", "");
        params.add("xzbz", "1");
        client.post(url, params, new AsyncHttpResponseHandler() {
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
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String data = "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"__EVENTTARGET\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"__EVENTARGUMENT\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATE\"\n" +
                "\n" +
                "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\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATEGENERATOR\"\n" +
                "\n" +
                "0374EA8B\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATEENCRYPTED\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"sele1\"\n" +
                "\n" +
                "201513028\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qjsj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qjsy\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qjsydm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qjjtyy\"\n" +
                "\n" +
                "    \n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"File1\"; filename=\"\"\n" +
                "Content-Type: application/octet-stream\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"fjm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"wcdz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"jzxmlxfs\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"nhxsj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qjts\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xjrq\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"czsj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"fdyspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"fdyspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"yxspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"yxspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xjspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xjspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"km\"\n" +
                "\n" +
                "st_xsqj\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"y_km\"\n" +
                "\n" +
                "st_xsqj\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd\"\n" +
                "\n" +
                "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_c\"\n" +
                "\n" +
                "sqpzck,\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_lock\"\n" +
                "\n" +
                "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_lock2\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_lock3\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_lock4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pzd_y\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xdm\"\n" +
                "\n" +
                "209\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"bjhm\"\n" +
                "\n" +
                "15数控技术1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xh\"\n" +
                "\n" +
                "201513028\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xm\"\n" +
                "\n" +
                "韩达明\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx_i\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx_u\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx_d\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx2_r\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx2_i\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx2_u\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"qx2_d\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"databcxs\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"databcdel\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xzbz\"\n" +
                "\n" +
                "t\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pkey\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pkey4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xs_bj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"bdbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"cw\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"hjzd\"\n" +
                "\n" +
                ",NHXSJ,QJSJ,QJTS,\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"st_xq\"\n" +
                "\n" +
                "2016-2017-2\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"st_nd\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"mc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"smbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"fjmf\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"psrc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pa\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pb\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pd\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pe\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"pf\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"msie\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"txxmxs\"\n" +
                "\n" +
                "201513028 韩达明\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"tkey\"\n" +
                "\n" +
                "qjsj\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"tkey4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pmc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pval\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_plx\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pkm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pzd\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pjxjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_ipbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl\n" +
                "Content-Disposition: form-data; name=\"xp_pjxjdm2\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundaryk7VcDEiWQ6aOVhpl--";

        client.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryk7VcDEiWQ6aOVhpl");
        byte[] bytes = data.getBytes();

        RequestParams params = new RequestParams();
        params.put("file", bytes);
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String Res = new String(responseBody);
                System.out.print(Res);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }
}

