package com.salutron.lifetrakwatchapp.adapter;

import android.view.LayoutInflater;
import android.view.View;

import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public interface MenuItem extends SalutronLifeTrakUtility {
	public int getItemViewType();
	public int getItemType();
	public View getView(LayoutInflater inflater, View convertView);
}
