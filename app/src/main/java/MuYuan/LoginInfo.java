package MuYuan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.example.qiuchen.myapplication.MainUser;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by QiuChen on 2017/4/15.
 */

public class LoginInfo {
    public static int ErrCode = 0;
    public static String Result;
    public static UserData mUserData = new UserData();
    public static Aolan aolanEx = new Aolan();
    public static Boolean IsRequestViews = false;
    public static Boolean IsLongRequestViews = false;
    public static ProgressDialog Dialog = null;
    public static Boolean IsInitChecked = false;
    public static Bitmap UserPic;
    public static AolanStudentDromRooms mStudentDromRooms=new AolanStudentDromRooms();
    public static Bitmap BackGroundPic;

    /**
     * 计算天数,重写js算法
     *
     * @param Request_Time   请假时间  如2011.1.1即可
     * @param BackSchoolTime 拟回校时间  如2011.1.1即可
     * @return 返回计算结果天数
     */
    public static String CalculationHoliday_Days(String Request_Time, String BackSchoolTime) {
        int retTime;
        String[] Request = Request_Time.split("\\.");
        String[] BackSchool = BackSchoolTime.split("\\.");
        retTime = (Integer.valueOf(BackSchool[0]) - Integer.valueOf(Request[0])) * 365 + (Integer.valueOf(BackSchool[1]) - Integer.valueOf(Request[1])) * 31 + (Integer.valueOf(BackSchool[2]) -
                Integer.valueOf(Request[2]) + 1);
        if (retTime <= 0) {
            return null;
        } else {
            return String.valueOf(retTime);
        }
    }
}
