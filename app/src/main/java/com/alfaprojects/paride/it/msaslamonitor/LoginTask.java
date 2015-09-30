package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by paride on 30/09/15.
 */
public class LoginTask extends AsyncTask {
    String jsonString;

    public LoginTask(String json){
        jsonString  =   json;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://"+Singletons.serverip+":8080/ServerMSA/LogInServlet");
        StringEntity entity  =   null;
        try {
            entity  =   new StringEntity(jsonString);
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
