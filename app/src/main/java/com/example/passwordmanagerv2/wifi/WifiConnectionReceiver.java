package com.example.passwordmanagerv2.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.example.passwordmanagerv2.DatabaseHelper;
import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;

public class WifiConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiConnectionReceiver";
    private DatabaseHelper dbHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Action received: " + action);

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null) {
                Log.d(TAG, "Network state: " + networkInfo.getState().name());

                if (networkInfo.isConnected()) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE);

                    if (wifiManager != null) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();

                        Log.d(TAG, "Connected to WiFi: " + ssid);
                        Log.d(TAG, "Signal strength: " + wifiInfo.getRssi());

                        if (ssid != null && !ssid.isEmpty() && !ssid.equals("<unknown ssid>")) {
                            ssid = ssid.replace("\"", "");
                            Log.d(TAG, "Processing WiFi network: " + ssid);

                            if(dbHelper == null) {
                                dbHelper = new DatabaseHelper(context);
                            }

                            String securityType = getSecurityType(wifiManager);
                            saveToDatabase(ssid, "", securityType);
                        }
                    }
                }
            }
        }
    }

    private String getSecurityType(WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            return "WPA/WPA2"; // default security type pentru majoritatea rețelelor moderne
        }
        return "Unknown";
    }

    private void saveToDatabase(String ssid, String password, String securityType) {
        try {
            WiFiPassword wifiPassword = new WiFiPassword(ssid, password, securityType);
            if (dbHelper != null) {
                dbHelper.saveWiFiPassword(wifiPassword);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving WiFi password", e);
        }
    }

    public void stopReceiver(Context context) {
        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered");
        }
    }
}