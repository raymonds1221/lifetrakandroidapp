package com.salutron.lifetrakwatchapp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ProgressBar;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;

public class WalgreensConnectFragment extends BaseFragment {
	private WebView mWebView;
	private ProgressBar mProgressIndicator;
	private MainActivity mMainActivity;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_walgreens_connect, null);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FlurryAgent.logEvent("Walgreens_Page");
		initializeObjects();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void initializeObjects() {
		hideCalendar();
		
		mWebView = (WebView) getView().findViewById(R.id.wvwWalgreens);
		mProgressIndicator = (ProgressBar) getView().findViewById(R.id.pgrIndicator);
		mMainActivity = (MainActivity) getActivity();
		
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mProgressIndicator.setVisibility(View.GONE);
		
		if(getArguments() != null) {
			CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(mWebView.getContext());
			CookieManager cookieManager = CookieManager.getInstance();
			
			String accessToken = mPreferenceWrapper.getPreferenceStringValue(ACCESS_TOKEN);
			String macAddress = getLifeTrakApplication().getSelectedWatch().getMacAddress();
			String cookie = String.format("access_token=%s;domain=my.lifetrakusa.com", accessToken);
			
			cookieManager.setCookie("https://my.lifetrakusa.com", cookie);
			cookieSyncManager.sync();
			
			cookie = String.format("mac_address=%s;domain=my.lifetrakusa.com", macAddress);
			cookieManager.setCookie("https://my.lifetrakusa.com", cookie);
			cookieSyncManager.sync();
			
			Log.i(TAG, "cookies: " + cookieManager.getCookie("https://my.lifetrakusa.com"));
			
			String url = getArguments().getString(AUTHORIZE_URL);
			mWebView.loadUrl(url);
		}
	}
	
	private WebViewClient mWebViewClient = new WebViewClient() {
		public void onPageStarted(WebView webview, String url, Bitmap favIcon) {
			mProgressIndicator.setVisibility(View.VISIBLE);
		}
		
		public void onPageFinished(WebView webView, String url) {
			mProgressIndicator.setVisibility(View.GONE);
		}
		
		public boolean shouldOverrideUrlLoading(WebView webView, String url) {
			Log.i(TAG, "url: " + url);
			
			if(url.equals("walgreensios://success")) {
				mPreferenceWrapper.setPreferenceBooleanValue(IS_WALGREENS_CONNECTED, true).synchronize();
				mMainActivity.getSupportFragmentManager().popBackStack();
			} else if(url.equals("walgreensios://failed")) {
				mMainActivity.getSupportFragmentManager().popBackStack();
			}
			
			webView.loadUrl(url);
			return false;
		}
	};
}
