package com.salutron.lifetrakwatchapp.web;

import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

import android.content.Context;
import android.util.Log;

public class WalgreensAsync<T> extends BaseAsync<T> {

	public WalgreensAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if (result != null && !result.has("error")) {
			if (mListener != null)
				mListener.onAsyncSuccess(result);
		} else if(result != null && result.has("error")) {
			try {
				mListener.onAsyncFail(status.getCode(), result.getString("error_description"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			if (status.getError() != null || status.getCode() != -101) {
				try {
					JSONObject error = new JSONObject(status.getError());
					LifeTrakLogger.error("LoginAsync error: " + error.toString());

					if (mListener != null) {
						if (error.has("error")) {
							mListener.onAsyncFail(status.getCode(), error.getString("error_description"));
						} else {
							mListener.onAsyncFail(status.getCode(), status.getMessage());
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				mListener.onAsyncFail(status.getCode(), status.getMessage());
			}
			status.invalidate();
		}
	}
}
