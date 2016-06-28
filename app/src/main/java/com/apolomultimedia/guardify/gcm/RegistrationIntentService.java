package com.apolomultimedia.guardify.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Intent registrationComplete = new Intent("registrationComplete");
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

    }
}
