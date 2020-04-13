package com.salutron.lifetrakwatchapp.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

import com.salutron.lifetrak.R;

public class IntroductionPageFragment extends SherlockFragment {
	private static final String POSITION = "position";
	
	/**
	 * Instantiate a new IntroductionPageFragment fragment
	 * 
	 * @param position		Position of the fragment
	 * @return				Returns (IntroductionPageFragment) object
	 */
	public static IntroductionPageFragment newInstance(int position) {
		IntroductionPageFragment introPageFragment = new IntroductionPageFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(POSITION, position);
		introPageFragment.setArguments(bundle);
		return introPageFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ImageView imageView = new ImageView(getActivity().getApplicationContext());
		
		int position = getArguments().getInt(POSITION);
		
		switch(position) {
		case 0:
			imageView.setImageResource(R.drawable.splash_intro_1);
			break;
		case 1:
			imageView.setImageResource(R.drawable.splash_intro_2);
			break;
		case 2:
			imageView.setImageResource(R.drawable.splash_intro_3);
			break;
		case 3:
			imageView.setImageResource(R.drawable.splash_intro_4);
			break;
		case 4:
			imageView.setImageResource(R.drawable.splash_intro_5);
			break;
		}
		return imageView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
}
