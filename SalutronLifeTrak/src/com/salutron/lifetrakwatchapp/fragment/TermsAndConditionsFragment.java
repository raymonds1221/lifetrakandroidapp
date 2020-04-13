package com.salutron.lifetrakwatchapp.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.salutron.lifetrak.R;

public class TermsAndConditionsFragment extends BaseFragment {
	private WebView mTermsAndConditions;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_terms_and_conditions, null);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mTermsAndConditions = (WebView) getView().findViewById(R.id.webTermsAndConditions);
		mTermsAndConditions.loadUrl(TERMS_AND_CONDITIONS_URI);
	}
	
}
