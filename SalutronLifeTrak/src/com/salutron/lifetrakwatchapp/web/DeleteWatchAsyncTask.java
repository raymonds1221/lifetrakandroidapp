package com.salutron.lifetrakwatchapp.web;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.salutron.lifetrak.R;

public class DeleteWatchAsyncTask extends AsyncTask<String, Void, JSONObject> {
	private String mAccessToken;
	private String mMacAddress;
	private AsyncListener mListener;
	private Context mContext;
	
	public DeleteWatchAsyncTask(String accessToken, String macAddress) { }
	
	public DeleteWatchAsyncTask(Context context, String accessToken, String macAddress) {
		mAccessToken = accessToken;
		mMacAddress = macAddress;
		mContext = context;
	}
	
	public void setAccessToken(String accessToken) {
		mAccessToken = accessToken;
	}
	
	public void setMacAddress(String macAddress) {
		mMacAddress = macAddress;
	}
	
	public DeleteWatchAsyncTask listener(AsyncListener listener) {
		mListener = listener;
		return this;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		
		if    (mListener != null)
			mListener.onAsyncStart();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(params[0]);
		
		List<NameValuePair> queries = new ArrayList<NameValuePair>();
		queries.add(new BasicNameValuePair("access_token", mAccessToken));
		queries.add(new BasicNameValuePair("mac_address", mMacAddress));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(queries));
			
			HttpResponse response = httpClient.execute(post);
			
			if    (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String value = inputStreamToString(response.getEntity().getContent());
				return new JSONObject(value);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void onPostExecute(JSONObject result) {
		if    (mListener != null)
			if    (result != null) {
				try {
					if   (result.has("status") && result.getInt("status") == 200) {
						mListener.onAsyncSuccess(result);
					} else {
						mListener.onAsyncFail(500, result.getString("error_description"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				mListener.onAsyncFail(500, mContext.getString(R.string.delete_failed));
			}
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
