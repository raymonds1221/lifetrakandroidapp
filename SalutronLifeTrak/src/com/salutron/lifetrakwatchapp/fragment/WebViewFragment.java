package com.salutron.lifetrakwatchapp.fragment;

import com.salutron.lifetrak.R;

import android.app.ProgressDialog;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends BaseFragment {

	private WebView webView;
	private ProgressDialog mProgressDialog;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView = new WebView(getActivity());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(mWebViewClient);
		webView.setOnKeyListener(mKeyListener);
		
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setMessage(getString(R.string.loading_text));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return webView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		final Bundle args = getArguments();
		final String url = args.getString("url");
		
		webView.loadUrl(url);
		hideCalendar();
	}
	
	private final WebViewClient mWebViewClient = new WebViewClient() {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
			
			if    (uri.getHost().indexOf("lifetrakusa.com") > -1 && url.endsWith(".pdf")) {
				view.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
				return true;
			}
			
			return false;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			if    (!mProgressDialog.isShowing())
				mProgressDialog.show();
		}
		
		@Override
		public void onPageFinished(final WebView view, String url) {
			super.onPageFinished(view, url);
			
			if    (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}
	};
	
	private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			
			if    (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
				webView.goBack();
				return true;
			}
			
			return false;
		}
	};
}
