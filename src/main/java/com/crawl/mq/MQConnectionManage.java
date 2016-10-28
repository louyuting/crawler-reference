package com.crawl.mq;

import com.crawl.config.Config;
import com.crawl.util.MyLogger;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Created by Administrator on 2016/8/28 0028.
 */
public class MQConnectionManage {
    private static Logger logger = MyLogger.getLogger(MQConnectionManage.class);
    private static ConnectionFactory connectionFactory;
    private static Connection connection;
    static {
        connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                Config.brokerURL);
    }
    public static Connection createConnection(){
        try {
            connection = connectionFactory.createConnection();
        } catch (JMSException e) {
            logger.error("JMSException", e);
        }
        return connection;
    }
    public static void close(Connection connection){
        try {
            connection.close();
        } catch (JMSException e) {
            logger.error("JMSException", e);
        }
    }
}
