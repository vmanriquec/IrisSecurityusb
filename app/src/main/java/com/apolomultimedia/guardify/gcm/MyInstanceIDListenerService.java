package com.apolomultimedia.guardify.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private String TAG = getClass().getSimpleName();

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

    }
}
