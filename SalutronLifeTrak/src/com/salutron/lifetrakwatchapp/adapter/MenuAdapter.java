package com.salutron.lifetrakwatchapp.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.ArrayAdapter;

public class MenuAdapter extends ArrayAdapter<MenuItem> {
	private List<MenuItem> mMenus;
	private LayoutInflater mInflater;

	public MenuAdapter(Context context, int resource, List<MenuItem> menus) {
		super(context, resource, menus);
		mMenus = menus;
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mMenus.get(position).getView(mInflater, convertView);
	}
	
	@Override
	public int getItemViewType(int position) {
		return mMenus.get(position).getItemViewType();
	}
	
	@Override
	public int getViewTypeCount() {
		return 4;
	}
}
