package com.salutron.lifetrakwatchapp.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.model.BaseModel;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

abstract class BaseArrayAdapter <T extends BaseModel> extends ArrayAdapter<T> implements SalutronLifeTrakUtility {
	protected LayoutInflater mInflater;
	private LifeTrakApplication mLifeTrakApplication;

	public BaseArrayAdapter(Context context, int resource, List<T> objects) {
		super(context, resource, objects);
		mInflater = LayoutInflater.from(context);
	}
	
	protected LifeTrakApplication getLifeTrakApplication() {
		if(mLifeTrakApplication == null)
			mLifeTrakApplication = (LifeTrakApplication) getContext().getApplicationContext();
		return mLifeTrakApplication;
	}
}
