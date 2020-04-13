package com.salutron.lifetrakwatchapp.adapter;

import java.util.Date;

import android.os.Parcelable;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;

import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public interface GoalItem extends Parcelable, SalutronLifeTrakUtility {

	void setDate(Date date);
	void updateGoal(Context context, double value);

	View getView(LayoutInflater inflater, View convertView);
	
	class ViewHolder {
		public TextView title;
		public TextView value;
		public TextView valueMin;
		public TextView valueMax;
		public SeekBar valueSeeker;
		public EditText valueEdit;
	}

	void applyValueFromEditor();
}
