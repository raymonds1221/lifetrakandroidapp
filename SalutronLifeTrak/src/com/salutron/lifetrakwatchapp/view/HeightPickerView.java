package com.salutron.lifetrakwatchapp.view;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.app.Dialog;
import android.view.View;
import android.content.Context;
import android.widget.NumberPicker;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

public class HeightPickerView extends Dialog implements View.OnClickListener, SalutronLifeTrakUtility {
	private NumberPicker mFeetPicker;
	private NumberPicker mInchPicker;
	private OnSelectHeightListener mListener;
	private final DecimalFormat mDecimalFormat = new DecimalFormat("#####0");

	public HeightPickerView(Context context) {
		super(context);
		setContentView(R.layout.view_height_picker);

		initializeObjects();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnOk) {
			float feet = mFeetPicker.getValue() * FEET_CM;
			float inch = mInchPicker.getValue() * INCH_CM;
			int value = (int) Math.ceil(feet + inch);

			if (mListener != null) {
				mListener.onSelectHeight((int) value);
			}
		}
		dismiss();
	}

	private void initializeObjects() {
		mDecimalFormat.setRoundingMode(RoundingMode.DOWN);
		mFeetPicker = (NumberPicker) findViewById(R.id.numFeet);
		mInchPicker = (NumberPicker) findViewById(R.id.numInch);

		mFeetPicker.setMinValue(3);
		mFeetPicker.setMaxValue(7);
		mInchPicker.setMinValue(0);
		mInchPicker.setMaxValue(11);

		findViewById(R.id.btnOk).setOnClickListener(this);
		findViewById(R.id.btnCancel).setOnClickListener(this);

		mFeetPicker.setOnValueChangedListener(mValueChangedListener);
		mInchPicker.setOnValueChangedListener(mValueChangedListener);
	}

	private final NumberPicker.OnValueChangeListener mValueChangedListener = new NumberPicker.OnValueChangeListener() {
		@Override
		public void onValueChange(NumberPicker view, int oldVal, int newVal) {
			initInchPicker();
		}
	};

	public static interface OnSelectHeightListener {
		public void onSelectHeight(int valueInCm);
	}

	public void setOnSelectHeightListener(OnSelectHeightListener listener) {
		mListener = listener;
	}

	public void setValue(int value) {
		double feetValue = Math.floor(value / FEET_CM);
		double inchValue = (value / INCH_CM) - (feetValue * 12);

		if (Math.round(inchValue) == 12) {
			feetValue++;
			inchValue = 0;
		}

		mFeetPicker.setValue((int) feetValue);
		mInchPicker.setValue((int) Math.round(inchValue));
		initInchPicker();
	}

	private void initInchPicker() {
		if (mFeetPicker.getValue() == 7) {
			mInchPicker.setMinValue(0);
			mInchPicker.setMaxValue(3);
		} else if (mFeetPicker.getValue() == 3) {
			mInchPicker.setMinValue(4);
			mInchPicker.setMaxValue(11);
		} else {
			mInchPicker.setMinValue(0);
			mInchPicker.setMaxValue(11);
		}
	}
}
