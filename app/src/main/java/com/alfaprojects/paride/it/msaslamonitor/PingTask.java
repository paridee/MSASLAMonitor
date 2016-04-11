package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by paride on 11/06/15.
 */
class PingTask extends AsyncTask<String, Void, Void> {
    PipedOutputStream mPOut;
    PipedInputStream mPIn;
    LineNumberReader mReader;
    Process mProcess;
    Context aContext;
    TextView aTextView;
    String endpoint;
    DeviceData device;
    double RTT  =   -1;
    public PingTask(DeviceData data){
        super();
        this.device =   data;
    }
    @Override
    protected void onPreExecute() {
        System.out.println("launching ping");
        mPOut = new PipedOutputStream();
        System.out.println("launching ping2");
        try {
            mPIn = new PipedInputStream(mPOut);
            System.out.println("launching ping3");
            mReader = new LineNumberReader(new InputStreamReader(mPIn));
            System.out.println("launching ping4");
        } catch (IOException e) {
            cancel(true);
        }

    }

    public void stop() {
        Process p = mProcess;
        if (p != null) {
            p.destroy();
        }
        cancel(true);
    }

    @Override
    protected Void doInBackground(String... params) {
        System.out.println("executing ping");
        try {
            mProcess = new ProcessBuilder()
                    .command("/system/bin/ping", params[0])
                    .redirectErrorStream(true)
                    .start();
            this.endpoint   =   params[0];
            try {
                InputStream in = mProcess.getInputStream();
                OutputStream out = mProcess.getOutputStream();
                byte[] buffer = new byte[1024];
                int count;

                // in -> buffer -> mPOut -> mReader -> 1 line of ping information to parse
                while ((count = in.read(buffer)) != -1) {
                    mPOut.write(buffer, 0, count);
                    publishProgress();
                }
                out.close();
                in.close();
                mPOut.close();
                mPIn.close();
            } finally {
                mProcess.destroy();
                mProcess = null;
            }
        } catch (IOException e) {
        }
        return null;
    }
    @Override
    protected void onProgressUpdate(Void... values) {
        try {
            // Is a line ready to read from the "ping" command?
            while (mReader.ready()) {
                // This just displays the output, you should typically parse it I guess.
                String myOutput   =    mReader.readLine();
                String[] output   =   myOutput.split("time");
                //System.out.println("lunghezza array "+output.length);
                if(output.length>1) {
                    String row = output[1];
                    row =    row.replace("=","");
                    String[] results = row.split(" ");
                    if(results.length>1){
                        System.out.println(results[0]);
                        double value   =   Double.parseDouble(results[0]);
                        Singletons.updateTVRTT(value,device,this.endpoint);
                        mProcess.destroy();
                        return;
                    }
                }
            }
        } catch (IOException t) {
        }
    }
}