package com.crawl.config;

import com.crawl.util.ProjectPathUtil;
import com.crawl.util.PropertiesUtil;

import java.util.Properties;

/**
 * Created by Administrator on 2016/8/23 0023.
 * 加载配置文件
 */
public class Config {
    /**
     * 是否持久化到数据库
     */
    public static boolean dbEnable;
    /**
     * 下载网页线程数
     */
    public static int downloadThreadSize;
    /**
     * 验证码保存在本地的路径
     */
    public static String verificationCodePath;
    /**
     * 知乎注册手机号码或有限
     */
    public static String emailOrPhoneNum;
    /**
     * 知乎密码
     */
    public static String password;
    /**
     * 爬虫入口
     */
    public static String  startURL;
    /**
     * 下载网页数
     */
    public static int downloadPageCount;
    /**
     * db.name
     */
    public static String dbName;
    /**
     * db.username
     */
    public static String dbUsername;
    /**
     * db.host
     */
    public static String dbHost;
    /**
     * db.password
     */
    public static String dbPassword;
    /**
     * 创建href表语句
     */
    public static String createHrefTable;
    /**
     * 创建user表语句
     */
    public static String createUserTable;
    public static boolean distributedEnable;
    public static String brokerURL;
    public static String userDetailUrlQueueName;
    public static String userFolloweeUrlQueueName;
    static {
        /*Properties p = new Properties();
        try {
            p.load(Config.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //Properties p = PropertiesUtil.getProperties("C:\\Users\\Administrator\\git\\mycrawler\\ZhihuCrawler\\target\\classes\\config.properties");
        Properties p = PropertiesUtil.getProperties(ProjectPathUtil.getClassesAbsolutePath()+"config.properties");
        dbEnable = Boolean.parseBoolean(p.getProperty("db.enable"));
        verificationCodePath = p.getProperty("verificationCodePath");
        emailOrPhoneNum = p.getProperty("zhiHu.emailOrPhoneNum");
        password = p.getProperty("zhiHu.password");
        startURL = p.getProperty("startURL");
        downloadPageCount = Integer.valueOf(p.getProperty("downloadPageCount"));
        downloadThreadSize = Integer.valueOf(p.getProperty("downloadThreadSize"));
        distributedEnable = Boolean.parseBoolean(p.getProperty("distributedEnable"));
        if (dbEnable){
            dbName = p.getProperty("db.name");
            dbHost = p.getProperty("db.host");
            dbUsername = p.getProperty("db.username");
            dbPassword = p.getProperty("db.password");
            createHrefTable = p.getProperty("createHrefTable");
            createUserTable = p.getProperty("createUserTable");
        }
        if (distributedEnable){
            brokerURL = p.getProperty("brokerURL");
            userDetailUrlQueueName = p.getProperty("userDetailUrlQueueName");
            userFolloweeUrlQueueName = p.getProperty("userFolloweeUrlQueueName");
        }
    }

}
