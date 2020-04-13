package com.salutron.lifetrakwatchapp.web;

import com.androidquery.callback.AjaxStatus;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class DeleteWatchAsync<T> extends BaseAsync<T> {

	public DeleteWatchAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null) {
			if(mListener != null)
				mListener.onAsyncSuccess(result);
		} else {
			if    (status.getError() != null) {
				try {
					JSONObject error = new JSONObject(status.getError());
					LifeTrakLogger.info("DeleteWatchAsync error: " + error.toString());
					
					if(mListener != null) {
						if(error.has("error")) {
							mListener.onAsyncFail(status.getCode(), error.getString("error_description"));
						} else {
							mListener.onAsyncFail(status.getCode(), status.getMessage());
						}
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
					mListener.onAsyncFail(status.getCode(), status.getMessage());
				}
			} else {
				mListener.onAsyncFail(status.getCode(), status.getMessage());
			}
			
			status.invalidate();
		}
	}
}