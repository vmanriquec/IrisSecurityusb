package com.apolomultimedia.guardify.app;

import android.app.Application;

import com.apolomultimedia.guardify.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GuardifyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

    }
}
