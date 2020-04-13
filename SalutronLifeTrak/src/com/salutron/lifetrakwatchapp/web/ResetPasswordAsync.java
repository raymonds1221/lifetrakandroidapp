package com.salutron.lifetrakwatchapp.web;

import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

import android.content.Context;
import android.util.Log;

public class ResetPasswordAsync<T> extends BaseAsync<T> {

	public ResetPasswordAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null) {
			if(mListener != null)
				mListener.onAsyncSuccess(result);
		} else {
			try {
				JSONObject error = new JSONObject(status.getError());
				LifeTrakLogger.info("LoginAsync error: " + error.toString());
				
				if(mListener != null) {
					if(error.has("error")) {
						mListener.onAsyncFail(status.getCode(), error.getString("error_description"));
					} else {
						mListener.onAsyncFail(status.getCode(), status.getMessage());
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			status.invalidate();
		}
	}

}
