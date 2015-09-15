package com.alfaprojects.paride.it.msaslamonitor;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by paride on 30/06/15.
 */

//TODO implement uplink speedtest
public class SpeedTestTask extends AsyncTask {
    double result=-1;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;
    TextView aTv;
    DeviceData deviceData;

    public SpeedTestTask(DeviceData deviceData){
        this.deviceData =   deviceData;
    }

    public SpeedInfo calculate(final long downloadTime, final long bytesIn){
        SpeedInfo info=new SpeedInfo();
        //from mil to sec
        long bytespersecond   =(bytesIn / downloadTime) * 1000;
        double kilobits=bytespersecond * BYTE_TO_KILOBIT;
        double megabits=kilobits  * KILOBIT_TO_MEGABIT;
        info.downspeed=bytespersecond;
        info.kilobits=kilobits;
        info.megabits=megabits;

        return info;
    }

    public static class SpeedInfo{
        public double kilobits=0;
        public double megabits=0;
        public double downspeed=0;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        SpeedInfo speedInfo =   (SpeedInfo)values[0];
        System.out.println("Velocita DL:" + speedInfo.megabits);
        this.aTv.setText("Velocita DL:" +speedInfo.megabits + " Mb/s");
        System.out.println("RICHIEDO UPDATE GRAFICO");
        Singletons.updateGraph(deviceData);
    }

    protected Object doInBackground(Object[] params) {
        InputStream stream=null;
        try {
            int bytesIn=0;
            String downloadFileUrl="http://www.latestanelpallone.com/wp-content/uploads/2015/04/Francesco_Totti_Roma.jpg";
            //String downloadFileUrl="http://"+Singletons.serverip+":8080/ServerMSA/test.jpeg";
            System.out.println("SPEEDTEST DL URL "+downloadFileUrl);
            long startCon=System.currentTimeMillis();
            URL url=new URL(downloadFileUrl);
            URLConnection con=url.openConnection();
            con.setUseCaches(false);
            stream=con.getInputStream();
            long start=System.currentTimeMillis();
            int currentByte=0;

            while((currentByte=stream.read())!=-1){
                //System.out.println("Bytes received");
                bytesIn++;
            }

            long downloadTime=(System.currentTimeMillis()-start);
            //Prevent AritchmeticException
            if(downloadTime==0){
                downloadTime=1;
            }

            SpeedInfo speed =   calculate(downloadTime, bytesIn);
            this.result =   speed.megabits;
            deviceData.bandwidthDL  =   speed.megabits;
            Object[] objects    =   new Object[1];
            objects[0]          =   speed;
            this.publishProgress(objects);
        }
        catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }finally{
            try {
                if(stream!=null){
                    stream.close();
                }
            } catch (IOException e) {
                //Suppressed
            }
        }
        return null;
    }
}
