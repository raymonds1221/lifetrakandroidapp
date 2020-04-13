package com.salutron.lifetrakwatchapp.fragment;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.adapter.RewardsAdapter;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.WalgreensAsync;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;

import roboguice.inject.InjectView;

public class RewardsFragment extends BaseFragment implements RewardsAdapter.OnButtonClickListener, AsyncListener {
	@InjectView(R.id.lstRewards) private ListView mRewardsList;
	private RewardsAdapter mAdapter;
	private WalgreensAsync<JSONObject> mWalgreensAsync;
	private ProgressDialog mProgressDialog;
	private MainActivity mMainActivity;
	private WalgreensConnectFragment mWalgreensFragment;
	private final int WALGREENS_OPERATION_CONNECT = 0x01;
	private final int WALGREENS_OPERATION_DISCONNECT = 0x02;
	private int mWalgreensOperation = WALGREENS_OPERATION_CONNECT;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_rewards, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects();
	}
	
	private void initializeObjects() {
		hideCalendar();
		
		mAdapter = new RewardsAdapter(getActivity());
		mRewardsList.setAdapter(mAdapter);
		
		mAdapter.setOnButtonClickListener(this);
		
		mWalgreensAsync = new WalgreensAsync<JSONObject>(getActivity());
		mProgressDialog = new ProgressDialog(getActivity());
		mMainActivity = (MainActivity) getActivity();
		mWalgreensFragment = FragmentFactory.newInstance(WalgreensConnectFragment.class);
		
		mWalgreensAsync.setAsyncListener(this);
		mProgressDialog.setMessage(getString(R.string.please_wait));
	}

	@Override
	public void onConnectClick() {
		mWalgreensOperation = WALGREENS_OPERATION_CONNECT;
		
		if(NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
			String macAddress = getLifeTrakApplication().getSelectedWatch().getMacAddress();
			String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
			String channel = "mobile";
			
			if (macAddress == null || macAddress.isEmpty())
				macAddress = mPreferenceWrapper.getPreferenceStringValue(MAC_ADDRESS);
			
			mProgressDialog.show();
			
			mWalgreensAsync.url(getApiUrl() + WALGREENS_CONNECT)
							.addParam("mac_address", macAddress)
							.addParam("access_token", accessToken)
							.addParam("channel", channel)
							.get();
		} else {
			if (getActivity() != null)
			NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
		}
	}

	@Override
	public void onDisconnectClick() {
		mWalgreensOperation = WALGREENS_OPERATION_DISCONNECT;
		
		if(NetworkUtil.getInstance(getActivity()).isNetworkAvailable()) {
			String macAddress = getLifeTrakApplication().getSelectedWatch().getMacAddress();
			String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
			String channel = "mobile";
			
			mProgressDialog.show();
			
			mWalgreensAsync.url(getApiUrl() + WALGREENS_DISCONNECT)
							.addParam("mac_address", macAddress)
							.addParam("access_token", accessToken)
							.addParam("channel", channel)
							.get();
		} else {
			if (getActivity() != null)
			NetworkUtil.getInstance(getActivity()).showConnectionErrorMessage();
		}
	}

	@Override
	public void onAsyncStart() {
		
	}

	@Override
	public void onAsyncFail(int status, String message) {
		mProgressDialog.dismiss();
		
		AlertDialog alert = new AlertDialog.Builder(getActivity())
											.setTitle(R.string.lifetrak_title)
											.setMessage(message)
											.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface arg0, int arg1) {
													arg0.dismiss();
												}
											}).create();
		alert.show();
	}

	@Override
	public void onAsyncSuccess(JSONObject result) {
		mProgressDialog.dismiss();
		
		try {
			if (mWalgreensOperation == WALGREENS_OPERATION_CONNECT && 
					result.get("result") instanceof JSONObject) {
				JSONObject value = result.getJSONObject("result");
				
				String url = value.getString("url");
				Bundle bundle = new Bundle();
				bundle.putString(AUTHORIZE_URL, url);
				mWalgreensFragment.setArguments(bundle);
				mMainActivity.switchFragment2(mWalgreensFragment);
			} else if (mWalgreensOperation == WALGREENS_OPERATION_DISCONNECT && 
					result.get("result") instanceof String) {
				mPreferenceWrapper.setPreferenceBooleanValue(IS_WALGREENS_CONNECTED, false)
									.synchronize();
				mAdapter.notifyDataSetChanged();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
