package com.crawl.dao;

import com.crawl.entity.User;
import com.crawl.util.MyLogger;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 *
 */
public class ZhiHuDAO {
    private static Logger logger = MyLogger.getMyLogger(ZhiHuDAO.class);

    /**
     * 数据库表初始化，创建数据库表。
     * 如果表已经存在就不创建
     * @param cn
     */
    public static void DBTablesInit(Connection cn){
        ResultSet rs = null;
        Properties p = new Properties();
        try {
            //加载properties文件
            p.load(ZhiHuDAO.class.getResourceAsStream("/config.properties"));
            //获取表头的信息
            rs = cn.getMetaData().getTables(null, null, "href", null);
            Statement st = cn.createStatement();
            //不存在href表
            if(!rs.next()){
                //创建href表
                st.execute(p.getProperty("createHrefTable"));
                logger.info("href表创建成功");
            }
            else{
                logger.info("href表已存在");
            }
            rs = cn.getMetaData().getTables(null, null, "user", null);
            //不存在user表
            if(!rs.next()){
                //创建user表
                st.execute(p.getProperty("createUserTable"));
                logger.info("user表创建成功");
            }
            else{
                logger.info("user表已存在");
            }
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 同步方法（获取class的锁）
     * 判断该数据库中是否存在该用户
     * @param cn
     * @param sql 判断该sql数据库中是否存在
     * @return
     */
    public synchronized static boolean isContain(Connection cn, String sql) throws SQLException {
        int num;
        PreparedStatement pstmt;
        pstmt = cn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            num = rs.getInt("count(*)");
            if(num == 0){
                return false;
            }else{
                return true;
            }
        }
        rs.close();
        pstmt.close();
        return true;
    }


    /**
     * 同步方法（获取class的锁）
     * user 插入数据库
     * @param user
     * @throws SQLException
     */
    public synchronized static boolean insertUserToDB(User user){
        String isContainSql = "select count(*) from user WHERE url='" + user.getUrl() + "'";
        Connection cn = ConnectionManage.getConnection();
        try {
            if(isContain(cn,isContainSql)){
                logger.info("数据库已经存在该用户---" + user.getUrl() + "---当前已获取用户数量为:");
                return false;
            }
            String colum = "location,business,sex,employment,username,url,agrees,thanks,asks," +
                    "answers,posts,followees,followers,hashId,education";
            String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            String sql = "insert into user (" + colum + ") values(" +values+")";
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            pstmt.setString(1,user.getLocation());
            pstmt.setString(2,user.getBusiness());
            pstmt.setString(3,user.getSex());
            pstmt.setString(4,user.getEmployment());
            pstmt.setString(5,user.getUsername());
            pstmt.setString(6,user.getUrl());
            pstmt.setInt(7,user.getAgrees());
            pstmt.setInt(8,user.getThanks());
            pstmt.setInt(9,user.getAsks());
            pstmt.setInt(10,user.getAnswers());
            pstmt.setInt(11,user.getPosts());
            pstmt.setInt(12,user.getFollowees());
            pstmt.setInt(13,user.getFollowers());
            pstmt.setString(14,user.getHashId());
            pstmt.setString(15,user.getEducation());
            //执行插入
            pstmt.executeUpdate();
            pstmt.close();
            cn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 将访问过的url插入数据库
     * @param md5Href 经过md5处理后的url
     * @return
     * @throws SQLException
     */
    public synchronized static boolean insertHrefToDBIfNotExist(String md5Href){
        String isContainSql = "select count(*) from href WHERE href='" + md5Href + "'";
        Connection cn = ConnectionManage.getConnection();
        try {
            if(isContain(cn,isContainSql)){
                logger.info("数据库已经存在该url---" + md5Href);
                return false;
            }
            String sql = "insert into href (href) values( ?)";
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            pstmt.setString(1,md5Href);
            pstmt.executeUpdate();
            pstmt.close();
            cn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("url插入成功---");
        return true;
    }

    /**
     * 直接插入url
     * @param md5Href
     */
    public static void insertHrefInDBDirect(Connection conn, String md5Href){
        try {
            String sql = "insert into href (href) values(?)";
            PreparedStatement pstmt;
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, md5Href);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("url插入成功---");
    }
    /**
     * 清空表
     * @param cn
     * @throws SQLException
     */
    public synchronized static void deleteHrefTable(Connection cn){
        String sql = "DELETE FROM href";
        PreparedStatement pstmt = null;
        try {
            pstmt = cn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("href表删除成功---");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
