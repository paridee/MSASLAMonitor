package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by paride on 26/09/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    public MyInstanceIDListenerService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }
}
