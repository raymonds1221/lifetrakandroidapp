package com.salutron.lifetrakwatchapp.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

public class LifeTrakViewPager extends ViewPager {

	public LifeTrakViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return false;
		}
		return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return false;
		}
		return super.onTouchEvent(event);
	}
}
