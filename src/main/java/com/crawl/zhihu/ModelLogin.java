package com.crawl.zhihu;

import com.crawl.config.Config;
import com.crawl.util.HttpClientUtil;
import com.crawl.util.MyLogger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 模拟登录知乎
 */
public class ModelLogin {
    private static Logger logger = MyLogger.getMyLogger(ModelLogin.class);
    //知乎首页
    final private static String INDEX_URL = "https://www.zhihu.com";
    //邮箱登录地址
    final private static String EMAIL_LOGIN_URL = "https://www.zhihu.com/login/email";
    //手机号码登录地址
    final private static String PHONENUM_LOGIN_URL = "https://www.zhihu.com/login/phone_num";
    //登录验证码地址
    final private static String YZM_URL = "https://www.zhihu.com/captcha.gif?type=login";
    /**
     * @param emailOrPhoneNum 邮箱或手机号码
     * @param pwd 密码
     * @return
     */
    public boolean login(ZhiHuHttpClient zhiHuHttpClient,
                         String emailOrPhoneNum,
                         String pwd){
        CloseableHttpClient httpClient = zhiHuHttpClient.getCloseableHttpClient();
        HttpClientContext context = zhiHuHttpClient.getHttpClientContext();
        String yzm = null;//验证码
        String loginState = null;//登录知乎后返回的JSON
        HttpGet getRequest = new HttpGet(INDEX_URL);
        /**
         *1.首先发送一次请求知乎首页的请求，表示即将登陆知乎 *
         */
        HttpClientUtil.getWebPage(httpClient,context, getRequest, "utf-8", true);
        /**
         * 2.定义POST请求，设定登录条件 *
         */
        HttpPost request = null;
        //表单参数键值对
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        if(emailOrPhoneNum.contains("@")){
            //通过邮箱登录
            request = new HttpPost(EMAIL_LOGIN_URL);
            formParams.add(new BasicNameValuePair("email", emailOrPhoneNum));
        }
        else {
            //通过手机号码登录
            request = new HttpPost(PHONENUM_LOGIN_URL);
            formParams.add(new BasicNameValuePair("phone_num", emailOrPhoneNum));
        }
        //调用方法获取验证码
        yzm = yzm(httpClient, context,YZM_URL);//肉眼识别验证码
        formParams.add(new BasicNameValuePair("captcha", yzm));
        formParams.add(new BasicNameValuePair("_xsrf", ""));//这个参数可以不用
        formParams.add(new BasicNameValuePair("password", pwd));
        formParams.add(new BasicNameValuePair("remember_me", "true"));
        //创建form表单实体
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(formParams, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        request.setEntity(entity);
        /**
         * 3. 登录知乎，返回的是JSON，表示是否登录成功
         */
        loginState = HttpClientUtil.getWebPage(httpClient,context, request, "utf-8", false);//登录
        JSONObject jo = new JSONObject(loginState);
        if(jo.get("r").toString().equals("0")){
            /**
             * 3.1 登录成功
             */
            logger.info("登录知乎成功");
            getRequest = new HttpGet("https://www.zhihu.com");
            /**
             * 访问首页
             */
            HttpClientUtil.getWebPage(httpClient,context ,getRequest, "utf-8", false);
            /**
             * 序列化CookieStore到本地文件
             */
            HttpClientUtil.serializeObject(context.getCookieStore(),"src/main/resources/zhihucookies");
            return true;
        }else{
            logger.info("登录知乎失败");
            throw new RuntimeException(HttpClientUtil.decodeUnicode(loginState));
        }
    }
    /**
     * 肉眼识别验证码
     * @param httpClient Http客户端
     * @param context Http上下文
     * @param url 验证码地址
     * @return 验证码
     */
    private String yzm(CloseableHttpClient httpClient,HttpClientContext context, String url){
        //路径包含图片文件名和后缀
        String verificationCodePath = Config.verificationCodePath;
        //获取文件目录
        String path = verificationCodePath.substring(0, verificationCodePath.lastIndexOf("/") + 1);
        //获取文件名
        String fileName = verificationCodePath.substring(verificationCodePath.lastIndexOf("/") + 1);

        //调用接口下载验证码图片
        HttpClientUtil.downloadFile(httpClient, context, url, path, fileName,true);

        logger.info("请输入 " + verificationCodePath + "路径下的验证码：");
        Scanner sc = new Scanner(System.in);
        String yzm = sc.nextLine();
        return yzm;
    }
}
