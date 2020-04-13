package com.salutron.lifetrakwatchapp.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;

public class LoginAsyncTask extends AsyncTask<String, Void, JSONObject> {

	/**
	 * HTTP connection timeout in milliseconds
	 */
	private static final int CONNECTION_TIMEOUT = 60 * 1000;

	private Map<String, String> mParams = new HashMap<String, String>();
	private AsyncListener mListener;
	private String mErrorMessage;
	private HttpPost httpPost;
	
	public LoginAsyncTask() {
		mParams.clear();
	}

	public LoginAsyncTask(String string) {
		mErrorMessage = string;
	}

	public LoginAsyncTask listener(AsyncListener listener) {
		mListener = listener;
		return this;
	}

	public void abortLogin() {
		cancel(true);
		if (httpPost != null) {
			httpPost.abort();
			httpPost = null;
		}
	}
	
	@Override
	  protected  void onPreExecute()
	  {
		if (mListener != null)
			mListener.onAsyncStart();
	  }

	@Override
	protected JSONObject doInBackground(String... params) {
		
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT);
		
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		httpPost = new HttpPost(params[0]);
		
		List<NameValuePair> queries = new ArrayList<NameValuePair>();
		
		Set<String> keys = mParams.keySet();
		Iterator<String> iterator = keys.iterator();
		
		while (iterator.hasNext()) {
			String key = iterator.next();
			queries.add(new BasicNameValuePair(key, mParams.get(key)));
		}
		
		mParams.clear();
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(queries));
			HttpResponse response = httpClient.execute(httpPost);
			
			/*if    (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ||
					response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
				String value = inputStreamToString(response.getEntity().getContent());
				return new JSONObject(value);
			}*/
            String value = inputStreamToString(response.getEntity().getContent());
            return new JSONObject(value);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void onPostExecute(JSONObject result) {
		if    (mListener != null)
			if    (result != null) {
				try {
					if   (result.has("access_token")&& result.has("expires")) {
						mListener.onAsyncSuccess(result);
					} else {
						mListener.onAsyncFail(500, result.getString("error_description"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				mListener.onAsyncFail(500, mErrorMessage);
			}
		else
			mListener.onAsyncFail(500, mErrorMessage);
	}
	
	public LoginAsyncTask addParam(String key, String value) {
		mParams.put(key, value);
		return this;
	}
	
	private String inputStreamToString(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int length = 0;
		
		StringBuffer sb = new StringBuffer();
		
		while    ((length = inputStream.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, length));
		}
		
		return sb.toString();
	}

}
