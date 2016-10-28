package com.crawl.util;

import org.apache.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控的工具类，监视ThreadPoolExecutor执行情况
 */
public class ThreadPoolMonitor implements Runnable{
    private static Logger logger = MyLogger.getMyLogger(ThreadPoolMonitor.class);

    //线程池对象
    private ThreadPoolExecutor executor;
    //是否停止监控
    public static volatile boolean isStopMonitor=false;
    //线程池名称
    private String name = "";

    //构造器
    public ThreadPoolMonitor(ThreadPoolExecutor executor,String name){
        this.executor = executor;
        this.name = name;
    }

    /**
     * 线程的run方法
     */
    @Override
    public void run(){
        while(!isStopMonitor){
            logger.info(name +
                    /*String.format("[monitor] [%d/%d] Active: %d, Completed: %d, queueSize: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getQueue().size(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));*/
                    String.format("【监控】: 池中的当前线程数/池中保存的线程数 [%d/%d] 正在执行的线程数: %d, 完成的线程数: %d, 任务队列的大小: %d, 计划执行任务数: %d, 程序是否终止: %s, 关闭时所有任务是否已经完成: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getQueue().size(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("线程池监控出现异常！",e);
            }
        }
    }
}