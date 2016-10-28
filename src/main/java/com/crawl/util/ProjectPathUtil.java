package com.crawl.util;

import org.junit.Test;

/**
 * java服务器端关于 获取服务器端路径的工具类
 * @author LL
 * 2016-9-20
 */
public class ProjectPathUtil {
	/**
	 * 获取编译后的classes目录的路径
	 * 比如：/D:/Java_web/apache-tomcat-7.0.69/webapps/Purplecollar/WEB-INF/classes/
	 * @return
	 */
	public static String getClassesAbsolutePath(){
		String classesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		LogUtil.log_debug("【classes文件的路径】"+classesPath);
		return classesPath;
	}

    /**
     * 获取idea的项目的项目的绝对路径
     * @return
     */
	public static String getProjectAbsolutePath(){
		String classesPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		LogUtil.log_debug("【classes文件的路径】"+classesPath);
		int index = classesPath.indexOf("/target/classes/");
		return classesPath.substring(0, index+1);
	}

    /**
     * 测试用例
     */
    @Test
    public void test(){
        LogUtil.log_debug(getClassesAbsolutePath());
        LogUtil.log_debug(getProjectAbsolutePath());
    }
	
}
