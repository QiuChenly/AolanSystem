package MuYuan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by QiuChen on 2017/4/16.
 */

public class UserData {
    //注意: y_ 开头的sb参数是根据网站原有sb参数名命名的,非本人自创.
    public String nd;//入学时间/年度
    public String ClassName;//班级名称
    public Boolean ClassLeader;//班干
    public String Term;//学期
    public String xdm;//系代码
    public String Name;//学生姓名
    public String y_xdm;//院系名称
    public String y_xh;//学号
    public String y_xbdm;//性别
    public String y_cell;//联系电话.什么鬼,明明是call才对吧,网站开发者英语三级过了吗?
    public String y_sshm;//宿舍所在编号
    public String y_yhzh;//银行帐号
    public String y_byzx;//毕业高中
    public String y_cwh;//床号
    public String y_qq;//联系QQ
    public String y_email;//电子邮箱
    public String y_mzdm;//民族
    public String y_zzmmdm;//政治面貌
    public String y_zzmmsj;//政治面貌加入时间
    public String y_syszddm;//学生生源地区
    public String y_xz;//学制,一般是3年.转本的不算
    public String y_jtdz;//家庭地址
    public String y_ksh;//考生号
    public String y_hkxzdm;//户口性质
    public List<String> ClassMates;//同班同学数据集合
    public List<Map<String, Object>> HolidaysEume;//請假記錄
    public int ItemSelection = 0;
    public String Password;
    public String UserNum;
    public String RoomsID;//宿舍编号
    public String HousePhoneNum;//家庭电话
    public String MySelfPhoneNum;//个人电话
    public static List<String> CategoryHolidays = new ArrayList<String>();
    public static List<String> WithOutCategory = new ArrayList<String>();
}
