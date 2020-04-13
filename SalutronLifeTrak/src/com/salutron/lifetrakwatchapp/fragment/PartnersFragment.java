package com.salutron.lifetrakwatchapp.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.MainActivity;

import roboguice.inject.InjectView;

public class PartnersFragment extends BaseFragment implements AdapterView.OnItemClickListener {
	@InjectView(R.id.lstPartners) private ListView mPartnersList;
	private ArrayAdapter<CharSequence> mAdapter;
	private MainActivity mMainActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_partners, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initializeObjects();
		((MainActivity)getActivity()).hideSoftKeyboard();
		FlurryAgent.logEvent("Partners_Page");
		
	}
	
	private void initializeObjects() {
		hideCalendar();
		
		mAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.partners_items, android.R.layout.simple_list_item_1);
		mPartnersList.setAdapter(mAdapter);
		mPartnersList.setOnItemClickListener(this);
		
		mMainActivity = (MainActivity) getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch(position) {
		case 0:
			mMainActivity.switchFragment2(FragmentFactory.newInstance(RewardsFragment.class));
			break;
		}
	}
}
