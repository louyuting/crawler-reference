package com.crawl.zhihu;

import com.crawl.config.Config;
import com.crawl.dao.ConnectionManage;
import com.crawl.dao.ZhiHuDAO;
import com.crawl.httpclient.HttpClient;
import com.crawl.mq.Receiver;
import com.crawl.mq.handler.DetailUrlMessageHandler;
import com.crawl.mq.handler.ListUrlMessageHandler;
import com.crawl.util.MyLogger;
import com.crawl.util.ProjectPathUtil;
import com.crawl.util.ThreadPoolMonitor;
import com.crawl.zhihu.task.DownloadTask;
import com.crawl.zhihu.task.ParseTask;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * 知乎的工具类
 */
public class ZhiHuHttpClient extends HttpClient{

    public static Logger logger = MyLogger.getLogger(ZhiHuHttpClient.class);
    /**
     * 内部类，创建了ZhiHuHttpClient实例
     */
    private static class ZhiHuHttpClientHolder {
        private static final ZhiHuHttpClient INSTANCE = new ZhiHuHttpClient();
    }
    /**
     * 获取ZhiHuHttpClient 实例
     * @return
     */
    public static final ZhiHuHttpClient getInstance(){
        return ZhiHuHttpClientHolder.INSTANCE;
    }
    /**
     * 定义解析网页线程池
     */
    private ThreadPoolExecutor parseThreadExecutor;
    /**
     * 定义下载网页线程池
     */
    private ThreadPoolExecutor downloadThreadExecutor;


    /**
     * 构造器里面
     */
    public ZhiHuHttpClient() {
        initHttpClient();
        intiThreadPool();
        /**
         * 两个监控器开始运行
         */
        new Thread(new ThreadPoolMonitor(downloadThreadExecutor, "DownloadPage ThreadPool")).start();
        new Thread(new ThreadPoolMonitor(parseThreadExecutor, "ParsePage ThreadPool")).start();
    }
    /**
     * 初始化知乎客户端
     * 模拟登录知乎，持久化Cookie到本地
     * 不用以后每次都登录
     */
    @Override
    public void initHttpClient() {
        if(!antiSerializeCookieStore(ProjectPathUtil.getClassesAbsolutePath()+"zhihucookies")){
            new ModelLogin().login(this, Config.emailOrPhoneNum, Config.password);
        }
        if(Config.dbEnable){
            ZhiHuDAO.DBTablesInit(ConnectionManage.getConnection());
        }
    }
    /**
     * 初始化线程池（包括解析线程池和下载线程池）
     */
    private void intiThreadPool(){
        //参数：池中所保存的线程数；池中允许的最大线程数；当线程数大于核心时，终止前多余的空闲线程等待新任务的最长时间；
        //keepAliveTime 参数的时间单位; 执行前用于保持任务的队列。此队列仅保持由 execute 方法提交的 Runnable 任务。
        //创建解析线程池对象
        parseThreadExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        //创建爬取线程池对象
        downloadThreadExecutor = new ThreadPoolExecutor(Config.downloadThreadSize,
                Config.downloadThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }


    /**
     * 爬虫入口程序
     */
    public void startCrawl(){
    	//支持分布式
        if(Config.distributedEnable){
        	//创建一个线程，参数是一个实现Runable接口的子类
            new Thread( new Receiver(Config.userDetailUrlQueueName, new DetailUrlMessageHandler()) ).start();
            new Thread( new Receiver(Config.userFolloweeUrlQueueName, new ListUrlMessageHandler()) ).start();
        }
        //不支持分布式，即单击运行
        else {
            downloadThreadExecutor.execute(new DownloadTask(Config.startURL));
        }
        manageZhiHuClient();
    }

    /**
     * 管理知乎客户端
     * 负责正常关闭
     */
    public void manageZhiHuClient(){
        if(Config.distributedEnable){
            return;
        }
        while (true) {
            /**
             * 下载网页数
             */
            long downloadPageCount = downloadThreadExecutor.getTaskCount();
            if (downloadPageCount >= Config.downloadPageCount &&
                    ! ZhiHuHttpClient.getInstance().getDownloadThreadExecutor().isShutdown()) {
                ParseTask.isStopDownload = true;
                /**
                 * shutdown 下载网页线程池
                 */
                ZhiHuHttpClient.getInstance().getDownloadThreadExecutor().shutdown();
            }
            if(ZhiHuHttpClient.getInstance().getDownloadThreadExecutor().isTerminated() &&
                    !ZhiHuHttpClient.getInstance().getParseThreadExecutor().isShutdown()){
                /**
                 * 下载网页线程池关闭后，再关闭解析网页线程池
                 */
                ZhiHuHttpClient.getInstance().getParseThreadExecutor().shutdown();
            }
            if(ZhiHuHttpClient.getInstance().getParseThreadExecutor().isTerminated()){
                /**
                 * 关闭线程池Monitor
                 */
                ThreadPoolMonitor.isStopMonitor = true;
                logger.info("--------------爬取结果--------------");
                logger.info("获取用户数:" +ParseTask.parseUserCount.get());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ThreadPoolExecutor getParseThreadExecutor() {

        return parseThreadExecutor;
    }

    public ThreadPoolExecutor getDownloadThreadExecutor() {

        return downloadThreadExecutor;
    }
}
