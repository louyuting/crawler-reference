package com.crawl.zhihu.task;

import com.crawl.entity.Page;
import com.crawl.util.MyLogger;
import com.crawl.zhihu.ZhiHuHttpClient;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

/**
 * 下载网页的线程（任务）
 */
public class DownloadTask implements Runnable{
	private static Logger logger = MyLogger.getMyLogger(DownloadTask.class);
	/**待下载的url*/
	private String url;
    //获取zhihuhttpclient的实例
	private static ZhiHuHttpClient zhiHuHttpClient = ZhiHuHttpClient.getInstance();

	/**
	 * 下载网页任务的构造器
	 * @param url 待下载的构造器
	 */
	public DownloadTask(String url){
		this.url = url;
	}

	/**
	 * run函数
	 */
	public void run(){
		try {
            //通过url获取当前页面的信息
			Page page = zhiHuHttpClient.getWebPage(url);
			int status = page.getStatusCode();

			logger.info(Thread.currentThread().getName() + " executing request " + page.getUrl() + "   status:" + status);
			if(status == HttpStatus.SC_OK){
				/**
				 * 下载成功，将网页内容放到解析线程池，等待解析
				 */
				zhiHuHttpClient.getParseThreadExecutor().execute(new ParseTask(page));
			}
			else if(status == 502 || status == 504 || status == 500 || status == 429){
				/**
				 * 将请求继续加入下载线程池
                 */
				Thread.sleep(100);
				zhiHuHttpClient.getDownloadThreadExecutor().execute(new DownloadTask(url));
				return ;
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException",e);
		} catch (NullPointerException e){
			logger.error("NullPointerException",e);
		}
	}
}
