package com.salutron.lifetrakwatchapp.fragment;

import com.salutron.lifetrak.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentWorkoutViewPager extends BaseFragment {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_workout_view_pager_layout, null);
		return v;
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPager.setScrollX(0);
        toggleNavigationMenu();
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) getView().findViewById(R.id.pager);
        mPagerAdapter = new FragmentWorkoutViewPagerAdapter(getSherlockActivity().getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                //invalidateOptionsMenu();
            }
        });
	}
	
    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class FragmentWorkoutViewPagerAdapter extends FragmentStatePagerAdapter {
        public FragmentWorkoutViewPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

       /* @Override
        public Fragment getItem(int position) {
            return new FragmentWorkout();
            		
            		//FragmentWorkoutViewPageAdapter.create(position);
        }*/

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

		@Override
		public android.support.v4.app.Fragment getItem(int arg0) {
			return new FragmentWorkout();
		}
	
    }
}
