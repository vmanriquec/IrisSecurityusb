package com.apolomultimedia.guardify.fragment;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.adapter.DeviceListAdapter;
import com.apolomultimedia.guardify.model.BluetoothDeviceModel;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.service.BlueetoothConnectionService;
import com.apolomultimedia.guardify.service.BluetoothService;
import com.apolomultimedia.guardify.util.RecyclerViewOnItemClickListener;
import com.apolomultimedia.guardify.util.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectFragment extends Fragment {

    private String TAG = getClass().getSimpleName();

    BluePrefs bluePrefs;

    View view;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDeviceModel> deviceList = new ArrayList<>();
    LinearLayoutManager layoutManager;

    @Bind(R.id.rv_devices)
    RecyclerView rv_devices;

    @Bind(R.id.srl_refresh)
    SwipeRefreshLayout srl_refresh;

    @Bind(R.id.ll_loading)
    LinearLayout ll_loading;

    @Bind(R.id.ll_bluetooth_off)
    LinearLayout ll_bluetooth_off;

    @Bind(R.id.ll_research)
    LinearLayout ll_research;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_connect, container, false);
        ButterKnife.bind(this, view);

        bluePrefs = new BluePrefs(getActivity());

        setupRecycler();
        searchDevices();
        setupSwipeRefresh();

        return view;

    }

    private void setupSwipeRefresh() {
        srl_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchDevices();
            }
        });
    }

    private void setupRecycler() {
        layoutManager = new LinearLayoutManager(getActivity());
        rv_devices.setLayoutManager(layoutManager);
    }

    private void searchDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "bluetooth enabled");
            deviceList = new ArrayList<>();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        } else {
            showUIBluetoothOff();
            Log.i(TAG, "bluetooth disabled");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter IF = new IntentFilter();
        IF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        IF.addAction(BluetoothDevice.ACTION_FOUND);
        IF.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IF.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(BR_BLUETOOTH, IF);

    }

    BroadcastReceiver BR_BLUETOOTH = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG, "action: " + action);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "change to disabled");
                        showUIBluetoothOff();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "change to enabled");
                        searchDevices();
                        break;
                }

            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                srl_refresh.setVisibility(View.VISIBLE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (deviceList.size() > 0) {
                    showUIFoundDevice();
                    srl_refresh.setRefreshing(false);

                    for (int i = 0; i < deviceList.size(); i++)
                        if (deviceList.get(i).getMACAddress().equals(device.getAddress()))
                            return;

                }
                deviceList.add(new BluetoothDeviceModel(device.getAddress(), device.getName()));
                rv_devices.setAdapter(new DeviceListAdapter(deviceList, new RecyclerViewOnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        BluetoothDeviceModel device = deviceList.get(position);
                        connectDevice(device);
                    }

                    @Override
                    public void onItemLongCLick(View v, int position) {

                    }
                }));

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                showLoading();

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                srl_refresh.setRefreshing(false);
                if (deviceList.size() == 0) {
                    showUINoDevices();
                }
            }

        }
    };
/*pasamos la mac a la actividad BluetoothService y la iniciamos */

    private void connectDevice(BluetoothDeviceModel device) {

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getMACAddress());
        ToastUtil.shortToast(getActivity(), getActivity().getString(R.string.connecting));

        Intent intent = new Intent(getActivity(), BluetoothService.class);
        intent.putExtra("mac", bluetoothDevice.getAddress());
        getActivity().startService(intent);

    }

    @OnClick(R.id.btn_research)
    void research() {
        searchDevices();
    }
/*validadciones de las pantalla botones o textos segun el estado de equipos Bluetooth*/
    private void showUINoDevices() {
        ll_loading.setVisibility(View.GONE);
        srl_refresh.setVisibility(View.GONE);
        ll_bluetooth_off.setVisibility(View.GONE);
        ll_research.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        ll_loading.setVisibility(View.VISIBLE);
        srl_refresh.setVisibility(View.GONE);
        ll_bluetooth_off.setVisibility(View.GONE);
        ll_research.setVisibility(View.GONE);
    }

    private void showUIFoundDevice() {
        ll_loading.setVisibility(View.GONE);
        srl_refresh.setVisibility(View.VISIBLE);
        ll_bluetooth_off.setVisibility(View.GONE);
        ll_research.setVisibility(View.GONE);
    }

    private void showUIBluetoothOff() {
        ll_loading.setVisibility(View.GONE);
        srl_refresh.setVisibility(View.GONE);
        ll_bluetooth_off.setVisibility(View.VISIBLE);
        ll_research.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(BR_BLUETOOTH);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void initProgressDialog(String msg) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void finishProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
