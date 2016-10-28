package com.crawl.httpclient;

import com.crawl.entity.Page;
import com.crawl.util.HttpClientUtil;
import com.crawl.util.MyLogger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 *  httpclient的基类
 */
public abstract class HttpClient {
    /**
     * 日志器
     */
    private Logger logger = MyLogger.getLogger(HttpClient.class);
    /**
     * 单例模式
     */
    private static HttpClient httpClient;
    /**
     * 定义httpclient
     */
    protected CloseableHttpClient closeableHttpClient;
    /**
     * 定义运行的上下文
     */
    protected HttpClientContext httpClientContext;
    /**
     * response
     */
    private CloseableHttpResponse closeableHttpResponse;

    /**
     * 构造器
     */
    protected HttpClient(){
        //获取设置了cookie策略的客户端
        this.closeableHttpClient = HttpClientUtil.getMyHttpClient();
        //获取设置了cookie策略的上下文
        this.httpClientContext = HttpClientUtil.getMyHttpClientContext();
    }


    /**
     * 执行request获取response
     * @param request
     * @return
     */
    private CloseableHttpResponse getResponse(HttpRequestBase request){
        try {
            closeableHttpResponse = closeableHttpClient.execute(request, this.httpClientContext);
            return closeableHttpResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 通过连接获取响应流
     * @param url
     * @return
     */
    public InputStream getWebPageInputStream(String url){
        HttpGet httpGet = new HttpGet(url);
        return getWebPageInputStream(httpGet);
    }


    /**
     * 根据request返回entity的流
     * @param request
     * @return
     */
    public InputStream getWebPageInputStream(HttpRequestBase request){
        try {
            HttpEntity httpEntity = getResponse(request).getEntity();
            return httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 个人剧url 获取网页的内容
     * @param url
     * @return
     */
    public String getWebPageContent(String url){
        InputStream is = getWebPageInputStream(url);
        if(is != null){
            try {
                return IOUtils.toString(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 提取出网页的内容对象
     * @param url
     * @return
     */
    public Page getWebPage(String url){
        return getWebPag(new HttpGet(url));
    }

    /**
     * @param request
     * @return
     */
    public Page getWebPag(HttpRequestBase request){
        Page page = new Page();
        CloseableHttpResponse response = getResponse(request);
        page.setStatusCode(response.getStatusLine().getStatusCode());
        page.setUrl(request.getURI().toString());
        try {
            if(page.getStatusCode() == 200){
                page.setHtml(IOUtils.toString(response.getEntity().getContent()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
            if(response != null){
                try {
                    //确保response实体被完全消耗
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return page;
    }
    /**
     * 反序列化CookiesStore
     * @param name cookie文件的路径
     * @return
     */
    public boolean antiSerializeCookieStore(String name){
        try {
            CookieStore cookieStore = (CookieStore) HttpClientUtil.antiSerializeMyHttpClient(name);
            httpClientContext.setCookieStore(cookieStore);
        } catch (Exception e){
            logger.warn("反序列化Cookie失败,没有找到Cookie文件");
            return false;
        }
        return true;
    }


    public CloseableHttpClient getCloseableHttpClient() {
        return closeableHttpClient;
    }

    public HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    public abstract void initHttpClient();
}
