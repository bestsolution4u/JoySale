package com.hitasoft.app.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.hitasoft.app.joysale.JoysaleApplication;


/**
 * Created by hitasoft on 4/3/17.
 */

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkReceiver.class.getSimpleName();
    public static ConnectivityReceiverListener connectivityReceiverListener;

    public NetworkReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        /*In the case status of network changed and then updating status for app(Connected/Disconnect)*/
        if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo activeNetwork =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean isConnected = false;
            if (activeNetwork != null) {
                if (activeNetwork.getState() == NetworkInfo.State.CONNECTING || activeNetwork.getState() == NetworkInfo.State.DISCONNECTING) {
                    return;
                }
                isConnected = activeNetwork.isConnected();
                if (connectivityReceiverListener != null) {
                    connectivityReceiverListener.onNetworkChanged(isConnected);
                }
            } else {
                if (connectivityReceiverListener != null) {
                    connectivityReceiverListener.onNetworkChanged(isConnected);
                }
            }
        } else if (action != null && action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = false;
            if (activeNetwork != null) {
                if (activeNetwork.getState() == NetworkInfo.State.CONNECTING || activeNetwork.getState() == NetworkInfo.State.DISCONNECTING) {
                    return;
                }
                isConnected = activeNetwork.isConnected();
                if (connectivityReceiverListener != null) {
                    connectivityReceiverListener.onNetworkChanged(isConnected);
                }
            } else {
                if (connectivityReceiverListener != null) {
                    connectivityReceiverListener.onNetworkChanged(isConnected);
                }
            }
        }
    }

    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) JoysaleApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public void setConnectivityListener(ConnectivityReceiverListener connectivityReceiverListener) {
        NetworkReceiver.connectivityReceiverListener = connectivityReceiverListener;
    }

    public interface ConnectivityReceiverListener {
        void onNetworkChanged(boolean isConnected);
    }
}
