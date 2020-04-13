package com.salutron.lifetrakwatchapp.web;

import android.content.Context;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterAsync<T> extends BaseAsync<T> {
	
	public RegisterAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null) {
			if(mListener != null)
				mListener.onAsyncSuccess((JSONObject)result);
		} else {
			status.invalidate();
			status.done();
			
			try {
				String message = status.getMessage();
				
				if(status.getError() != null) {
					JSONObject error = new JSONObject(status.getError());
					message = error.getString("error_description");
				}
					
				if(mListener != null)
					mListener.onAsyncFail(status.getCode(), message);
			} catch (JSONException e) {
				if(mListener != null)
					mListener.onAsyncFail(0, e.getMessage());
				e.printStackTrace();
			}
		}
	}
}