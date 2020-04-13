package com.salutron.lifetrakwatchapp.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AlertSettingsFragment extends BaseFragment {

	private static String TAG = AlertSettingsFragment.class.getCanonicalName();
	private Button wakeUpAlertButton;
	private Button dayLightAlertButton;
	private Button nightLightAlertButton;
	private Button inactiveAlertButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(TAG, "OnCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_alert_settings, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		final ActionListener l = new ActionListener();
		
		wakeUpAlertButton = (Button) getView().findViewById(R.id.wake_up_alert);
		wakeUpAlertButton.setOnClickListener(l) ;
		
		dayLightAlertButton = (Button) getView().findViewById(R.id.day_light_alert);
		dayLightAlertButton.setOnClickListener(l);
		
		nightLightAlertButton = (Button) getView().findViewById(R.id.night_light_alert);
		nightLightAlertButton.setOnClickListener(l);
		
		inactiveAlertButton = (Button) getView().findViewById(R.id.inactive_alert);
		inactiveAlertButton.setOnClickListener(l);
	}	

	private class ActionListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			final int id = v.getId();

			switch (id) {
			case R.id.wake_up_alert:
				//Go to wakeUpFragment screen
				WakeUpAlertFragment wakeUpAlert = new WakeUpAlertFragment();
				switchFragment(wakeUpAlert);
				break;
			case R.id.day_light_alert:
				//Go to dayLightAlertFragment screen
				DayLightAlertFragment dayLightAlert = new DayLightAlertFragment();
				switchFragment(dayLightAlert);
				break;
			case R.id.night_light_alert:
				//Go to dayLightAlertFragment screen
				NightLightAlertFragment nightLightAlert = new NightLightAlertFragment();
				switchFragment(nightLightAlert);
				break;
			case R.id.inactive_alert:
				//Go to dayLightAlertFragment screen
				InactiveAlertFragment inactiveAlert = new InactiveAlertFragment();
				switchFragment(inactiveAlert);
//				LightPlotPagerFragment f = new LightPlotPagerFragment();
//				switchFragment(f);
				break;
				
			}
		}
		
		private void switchFragment(final SherlockFragment fragment) {
			final MainActivity activity = ((MainActivity) getActivity());
			activity.getSupportFragmentManager().beginTransaction()
					.detach(fragment)
					.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK)
					.replace(R.id.frmContentFrame, fragment).attach(fragment)
					.addToBackStack("fragment_tag1").commit();
		}

	}
	
	
}
