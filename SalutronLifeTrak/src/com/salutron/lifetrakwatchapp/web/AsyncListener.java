package com.salutron.lifetrakwatchapp.web;

import org.json.JSONObject;

public interface AsyncListener {
	void onAsyncStart();
	void onAsyncFail(int status, String message);
	void onAsyncSuccess(JSONObject result);
}
