package com.apolomultimedia.guardify.util;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

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

}
