package com.yhmsi.job.record.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <Description>
 *
 * @author ZhengWei
 * @version 1.0
 * @taskId:
 * @createDate 2021/02/02 13:02
 * @see com.yhmsi.job.record.utils
 */
public class HttpUtils {
    private static final int timeOut = 100 * 1000;

    /**
     * 创建httpClient 对象
     *
     * @param ip
     * @param port
     * @return
     */
    public static HttpClient getHttpClient(String ip, Integer port) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(getDefaultRegistry());
        // 将最大连接数增加
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);
        HttpHost httpHost = new HttpHost(ip, port);
        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), 200);
        HttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
        // 设置重试次数
                .setRetryHandler(new HttpRequestRetryHandler() {
                    @Override
                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                        if (executionCount > 3) {
                            return false;
                        }
                        // 服务器端没有返回的情况,一般是连接池里面的连接失效,需要重试
                        if (exception instanceof org.apache.http.NoHttpResponseException) {
                            return true;
                        }
                        if (exception instanceof java.net.SocketException) {
                            // 客户端主动关闭连接的情况,一般是连接池里面的连接失效,需要重试
                            if (exception.getMessage().indexOf("Connection reset") >= 0) {
                                return true;
                            }
                        }
                        if (exception instanceof java.net.SocketTimeoutException) {
                            // 连接或者响应超时,需要重试
                            return true;
                        }
                        return false;
                    }
                })
                .build();
        return httpClient;
    }

    private static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
    }

    /**
     * httpClient 发送post请求
     *
     * @param client
     * @param url
     * @param params
     * @return 返回
     * @throws Exception
     */
    public static String sendPost(HttpClient client, String url, Map<String, Object> params) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        setPostParams(httppost, params);
        HttpResponse response = null;
        try {
            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    public static String sendPost(HttpClient client, String url, String data) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        StringEntity stringEntity = new StringEntity(data, Charset.forName("UTF-8"));
        httppost.setEntity(stringEntity);
        HttpResponse response = null;
        try {
            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /***
     * 发送post 请求 ，附带header信息
     * @param client
     * @param url
     * @param data
     * @param headers
     * @return
     * @throws Exception
     */
    public static String sendPost(HttpClient client, String url, String data, Map<String, String> headers) throws Exception {
        HttpPost httppost = new HttpPost(url);
        config(httppost);
        fillHttpPostHeader(httppost, headers);
        StringEntity stringEntity = new StringEntity(data, Charset.forName("UTF-8"));
        httppost.setEntity(stringEntity);
        HttpResponse response = null;
        try {
            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    private static void setPostParams(HttpPost httpost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static void config(HttpRequestBase httpRequestBase) {
        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeOut)
                .setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 为HttpPost添加header信息
     *
     * @param post
     * @param headers
     */
    private static void fillHttpPostHeader(HttpPost post, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        for (String key : headers.keySet()) {
            post.addHeader(key, headers.get(key));
        }
    }


}