package com.salutron.lifetrakwatchapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;

public class RewardsAdapter extends BaseAdapter implements SalutronLifeTrakUtility, View.OnClickListener {
	private LayoutInflater mInflater;
	private PreferenceWrapper mPreferenceWrapper;
	private OnButtonClickListener mListener;
	
	public RewardsAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mPreferenceWrapper = PreferenceWrapper.getInstance(context);
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View c, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.adapter_rewards, null);
		Button button = (Button) view.findViewById(R.id.btnConnectDisconnect);
		
		if(mPreferenceWrapper.getPreferenceBooleanValue(IS_WALGREENS_CONNECTED)) {
			button.setBackgroundResource(R.drawable.assets_help_rewards_disconnect);
			button.setText(R.string.walgreens_disconnect);
		} else {
			button.setBackgroundResource(R.drawable.assets_help_rewards_connectbutton);
			button.setText(R.string.walgreens_connect);
		}
		
		button.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View view) {
		if(mPreferenceWrapper.getPreferenceBooleanValue(IS_WALGREENS_CONNECTED)) {
			if(mListener != null)
				mListener.onDisconnectClick();
		} else {
			if(mListener != null)
				mListener.onConnectClick();
		}
	}
	
	public void setOnButtonClickListener(OnButtonClickListener listener) {
		mListener = listener;
	}
	
	public static interface OnButtonClickListener {
		void onConnectClick();
		void onDisconnectClick();
	}
}
