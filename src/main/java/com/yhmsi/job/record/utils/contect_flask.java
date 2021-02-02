package com.yhmsi.job.record.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

/**
 * <Description>
 *
 * @author ZhengWei
 * @version 1.0
 * @taskId:
 * @createDate 2021/02/02 13:09
 * @see com.yhmsi.job.record.utils
 */
public class contect_flask {
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Map<String, String> map = new HashMap<String, String>();
        map.put("name",	"root");
        map.put("password", "123456");
        String result = contect_flask.get(map, "http://172.30.12.188:5003/test/hello");

        System.out.println(result);

    }

    public static String get(Map<String, String> paramMap, String url){
        String result = "";
        HttpGet get = new HttpGet(url);
        try{
            CloseableHttpClient httpClient = HttpClients.createDefault();
            List<NameValuePair> params = setHttpParams(paramMap);
            String param = URLEncodedUtils.format(params, "UTF-8");
            get.setURI(URI.create(url + "?" + param));
            HttpResponse response = httpClient.execute(get);
            result = getHttpEntityContent(response);

            if(response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK){
                result = "服务器异常";
            }
        } catch (Exception e){
            System.out.println("请求异常");
            throw new RuntimeException(e);
        } finally{
            get.abort();
        }
        return result;
    }

    public static List<NameValuePair> setHttpParams(Map<String, String> paramMap){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> set = paramMap.entrySet();
        for(Map.Entry<String, String> entry : set){
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return params;
    }

    public static String getHttpEntityContent(HttpResponse response) throws UnsupportedOperationException, IOException {
        String result = "";
        HttpEntity entity = response.getEntity();
        if(entity != null){
            InputStream in = entity.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuilder strber= new StringBuilder();
            String line = null;
            while((line = br.readLine())!=null){
                strber.append(line+'\n');
            }
            br.close();
            in.close();
            result = strber.toString();
        }

        return result;
    }
}
