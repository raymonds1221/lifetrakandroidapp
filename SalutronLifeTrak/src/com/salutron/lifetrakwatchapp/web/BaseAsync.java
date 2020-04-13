package com.salutron.lifetrakwatchapp.web;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import android.content.Context;
import android.os.AsyncTask;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.AbstractAjaxCallback;
import com.androidquery.callback.AjaxCallback;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONObject;

import com.salutron.lifetrakwatchapp.util.CustomSSLFactory;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public abstract class BaseAsync<T> implements SalutronLifeTrakUtility {
	private AQuery mAQuery;
	private String mUrl;
	private final Map<String, Object> mParams = new HashMap<String, Object>();
	protected AsyncListener mListener;
	private BaseAjaxCallback mBaseAjaxCallback;

	public BaseAsync(Context context) {
		mBaseAjaxCallback = new BaseAjaxCallback();
		LifeTrakLogger.configure();

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLSocketFactory socketFactory = new CustomSSLFactory(trustStore);
			AbstractAjaxCallback.setSSF(socketFactory);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		mAQuery = new AQuery(context);
	}

	public abstract void onCallback(String url, JSONObject result, AjaxStatus status);

	public BaseAsync<T> url(String url) {
		mUrl = url;
		mParams.clear();
		return this;
	}

	public BaseAsync<T> addParam(String key, String value) {
		mParams.put(key, value);
		return this;
	}

	public BaseAsync<T> addParam(String key, byte[] value) {
		mParams.put(key, value);
		return this;
	}

	public BaseAsync<T> addParam(String key, JSONObject value) {
		mParams.put(key, value);
		return this;
	}

	public void post() {
		if (mListener != null)
			mListener.onAsyncStart();
		mBaseAjaxCallback.timeout(3600);
		mAQuery.clear();
		mAQuery.ajax(mUrl, mParams, JSONObject.class, mBaseAjaxCallback);
	}

	public void get() {
		if (mListener != null)
			mListener.onAsyncStart();
		// mAQuery.ajax(mUrl, mParams, JSONObject.class, mBaseAjaxCallback);

		mBaseAjaxCallback.timeout(0);
		
		Set<String> keys = mParams.keySet();
		Iterator<String> iterator = keys.iterator();
		String query = "";

		while (iterator.hasNext()) {
			String key = iterator.next();
			query += String.format("%s=%s&", key, mParams.get(key).toString());
		}
		mAQuery.ajax(mUrl + "/?" + query, JSONObject.class, mBaseAjaxCallback);
	}
	
	public void cancel() {
		new AsyncTask<String, String, String>() {
			@Override
			protected String doInBackground(String... arg0) {
				AjaxCallback.cancel();
				mBaseAjaxCallback.abort();
				return null;
			}
		}.execute();
	}

	public void setAsyncListener(AsyncListener listener) {
		mListener = listener;
	}

	private class BaseAjaxCallback extends AjaxCallback<JSONObject> {

		public BaseAjaxCallback() {
			
		}

		@Override
		public void callback(String url, JSONObject result, AjaxStatus status) {
			onCallback(url, result, status);
		}
	}
}
