package MuYuan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by QiuChenly on 2017/4/22.
 */

public class HttpUntils {

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
    public static String submitPostData(URL url, String Datas, String Cookie, String ContentType) throws IOException {
        return submitPostData(url, Datas.getBytes(), Cookie, ContentType);
    }


    /**
     * 提交自定义数据到服务器
     *
     * @param url         网址
     * @param Datas       数据,字节
     * @param Cookie      附带的Cookie,有时候服务器会需要你提供这个
     * @param ContentType 协议头
     * @return 返回网页数据
     * @throws IOException IO异常捕捉,请在外部调用Try
     */
    public static String submitPostData(URL url, byte[] Datas, String Cookie, String ContentType) throws IOException {

        byte[] data = Datas;//获得请求体
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", ContentType);
            httpURLConnection.setRequestProperty("Cookie", Cookie);
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
         * Function  :   处理服务器的响应结果（将输入流转化成字符串）
         * Param     :   inputStream服务器的响应输入流
         * Author    :   博客园-依旧淡然
         */
    public static String dealResponseResult(InputStream inputStream) throws UnsupportedEncodingException {
        String resultData;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public static String dealResponseResult(InputStream inputStream,String CharSet) throws UnsupportedEncodingException {
        String resultData;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray(), CharSet);
        return resultData;
    }


    /**
     * 获取指定URL的响应字符串
     *
     * @param urlString
     * @return
     */
    public static String getURLResponse(String urlString) throws IOException {
        String s = null;
        URL url = new URL(urlString); //URL对象
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
        httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
        //httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
        httpURLConnection.setRequestMethod("GET");     //设置以Post方式提交数据
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestProperty("Accept", "*/*");
        httpURLConnection.setRequestProperty("Referer", urlString);
        httpURLConnection.setRequestProperty("Accept-Language", "zh-cn");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
        if (response == HttpURLConnection.HTTP_OK) {
            InputStream inptStream = httpURLConnection.getInputStream();
            s = dealResponseResult(inptStream,"GBK");                     //处理服务器的响应结果
        } else {
            s = null;
        }
        return s;
    }

    /**
     * 从指定URL获取图片
     *
     * @param url
     * @return
     */
    public static Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
