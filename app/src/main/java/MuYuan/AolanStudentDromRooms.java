package MuYuan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auther: cheny
 * CreateDate 6/28/2017.
 */

public class AolanStudentDromRooms {
    String VIEWSTATE, VIEWSTATEGENERSTOR;


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
        VIEWSTATE = GetSubText(ResponseBody, "id=\"__VIEWSTATE\" value=\"", "\"", 0);
        VIEWSTATEGENERSTOR = GetSubText(ResponseBody, "id=\"__VIEWSTATEGENERATOR\" value=\"", "\"", 0);
    }

    public void mInitDromRoomsView(String Semester, String Years) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/txxm/rsbulid/r_3_3_st_ssbd.aspx?" +
                "xq=" + Semester + "&nd=" + Years + "&msie=1";
        String result = HttpUtils.Get(url, HttpUtils.Cookie,
                "http://alst.jsahvc.edu.cn/txxm/default.aspx?dfldm=02");
        UpdataViewState(result);
    }


    public void mGetBuildInfo() throws IOException {
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        UpdataViewState(HttpUtils.Get(url, HttpUtils.Cookie));
        String data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" +
                HttpUtils.EncoderStr(VIEWSTATE) + "&__VIEWSTATEGENERATOR=" + VIEWSTATEGENERSTOR +
                "&mh=&tj=&pdm=&pmc=sshm&pdm2=&pmc2=&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=&pzd=sqrq&x" +
                "zbz=0&ipbz=1&cwbz=&xz1v=";
        ResponseData res = HttpUtils.POST(url, data, HttpUtils.Cookie, "application/x-www-form-urlencoded");
        //FIXME:获取楼号信息,正则表达式数据集合未设置
    }

    /**
     * 根据楼号粗略搜索宿舍号
     * @param BuildNum 楼号
     * @param SearchText 宿舍关键词
     * @return 返回集合
     * @throws IOException
     */
    public List<String> mSearchRoomsInBuildNum(String BuildNum, String SearchText) throws IOException {
        String url = "http://alst.jsahvc.edu.cn/xzdm.aspx";
        List<String> Returns = new ArrayList<>();
        UpdataViewState(HttpUtils.Get(url, HttpUtils.Cookie));
        String data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" +
                HttpUtils.EncoderStr(VIEWSTATE) +
                "&__VIEWSTATEGENERATOR=" + VIEWSTATEGENERSTOR + "&xz1=" +
                HttpUtils.EncoderStr(BuildNum) + "&mh=" + SearchText + "&tj=&pdm=sshm" +
                "&pmc=sshm&pdm2=sshm&pmc2=sshm&pjxjdm=&pjxjdm2=&pval=&plx=&pkm=ss_jbxx&pzd=sqrq" +
                "&xzbz=&ipbz=1&cwbz=&xz1v=" + HttpUtils.EncoderStr(BuildNum);
        ResponseData res = HttpUtils.POST(url, data, HttpUtils.Cookie, "application/x-www-form-urlencoded");
        Pattern p = Pattern.compile("<span id=\"MyDataGrid__.*_dm\">(.*?)\\s*</span>");
        Matcher m = p.matcher(res.ResponseText);
        while (m.find()) {
            Returns.add(m.group(1));
        }
        return Returns;
    }

}
