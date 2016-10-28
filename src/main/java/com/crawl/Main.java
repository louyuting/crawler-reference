package com.crawl;

import com.crawl.zhihu.ZhiHuHttpClient;

/**
 * Created by LL  on 2016/8/23 0023.
 * 爬虫入口
 */
public class Main {
    public static void main(String args []){

        ZhiHuHttpClient.getInstance().startCrawl();
    }
}
