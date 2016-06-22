package com.apolomultimedia.guardify.util;

import android.content.Context;
import android.widget.Toast;

import com.apolomultimedia.guardify.R;

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

    public static void showToastConnection(Context context) {
        Toast.makeText(context, context.getString(R.string.looks_like_connection_problems),
                Toast.LENGTH_LONG).show();
    }

}
