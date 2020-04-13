package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public interface DashboardItem extends Parcelable, SalutronLifeTrakUtility {
	public int getItemViewType();
	public int getDashboardType();
	public void setDate(Date date);
	public View getView(LayoutInflater inflater, View convertView);
}
