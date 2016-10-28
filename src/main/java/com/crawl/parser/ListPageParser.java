package com.crawl.parser;

import com.crawl.entity.Page;

import java.util.List;

/**
 * Created by yangwang on 16-8-23.
 */
public abstract class ListPageParser implements Parser {
    /**
     * 需要传入Page对象
     * @param page
     * @return
     */
    public abstract List<String> parse(Page page);
}
