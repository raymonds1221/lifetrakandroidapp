package com.salutron.lifetrakwatchapp.fragment;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragment;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.service.LogCollectorService;
import com.apptentive.android.sdk.Apptentive;
import com.flurry.android.FlurryAgent;

public class HelpFragment extends BaseFragment {

	private ViewFlipper viewFlipper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_help, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//((MainActivity)getActivity()).hideSoftKeyboard();

		viewFlipper = (ViewFlipper) getView().findViewById(R.id.help_flipper);
		viewFlipper.setDisplayedChild(0);
		FlurryAgent.logEvent("Help_Page");
		setViewObservers();
		hideCalendar();
		
		
	}

	private void setViewObservers() {
		final ActionListener l = new ActionListener();
		//View v = getView().findViewById(R.id.app_settings_compat_apps_btn);
		//v.setOnClickListener(l);

		View v = getView().findViewById(R.id.app_settings_faq_btn);
		v.setOnClickListener(l);

		v = getView().findViewById(R.id.app_settings_guides_btn);
		v.setOnClickListener(l);

		v = getView().findViewById(R.id.app_settings_support_btn);
		v.setOnClickListener(l);
		
		v = getView().findViewById(R.id.app_message_center_btn);
		v.setOnClickListener(l);
		
		v = getView().findViewById(R.id.app_terms_and_conditions);
		v.setOnClickListener(l);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity)getActivity()).hideSoftKeyboard();
		((MainActivity)getActivity()).hideSoftKeyboard();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

	private void showUrl(final String url) {
		final WebViewFragment fragment = FragmentFactory
				.newInstance(WebViewFragment.class);
		final Bundle args = new Bundle();

		args.putString("url", url);
		fragment.setArguments(args);

		switchFragment(fragment);
	}

	private void switchFragment(final SherlockFragment fragment) {
		final MainActivity activity = ((MainActivity) getActivity());
		activity.getSupportFragmentManager().beginTransaction()
				.detach(fragment)
				.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
				.replace(R.id.frmContentFrame, fragment).attach(fragment)
				.addToBackStack("fragment_tag1").commit();
	}

	private class ActionListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			final int id = v.getId();

			switch (id) {
			/*case R.id.app_settings_compat_apps_btn:
				final HelpCompatAppsFragment fragment = FragmentFactory
						.newInstance(HelpCompatAppsFragment.class);
				switchFragment(fragment);
				break;*/
			case R.id.app_settings_support_btn:
				getActivity().startService(
						new Intent(getActivity(), LogCollectorService.class));
				break;
			case R.id.app_settings_faq_btn:
				showUrl("http://lifetrakusa.com/support/frequently-asked-questions/");
				break;
			case R.id.app_settings_guides_btn:
				showUrl("http://lifetrakusa.com/user-guides/");
				break;
			case R.id.app_message_center_btn:
				Apptentive.showMessageCenter(getActivity());
				break;
			case R.id.app_terms_and_conditions:
				TermsAndConditionsFragment termsAndConditions = new TermsAndConditionsFragment();
				switchFragment(termsAndConditions);
				break;
			}
		}

	}
}
