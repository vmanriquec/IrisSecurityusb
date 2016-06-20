package com.apolomultimedia.irissecurity.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by developer on 20/06/2016.
 */
public class ToastUtil {

    public static void shortToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void longToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

}
