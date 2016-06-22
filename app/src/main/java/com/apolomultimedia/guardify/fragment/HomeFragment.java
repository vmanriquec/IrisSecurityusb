package com.apolomultimedia.guardify.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.model.BluetoothDeviceModel;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    private String TAG = getClass().getSimpleName();

    View view;
    UserPrefs userPrefs;
    BluePrefs bluePrefs;

    @Bind(R.id.iv_celphone)
    ImageView iv_celphone;
    @Bind(R.id.iv_camera)
    ImageView iv_camera;
    @Bind(R.id.iv_microphone)
    ImageView iv_microphone;

    @Bind(R.id.ll_bottom)
    LinearLayout ll_bottom;
    @Bind(R.id.tv_status)
    TextView tv_status;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        userPrefs = new UserPrefs(getActivity());
        bluePrefs = new BluePrefs(getActivity());

        checkBlueConnetion();
        loadOnPressedImages();

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter IF = new IntentFilter();
        IF.addAction(Constantes.BR_DEVICE_CONNECTED);
        IF.addAction(Constantes.BR_DEVICE_DISCONNECTED);
        getActivity().registerReceiver(BR, IF);
    }

    BroadcastReceiver BR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action: " + action);

            switch (action) {
                case Constantes.BR_DEVICE_CONNECTED:
                case Constantes.BR_DEVICE_DISCONNECTED:
                    checkBlueConnetion();
                    break;

            }

        }
    };

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(BR);

    }

    private void checkBlueConnetion() {
        if (bluePrefs.getKeyBlueConnected()) {
            tv_status.setText(getActivity().getString(R.string.connected));
            ll_bottom.setBackgroundResource(R.drawable.bg_connected);
        } else {
            tv_status.setText(getActivity().getString(R.string.disconnected));
            ll_bottom.setBackgroundResource(R.drawable.bg_disconnected);
        }
    }

    private void loadOnPressedImages() {
        iv_celphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iv_celphone.setImageResource(R.drawable.icon2_3_2);
                        break;
                    case MotionEvent.ACTION_UP:
                        iv_celphone.setImageResource(R.drawable.icon2_3_1);
                        break;
                }

                return false;
            }
        });

        iv_camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iv_camera.setImageResource(R.drawable.icon3_3_2);
                        break;
                    case MotionEvent.ACTION_UP:
                        iv_camera.setImageResource(R.drawable.icon3_3_1);
                        break;
                }

                return false;
            }
        });

        iv_microphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        iv_microphone.setImageResource(R.drawable.icon4_3_2);
                        break;
                    case MotionEvent.ACTION_UP:
                        iv_microphone.setImageResource(R.drawable.icon4_3_1);
                        break;
                }

                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
