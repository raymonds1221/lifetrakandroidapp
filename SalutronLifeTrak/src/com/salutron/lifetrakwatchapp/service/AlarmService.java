package com.salutron.lifetrakwatchapp.service;

import java.util.List;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.db.*;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

import android.app.IntentService;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmService extends IntentService {
	private NotificationManager notificationManager;
	private PendingIntent pendingIntent;
	private static final String TAG = "ALARM";

	public AlarmService() {
		super("LifeTrak");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = this.getApplicationContext();

        long watchId = PreferenceWrapper.getInstance(context)
                                        .getPreferenceLongValue(SalutronLifeTrakUtility.LAST_CONNECTED_WATCH_ID);

        List<Watch> watches = DataSource.getInstance(context)
                                        .getReadOperation()
                                        .query("_id=?", String.valueOf(watchId))
                                        .getResults(Watch.class);

        if (watches.size() > 0) {
            ((LifeTrakApplication) getApplicationContext()).setSelectedWatch(watches.get(0));
        }

		Intent mIntent = new Intent(this, MainActivity.class);
		Bundle bundle = new Bundle(); 
		bundle.putBoolean(SalutronLifeTrakUtility.OPENED_FROM_NOTIFICATION, true);
		mIntent.putExtras(bundle);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (MainActivity.isVisible()) {
			startActivity(mIntent);
			return;
		}

		pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Resources res = this.getResources();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                .setTicker("")
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.scheduled_sync));


		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());
	}

}
