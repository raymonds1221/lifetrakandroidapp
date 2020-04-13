package com.salutron.lifetrakwatchapp.model;

import android.content.Context;
import android.os.Parcel;

public class Menu extends BaseModel {
	private int resourceId;
	private String name;
	private boolean separatorVisible;
	private int menuItemType;
	
	public Menu() {
	}

	public Menu(Context context) {
		super(context);
	}
	
	public Menu(int resourceId, String name, boolean separatorVisible, int menuItemType) {
		this.resourceId = resourceId;
		this.name = name;
		this.separatorVisible = separatorVisible;
		this.menuItemType = menuItemType;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}

	@Override
	public void readFromParcel(Parcel source) {
		
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSeparatorVisible() {
		return separatorVisible;
	}

	public int getMenuItemType() {
		return menuItemType;
	}
}
