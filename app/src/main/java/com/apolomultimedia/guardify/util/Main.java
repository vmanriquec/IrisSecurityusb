package com.apolomultimedia.guardify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.service.SocketService;
import com.apolomultimedia.guardify.service.TrackingGPSService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by developer on 20/06/2016.
 */
public class Main {

    public static boolean hasConnecion(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean hasGPSEnabled(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void unLockScreenCPU(Activity activity) {
        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager pm = (PowerManager) activity
                .getSystemService(activity.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.FULL_WAKE_LOCK, "Taxitel");
        wl.acquire();
        wl.release();
    }

    public static void hideKeyboard(Activity context) {
        if (context.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public static void startServiceOptionSuboption(Context context, String option, String suboption) {

        context.startService(new Intent(context, SocketService.class));

        BluePrefs bluePrefs = new BluePrefs(context);
        bluePrefs.setKeyOption(option);
        bluePrefs.setKeySuboption(suboption);

        boolean finish_socket = false;

        if (option.equals(Constantes.OPT_TRACKGPS)) {
            switch (suboption) {
                case Constantes.SUBOPT_FIRST:
                    context.startService(new Intent(context, TrackingGPSService.class));
                    break;

                case Constantes.SUBOPT_SECOND:
                    context.startService(new Intent(context, TrackingGPSService.class));
                    break;

                case Constantes.SUBOPT_THIRD:
                    context.stopService(new Intent(context, TrackingGPSService.class));
                    finish_socket = true;
                    break;

            }

        }

        if (finish_socket) {
            context.stopService(new Intent(context, SocketService.class));
        }

    }

}
