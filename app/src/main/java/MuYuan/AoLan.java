package MuYuan;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.*;
import java.net.URLEncoder;
import java.security.*;
import java.util.*;
import java.util.regex.*;

/**
 * This Code Is Created by QiuChenly on 2017/4/21 16:00.
 */

public class Aolan {
    private String _viewstate = "";
    private String _viewStategenerator = "";

    /**
     * 取文本中间方法
     *
     * @param AllString  所有文本
     * @param left       左边文本
     * @param Right      右边文本
     * @param StartIndex 可空,起始值
     * @return 中间文本
     */
    private String GetSubText(String AllString, String left, String Right, int StartIndex) {
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
        _viewStategenerator =
                GetSubText(ResponseBody, "id=\"__VIEWSTATEGENERATOR\" value=\"", "\"", 0);
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


    /**
     * 登录系统
     * 2017.6.29 第一次重构登录方法
     *
     * @param userid   学号
     * @param PassWord 密码
     * @return 1=OK -1=失败
     */
    public int login(String userid, String PassWord) {
        ResponseDataEx responseDataEx = null;
        try {
            responseDataEx = httpClient.Request("http://alst.jsahvc.edu.cn/login.aspx",
                    "__VIEWSTATE=&__VIEWSTATEGENERATOR=&userbh=" + userid + "&pass=" + md5(PassWord) + "&vcode=&xzbz=1", httpClient.Cookie, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseDataEx.ResponseCode == 302) {
            //302跳转表示登陆成功
            try {
                responseDataEx = httpClient.Request("http://alst.jsahvc.edu.cn/default.aspx",
                        httpClient.Cookie, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //是否为班干部
        if (responseDataEx.ResponseStr.contains("班干部")) {
            LoginInfo.mUserData.ClassLeader = true;
        } else {
            LoginInfo.mUserData.ClassLeader = false;
        }

        if (responseDataEx.ResponseStr.contains("><b>欢迎你:")) {
            LoginInfo.mUserData.Name = GetSubText(responseDataEx.ResponseStr, "><b>欢迎你:", "\n", 0).trim();
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 获取个人简要信息参数 (由于GetFullMyInfo()的关系,本方法即将废弃)
     */
    public void GetMyInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/default.aspx?dfldm=01";
        String Res = "";
        try {
            Res = httpClient.Request_Str(url,httpClient.Cookie,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取参数 nd 和 学期
        LoginInfo.mUserData.nd =
                GetSubText(Res, "type=\"hidden\" id=\"nd\" value=\"", "\"", 0);
        LoginInfo.mUserData.Term =
                GetSubText(Res, "<option value=\"", "\">", 0);
    }


    /**
     * 查询完全的个人数据
     * 不返回数据
     */
    public void GetFullMyInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jbxg.aspx?xq=" +
                LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd;
        // s 即为所需要的结果
        String Res = "";
        try {
            Res = httpClient.Request_Str(url, httpClient.Cookie, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    }


    /**
     * 获取学生和我自己的日常请假数据
     */
    public void getClassMatesInfo() {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=" +
                LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        // s 即为所需要的结果
        String Res = "";
        try {
            Res = httpClient.Request_Str(url, httpClient.Cookie, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        UpdataViewState(Res);
        Pattern p = Pattern.compile("<option value=\"(.*?)\">(.*?)</option>");
        Matcher m = p.matcher(Res);
        LoginInfo.mUserData.ClassMates = new ArrayList<String>();//初始化班级同学数据
        System.out.print(Res);
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
    }

    /**
     * 获取学生日常请假信息
     *
     * @param StudentID   学号
     * @param StudentName 姓名
     * @throws IOException IO异常捕捉
     */
    public void getStudentHolidaysInfo(String StudentID, String StudentName) throws IOException {
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
        datas = datas.replace("{__VIEWSTATE}", _viewstate).replace("{__VIEWSTATEGENERATOR}", _viewStategenerator);
        String Cookie = httpClient.Cookie;
        ResponseData r = HttpUtils.POST(url, datas, Cookie, "multipart/form-data; boundary=----WebKitFormBoundarymAMsOSk5Z6IB3N2f");
        UpdataViewState(r.ResponseText);
        LoginInfo.mUserData.HolidaysEume = UpdataHolidaysEume(r.ResponseText);
    }

    /**
     * 更新获取当前页面请假数据
     *
     * @param string 网页数据
     * @return 返回适配好的ListMap
     */
    public List<Map<String, String>> UpdataHolidaysEume(String string) {
        List<Map<String, String>> mList1 = new ArrayList<Map<String, String>>();
        Pattern p = Pattern.compile("</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>");
        Matcher m = p.matcher(string);
        int i = 0;//重设计数器
        while (m.find()) {
            String Temp;
            if (m.group(9).length() <= 0) {
                Temp = "等待处理";
            } else {
                Temp = m.group(9);
            }
            mList1.add(
                    HashMapEx(
                            String.valueOf(i + 1),
                            CheckNull(m.group(2).trim()),
                            "类型:" + m.group(1).trim() + " 请假时间:" + CheckNull(m.group(5)),
                            "外出地址:" + CheckNull(m.group(3)),
                            Temp));

            i++;
        }
        return listUnder(mList1);
    }

    /**
     * 倒序排列数据
     *
     * @param list 数据源
     * @return 排序好的数据
     */
    private List<Map<String, String>> listUnder(List<Map<String, String>> list) {
        //实现倒序排列,让最近的数据优先显示
        List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
        Log.d("QiuChen", String.valueOf(list.size()));
        for (int a = 1; a <= list.size(); a++) {
            Map<String, String> map = list.get(list.size() - a);
            map.put("mItemIndex", String.valueOf(a));
            mList.add(map);
        }
        return mList;
    }

    private String CheckNull(String Data) {
        if (Data.length() <= 0) {
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
    private Map<String, String> HashMapEx(String Index, String mItem_HolidayBecause, String mItem_HolidayTime, String mItem_WhereOutSide, String mItemAcceptState) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("mItemIndex", Index);
        map.put("mItem_HolidayBecause", mItem_HolidayBecause);
        map.put("mItem_HolidayTime", mItem_HolidayTime);
        map.put("mItem_WhereOutSide", mItem_WhereOutSide);
        map.put("mItemAcceptState", mItemAcceptState);
        return map;
    }

    /**
     * 初始化请假选项数据 如 病假 其他 事假 等
     *
     * @throws IOException
     */
    public void Init_Holidays_xzdm() throws IOException {
        LoginInfo.mUserData.CategoryHolidays = new ArrayList<>();
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        UpdataViewState(httpClient.Request_Str(url, httpClient.Cookie, null));
        String data = "__EVENTTARGET=" +
                "&__EVENTARGUMENT=" +
                "&__LASTFOCUS=" +
                "&__VIEWSTATE=" + httpClient.EncodeStr(_viewstate) +
                "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
                "&mh=&tj=&pdm=&pmc=qjsy&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=QJSYDM" +
                "&pzd=qjjtyy&xzbz=0&ipbz=1&cwbz=&xz1v=";
        String s = httpClient.Request_Str(url, data, httpClient.Cookie, null);

        Pattern p = Pattern.compile("\\(&#39;(.*?)&#39;,&#39;(.*?)&#39;,&#39;0&#39;\\)");
        Matcher m = p.matcher(s);
        while (m.find()) {
            String Line = m.group(2) + "|" + m.group(1);
            LoginInfo.mUserData.CategoryHolidays.add(Line);
        }
    }


    /**
     * 日常请假
     * 2017.6.26 修复因服务器更新导致请假失败的问题
     *
     * @param data 学生数据
     * @return 返回结果, 1=OK -1=FAILED
     * @throws IOException IO异常捕捉
     */
    public int Request_Holidays(Map<String, String> data) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_xsqj.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        UpdataViewState(HttpUtils.Get(url, httpClient.Cookie));
        Calendar calendar = Calendar.getInstance();
        String pkey = String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String Data = "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"__EVENTTARGET\"\n" +
                "\n" +
                "dcbc\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"__EVENTARGUMENT\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATE\"\n" +
                "\n" +
                _viewstate + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATEGENERATOR\"\n" +
                "\n" +
                _viewStategenerator + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"__VIEWSTATEENCRYPTED\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qjsj\"\n" +
                "\n" +
                data.get("Request_Time") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qjsy\"\n" +
                "\n" +
                data.get("Request_Categroy_str") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qjsydm\"\n" +
                "\n" +
                data.get("Request_Categroy_int") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qjjtyy\"\n" +
                "\n" +
                data.get("Request_Reason") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"File1\"; filename=\"\"\n" +
                "Content-Type: application/octet-stream\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"fjm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"wcdz\"\n" +
                "\n" +
                data.get("Request_OutAddress") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"jzxmlxfs\"\n" +
                "\n" +
                data.get("Request_ContactInformation") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"nhxsj\"\n" +
                "\n" +
                data.get("Request_BackSchoolTime") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qjts\"\n" +
                "\n" +
                data.get("Request_HolidaysDay") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xjrq\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"czsj\"\n" +
                "\n" +
                data.get("Request_CaoZhuoTime") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"fdyspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"fdyspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"yxspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"yxspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xjspyj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xjspyjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"ck_sqpzck\"\n" +
                "\n" +
                "False\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"km\"\n" +
                "\n" +
                "st_xsqj\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"y_km\"\n" +
                "\n" +
                "st_xsqj\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd\"\n" +
                "\n" +
                "qjsj,qjsydm,qjjtyy,fjm,wcdz,jzxmlxfs,nhxsj,qjts,xjrq,czsj,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,sqpzck\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_c\"\n" +
                "\n" +
                "sqpzck,\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_lock\"\n" +
                "\n" +
                "xjrq,sqpzck,fdyspyjdm,fdy,fdyspsj,yxspyjdm,yxspr,yxspsj,xjspyjdm,xjspr,xjspsj,\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_lock2\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_lock3\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_lock4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pzd_y\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xdm\"\n" +
                "\n" +
                LoginInfo.mUserData.xdm + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"bjhm\"\n" +
                "\n" +
                LoginInfo.mUserData.ClassName + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xh\"\n" +
                "\n" +
                data.get("StudentID") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xm\"\n" +
                "\n" +
                data.get("StudentName") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx_r\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx_i\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx_u\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx_d\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx2_r\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx2_i\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx2_u\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"qx2_d\"\n" +
                "\n" +
                "0\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"databcxs\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"databcdel\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xzbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pkey\"\n" +
                "\n" +
                pkey + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pkey4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xs_bj\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"bdbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"cw\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"hjzd\"\n" +
                "\n" +
                ",NHXSJ,QJSJ,QJTS,\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"st_xq\"\n" +
                "\n" +
                LoginInfo.mUserData.Term + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"st_nd\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"mc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"smbz\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"fjmf\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"psrc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pa\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pb\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pc\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pd\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pe\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"pf\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"msie\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"txxmxs\"\n" +
                "\n" +
                data.get("StudentID") + " " + data.get("StudentName") + "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"tkey\"\n" +
                "\n" +
                "qjsj\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"tkey4\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pmc\"\n" +
                "\n" +
                "qjsy\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pval\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_plx\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pkm\"\n" +
                "\n" +
                "QJSYDM\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pzd\"\n" +
                "\n" +
                "qjjtyy\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pjxjdm\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_ipbz\"\n" +
                "\n" +
                "1\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw\n" +
                "Content-Disposition: form-data; name=\"xp_pjxjdm2\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarynenXgjYBUoeNphnw--";
        String Cookie = httpClient.Cookie;
        ResponseData r = HttpUtils.POST(url, Data, Cookie, "multipart/form-data; boundary=----WebKitFormBoundarynenXgjYBUoeNphnw");
        if (r.ResponseText.contains("增加记录成功")) {
            return 1;
        } else {
            LoginInfo.Result = GetSubText(r.ResponseText, "<input name=\"cw\" type=\"hidden\" id=\"cw\" value=\"", "\" />", 0);
            return -1;
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
    public Map<String, String> BundleData(
            String StudentID, String StudentName, String Request_Time, String Request_Categroy_str, String Request_Categroy_int, String Request_Reason, String Request_OutAddress, String
            Request_ContactInformation, String Request_BackSchoolTime, String Request_HolidaysDay, String Request_CaoZhuoTime
    ) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("StudentID", StudentID);
        data.put("Request_Time", Request_Time);
        data.put("Request_Categroy_str", Request_Categroy_str);
        data.put("Request_Categroy_int", Request_Categroy_int);
        data.put("Request_Reason", Request_Reason);
        data.put("Request_OutAddress", Request_OutAddress);
        data.put("Request_ContactInformation", Request_ContactInformation);
        data.put("Request_BackSchoolTime", Request_BackSchoolTime);
        data.put("Request_HolidaysDay", Request_HolidaysDay);
        data.put("Request_CaoZhuoTime", Request_CaoZhuoTime);
        data.put("StudentName", StudentName);
        return data;
    }

    public void get_Holidays_LongTime() throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        LoginInfo.ErrCode = 0;
        String s;
        s = httpClient.Request_Str(url, httpClient.Cookie, null);
        UpdataViewState(s);
        Pattern p = Pattern.compile("<option value=\"(.*?)\">(.*?)</option>");
        Matcher m = p.matcher(s);
        LoginInfo.mUserData.ClassMates = new ArrayList<String>();//初始化班级同学数据
        System.out.print(s);
        //当前学生:201513043 陈玉奇
        String Temps = GetSubText(s, "当前学生:", " ", 0);//获取自己的数据
        int ii = 0;
        while (m.find()) {
            String Temp = m.group(2);
            if (Temp.contains(Temps)) {//顺带寻找当前个人位置
                LoginInfo.mUserData.ItemSelection = ii;//为了自适应Spinner控件的表项数据
            }
            LoginInfo.mUserData.ClassMates.add(Temp);//加入成员数据
            ii++;//数组从0开始,故i++放在最后
        }
        LoginInfo.mUserData.HolidaysEume = getViewByHolidaysLongInfomation(s);
        LoginInfo.mUserData.RoomsID = GetSubText(s, "<span id=\"l_sy_jbgr_sshm\">", "</span>", 0);
        LoginInfo.mUserData.HousePhoneNum = GetSubText(s, "<span id=\"l_sy_jbgr_jtdh\">", "</span>", 0);
        LoginInfo.mUserData.MySelfPhoneNum = GetSubText(s, "<span id=\"l_sy_jbgr_cell\">", "</span>", 0);
        LoginInfo.ErrCode = 1;
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
    public void QueryHolidaysLongClassMatesInfomation(String ID, String Name, String jjr, String jjrdm) throws IOException {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String Datas = "__EVENTTARGET=" + "&__EVENTARGUMENT=" + "&__VIEWSTATE=" + EncodeStr(_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator + "&__VIEWSTATEENCRYPTED=&sele1=" + ID +
                "&jjr=" + EncodeStr("") + "&jjrdm=" + "" + "&qxlb=&qxlbdm=&wcdz=&lxrq=&nhxsj=&fxrq=&ptbz=&km=st_jjrqx&y_km=st_jjrqx&pzd=jjrdm%2Cqxlbdm%2Cwcdz%2Clxrq%2Cnhxsj%2Cfxrq%2Cptbz&pzd_c" +
                "=&pzd_lock=&pzd_lock2=&pzd_lock3=&pzd_lock4=&pzd_y=&xdm=" + LoginInfo.mUserData.xdm + "&bjhm=" + EncodeStr(LoginInfo.mUserData.ClassName) + "&xh=" + ID + "&xm=" + EncodeStr(Name)
                + "&qx_i=1&qx_u=1&qx_d=0&qx2_r=1&qx2_i=0&qx2_u=0&qx2_d=0&databcxs=&databcdel=&xzbz=t&pkey=04&pkey4=&xs_bj=&bdbz=&cw=&hjzd=%2CCP10%2CCP1%2CCP2%2CCP3%2CCP4%2CCP5%2CCP6%2CCP7%2CCP8" +
                "%2CCP9" + "" + "" + "" + "" + "" + "" + "%2CCPZF%2C&st_xq=" + LoginInfo.mUserData.Term + "&st_nd=&mc=&smbz=&fjmf=&psrc=&pa=&pb=&pc=&pd=&pe=&pf=&msie=1&txxmxs=" + EncodeStr(ID + " " +
                "" + "" + Name) + "&tkey=jjrdm&tkey4=&xp_pmc=jjr&xp_pval=&xp_plx=&xp_pkm=JJRDM&xp_pzd=qxlbdm&xp_pjxjdm=&xp_ipbz=1&xp_pjxjdm2=";
        String Cookie = httpClient.Cookie;;
        ResponseData r = HttpUtils.POST(url, Datas, Cookie, "application/x-www-form-urlencoded");
        LoginInfo.mUserData.HolidaysEume = getViewByHolidaysLongInfomation(r.ResponseText);
        LoginInfo.mUserData.RoomsID = GetSubText(r.ResponseText, "<span id=\"l_sy_jbgr_sshm\">", "</span>", 0);
        LoginInfo.mUserData.HousePhoneNum = GetSubText(r.ResponseText, "<span id=\"l_sy_jbgr_jtdh\">", "</span>", 0);
        LoginInfo.mUserData.MySelfPhoneNum = GetSubText(r.ResponseText, "<span id=\"l_sy_jbgr_cell\">", "</span>", 0);
        LoginInfo.ErrCode = 1;
    }

    /**
     * 把汉字编码为UTF-8编码,解决报错问题
     *
     * @param str 原字符
     * @return 转换后的UTF-8字符
     * @throws UnsupportedEncodingException 异常捕捉
     */
    public String EncodeStr(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    /**
     * 获取请假数据 请假分类  去向类别
     *
     * @param Category 获取的信息类型,1=请假分类,如 五一节  2=去向类别,如  回家 留校 等.
     * @throws IOException
     */
    public void Init_HolidaysLong(int Category) throws IOException {
        LoginInfo.ErrCode = 0;
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        String Data;
        if (Category == 1) {
            LoginInfo.mUserData.CategoryHolidays_Long = new ArrayList<>();
            Data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" + EncodeStr(_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
                    "&mh=&tj=&pdm=&pmc=jjr&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=JJRDM&pzd=qxlbdm&xzbz=0&ipbz=1&cwbz=&xz1v=";
        } else {
            LoginInfo.mUserData.WithOutCategory = new ArrayList<>();
            Data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" + EncodeStr(_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator +
                    "&mh=&tj=&pdm=&pmc=qxlb&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=QXLBDM&pzd=wcdz&xzbz=0&ipbz=1&cwbz=&xz1v=";
        }
        String Cookie = httpClient.Cookie;;
        ResponseData r = HttpUtils.POST(url, Data, Cookie, "application/x-www-form-urlencoded");
        Pattern p = Pattern.compile("<span id=\".*?\">(.*?)[\\s]*</span>[\\s]*</font></td><td nowrap=\"nowrap\"><font color=\"Black\">[\\s]*<span id=\".*?\">(.*?)[\\s]*</span>");
        Matcher m = p.matcher(r.ResponseText);
        while (m.find()) {
            String Line = m.group(1) + "|" + m.group(2);
            if (Category == 1) {
                UserData.CategoryHolidays_Long.add(Line);
                System.out.print(Line);
            } else {
                UserData.WithOutCategory.add(Line);
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
    private List<Map<String, String>> getViewByHolidaysLongInfomation(String str) {
        List<Map<String, String>> mlist = new ArrayList<Map<String, String>>();
        Pattern p = Pattern.compile("<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)" +
                "[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td " +
                "nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td><td nowrap=\"nowrap\">[\\s]*<span>(.*?)[\\s]*</span>[\\s]*</td>");
        Matcher m = p.matcher(str);
        int i = 0;
        while (m.find()) {
            mlist.add(HashMapEx(String.valueOf(i), "节假日:" + m.group(1) + " 去向类别:" + m.group(2), "离校日期:" + m.group(4), "外出地址:" + CheckNull(m.group(3)), ""));
        }
        return listUnder(mlist);
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
    public int HolidaysDays_Long(String y_jjr, String y_jjrdm, String y_qxlb, String y_qxlbdm, String y_wcdz, String y_lxrq, String y_nhxsj, String y_fxrq, String y_ptbz) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_jjrqx.aspx?xq=" + LoginInfo.mUserData.Term + "&nd=" + LoginInfo.mUserData.nd + "&msie=1";
        String Data = "__EVENTTARGET=databc&__EVENTARGUMENT=&__VIEWSTATE=" + EncodeStr(_viewstate) + "&__VIEWSTATEGENERATOR=" + _viewStategenerator + "&__VIEWSTATEENCRYPTED=&jjr=" + EncodeStr
                (y_jjr) + "&jjrdm=" + y_jjrdm + "&qxlb=" + EncodeStr(y_qxlb) + "&qxlbdm=" + y_qxlbdm + "&wcdz=" + EncodeStr(y_wcdz) + "&lxrq=" + y_lxrq + "&nhxsj=" + y_nhxsj + "&fxrq=" + y_fxrq +
                "&ptbz=" + EncodeStr(y_ptbz) + "&km=st_jjrqx&y_km=st_jjrqx&pzd=jjrdm%2Cqxlbdm%2Cwcdz" +
                "%2Clxrq%2Cnhxsj%2Cfxrq%2Cptbz&pzd_c=&pzd_lock=&pzd_lock2=&pzd_lock3=&pzd_lock4=&pzd_y=&xdm=" + LoginInfo.mUserData.xdm + "&bjhm=" + EncodeStr(LoginInfo.mUserData.ClassName) +
                "&xh=" + LoginInfo.mUserData.y_xh + "&xm=" + EncodeStr(LoginInfo.mUserData.Name) + "&qx_i=1&qx_u=1&qx_d=0&qx2_r=1&qx2_i=0&qx2_u=0&qx2_d=0&databcxs=1&databcdel=&" +
                "xzbz=&pkey=04&pkey4=&xs_bj=&bdbz=&cw=&hjzd=%2CCP10%2CCP1%2CCP2%2CCP3%2CCP4%2CC" + "P5%2CCP6%2CCP7%2CCP8%2CCP9%2CCPZF%2C&st_xq=" + LoginInfo.mUserData.Term +
                "&st_nd=&mc=&smbz=&fjmf=&psrc=&pa=&pb=&pc=&pd=&pe=&pf=&msie=1&txxmxs=" + LoginInfo.mUserData.UserNum + EncodeStr(" " + LoginInfo.mUserData.Name) +
                "&tkey=jjrdm&tkey4=&xp_pmc=qxlb&xp_pval=&xp_plx=&xp_pkm=QXLBDM&xp_pzd=wcdz" + "&xp_pjxjdm=&xp_ipbz=1&xp_pjxjdm2=";
        ResponseData Result = HttpUtils.POST(url, Data, httpClient.Cookie, "application/x-www-form-urlencoded");
        System.out.print(Result.ResponseText);
        if (Result.ResponseText.contains("增加记录成功")) {
            return 1;
        } else {
            LoginInfo.Result = GetSubText(Result.ResponseText, "<input name=\"cw\" type=\"hidden\" id=\"cw\" value=\"", "\"", 0);
            return -1;
        }
    }

    public String getOICQName(String Uin) throws IOException {
        String url = "http://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + Uin;
        url = HttpUtils.Get_s(url);
        url = GetSubText(url, "-1,0,0,0,\"", "\"", 0);
        return url;
    }

    public Bitmap getOICQBitMap(String Uin) {
        String url = "http://q2.qlogo.cn/headimg_dl?bs=" + Uin + "&dst_uin=" + Uin + "&dst_uin=" +
                Uin + "&dst_uin=" + Uin + "&spec=100&url_enc=0&referer=bu_interface&term_type=PC";
        return HttpUtils.getImageBitmap(url);
    }

    public List<String> getDormChangeItem() throws IOException {
        String Url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        String res = HttpUtils.Get(Url, HttpUtils.Cookie);
        UpdataViewState(res);
        Map<String, String> map = new HashMap<>();
        map.put("Host", "alst.jsahvc.edu.cn");
        map.put("Connection", "keep-alive");
        map.put("Content-Length", "349");
        map.put("Cache-Control", "max-age=0");
        map.put("Origin", "http://alst.jsahvc.edu.cn");
        map.put("Upgrade-Insecure-Requests", "1");
        map.put("User-Agent", "Mozilla/5.0(Windows NT 10.0;bWin64;x64)AppleWebKit/537.36 " +
                "(KHTML, like Gecko)Chrome/58.0 .3029 .110 Safari / 537.36");
        map.put("Content-Type", "application/x - www - form - urlencoded");
        map.put("Accept", "text/html, application / xhtml + xml, application / xml;q = 0.9, " +
                "image/webp,*/*;q=0.8");
        map.put("Referer", "http://alst.jsahvc.edu.cn/xzdm.aspx");
        map.put("Cookie", HttpUtils.Cookie);
        String data = "__EVENTTARGET=&" +
                "__EVENTARGUMENT=&" +
                "__LASTFOCUS=&" +
                "__VIEWSTATE=" + EncodeStr(_viewstate) + "&" +
                "__VIEWSTATEGENERATOR=" + _viewStategenerator + "&" +
                "mh=&tj=&pdm=&pmc=ssbd&pdm2=&pmc2=&" +
                "pjxjdm=&pjxjdm2=&pval=&plx=&pkm=SSBDDM&pzd=sqrq&xzbz=0&ipbz=1&cwbz=&xz1v=";
        ResponseData rs = HttpUtils.POST(Url, data, map, false);
        Pattern p = Pattern.compile("onclick=\"xzst\\(&#39;(.*?)&#39;,&#39;(.*?)&#39;,&#39;0&#39;\\)");
        Matcher m = p.matcher(rs.ResponseText);
        List<String> list = new ArrayList<>();
        while (m.find()) {
            list.add(m.group(1) + "|" + m.group(2));
        }
        return list;
    }


}
