package com.apolomultimedia.guardify.adapter;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.model.BluetoothDeviceModel;
import com.apolomultimedia.guardify.util.RecyclerViewOnItemClickListener;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private ArrayList<BluetoothDeviceModel> deviceList;
    private RecyclerViewOnItemClickListener recyclerViewOnItemClickListener;

    public DeviceListAdapter(ArrayList<BluetoothDeviceModel> deviceList,
                             RecyclerViewOnItemClickListener recyclerViewOnItemClickListener) {
        this.deviceList = deviceList;
        this.recyclerViewOnItemClickListener = recyclerViewOnItemClickListener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_bluetooth,
                parent, false);
        return new DeviceViewHolder(row);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        BluetoothDeviceModel model = deviceList.get(position);
        holder.getTv_device_address().setText(model.getMACAddress());
        holder.getTv_device_name().setText(model.getName());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView tv_device_name, tv_device_address;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tv_device_name = (TextView) itemView.findViewById(R.id.tv_device_name);
            tv_device_address = (TextView) itemView.findViewById(R.id.tv_device_address);
        }

        public TextView getTv_device_name() {
            return tv_device_name;
        }

        public TextView getTv_device_address() {
            return tv_device_address;
        }

        @Override
        public void onClick(View v) {
            recyclerViewOnItemClickListener.onClick(v, getAdapterPosition());
        }
    }

}
