package com.crawl.zhihu.task;

import com.crawl.config.Config;
import com.crawl.dao.ConnectionManage;
import com.crawl.dao.ZhiHuDAO;
import com.crawl.entity.Page;
import com.crawl.entity.User;
import com.crawl.mq.Sender;
import com.crawl.parser.zhihu.ZhiHuUserFollowingListPageParser;
import com.crawl.parser.zhihu.ZhiHuUserIndexDetailPageParser;
import com.crawl.util.Md5Util;
import com.crawl.util.MyLogger;
import com.crawl.zhihu.ZhiHuHttpClient;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 解析网页线程任务
 */
public class ParseTask implements Runnable {
    private static Logger logger = MyLogger.getLogger(ParseTask.class);
    private Page page;
    private static ZhiHuHttpClient zhiHuHttpClient = ZhiHuHttpClient.getInstance();
    /**
     * 统计用户数量
     */
    public static AtomicInteger parseUserCount = new AtomicInteger(0);
    public static volatile boolean isStopDownload = false;

    /**
     * 构造器
     * @param page 待解析的网页
     */
    public ParseTask(Page page){
        this.page = page;
    }


    @Override
    public void run() {
        parse();
    }

    /**
     * 解析网页的过程
     */
    private void parse(){
        Document doc = Jsoup.parse(page.getHtml());
        //如果当前用户包含关注的人
        if(doc.select("title").size() != 0) {
            //解析获取当前用户的信息
            User user = ZhiHuUserIndexDetailPageParser.getInstance().parse(page);
            //持久化到数据库
            if(Config.dbEnable){
                ZhiHuDAO.insertUserToDB(user);
            }
            parseUserCount.incrementAndGet();
            logger.info("解析用户成功:" + user.toString());
            /**
             * 获取关注用户列表,知乎每次最多返回20个关注用户
             */
            List<String> userIndexHref = new ArrayList<String>(user.getFollowees());
            for(int i = 0;i < user.getFollowees()/20 + 1;i++) {
                /**
                 * 获取关注用户列表页url
                 */
                String userFolloweesUrl = formatUserFolloweesUrl(20*i, user.getHashId());
                userIndexHref.add(userFolloweesUrl);
            }
            handleListUrl(userIndexHref);
        }
        else {
            /**
             * "我关注的人"列表页
             */
            if(!isStopDownload && zhiHuHttpClient.getDownloadThreadExecutor().getQueue().size() <= 100){
                List<String> userIndexHref = ZhiHuUserFollowingListPageParser.getInstance().parse(page);
                for(String s : userIndexHref){
                    handleUrl(s);
                }
            }
        }
    }
    public String formatUserFolloweesUrl(int offset, String userHashId){
        String url = "https://www.zhihu.com/node/ProfileFolloweesListV2?params={%22offset%22:" + offset + ",%22order_by%22:%22created%22,%22hash_id%22:%22" + userHashId + "%22}";
        url = url.replaceAll("[{]", "%7B").replaceAll("[}]", "%7D").replaceAll(" ", "%20");
        return url;
    }


    /**
     * 处理一个url
     * @param url
     */
    private void handleUrl(String url){
        if(!Config.dbEnable){//不持久化到数据库
            /**
             * 当下载网页队列小于100时才获取该用户关注用户
             * 防止下载网页线程池任务队列过量增长
             */
            if (!isStopDownload && zhiHuHttpClient.getDownloadThreadExecutor().getQueue().size() <= 100) {
                zhiHuHttpClient.getDownloadThreadExecutor().execute(new DownloadTask(url));
            }
        }
        else {//持久化到数据库
            String md5Url = Md5Util.Convert2Md5(url);
            /**
             * 该url是已经download
             */
            boolean isRepeat = ZhiHuDAO.insertHrefToDBIfNotExist(md5Url);
            if(!Config.distributedEnable){//不支持分布式
                if(!isRepeat ||
                        (!zhiHuHttpClient.getDownloadThreadExecutor().isShutdown() &&
                                zhiHuHttpClient.getDownloadThreadExecutor().getQueue().size() < 30)){
                    /**
                     * 防止互相等待，导致死锁
                     */
                    zhiHuHttpClient.getDownloadThreadExecutor().execute(new DownloadTask(url));
                }
            }
            else {//分布式处理，发送url到消息队列
                Sender.sendMessage(url, Config.userDetailUrlQueueName);
            }
        }
    }


    /**
     * 处理Url集合：
     * @param urlList
     */
    private void handleListUrl(List<String> urlList){
        //获取数据库连接
        Connection conn = ConnectionManage.getConnection();
        //迭代
        for(String url : urlList){
            //将url地址进行MD5加密
            String md5Url = Md5Util.Convert2Md5(url);
            ZhiHuDAO.insertHrefInDBDirect(conn, md5Url);
            /**
             * 用户关注列表页url
             */
            Sender.sendMessage(url, Config.userFolloweeUrlQueueName);
        }
        //关闭数据库连接
        ConnectionManage.close();
    }
}


