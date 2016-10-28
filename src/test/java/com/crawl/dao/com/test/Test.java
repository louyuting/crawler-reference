package com.crawl.dao.com.test;

import java.util.Map;

/**
 * Created by Administrator on 2016/10/28.
 */
public class Test {

    @org.junit.Test
    public void test(){

        Map<String,String> envs = System.getenv();

        for(String key: envs.keySet()){
            System.out.println(key+":" + envs.get(key));
        }
    }

}
