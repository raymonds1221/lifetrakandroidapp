package com.salutron.lifetrakwatchapp.adapter;

import java.util.List;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ArrayAdapter;

public class DashboardItemAdapter extends ArrayAdapter<DashboardItem> {
	private List<DashboardItem> mDashboardItems;
	private LayoutInflater mInflater;

	public DashboardItemAdapter(Context context, int resource, List<DashboardItem> dashboardItems) {
		super(context, resource, dashboardItems);
		mDashboardItems = dashboardItems;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mDashboardItems.get(position).getView(mInflater, convertView);
	}
	
	@Override
	public int getItemViewType(int position) {
		return mDashboardItems.get(position).getItemViewType();
	}
	
	@Override
	public int getViewTypeCount() {
		return 8;
	}
	
	public DashboardItem getItemWithType(int type) {
		for(DashboardItem dashboardItem : mDashboardItems) {
			if(dashboardItem.getDashboardType() == type) {
				return dashboardItem;
			}
		}
		return null;
	}
	
	@Override
	public DashboardItem getItem(int position) {
		return mDashboardItems.get(position);
	}
}
