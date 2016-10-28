package com.crawl.mq;

/**
 * Created by Administrator on 2016/8/27 0027.
 * 接收消息
 */
import com.crawl.mq.handler.MessageHandler;
import com.crawl.util.MyLogger;
import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * 实现Runable 接口的自定义类
 */
public class Receiver implements Runnable{
    //定义日志logger
    public static Logger logger = MyLogger.getMyLogger(Receiver.class);
    /**
     * 队列的名称
     */
    private String queueName;
    /**
     * 定义的handler
     */
    private MessageHandler messageHandler;

    /**
     * 构造器
     * @param queueName
     * @param messageHandler
     */
    public Receiver(String queueName, MessageHandler messageHandler){
        this.messageHandler = messageHandler;
        this.queueName = queueName;
    }

    /**
     * 接收消息
     * @param queueName
     */
    private void receiverMessage(String queueName){
        // Connection ：JMS 客户端到JMS Provider 的连接
        Connection connection = null;
        // Session： 一个发送或接收消息的线程
        Session session;
        // Destination ：消息的目的地;消息发送给谁.
        Destination destination;
        // 消费者，消息接收者
        MessageConsumer consumer;
        try {
            connection = MQConnectionManage.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.FALSE,
                    Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            consumer = session.createConsumer(destination);
            logger.info("消息接收中");
            while (true) {
                TextMessage message = (TextMessage) consumer.receive(10000);
                if (null != message) {
                    logger.info("成功收到消息" + message.getText());
                    messageHandler.handler(message.getText());
                } else {
                    break;
                }
            }
            logger.info("暂无消息");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != connection)
                    connection.close();
            } catch (Throwable ignore) {
            }
        }
    }

    /**
     * 重载run方法
     */
    @Override
    public void run() {
        receiverMessage(queueName);
    }
}
