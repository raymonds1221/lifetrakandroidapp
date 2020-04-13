package com.salutron.lifetrakwatchapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


import android.widget.LinearLayout;
import android.widget.TextView;


import com.salutron.lifetrak.R;

public class NotificationsFragment  extends BaseFragment implements OnClickListener{
	
	private TextView textviewDisplayWatch;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_notification, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		textviewDisplayWatch = (TextView) getView().findViewById(R.id.textviewDisplayWatch);
		
		textviewDisplayWatch.setOnClickListener(this);
	}

	private void showChoices() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		LinearLayout layout       = new LinearLayout(getActivity());
		TextView tvAlways       = new TextView(getActivity());
		TextView tvOnly      = new TextView(getActivity());

		tvAlways.setText(getString(R.string.settings_notif_display_onwatch_always));
		tvOnly.setText(getString(R.string.settings_notif_display_onwatch_when_awake));
		
		tvAlways.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 TextView b = (TextView)v;
				textviewDisplayWatch.setText(b.getText().toString());
			}
		});
		
		tvOnly.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 TextView b = (TextView)v;
					textviewDisplayWatch.setText(b.getText().toString());
			}
		});
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(tvAlways);
		layout.addView(tvOnly);
		alert.setView(layout);

		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alert.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textviewDisplayWatch:
			showChoices();
			break;

		default:
			break;
		}
		
	}


}
