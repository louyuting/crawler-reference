package com.crawl.util;

import org.apache.log4j.Logger;

public class MyLogger extends Logger{
    protected MyLogger(String name) {

        super(name);
    }
    public static Logger getMyLogger(Class<?> c){
        Logger logger = Logger.getLogger(c);
        return logger;
    }
}
