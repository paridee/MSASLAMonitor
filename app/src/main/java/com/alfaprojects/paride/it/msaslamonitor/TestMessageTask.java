package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by paride on 28/09/15.
 */
public class TestMessageTask extends AsyncTask {
    String jsonString;
    public TestMessageTask(String json){
        super();
        jsonString  =   json;
    }
    @Override
    protected Object doInBackground(Object[] params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://gcm-http.googleapis.com/gcm/send");
        StringEntity entity  =   null;
        try {
            entity  =   new StringEntity(jsonString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", "key=AIzaSyD6T7D-etAt76vPd_N6D22p6kpRLR9O-bg");
        System.out.println("TestMessageTask json messaggio " + jsonString);
        System.out.println("TestMessageTask http request " + httpPost.toString());
        Header[] headers;
        headers =   httpPost.getAllHeaders();
        for(int i=0;i<headers.length;i++){
            String out  =   headers[i].getName()+" "+headers[i].getValue();
            System.out.println("TestMessageTask header "+out);
        }
        String requestString = null;
        try {
            requestString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("POSTTASK:PostRequest:" + requestString);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity, "UTF-8");
            System.out.println("POSTTASK:PostResponse:" + responseString);
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }

        return null;
    }
}
