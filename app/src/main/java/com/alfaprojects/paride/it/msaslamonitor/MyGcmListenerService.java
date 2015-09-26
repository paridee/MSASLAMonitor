package com.alfaprojects.paride.it.msaslamonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
    public MyGcmListenerService() {
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
        super.onSendError(msgId, error);
    }
}
