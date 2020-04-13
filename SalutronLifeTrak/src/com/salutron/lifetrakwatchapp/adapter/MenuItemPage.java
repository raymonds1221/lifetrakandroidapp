package com.salutron.lifetrakwatchapp.adapter;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.Menu;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuItemPage implements MenuItem {
	private Menu mMenu;
	
	public MenuItemPage(Menu menu)  {
		mMenu = menu;
	}
	
	@Override
	public int getItemViewType() {
		return MENU_VIEW_TYPE_PAGE;
	}
	
	@Override
	public int getItemType() {
		return mMenu.getMenuItemType();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = convertView;
		ViewHolder viewHolder = null;
		
		if(view == null) {
			viewHolder = new ViewHolder();
			view = inflater.inflate(R.layout.adapter_menu, null);
			viewHolder.menuIcon = (ImageView) view.findViewById(R.id.imgMenuIcon);
			viewHolder.menuItem = (TextView) view.findViewById(R.id.tvwMenuItem);
			viewHolder.menuSeparator = view.findViewById(R.id.vwMenuSeparator);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.menuIcon.setImageResource(mMenu.getResourceId());
		viewHolder.menuItem.setText(mMenu.getName());
		viewHolder.menuSeparator.setVisibility(mMenu.isSeparatorVisible() ? View.VISIBLE : View.GONE);
		
		return view;
	}

	private class ViewHolder {
		public ImageView menuIcon;
		public TextView menuItem;
		public View menuSeparator;
	}
}
