package com.salutron.lifetrakwatchapp.view;

import com.salutron.lifetrak.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LifeTrakWebView extends WebView {

	private Dialog progressBar;

	public LifeTrakWebView(Context context) {
		super(context);

		setWebViewClient(new WebViewClient() {
			boolean loadingFinished = true;
			boolean redirect = false;

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				redirect = true;
				loadingFinished = false;

				if (progressBar.isShowing()) {
					progressBar.dismiss();
				}

				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(final WebView view, String url) {
				if (!redirect) {
					loadingFinished = true;
				}

				if (loadingFinished && !redirect) {
					if (progressBar.isShowing()) {
						progressBar.dismiss();
					}
				} else {
					redirect = false;
				}
			}

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar = ProgressDialog.show(getContext(), "", getContext().getString(R.string.loading_text));
				progressBar.setCancelable(true);
			}
		});
	}

//	@Override
//	public void loadUrl(final String url) {
//		super.loadUrl(url);
//
//		final Handler loading = new Handler();
//		loading.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					progressBar.dismiss();
//				} catch (Exception e) {
//				}
//			}
//		}, 10000);
//	}
}
