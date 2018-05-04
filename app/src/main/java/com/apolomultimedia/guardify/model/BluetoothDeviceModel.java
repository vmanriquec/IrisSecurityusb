package com.apolomultimedia.guardify.model;

import java.util.UUID;

public class BluetoothDeviceModel {

    String MACAddress, name;

    public BluetoothDeviceModel() {
    }

    public BluetoothDeviceModel(String MACAddress, String name) {
        this.MACAddress = MACAddress;
        this.name = name;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*identificar el servicio del blotoo UUID_SERIAL*/

    private static final String UUID_SERIAL = "00001101-0000-1000-8000-00805F9B34FB";

    public static java.util.UUID getUUID()
    {
        return UUID.fromString(UUID_SERIAL); //Standard SerialPortService ID
    }
}
