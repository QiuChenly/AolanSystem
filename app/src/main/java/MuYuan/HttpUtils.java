package MuYuan;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by QiuChenly on 2017/4/22.
 */


public class HttpUtils {
    public static String Cookie;

    /**
     * 提交自定义数据到服务器
     *
     * @param url         网址
     * @param Datas       数据,文本
     * @param Cookie      附带的Cookie,有时候服务器会需要你提供这个
     * @param ContentType 协议头
     * @return 返回网页数据
     * @throws IOException IO异常捕捉,请在外部调用Try
     */
    public static ResponseData POST(String url, String Datas, String Cookie, String ContentType) throws IOException {
        return POST (url, Datas.getBytes (), Cookie, ContentType, false);
    }

    /**
     *  提交自定义数据到服务器
     * @param url
     * @param Datas
     * @param Cookie
     * @param ContentType
     * @param Direct
     * @return 返回自定义数据类型
     * @throws IOException
     */
    public static ResponseData POST(String url, String Datas, String Cookie, String ContentType, boolean Direct) throws IOException {
        return POST (url, Datas.getBytes (), Cookie, ContentType, Direct);
    }


    /**
     * 提交自定义数据到服务器
     * QiuChenly
     *
     * @param Url       网址
     * @param Datas       数据,Byte数据
     * @param Cookies     附带的Cookie,有时候服务器会需要你提供这个
     * @param ContentType 协议头
     * @return 返回网页数据
     * @throws IOException IO异常捕捉,请在外部调用Try
     */
    public static ResponseData POST (String Url, byte[] Datas, String Cookies, String ContentType, Boolean Redirct) throws IOException {

        URL url=new URL(Url);
        byte[] data = Datas;//获得请求体
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection ();
        httpURLConnection.setConnectTimeout (10000);     //设置连接超时时间
        httpURLConnection.setDoInput (true);                  //打开输入流，以便从服务器获取数据
        httpURLConnection.setDoOutput (true);                 //打开输出流，以便向服务器提交数据
        httpURLConnection.setRequestMethod ("POST");     //设置以Post方式提交数据
        httpURLConnection.setUseCaches (false);               //使用Post方式不能使用缓存
        httpURLConnection.setInstanceFollowRedirects (Redirct);
        //设置请求体的类型
        httpURLConnection.setRequestProperty ("Content-Type", ContentType);
        httpURLConnection.setRequestProperty ("Cookie", Cookies);
        httpURLConnection.setRequestProperty ("Content-Length", String.valueOf (data.length));
        //获得输出流，向服务器写入数据
        OutputStream outputStream = httpURLConnection.getOutputStream ();
        outputStream.write (data);
        ResponseData res = new ResponseData ();
        int response = 0;
        try {
            response = httpURLConnection.getResponseCode ();            //获得服务器的响应码

            String cookieskey = "Set-Cookie";
            Map<String, List<String>> maps = httpURLConnection.getHeaderFields ();
            List<String> coolist = maps.get (cookieskey);
            if (coolist != null) {
                Iterator<String> it = coolist.iterator ();
                StringBuffer sbu = new StringBuffer ();
                while (it.hasNext ()) {
                    sbu.append (it.next ());
                }
                Cookie = sbu.toString ();
            }

            if (response == HttpURLConnection.HTTP_OK) {

                InputStream inptStream = httpURLConnection.getInputStream ();
                res.ResponseCode = response;
                res.ResponseText = dealResponseResult (inptStream);                     //处理服务器的响应结果
            } else {
                //如果涉及到非200的跳转,这里直接将其抛出
                String urls = httpURLConnection.getHeaderField ("Location");
                res.ResponseCode = response;
                res.RedirctUrl = urls;
                res.ResponseText = "";
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        //获得服务器的响应码
        Log.d ("QiuChen", String.valueOf (response));
        return res;
    }

    /**
     * 秋城落叶 重写 POST方法,支持Cookie自动更新
     *
     * @param urls          请求的Http网址
     * @param Datas         字符串数据
     * @param RequestHeader 协议头 Cookie也在里面设置
     * @param Redirct       是否允许重定向
     * @return 请求数据
     * @throws IOException IO异常
     */
    public static ResponseData POST (String urls, String Datas, Map<String, String> RequestHeader, Boolean Redirct) throws IOException {
        byte[] data = Datas.getBytes ();//获得请求体
        URL url = new URL (urls);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection ();
        httpURLConnection.setConnectTimeout (10000);     //设置连接超时时间
        httpURLConnection.setDoInput (true);                  //打开输入流，以便从服务器获取数据
        httpURLConnection.setDoOutput (true);                 //打开输出流，以便向服务器提交数据
        httpURLConnection.setRequestMethod ("POST");     //设置以Post方式提交数据
        httpURLConnection.setUseCaches (false);               //使用Post方式不能使用缓存
        httpURLConnection.setInstanceFollowRedirects (Redirct);

        for (Map.Entry<String, String> vo : RequestHeader.entrySet ()) {
            httpURLConnection.setRequestProperty (vo.getKey (), vo.getValue ());
        }
        //设置请求体的长度
        httpURLConnection.setRequestProperty ("Content-Length", String.valueOf (data.length));
        //获得输出流，向服务器写入数据
        OutputStream outputStream = httpURLConnection.getOutputStream ();
        outputStream.write (data);
        ResponseData res = new ResponseData ();
        int response = 0;
        try {
            response = httpURLConnection.getResponseCode ();            //获得服务器的响应码

            String cookieskey = "Set-Cookie";
            Map<String, List<String>> maps = httpURLConnection.getHeaderFields ();
            List<String> coolist = maps.get (cookieskey);
            if (coolist != null) {
                Iterator<String> it = coolist.iterator ();
                StringBuffer sbu = new StringBuffer ();
                while (it.hasNext ()) {
                    sbu.append (it.next ());
                }
                Cookie = sbu.toString ();
            }

            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream ();
                res.ResponseCode = response;
                res.ResponseText = dealResponseResult (inptStream);                     //处理服务器的响应结果
            } else {
                //如果涉及到非200的跳转,这里直接将其抛出
                String urlss = httpURLConnection.getHeaderField ("Location");
                res.ResponseCode = response;
                res.RedirctUrl = urlss;
                res.ResponseText = "";
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        //获得服务器的响应码
        Log.d ("QiuChen", String.valueOf (response));
        return res;
    }




    /*
         * Function  :   处理服务器的响应结果（将输入流转化成字符串）
         * Param     :   inputStream服务器的响应输入流
         * Author    :   博客园-依旧淡然
         */

    public static String dealResponseResult (InputStream inputStream) throws UnsupportedEncodingException {
        String resultData;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read (data)) != - 1) {
                byteArrayOutputStream.write (data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        resultData = new String (byteArrayOutputStream.toByteArray ());
        return resultData;
    }

    /**
     *  自定义解码数据
     * @param inputStream 输入流
     * @param CharSet 编码方式
     * @return 返回解码的数据
     * @throws UnsupportedEncodingException
     */
    public static String dealResponseResult (InputStream inputStream, String CharSet) throws UnsupportedEncodingException {
        String resultData;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read (data)) != - 1) {
                byteArrayOutputStream.write (data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        resultData = new String (byteArrayOutputStream.toByteArray (), CharSet);
        return resultData;
    }

    public static String Get (String urlString, String Cookies, String Referer) throws IOException {
        Map<String ,String> m=new HashMap<>();
        m.put("Accept", "*/*");
        m.put("Referer", Referer);
        m.put("Accept-Language", "zh-cn");
        m.put("Cookie", Cookies);
        m.put("Content-Type", "application/x-www-form-urlencoded");
        return Get (urlString, "UTF-8", m);
    }

    /**
     * Get方式访问网页,获取响应流
     * @param urlString URL地址
     * @param Cookies 带上Cookie访问,可为空.
     * @return 返回响应的数据
     * @throws IOException
     */
    public static String Get (String urlString, String Cookies) throws IOException {
        Map<String ,String> m=new HashMap<>();
        m.put("Accept", "*/*");
        m.put("Referer", urlString);
        m.put("Accept-Language", "zh-cn");
        m.put("Cookie", Cookies);
        m.put("Content-Type", "application/x-www-form-urlencoded");
        return Get (urlString, "UTF-8", m);
    }

    /**
     *Get方式访问网页,获取响应流
     * @param urlString URL地址
     * @return  返回响应的数据
     * @throws IOException 使用不当会出SB NPE,抛出就好了
     */
    public  static String Get(String urlString) throws IOException {
        Map<String ,String> m=new HashMap<>();
        m.put("Accept", "*/*");
        m.put("Referer", urlString);
        m.put("Accept-Language", "zh-cn");
        m.put("Cookie", "");
        m.put("Content-Type", "application/x-www-form-urlencoded");
        return Get (urlString, "UTF-8", m);
    }


    /**
     * GBK编码
     * @param urlString
     * @return
     * @throws IOException
     */
    public  static String Get_s(String urlString) throws IOException {
        Map<String ,String> m=new HashMap<>();
        m.put("Accept", "*/*");
        m.put("Referer", urlString);
        m.put("Accept-Language", "zh-cn");
        m.put("Cookie", "");
        m.put("Content-Type", "application/x-www-form-urlencoded");
        return Get (urlString, "GBK", m);
    }

    /**
     *  Get方式访问网页,获取响应流
     * @param urlString URL地址
     * @param CharSet 设置返回的解码字符串
     * @param RequestHeader 协议头
     * @return 返回解码的数据
     * @throws IOException NPE,抛出就好了
     */
    public static String Get (String urlString, String CharSet, Map<String,String> RequestHeader) throws IOException {
        String s;
        URL url = new URL (urlString); //URL对象
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection ();
        httpURLConnection.setConnectTimeout (3000);     //设置连接超时时间
        httpURLConnection.setDoInput (true);                  //打开输入流，以便从服务器获取数据
        httpURLConnection.setRequestMethod ("GET");     //设置以Get方式提交数据
        httpURLConnection.setUseCaches (false);

        for (Map.Entry<String, String> vo : RequestHeader.entrySet ()) {
            httpURLConnection.setRequestProperty (vo.getKey (), vo.getValue ());
        }
        int response = httpURLConnection.getResponseCode ();            //获得服务器的响应码
        if (response == HttpURLConnection.HTTP_OK) {
            InputStream inptStream = httpURLConnection.getInputStream ();
            String cookieskey = "Set-Cookie";
            Map<String, List<String>> maps = httpURLConnection.getHeaderFields ();
            List<String> coolist = maps.get (cookieskey);
            if (coolist != null) {
                Iterator<String> it = coolist.iterator ();
                StringBuffer sbu = new StringBuffer ();
                while (it.hasNext ()) {
                    sbu.append (it.next ());
                }
                Cookie = sbu.toString ();
            }
            s = dealResponseResult (inptStream, CharSet);                     //处理服务器的响应结果
        } else {
            s = null;
        }
        return s;
    }


    public static Bitmap getImageBitmap(String url){
        return getImageBitmap(url,"");
    }

    /**
     * 从指定URL获取图片
     * @param url
     * @return Bitmap数据
     */
    public static Bitmap getImageBitmap (String url,String Cookies) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL (url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection ();
            conn.setDoInput (true);
            conn.setRequestProperty ("Cookie", Cookies);
            conn.connect ();
            InputStream is = conn.getInputStream ();
            bitmap = BitmapFactory.decodeStream (is);
            is.close ();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        return bitmap;
    }

    public static Bitmap getBingImage() throws IOException {
        String s= Get("http://guolin.tech/api/bing_pic");
        return   getImageBitmap(s);
    }

}
