package com.salutron.lifetrakwatchapp.adapter;

import java.util.Calendar;
import java.util.Date;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public abstract class BaseFragmentAdapter extends FragmentStatePagerAdapter implements SalutronLifeTrakUtility {

	public BaseFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public Date getYesterdayForDate(Date date) {
		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		return calYesterday.getTime();
	}
	
	public Date getTomorrowForDate(Date date) {
		Calendar calTomorrow = Calendar.getInstance();
		calTomorrow.setTime(date);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		
		return calTomorrow.getTime();
	}
}
