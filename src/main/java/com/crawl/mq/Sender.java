package com.crawl.mq;

/**
 * Created by Administrator on 2016/8/27 0027.
 * 发送消息
 */
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import com.crawl.config.Config;
import com.crawl.util.MyLogger;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.log4j.Logger;

public class Sender {
    private static Logger logger = MyLogger.getLogger(Sender.class);
    public static void main(String[] args) {
        sendMessage("https://www.zhihu.com/people/wo-yan-chen-mo/followees", Config.userDetailUrlQueueName);
    }
    /**
     * @param url 消息内容,待爬取url
     * @param queueName 消息队列名
     */
    public static void sendMessage(String url, String queueName){
        // Connection ：JMS 客户端到JMS Provider 的连接
        Connection connection = MQConnectionManage.createConnection();
        // Session： 一个发送或接收消息的线程
        Session session;
        // Destination ：消息的目的地;消息发送给谁.
        Destination destination;
        // MessageProducer：消息发送者
        MessageProducer producer;
        try {
            // 启动
            connection.start();
            session = connection.createSession(Boolean.TRUE,
                    Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);
            producer = session.createProducer(destination);
            // 设置不持久化
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = session.createTextMessage(url);
            producer.send(message);
            session.commit();
            logger.info("消息发送成功--" + url);
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            try {
                if (null != connection)
                    connection.close();
            } catch (Throwable ignore) {
            }
        }
    }
}
