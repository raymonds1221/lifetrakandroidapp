package com.salutron.lifetrakwatchapp.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

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

import javax.net.ssl.HttpsURLConnection;

public class ServerSyncAsyncTask extends AsyncTask<String, Void, JSONObject> {
    private Map<String, String> mParams = new HashMap<String, String>();
    private AsyncListener mListener;
    private String mErrorMessage;

    public ServerSyncAsyncTask() {
        mParams.clear();
    }

    public ServerSyncAsyncTask(String string) {
        mErrorMessage = string;
    }

    public ServerSyncAsyncTask listener(AsyncListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        if (mListener != null)
            mListener.onAsyncStart();
        // 60 mins
//        HttpParams httpParams = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParams, 60 * 60000);
//        HttpConnectionParams.setSoTimeout(httpParams,  60 * 60000);
//
//        HttpClient httpClient = new DefaultHttpClient(httpParams);
//        HttpPost post = new HttpPost(params[0]);

        List<NameValuePair> queries = new ArrayList<NameValuePair>();

        Set<String> keys = mParams.keySet();
        Iterator<String> iterator = keys.iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            queries.add(new BasicNameValuePair(key, mParams.get(key)));
        }

        mParams.clear();
        URL url;
        HttpURLConnection conn = null;
        try {

            url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30 * 60000);
            conn.setConnectTimeout(30 * 60000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(queries));
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            if (isCancelled())
            {
                if(conn != null) {
                    conn.disconnect();
                }
                return (null); // don't forget to terminate this method
            }
            int response =conn.getResponseCode();

            if (isCancelled())
            {
                if(conn != null) {
                    conn.disconnect();
                }
                return (null);
            }
            if    (response == HttpsURLConnection.HTTP_OK ||
                    response == HttpsURLConnection.HTTP_ACCEPTED) {
                String value = inputStreamToString(conn.getInputStream());
                return new JSONObject(value);
            }
            else{
                mErrorMessage = inputStreamToString(conn.getErrorStream());
                LifeTrakLogger.info("Error on syncing ResponseCode : " + String.valueOf(response));
                LifeTrakLogger.info("Error on syncing :" + mErrorMessage.toString());
            }

        } catch (UnsupportedEncodingException e) {
            mErrorMessage = "";
            LifeTrakLogger.info("Error sync : " + e.getLocalizedMessage());
            mListener.onAsyncFail(500, mErrorMessage);
        } catch (ClientProtocolException e) {
            mErrorMessage = "";
            LifeTrakLogger.info("Error sync : " + e.getLocalizedMessage());
            mListener.onAsyncFail(500, mErrorMessage);
        } catch (IOException e) {
            mErrorMessage = "";
            LifeTrakLogger.info("Error sync : " + e.getLocalizedMessage());
            mListener.onAsyncFail(500, mErrorMessage);
        } catch (JSONException e) {
            mErrorMessage = "";
            LifeTrakLogger.info("Error sync : " + e.getLocalizedMessage());
            mListener.onAsyncFail(500, mErrorMessage);
        }
        catch (Exception e){
            mErrorMessage = "";
            LifeTrakLogger.info("Error sync : " + e.getLocalizedMessage());
            mListener.onAsyncFail(500, mErrorMessage);
        }
        finally {
            if(conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    @Override
    public void onPostExecute(JSONObject result) {
        if    (mListener != null) {
            if (result != null) {
                try {
                    if (result.has("status") && (result.getInt("status") == HttpStatus.SC_OK ||
                            result.getInt("status") == HttpStatus.SC_ACCEPTED)) {
                        mListener.onAsyncSuccess(result);
                    } else {
                        mListener.onAsyncFail(500, result.getString("error_description"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mListener.onAsyncFail(500, mErrorMessage);
                }
            } else {
                mListener.onAsyncFail(500, mErrorMessage);
            }
        }
        else
        {
            LifeTrakLogger.info("mListener is null");
        }
    }

    public ServerSyncAsyncTask addParam(String key, String value) {
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