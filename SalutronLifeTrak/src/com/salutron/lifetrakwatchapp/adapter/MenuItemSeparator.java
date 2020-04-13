package com.salutron.lifetrakwatchapp.adapter;

import android.view.LayoutInflater;
import android.view.View;

import com.salutron.lifetrak.R;

public class MenuItemSeparator implements MenuItem {

	@Override
	public int getItemViewType() {
		return MENU_VIEW_TYPE_SEPARATOR;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		return inflater.inflate(R.layout.menu_item_separator, null);
	}

	@Override
	public int getItemType() {
		return MENU_ITEM_SEPARATOR;
	}
}
