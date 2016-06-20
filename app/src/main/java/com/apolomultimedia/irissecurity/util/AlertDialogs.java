package com.apolomultimedia.irissecurity.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.apolomultimedia.irissecurity.R;

/**
 * Created by developer on 20/06/2016.
 */
public class AlertDialogs {


    public static void buildAlertConexionRevisar(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
                .setTitle(activity.getString(R.string.connection_issues))
                .setMessage(
                        activity.getString(R.string.cannot_access_network_check))
                .setPositiveButton(activity.getString(R.string.review_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(
                                Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void buildAlertNoGPS(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
                .setTitle(activity.getString(R.string.location_services_disabled))
                .setMessage(
                        activity.getString(R.string.enable_location_services))
                .setPositiveButton(activity.getString(R.string.enable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
