package com.salutron.lifetrakwatchapp.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.salutron.lifetrak.R;

public class BluetoothListener extends BroadcastReceiver {

	private class DialogButtonClickListener implements OnClickListener {

		private Context context;

		DialogButtonClickListener(final Context context) {
			this.context = context;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_NEGATIVE:
				final Intent receiver = new Intent();
				receiver.setAction("com.salutron.lifetrak.bluetoothoff");
				context.sendBroadcast(receiver);
				break;
			case DialogInterface.BUTTON_POSITIVE:
				final Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				owner.startActivityForResult(enableBtIntent, REQ_BT_ENABLE);
			}
		}
	}

	public static final int REQ_BT_ENABLE = 101;
	private Activity owner;

	public BluetoothListener(final Activity activity) {
		owner = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
					BluetoothAdapter.ERROR);

			switch (state) {
			case BluetoothAdapter.STATE_OFF:
				owner.setRequestedOrientation(owner.getResources()
						.getConfiguration().orientation);

				final OnClickListener listener = new DialogButtonClickListener(
						context);
				final AlertDialog.Builder alert = new AlertDialog.Builder(
						context);

				alert.setMessage(context
						.getString(R.string.bluetooth_required_on));
				alert.setNegativeButton(R.string.cancel, listener);
				alert.setPositiveButton(R.string.ok, listener);
				alert.create().show();

				break;
			}
		}
	}
}
