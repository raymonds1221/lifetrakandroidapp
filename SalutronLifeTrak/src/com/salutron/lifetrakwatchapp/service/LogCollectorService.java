package com.salutron.lifetrakwatchapp.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public class LogCollectorService extends IntentService {

	private final static String LINE_SEPARATOR = System
			.getProperty("line.separator");

	public LogCollectorService() {
		this("log_collector");
	}

	public LogCollectorService(String name) {
		super(name);
	}

	private final String TAG = SalutronLifeTrakUtility.TAG;
	private File logfile;

	@Override
	public void onCreate() {
		super.onCreate();

		final File path = LifeTrakApplication.PUBLIC_DIR;

		if (!path.exists()) {
			path.mkdirs();
		}

		logfile = new File(path, "logcat.txt");

		Toast.makeText(getApplicationContext(),
				"Running LifeTrak Log Collector", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(getApplicationContext(),
				"Stopping LifeTrak Log Collector", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			final String command = String.format("logcat -d %s: * SALBLEService: * *: S ", TAG);
            //final String command = "logcat -d";
			final Process process = Runtime.getRuntime().exec(command);
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					logfile));
			
			// Write Android information
			writer.write("Kernel version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")" +
					"\nOS API version: " + android.os.Build.VERSION.SDK_INT +
					"\nAndroid version: " + android.os.Build.VERSION.RELEASE +
					"\nDevice: " + android.os.Build.DEVICE +
					"\nModel name:" + getDeviceName() +
					"\nBaseband version: " + Build.getRadioVersion() +
					"\nBuild number: " + Build.ID +
					"\nProduct: " + android.os.Build.PRODUCT + "\n\n");
			
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write(LINE_SEPARATOR);
			}

			writer.flush();
			writer.close();

			final Intent receiver = new Intent();
			receiver.setAction("com.salutron.lifetrak.sendlog");
			sendBroadcast(receiver);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}
	
	public String getDeviceName() {
	    String manufacturer = Build.MANUFACTURER;
	    String model = Build.MODEL;
	    if (model.startsWith(manufacturer)) {
	        return capitalize(model);
	    } else {
	        return capitalize(manufacturer) + " " + model;
	    }
	}


	private String capitalize(String s) {
	    if (s == null || s.length() == 0) {
	        return "";
	    }
	    char first = s.charAt(0);
	    if (Character.isUpperCase(first)) {
	        return s;
	    } else {
	        return Character.toUpperCase(first) + s.substring(1);
	    }
	}
	
}
