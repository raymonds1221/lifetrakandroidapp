package com.salutron.lifetrakwatchapp.web;

import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;
import android.content.Context;

public class EditProfileAsync<T> extends BaseAsync<T> {

	public EditProfileAsync(Context context) {
		super(context);
	}

	@Override
	public void onCallback(String url, JSONObject result, AjaxStatus status) {
		if(result != null) {
			if(mListener != null)
				mListener.onAsyncSuccess(result);
		} else {
			if(mListener != null) {
				mListener.onAsyncFail(status.getCode(), status.getError());
			}
			status.invalidate();
		}
	}
}
