package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by paride on 01/09/15.
 */
public class SpeedTestUploadTask extends AsyncTask {
    DeviceData data;

    public SpeedTestTask.SpeedInfo calculate(final long downloadTime, final long bytesIn){
        SpeedTestTask.SpeedInfo info=new SpeedTestTask.SpeedInfo();
        //from mil to sec
        long bytespersecond   =(bytesIn / downloadTime) * 1000;
        double kilobits=bytespersecond * BYTE_TO_KILOBIT;
        double megabits=kilobits  * KILOBIT_TO_MEGABIT;
        info.downspeed=bytespersecond;
        info.kilobits=kilobits;
        info.megabits=megabits;

        return info;
    }

    Context aContext;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;
    public TextView    aTv;
    SpeedTestTask.SpeedInfo result;
    public SpeedTestUploadTask(Context aContext,DeviceData data){
        super();
        this.aContext   =   aContext;
        this.data       =   data;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        this.aTv.setText("Velocita UL:" + result.megabits + " Mb/s");
        data.bandwidthUL  =   result.megabits;
        Singletons.updateGraph(data);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        BufferedReader br   =   null;
        StringBuilder sb    =   null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(aContext.getAssets().open("divinacommedia.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            String everything = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TaskInstance pippo  =   new TaskInstance(1,"sumA",100,1,new Date(), new Date());
        System.out.println("SPEEDTESTUPLOAD");
        String pippostr     =   sb.toString();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://www.trenitalia.com");
        //HttpPost httpPost = new HttpPost("http://"+Singletons.serverip+":8080/ServerMSA/SpeedTest");
        StringEntity entity  =   null;
        try {
            entity  =   new StringEntity(pippostr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type","text/plain");
        try {
            long    start   =   System.currentTimeMillis();
            HttpResponse response = httpClient.execute(httpPost);
            long    end     =   System.currentTimeMillis();
            long    delta   =   end-start;
            System.out.println("Valore delta " + delta);
            result  =   this.calculate(delta, pippostr.length());
            System.out.println("Valore banda "+result.megabits+" Mb/s");
            this.publishProgress();
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
