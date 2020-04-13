package com.salutron.lifetrakwatchapp.fragment;

import java.text.ParseException; 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.ActivityAlertSetting;

public class InactiveAlertFragment extends BaseFragment implements  OnSeekBarChangeListener, OnClickListener, OnCheckedChangeListener, OnTimeSetListener{
	private Switch switchStatus;
	private TextView textviewTimeDurationMinute;
	private TextView textviewTimeDurationHour;
	private TextView textviewStepsThreshold;
	private TextView textviewStartTime;
	private TextView textviewEndTime;
	private TextView textviewTD;
	private TextView textviewSTime;
	private TextView textviewETime;
	private TextView textViewST;
	private TextView tvStart;
	private TextView tvEnd;
	private TextView textviewTimeDurationLabelHour;
	private TextView textviewTimeDurationLabelMinute;
	private EditText editTextStepThreshold;
	
	private RelativeLayout relativeClick;

	private SeekBar seekBarStepsThreshold;

	TimePickerDialog mTimePicker;

	private View view1, view2, view3, view4, view5;

	private List<ActivityAlertSetting> alertSettings ;
	private ActivityAlertSetting mActivityAlertSettings;

	private boolean isdateshow = false;
	private boolean flag_ischange= false;
	private boolean isStartTime = false;
	private boolean isEndTime = false;
	private boolean noError = false;
	private Dialog alertDialog;
	
	private String exposureDurationHourToDisplay = "0";
	private String exposureDurationMinToDisplay = "0";
	
	private NumberPicker dayLightNumberPickerHour;
	private NumberPicker dayLightNumberPickerMin;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_inactive_alert, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		switchStatus = (Switch) getView().findViewById(R.id.switchStatus);
		textviewTimeDurationMinute = (TextView)getView().findViewById(R.id.textviewTimeDurationMinute);
		textviewTimeDurationHour =(TextView) getView().findViewById(R.id.textviewTimeDurationHour);
		textviewStepsThreshold = (TextView) getView().findViewById(R.id.textviewStepsThreshold);
		textviewStartTime = (TextView) getView().findViewById(R.id.textviewStartTime);
		textviewEndTime = (TextView) getView().findViewById(R.id.textviewEndTime);
		seekBarStepsThreshold = (SeekBar) getView().findViewById(R.id.seekBarStepsThreshold);
		textviewTD = (TextView) getView().findViewById(R.id.textviewTD);
		textviewSTime = (TextView) getView().findViewById(R.id.textviewSTime);
		textviewETime =(TextView) getView().findViewById(R.id.textviewETime);
		textViewST = (TextView) getView().findViewById(R.id.textViewST);
		tvStart = (TextView) getView().findViewById(R.id.tvStart);
		tvEnd = (TextView) getView().findViewById(R.id.tvEnd);
		textviewTimeDurationLabelHour = (TextView) getView().findViewById(R.id.textviewTimeDurationLabelHour);
		textviewTimeDurationLabelMinute = (TextView) getView().findViewById(R.id.textviewTimeDurationLabelMinute);
		editTextStepThreshold = (EditText) getView().findViewById(R.id.editTextStepThreshold);
		relativeClick =  (RelativeLayout) getView().findViewById(R.id.relativeClick);

		view1 = (View) getView().findViewById(R.id.view1);
		view2 = (View) getView().findViewById(R.id.view2);
		view3 = (View) getView().findViewById(R.id.view3); 
		view4 = (View) getView().findViewById(R.id.view4); 
		view5 = (View) getView().findViewById(R.id.view5);

		intializeData();

		switchStatus.setOnCheckedChangeListener(this);
		seekBarStepsThreshold.setOnSeekBarChangeListener(this);
		textviewStartTime.setOnClickListener(this);
		textviewEndTime.setOnClickListener(this);
		relativeClick.setOnClickListener(this);

		editTextStepThreshold.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try{
					if (Integer.parseInt(editTextStepThreshold.getText().toString().trim()) < 1)
						editTextStepThreshold.setText("1");
					
				   if (!editTextStepThreshold.getText().toString().trim().equals(""))
						seekBarStepsThreshold.setProgress(Integer.parseInt(editTextStepThreshold.getText().toString()));
				}catch (Exception e){}

				editTextStepThreshold.setSelection(editTextStepThreshold.getText().length());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {


			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	
	
		View view = getView();
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (!switchStatus.isChecked()){
					getFragmentManager().popBackStackImmediate();
					return true;
				}
				else{
					if (event.getAction()!=KeyEvent.ACTION_DOWN)
						return true;	
					if( keyCode == KeyEvent.KEYCODE_BACK ) {
						compareTime();
						if(noError || flag_ischange == false){
							getFragmentManager().popBackStackImmediate();
							return true;
						}
						else return false;
					} else {
						return false;
					}
				}

			}
		});

	}

	private void showViews(boolean mboolean){
		textviewTimeDurationMinute.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewTimeDurationHour.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewStepsThreshold.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewStartTime.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewEndTime.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		seekBarStepsThreshold.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewTD.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewSTime.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewETime.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textViewST.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		tvStart.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		tvEnd.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewTimeDurationLabelHour.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		textviewTimeDurationLabelMinute.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		editTextStepThreshold.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		relativeClick.setVisibility((mboolean) ? View.VISIBLE : View.GONE);

		view1.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view2.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view3.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view4.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
		view5.setVisibility((mboolean) ? View.VISIBLE : View.GONE);
	}

	/*private void SelectDate(final View v){
		Calendar mcurrentTime = Calendar.getInstance();
		int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
		int minute = mcurrentTime.get(Calendar.MINUTE);
		boolean is24hours = false;
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			is24hours = false;
			break;
		case TIME_FORMAT_24_HR:
			is24hours = true;
			break;	
		}	

		mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				int mHour=hourOfDay;
				int mMin=minute;
				String AM_PM ;
				if(hourOfDay < 12) {
					AM_PM = "AM";

				} else {
					AM_PM = "PM";
					mHour = mHour-12;
				}

				if (mHour == 0)
					mHour = 12;

				switch(v.getId()){
				case R.id.textviewStartTime:

					textviewStartTime.setText(changeTimeFormat("" + mHour + ":" + String.format("%02d", mMin) + " "+AM_PM));
					//compareTime();
					break;
				case R.id.textviewEndTime:
					textviewEndTime.setText(changeTimeFormat("" + mHour + ":" + String.format("%02d", mMin) + " "+AM_PM));
					//compareTime();
					break;
				}
				
				Date fDate = null;
				Date lDate = null; 
				switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
				case TIME_FORMAT_12_HR:
					lDate = returnDate(textviewEndTime.getText().toString());
					fDate = returnDate(textviewStartTime.getText().toString());
					break;
				}
				case TIME_FORMAT_24_HR:
					lDate = returnDate24Hours(textviewEndTime.getText().toString());
					fDate = returnDate24Hours(textviewStartTime.getText().toString());
					break;
				}

				Calendar fcalendar = Calendar.getInstance();
				fcalendar.setTime(fDate);
				int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
				int fminutes = fcalendar.get(Calendar.MINUTE);

				Calendar ecalendar = Calendar.getInstance();
				ecalendar.setTime(lDate);
				int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
				int eminutes = ecalendar.get(Calendar.MINUTE);
				eminutes = eminutes - fminutes;
				if (eminutes < 0){
					eminutes = eminutes + 60;
					ehours = ehours -1;
				}
				textviewTimeDurationHour.setText("" + (ehours - fhours));
				textviewTimeDurationMinute.setText("" + eminutes);


			}
		}, hour, minute, is24hours);
		mTimePicker.setTitle("Select Time");
		mTimePicker.show();
	}*/

	@SuppressLint("SimpleDateFormat")
	private Date returnDate(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");	
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
	}
	@SuppressLint("SimpleDateFormat")
	public String changeTimeFormat(String time) {
		String returnValue = "";
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			returnValue = time;
			break;
		case TIME_FORMAT_24_HR:
			SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
			try {
				returnValue = displayFormat.format(parseFormat.parse(time));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		}
		return returnValue;
	}

	@SuppressLint("SimpleDateFormat")
	private Date returnDate24Hours(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");	
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
	}

	private void compareTime(){
		if (isdateshow){
			isdateshow = false;

			flag_ischange = true;

			Date fDate = null;
			Date lDate = null; 
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				lDate = returnDate(textviewEndTime.getText().toString());
				fDate = returnDate(textviewStartTime.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				lDate = returnDate24Hours(textviewEndTime.getText().toString());
				fDate = returnDate24Hours(textviewStartTime.getText().toString());
				break;
			}


			boolean isthirtymin = false;
			int mhours, mminutes = 0;

			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			Calendar ecalendar = Calendar.getInstance();
			ecalendar.setTime(lDate);
			int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
			int eminutes = ecalendar.get(Calendar.MINUTE);

			/* ------------------- */

			mminutes = fminutes + 29;
			mhours = fhours;
			if (mminutes > 59){
				mhours = fhours + 1;
				mminutes = mminutes - 59;
			}
			if ((mminutes + (mhours*60)) > ((ehours*60) + eminutes))
				isthirtymin = true;

			/* ------------------- */
			
//			eminutes = eminutes - fminutes;
//			if (eminutes < 0){
//				eminutes = eminutes + 60;
//				ehours = ehours -1;
//			}
			
			//Comparing data
			if (fDate.getTime() > lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error1));
				setInitialDate(fhours, fminutes);
			}
			else if (fDate.getTime() == lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error2));
				setInitialDate(fhours, fminutes);
			}
			else if ((Integer.parseInt(textviewTimeDurationHour.getText().toString()) * 60 + Integer.parseInt(textviewTimeDurationMinute.getText().toString())) 
					> 
					(((ehours- fhours) * 60) + eminutes)){
				showAlert(getString(R.string.daylight_exposure_error6));
					int exposureDurationHourToDisplay = (ecalendar.get(Calendar.HOUR_OF_DAY) - fcalendar.get(Calendar.HOUR_OF_DAY));
					int exposureDurationMinToDisplay = (ecalendar.get(Calendar.MINUTE) - fcalendar.get(Calendar.MINUTE));
					if (exposureDurationMinToDisplay < 0){
						exposureDurationHourToDisplay = exposureDurationHourToDisplay - 1;
						exposureDurationMinToDisplay = exposureDurationMinToDisplay + 60;
					}
						
					textviewTimeDurationHour.setText(""+exposureDurationHourToDisplay);
					textviewTimeDurationMinute.setText(""+exposureDurationMinToDisplay);
					isdateshow = true;
				}
			else if (isthirtymin){
				showAlert(getString(R.string.daylight_exposure_error3));
				setInitialDate(eminutes, ehours, fhours, fminutes);
			}
			else //valid time input
			{
				noError = true;
			}
		}
	}

	private void setInitialDate(int eminutes, int ehours, int fhours,int fminutes){
		eminutes = fminutes + 30;
		if (eminutes >= 60){
			eminutes = eminutes - 60;
			ehours = ehours+1;
		}
		isdateshow = true;
	//	textviewTimeDurationHour.setText("0");
	//	textviewTimeDurationMinute.setText("30");

		String AM_PM ;
		if(ehours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			ehours=ehours-12;
		}

		if (ehours == 0)
			ehours = 12;
		
		textviewEndTime.setText(changeTimeFormat(ehours + ":" + String.format("%02d", eminutes) + " " + AM_PM));
	}

	private void setInitialDate(int fhours,int fminutes){
		fminutes = fminutes + 30;
		if (fminutes >= 60){
			fminutes = fminutes - 60;
			fhours = fhours+1;
		}
		isdateshow = true;
		//textviewTimeDurationHour.setText("0");
		//textviewTimeDurationMinute.setText("30");

		String AM_PM ;
		if(fhours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			fhours=fhours-12;
		}

		if (fhours == 0)
			fhours = 12;

		textviewEndTime.setText(changeTimeFormat(fhours + ":" + String.format("%02d", fminutes) + " " + AM_PM));
	}

	private void showAlert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setTitle(getString(R.string.error_text));
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}

		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		flag_ischange = true;
		isdateshow = true; 
		editTextStepThreshold.setText(""+ progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {


	}

	@Override		
	public void onStopTrackingTouch(SeekBar seekBar) {


	}
	
	private void SelectDate(final View v){
       // Calendar mcurrentTime = Calendar.getInstance();
        int hour = 0;
        int minute = 0;

		if (isStartTime){
            Date fDate = null;
            switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
                case TIME_FORMAT_12_HR:
                    fDate = returnDate(textviewStartTime.getText().toString());

                    break;
                case TIME_FORMAT_24_HR:
                    fDate = returnDate24Hours(textviewStartTime.getText().toString());

                    break;
            }

            Calendar fcalendar = Calendar.getInstance();
            fcalendar.setTime(fDate);
            hour  = fcalendar.get(Calendar.HOUR_OF_DAY);
            minute = fcalendar.get(Calendar.MINUTE);

		}
		if (isEndTime){
            Date eDate = null;
            switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
                case TIME_FORMAT_12_HR:
                    eDate = returnDate(textviewEndTime.getText().toString());

                    break;
                case TIME_FORMAT_24_HR:
                    eDate = returnDate24Hours(textviewEndTime.getText().toString());

                    break;
            }

            Calendar fcalendar = Calendar.getInstance();
            fcalendar.setTime(eDate);
            hour  = fcalendar.get(Calendar.HOUR_OF_DAY);
            minute = fcalendar.get(Calendar.MINUTE);
		}

		
		boolean is24hours = false;
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			is24hours = false;
			break;
		case TIME_FORMAT_24_HR:
			is24hours = true;
			break;					
		}

		mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				int mHour=hourOfDay;
				int mMin=minute;
				String AM_PM ;
				if(hourOfDay < 12) {
					AM_PM = getString(R.string.am);

				} else {
					AM_PM = getString(R.string.pm);
					mHour=mHour-12;
				}
				if (mHour == 0)
					mHour = 12;
				
				flag_ischange = true;
				if (isStartTime){
					isStartTime = false;
					textviewStartTime.setText(changeTimeFormat("" + mHour + ":" + String.format("%02d", mMin) + " "+AM_PM));
				}
				if (isEndTime){
					isEndTime = false;
					textviewEndTime.setText(changeTimeFormat("" + mHour + ":" + String.format("%02d", mMin) + " "+AM_PM));	
				}


			}
		}, hour, minute, is24hours);
		mTimePicker.setTitle(getString(R.string.select_time));
		mTimePicker.show();
	}

	private void SelectDuration(final View v){
		Calendar mcurrentTime = Calendar.getInstance();
		int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
		int minute = mcurrentTime.get(Calendar.MINUTE);
		
		
//		mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
//			@Override
//			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//				flag_ischange = true;
//				textviewTimeDurationHour.setText("" + hourOfDay);
//				textviewTimeDurationMinute.setText("" + minute);
//
//
//			}
//		}, hour, minute, true);
//		mTimePicker.setTitle(getString(R.string.select_duration));
//		mTimePicker.show();
	}

	
	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.textviewStartTime:
			isdateshow = true;
			isStartTime = true;
			SelectDate(v); 
		
//			String timeStart  = textviewStartTime.getText().toString(); 
//			String hourStart;
//			String minStart;
//			if(!is24hours){
//			if(timeStart.length() > 7){
//				hourStart = timeStart.substring(0, 2);
//				minStart = timeStart.substring(3, 5);
//				ampm = timeStart.substring(6, 8);
//			}
//			else{
//				hourStart = timeStart.substring(0, 1);
//				minStart = timeStart.substring(2, 4);
//				ampm = timeStart.substring(5, 7);
//			}
//			}
//			else{
//				hourStart = timeStart.substring(0, 2);
//				minStart = timeStart.substring(3, 5);
//				ampm = timeStart.substring(6, 8);
//			}
//			if(ampm.equals("PM"))
//				finalHour = Integer.parseInt(hourStart) + 12;
//			else 
//				finalHour = Integer.parseInt(hourStart);
//			mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, finalHour, Integer.parseInt(minStart), is24hours);
//			mTimePicker.show();
			break;
		case R.id.textviewEndTime:
			isdateshow = true;
			isEndTime = true;
			SelectDate(v); 
//			
//
//			String timeEnd  = textviewEndTime.getText().toString(); 
//			String hourEnd;
//			String minEnd;
//			if(!is24hours){
//			if(timeEnd.length() > 7){
//				hourEnd = timeEnd.substring(0, 2);
//				minEnd = timeEnd.substring(3, 5);
//				ampm = timeEnd.substring(6, 8);
//			}
//			else{
//				hourEnd = timeEnd.substring(0, 1);
//				minEnd = timeEnd.substring(2, 4);
//				ampm = timeEnd.substring(5, 7);	
//			}
//			}
//			else{
//				hourEnd = timeEnd.substring(0, 2);
//				minEnd = timeEnd.substring(3, 5);
//				ampm = timeEnd.substring(6, 8);
//			}
//			
//			if(ampm.equals("PM"))
//				finalHour = Integer.parseInt(hourEnd) + 12;
//			else 
//				finalHour = Integer.parseInt(hourEnd);
//			mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, finalHour, Integer.parseInt(minEnd), is24hours);
//			mTimePicker.show();
			break;
			
		case R.id.relativeClick:
			isdateshow = true;
			SelectDuration(v);
		case R.id.day_light_alert_exposure_duration:
			isdateshow = true;
			Log.d("DAYLIGHT", "Daylight");
			alertDialog = new Dialog(getActivity());
			alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			alertDialog.setContentView(R.layout.number_picker_custom_dialog_layout);

			alertDialog.findViewById(R.id.number_picker_custom_dialog_set).setOnClickListener(alertListener);
			alertDialog.findViewById(R.id.number_picker_custom_dialog_cancel).setOnClickListener(alertListener);

			// Hours
			dayLightNumberPickerHour = (NumberPicker) alertDialog.findViewById(R.id.number_picker_custom_dialog_hour);
			dayLightNumberPickerHour.setMaxValue(4);       
			dayLightNumberPickerHour.setMinValue(0);         
			dayLightNumberPickerHour.setValue(Integer.parseInt(exposureDurationHourToDisplay));
			dayLightNumberPickerHour.setWrapSelectorWheel(true);
			dayLightNumberPickerHour.setOnValueChangedListener( new NumberPicker.
					OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					int minVal = Integer.valueOf(exposureDurationMinToDisplay);
					int hourVal = newVal;
					
					if (minVal < 10 && hourVal == 0) {
						minVal = 10;
					} else if (hourVal == 5 && minVal > 0) {
						minVal = 0;
						//showAlert(getResources().getString(R.string.invalid_duration_time));
					}

					exposureDurationHourToDisplay = Integer.toString(hourVal);
					dayLightNumberPickerHour.setValue(hourVal);
					exposureDurationMinToDisplay = Integer.toString(minVal);
					dayLightNumberPickerMin.setValue(minVal);
					//dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
					//dayLightDurationValueMin.setText(exposureDurationMinToDisplay);
					Log.e(TAG, "Selected number is "+ exposureDurationHourToDisplay);
					flag_ischange = true;
				}
			});

			// Minutes
			dayLightNumberPickerMin = (NumberPicker) alertDialog.findViewById(R.id.number_picker_custom_dialog_minutes);
			dayLightNumberPickerMin.setMaxValue(59);       
			dayLightNumberPickerMin.setMinValue(0);         
			dayLightNumberPickerMin.setValue(Integer.parseInt(exposureDurationMinToDisplay)); //Integer.parseInt(nightLightDurationValueMin.getText().toString())
			dayLightNumberPickerMin.setWrapSelectorWheel(true);
			dayLightNumberPickerMin.setOnValueChangedListener( new NumberPicker.OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					int minVal = newVal;
					int hourVal = Integer.valueOf(exposureDurationHourToDisplay);
					Log.e(TAG, "Selected number is "+ exposureDurationHourToDisplay);    
					if (minVal < 10 && hourVal == 0) {
						minVal = 10;
					}
//                    else if (hourVal == 4 && minVal > 0) {
//						minVal = 0;
//						//showAlert(getResources().getString(R.string.invalid_duration_time));
//					}

					exposureDurationHourToDisplay = Integer.toString(hourVal);
					dayLightNumberPickerHour.setValue(hourVal);
					exposureDurationMinToDisplay = Integer.toString(minVal);
					dayLightNumberPickerMin.setValue(minVal);
					//dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
					//dayLightDurationValueMin.setText(exposureDurationMinToDisplay);
					flag_ischange = true;
				}
			});

			alertDialog.setCancelable(false);
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.show();		
			break;

		}

	}

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (flag_ischange)
            syncData();
    }

	@Override
	public void onDetach() {
		super.onDetach();
		if(isRemoving()){
			if (flag_ischange)
				syncData();
		}
	}

	private void intializeData(){
		alertSettings = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchActivityAlert = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(ActivityAlertSetting.class);

		if (alertSettings.size() > 0) {
			mActivityAlertSettings = alertSettings.get(0);
			mActivityAlertSettings.update();

			textviewStartTime.setText(changeTimeFormat(convertMinuteToTime(mActivityAlertSettings.getStartTime())));
			textviewEndTime.setText(changeTimeFormat(convertMinuteToTime(mActivityAlertSettings.getEndTime())));

			seekBarStepsThreshold.setProgress(mActivityAlertSettings.getStepsThreshold());
			editTextStepThreshold.setText("" + mActivityAlertSettings.getStepsThreshold());

			switchStatus.setChecked(mActivityAlertSettings.isEnabled());

			int hours = mActivityAlertSettings.getTimeInterval() / 60;
			int hoursToDisplay = hours;

			if (hours > 12) {
				hoursToDisplay = hoursToDisplay - 12;
			}

			int minutesToDisplay = mActivityAlertSettings.getTimeInterval() - (hours * 60);

			textviewTimeDurationMinute.setText(String.format("%02d", minutesToDisplay));
			textviewTimeDurationHour.setText(""+ hoursToDisplay);

		} else 
			initialData();	

		if (switchStatus.isChecked())
			showViews(true);
		else
			showViews(false);
	}

	private void initialData(){
		textviewStartTime.setText(changeTimeFormat("9:00 " + getString(R.string.am)));
		textviewEndTime.setText(changeTimeFormat("12:00 " + getString(R.string.pm)));
		seekBarStepsThreshold.setProgress(200);
		editTextStepThreshold.setText("" + 200);
		//switchStatus.setChecked(true);
		textviewTimeDurationHour.setText(""+3);
		textviewTimeDurationMinute.setText(""+00);
		
		mActivityAlertSettings = new ActivityAlertSetting();
		
		Date fDate = null;
		Date lDate = null;
		
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			fDate = returnDate(textviewStartTime.getText().toString());
			lDate = returnDate(textviewEndTime.getText().toString());
			break;
		case TIME_FORMAT_24_HR:
			fDate = returnDate24Hours(textviewStartTime.getText().toString());
			lDate = returnDate24Hours(textviewEndTime.getText().toString());
			break;
		}
		
		Calendar fcalendar = Calendar.getInstance();
		fcalendar.setTime(fDate);
		int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
		int fminutes = fcalendar.get(Calendar.MINUTE);

		Calendar ecalendar = Calendar.getInstance();
		ecalendar.setTime(lDate);
		int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
		int eminutes = ecalendar.get(Calendar.MINUTE);

		fminutes = (fhours*60) + fminutes;
		eminutes = (ehours*60) + eminutes;

		int stepThreshold = 1;
		if (!editTextStepThreshold.getText().toString().trim().equals(""))
			stepThreshold = Integer.parseInt(editTextStepThreshold.getText()
					.toString()
					.replace("steps", "")
					.trim());
		
//		mActivityAlertSettings.setStepsThreshold(stepThreshold);
//
//		mActivityAlertSettings.setStartTime(fminutes);
//		mActivityAlertSettings.setEndTime(eminutes);
//		mActivityAlertSettings.setEnabled(switchStatus.isChecked());
//		mActivityAlertSettings.setTimeInterval((Integer.parseInt(textviewTimeDurationHour.getText().toString()) *60) + Integer.parseInt(textviewTimeDurationMinute.getText().toString()));
//		mActivityAlertSettings.setWatch(getLifeTrakApplication().getSelectedWatch());
//
		//mActivityAlertSettings.insert();
	}

	private String convertMinuteToTime(int totalMinutesInt){

		int hours = totalMinutesInt / 60;
		int hoursToDisplay = hours;

		if (hours > 12) {
			hoursToDisplay = hoursToDisplay - 12;
		}

		if (hoursToDisplay == 0)
			hoursToDisplay = 12;

		int minutesToDisplay = totalMinutesInt - (hours * 60);

		String minToDisplay = null;
		if(minutesToDisplay == 0 ) minToDisplay = "00";     
		else if( minutesToDisplay < 10 ) minToDisplay = "0" + minutesToDisplay ;
		else minToDisplay = "" + minutesToDisplay ;

		String displayValue = hoursToDisplay + ":" + minToDisplay;

		if (hours < 12)
			displayValue = displayValue + " " + getString(R.string.am);
		else
			displayValue = displayValue + " " + getString(R.string.pm);

		return displayValue;
	}

	private void syncData(){
		if (flag_ischange){
			Date fDate = null;
			Date lDate = null;

			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				fDate = returnDate(textviewStartTime.getText().toString());
				lDate = returnDate(textviewEndTime.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				fDate = returnDate24Hours(textviewStartTime.getText().toString());
				lDate = returnDate24Hours(textviewEndTime.getText().toString());
				break;
			}


			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			Calendar ecalendar = Calendar.getInstance();
			ecalendar.setTime(lDate);
			int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
			int eminutes = ecalendar.get(Calendar.MINUTE);

			fminutes = (fhours*60) + fminutes;
			eminutes = (ehours*60) + eminutes;

			int stepThreshold = 1;
			if (!editTextStepThreshold.getText().toString().trim().equals(""))
				stepThreshold = Integer.parseInt(editTextStepThreshold.getText()
						.toString()
						.replace("steps", "")
						.trim());
			mActivityAlertSettings.setEnabled(switchStatus.isChecked());
			mActivityAlertSettings.setStepsThreshold(stepThreshold);
			mActivityAlertSettings.setStartTime(fminutes);
			mActivityAlertSettings.setEndTime(eminutes);
			mActivityAlertSettings.setTimeInterval((Integer.parseInt(textviewTimeDurationHour.getText().toString()) *60) + Integer.parseInt(textviewTimeDurationMinute.getText().toString()));
			mActivityAlertSettings.setWatch(getLifeTrakApplication().getSelectedWatch());
			mActivityAlertSettings.setContext(getActivity());

			if (alertSettings.size() > 0) {
				mActivityAlertSettings.update();
			} else {
				mActivityAlertSettings.insert();
			}	
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		flag_ischange = true;
		isdateshow=true;
		showViews(isChecked);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		
	}

//	// TimePicker
//	private final TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
//		@Override
//		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//			String AM_PM ;
//			if(hourOfDay < 12) {
//				AM_PM = "AM";
//
//			} else {
//				AM_PM = "PM";
//				hourOfDay=hourOfDay-12;
//			}
//			if (isStartTime) {
//				//nightLightStartTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
//				textviewStartTime.setText( "" + hourOfDay + ":" + String.format("%02d", minute) + " "+AM_PM);
//				isStartTime = false;
//				compareTime();
//			}
//			else if (isEndTime) {
//				//nightLightEndTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
//				textviewEndTime.setText( "" + hourOfDay + ":" + String.format("%02d", minute) + " "+AM_PM);
//				isEndTime = false;
//				compareTime();
//			} 
//
//		}
//	};
	private final View.OnClickListener alertListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.number_picker_custom_dialog_set:
				if(exposureDurationHourToDisplay == "")
					exposureDurationHourToDisplay = "0";
				if(exposureDurationMinToDisplay == "")
					exposureDurationMinToDisplay = "00";
				textviewTimeDurationHour.setText(exposureDurationHourToDisplay);
				textviewTimeDurationMinute.setText(exposureDurationMinToDisplay);
				alertDialog.dismiss();
				break;
			case R.id.number_picker_custom_dialog_cancel:
				alertDialog.dismiss();
				break;
			}
		}
	};
}