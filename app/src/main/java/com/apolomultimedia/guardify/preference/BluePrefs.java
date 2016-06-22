package com.apolomultimedia.guardify.preference;

import android.content.Context;

/**
 * Created by developer on 22/06/2016.
 */
public class BluePrefs {

    private SingletonPrefs prefs;

    private static final String KEY_CONNECTED = "blue_connected";

    private static final String KEY_PAIRED_MACCADDRESS = "paired_mac_address";

    public BluePrefs(Context context) {
        prefs = SingletonPrefs.getInstance(context);
    }

    public void reset() {
        setKeyBlueConnected(false);
        setKeyPairedMaccAddress("");
    }

    public void setKeyPairedMaccAddress(String value) {
        prefs.put(KEY_PAIRED_MACCADDRESS, value);
    }

    public String getKeyPairedMaccAddress() {
        return prefs.getString(KEY_PAIRED_MACCADDRESS);
    }

    public void setKeyBlueConnected(Boolean val) {
        prefs.put(KEY_CONNECTED, val);
    }

    public Boolean getKeyBlueConnected() {
        return prefs.getBoolean(KEY_CONNECTED);
    }

}
