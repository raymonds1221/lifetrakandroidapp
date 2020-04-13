package com.salutron.lifetrakwatchapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Janwel Ocampo on 9/14/2015.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        debugIntent(intent);
    }

    private void debugIntent(Intent intent) {
       LifeTrakLogger.info("action: " + intent.getAction());
        LifeTrakLogger.info("component: " + intent.getComponent());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key: extras.keySet()) {
                LifeTrakLogger.info("key [" + key + "]: " +
                        extras.get(key));
            }
        }
        else {
            LifeTrakLogger.info("no extras");
        }
    }
}

