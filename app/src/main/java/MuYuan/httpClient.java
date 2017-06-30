package MuYuan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Auther: cheny
 * CreateDate 6/28/2017.
 * 17.6.29:重构网络请求模块代码
 */

public class httpClient {
    public static String Cookie = "";

    public static ResponseDataEx Request(String url, String data, String Cookie, String Headers) throws IOException {
        return Request(url, 1, data, Cookie, Headers, 10000, 10000, "", false);
    }

    public static ResponseDataEx Request(String url, String Cookie, String Headers) throws IOException {
        return Request(url, 0, null, Cookie, Headers, 10000, 10000, "", false);
    }

    public static ResponseDataEx Request(String url) throws IOException {
        return Request(url, null, null);
    }

    public static String Request_Str(String url, String data, String Cookie, String Headers) throws IOException {
        ResponseDataEx rs = Request(url, 1, data, Cookie, Headers, 10000, 10000, "", false);
        if (rs != null) {
            return DecodeByteToString(rs.ResponseStream);
        }
        return null;
    }

    public static String Request_Str(String url, String Cookie, String Headers) throws IOException {
        ResponseDataEx rs = Request(url, Cookie, Headers);
        if (rs != null) {
            return rs.ResponseStr;
        }
        return null;
    }

    public static String Request_Str(String url) throws IOException {
        ResponseDataEx rs = Request(url);
        return DecodeByteToString(rs.ResponseStream);
    }

    /**
     * 网络访问协议
     *
     * @param HttpUrl        网页地址
     * @param RequestMethod  请求方法 0=GET 1=POST
     * @param RequestData    POST时请求的数据
     * @param RequestCookie  请求时附带的Cookie
     * @param RequestHeader  请求协议头 换行符分割
     * @param ConnectTimeout 连接超时
     * @param ReadTimeout 数据读取超时
     * @param ProxyIP 代理地址IP
     * @return 返回数据聚合
     */
    public static ResponseDataEx Request(String HttpUrl, int RequestMethod, String RequestData,
                                         String RequestCookie, String RequestHeader,
                                         int ConnectTimeout, int ReadTimeout,
                                         String ProxyIP, boolean AllowRedirect)
            throws IOException {

        URL mUrl = new URL(HttpUrl);
        //打开目标地址
        HttpURLConnection httpURLConnection = (HttpURLConnection) mUrl.openConnection();
        //设置访问方式
        switch (RequestMethod) {
            case 0:
                return GET(httpURLConnection, RequestCookie, RequestHeader, ConnectTimeout,
                        ReadTimeout, ProxyIP, AllowRedirect);
            case 1:
                return POST(httpURLConnection, RequestData, RequestCookie, RequestHeader,
                        ConnectTimeout, ReadTimeout, ProxyIP, AllowRedirect);
            default:
                return null;
        }

    }


    static ResponseDataEx GET(HttpURLConnection Url, String RequestCookie,
                              String RequestHeader, int ConnectTimeout,
                              int ReadTimeout, String ProxyIP,
                              boolean AlloRedirect) throws IOException {
        ResponseDataEx responseDataEx = new ResponseDataEx();

        //设置请求方式
        Url.setRequestMethod("GET");
        //设置连接超时
        Url.setConnectTimeout(ConnectTimeout);
        //设置读取超时
        Url.setReadTimeout(ReadTimeout);
        //打开输入流,以便从服务器读取数据
        Url.setDoInput(true);
        //设置缓存方式
        Url.setUseCaches(false);

        //设置是否允许重定向,默认自动重定向
        Url.setInstanceFollowRedirects(AlloRedirect);

        //开始设置常用协议头
        Url.setRequestProperty("Accept", "*/*");
        Url.setRequestProperty("Referer", Url.getURL().toString());
        Url.setRequestProperty("Accept-Language", "zh-cn");
        Url.setRequestProperty("Cookie", RequestCookie);
        Url.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (RequestHeader != null) {
            //开始设置自定义协议头
            String[] RequestHeaders = RequestHeader.split("\n");
            for (String Header : RequestHeaders) {
                String[] TempHeader = Header.split(":");
                if (TempHeader.length == 2) {
                    Url.setRequestProperty(TempHeader[0].trim(), TempHeader[1].trim());
                }
            }
        }

        //发送请求,并获取服务器返回的状态码
        int ResponseCode = Url.getResponseCode();

        //获取返回的协议头
        Map<String, List<String>> ResponseHeaders = Url.getHeaderFields();
        List<String> CookieList = ResponseHeaders.get("Set-Cookie");

        //判断返回的Cookie是否为空
        if (CookieList != null) {
            Iterator<String> iterator = CookieList.iterator();
            //使用StringBuffer获取数据
            StringBuffer stringBuffer = new StringBuffer();
            while (iterator.hasNext()) {
                String mCookie = iterator.next();
                stringBuffer.append(mCookie);
                CookieUpdata(mCookie);
            }
            responseDataEx.ResponseCookie = stringBuffer.toString();
        }


        if (ResponseCode == 200) {
            InputStream inputStream = Url.getInputStream();
            responseDataEx.ResponseCode = 200;
            responseDataEx.ResponseStream = DecodeInputStream(inputStream);
            responseDataEx.ResponseStr = DecodeByteToString(responseDataEx.ResponseStream);
        } else if (ResponseCode == 302) {
            //如果是涉及到302跳转,直接获取跳转地址
            responseDataEx.ResponseCode = 302;
            responseDataEx.Location = Url.getHeaderField("Location");
        }
        return responseDataEx;
    }

    static ResponseDataEx POST(HttpURLConnection Url, String RequestData,
                               String RequestCookie, String RequestHeader,
                               int ConnectTimeout, int ReadTimeout,
                               String ProxyIP, boolean isRedirect) throws IOException {
        byte[] data = RequestData.getBytes();

        ResponseDataEx responseDataEx = new ResponseDataEx();
        //设置请求方式
        Url.setRequestMethod("POST");
        //设置输出输入流
        Url.setDoInput(true);
        Url.setDoOutput(true);
        //设置连接超时
        Url.setConnectTimeout(ConnectTimeout);
        Url.setReadTimeout(ReadTimeout);

        //设置重定向选项
        Url.setInstanceFollowRedirects(isRedirect);

        //设置使用缓存
        Url.setUseCaches(false);

        //设置默认内置协议头
        Url.setRequestProperty("Accept", "*/*");
        Url.setRequestProperty("Referer", Url.getURL().toString());
        Url.setRequestProperty("Accept-Language", "zh-cn");
        Url.setRequestProperty("Cookie", RequestCookie);
        Url.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (RequestHeader != null) {
            //开始设置用户设置协议头
            String[] RequestHead = RequestHeader.split("\n");
            for (String head : RequestHead) {
                String[] d = head.split(":");
                if (d.length >= 2) {
                    Url.setRequestProperty(d[0].trim(), d[1].trim());
                }
            }
        }

        //开始设置POST方式独有的数据流长度
        Url.setRequestProperty("Content-Length", String.valueOf(data.length));

        OutputStream outputStream = Url.getOutputStream();

        //写入POST数据流
        outputStream.write(data);

        //获取服务器响应代码
        int responseCode = Url.getResponseCode();

        //获取响应协议头
        Map<String, List<String>> ResponseCookie = Url.getHeaderFields();

        //开始获取Cookie
        List<String> CookieList = ResponseCookie.get("Set-Cookie");

        if (CookieList != null) {
            //迭代器枚举
            Iterator<String> iterator = CookieList.iterator();
            StringBuffer stringBuffer = new StringBuffer();

            while (iterator.hasNext()) {
                String mCookie = iterator.next();
                stringBuffer.append(mCookie);
                CookieUpdata(mCookie);
            }
            responseDataEx.ResponseCookie = stringBuffer.toString();
        }

        if (responseCode == 200) {

            //服务器返回200表示成功,开始获取相关数据
            responseDataEx.ResponseCode = 200;

            //获取服务器响应数据
            InputStream inputStream = Url.getInputStream();

            responseDataEx.ResponseStream = DecodeInputStream(inputStream);
            responseDataEx.ResponseStr = DecodeByteToString(responseDataEx.ResponseStream);
        } else if (responseCode == 302) {
            responseDataEx.ResponseCode = 302;
            responseDataEx.Location = Url.getHeaderField("Location");
        }
        return responseDataEx;
    }

    static byte[] DecodeInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len;
        byte[] temp = new byte[1024];
        while ((len = in.read(temp)) != -1) {
            byteArrayOutputStream.write(temp, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String DecodeByteToString(byte[] data) {
        return new String(data);
    }

    public static String DecodeByteToString(byte[] data, String Chaset) throws UnsupportedEncodingException {
        return new String(data, Chaset);
    }

    public static Bitmap DecodeByteToImage(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }


    public static String EncodeStr(String str) throws UnsupportedEncodingException {
        return EncodeStr(str, "UTF-8");
    }

    public static String EncodeStr(String str, String chaset) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, chaset);
    }

    public static void CookieUpdata(String NewCookie) {
        Cookie += NewCookie;
    }

    public static Bitmap getBingImage() throws IOException {
        ResponseDataEx s = Request("http://guolin.tech/api/bing_pic");
        s = Request(s.ResponseStr);
        return DecodeByteToImage(s.ResponseStream);
    }


    /**
     * 创建高斯模糊后的图片
     * @param image 原图
     * @param context getApplicationContext()
     * @param blur 0-25.0f
     * @return 返回处理后的图片
     */

    public static Bitmap BitmapBlur(Bitmap image, Context context,float blur){
        //首先创建一个临时Image
        Bitmap TempBitmap=Bitmap.createBitmap(image.getWidth(),image.getHeight(),
                Bitmap.Config.ARGB_8888);

        //初始化模糊脚本
        RenderScript renderScript=RenderScript.create(context);

        //初始化过滤脚本
        ScriptIntrinsicBlur scriptIntrinsicBlur=ScriptIntrinsicBlur.create(
                renderScript, Element.U8_4(renderScript));

        //设置allocation
        Allocation allocationIn=Allocation.createFromBitmap(renderScript,image);
        Allocation allocationOut=Allocation.createFromBitmap(renderScript,TempBitmap);

        scriptIntrinsicBlur.setRadius(blur);
        scriptIntrinsicBlur.setInput(allocationIn);
        scriptIntrinsicBlur.forEach(allocationOut);

        allocationOut.copyTo(TempBitmap);

        renderScript.destroy();
        image.recycle();
        return TempBitmap;
    }

}
