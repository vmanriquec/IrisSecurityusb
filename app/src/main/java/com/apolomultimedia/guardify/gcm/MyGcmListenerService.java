package com.apolomultimedia.guardify.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private String TAG = getClass().getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {

        Log.d(TAG, "From: " + from);

    }
}
