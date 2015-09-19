package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by paride on 01/09/15.
 */
public class TestJson extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        TaskInstance pippo  =   null;//new TaskInstance(1,"sumA",100,1,new Date(),new Date());
        String pippostr     =   pippo.generateJson();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://"+Singletons.serverip+":8080/ServerMSA/MSAServlet");
        StringEntity    entity  =   null;
        try {
            entity  =   new StringEntity(pippostr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type","application/json");
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            Log.d("POSTTASK:PostResponse:", response.toString());
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
