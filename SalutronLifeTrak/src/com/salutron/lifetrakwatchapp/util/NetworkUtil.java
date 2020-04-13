package com.salutron.lifetrakwatchapp.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.salutron.lifetrak.R;

public class NetworkUtil {
	private static final Object LOCK_OBJECT = NetworkUtil.class;
	private static NetworkUtil mNetworkUtil;
	private Context mContext;
	private ConnectivityManager mConnectivityManager;
	
	private NetworkUtil(Context context) {
		mContext = context;
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public static final NetworkUtil getInstance(Context context) {
		synchronized(LOCK_OBJECT) {
			if(mNetworkUtil == null)
				mNetworkUtil = new NetworkUtil(context);
			return mNetworkUtil;
		}
	}
	
	public boolean isNetworkAvailable() {
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		
		if(networkInfo != null && networkInfo.isConnectedOrConnecting())
			return true;
		return false;
	}
	
	public void showConnectionErrorMessage() {
		AlertDialog alert = new AlertDialog.Builder(mContext)
											.setTitle(R.string.lifetrak_title)
											.setMessage(R.string.check_network_connection)
											.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface arg0, int arg1) {
													arg0.dismiss();
												}
											}).create();
		alert.show();
	}
}
