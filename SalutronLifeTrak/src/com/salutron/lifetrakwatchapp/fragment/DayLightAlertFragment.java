package com.salutron.lifetrakwatchapp.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.salutron.blesdk.SALBLEService;
import com.salutron.blesdk.SALDayLightDetectSetting;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.db.DataSource;
import com.salutron.lifetrakwatchapp.model.DayLightDetectSetting;
import com.salutron.lifetrakwatchapp.model.Goal;
import com.salutron.lifetrakwatchapp.model.NightLightDetectSetting;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

public class DayLightAlertFragment extends BaseFragment implements OnSeekBarChangeListener, OnCheckedChangeListener {

	private static String TAG = DayLightAlertFragment.class.getCanonicalName();

	private Switch switchStatus;
	private TextView dayLightExposureLevel;
	private TextView dayLightDurationValueHour;
	private TextView dayLightDurationValueMin;
	private TextView dayLightStartTimeValue;
	private TextView dayLightEndTimeValue;
	private View dayLightDuration;


	private NumberPicker dayLightNumberPickerHour;
	private NumberPicker dayLightNumberPickerMin;

	private EditText alertFrequencyValue;
	private int minFrequencyValue;

	private Dialog alertDialog;
	private TimePickerDialog mTimePicker;
	private boolean isStartTime = false;
	private boolean isEndTime = false;
	private boolean noError = false;

	private String exposureDurationHourToDisplay = "0";
	private String exposureDurationMinToDisplay = "0";

	private SeekBar alertFrequencySeekerBar;
	private boolean isdateshow = false;

	private List<DayLightDetectSetting> dayLightDetecttSettings ;
	private DayLightDetectSetting mDayLightDetectSetting;
	private boolean flag_ischange= false;

    private List<NightLightDetectSetting> nightLightDetectSetting ;
    private NightLightDetectSetting mNightLightDetectSetting;

	//private boolean flag_textchange = false;

	SALBLEService objService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(TAG, "OnCreate");
	}	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_day_light_alert, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switchStatus = (Switch) getView().findViewById(R.id.day_light_alert_switch_status);
		dayLightExposureLevel = (TextView) getView().findViewById(R.id.daylight_alert_exposure_level);
		dayLightDurationValueHour = (TextView) getView().findViewById(R.id.day_light_alert_exposure_duration_hour);
		dayLightDurationValueMin = (TextView) getView().findViewById(R.id.day_light_alert_exposure_duration_minute);
		dayLightStartTimeValue = (TextView) getView().findViewById(R.id.daylight_alert_start_time);
		dayLightEndTimeValue = (TextView) getView().findViewById(R.id.daylight_alert_end_time);
		alertFrequencyValue = (EditText) getView().findViewById(R.id.day_light_alert_frequency);
		alertFrequencySeekerBar = (SeekBar) getView().findViewById(R.id.fragment_day_light_seekerbar);

		intializeData();

		switchStatus.setOnCheckedChangeListener(this);
		dayLightExposureLevel.setOnClickListener(mClickListener);
		dayLightStartTimeValue.setOnClickListener(mClickListener);
		dayLightEndTimeValue.setOnClickListener(mClickListener);
		alertFrequencySeekerBar.setOnSeekBarChangeListener(this);

		LifeTrakLogger.configure();


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
						if (switchStatus.isChecked()){
							isdateshow = true;
							compareTime();
						}
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

		dayLightDuration = getView().findViewById(R.id.day_light_alert_exposure_duration);
		dayLightDuration.setOnClickListener(mClickListener);

		alertFrequencyValue.setOnEditorActionListener(
				new EditText.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if ( actionId == EditorInfo.IME_ACTION_DONE ||
								event.getAction() == KeyEvent.ACTION_DOWN ) {
							try {

								int frenqVal = Integer.parseInt(alertFrequencyValue.getText().toString());
								if (frenqVal >120){
									alertFrequencyValue.setText("120");
									alertFrequencySeekerBar.setProgress(120);
								}
								else if (frenqVal <= 5){
									alertFrequencyValue.setText("5");
									alertFrequencySeekerBar.setProgress(0);
								}
								else
									alertFrequencySeekerBar.setProgress(frenqVal);
								InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(alertFrequencyValue.getWindowToken(), 0); 
							} catch (NumberFormatException ex) {
								alertFrequencyValue.setText("5");
								alertFrequencySeekerBar.setProgress(0);
							}
							return true;
						}
						return false;
					}
				});


		alertFrequencyValue.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {

			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){
				try {
					int frenqVal = Integer.parseInt(alertFrequencyValue.getText().toString());
					if (frenqVal >120){
						alertFrequencyValue.setText("120");
						alertFrequencySeekerBar.setProgress(120);
					}
				} catch (NumberFormatException ex) {

					alertFrequencyValue.setText("5");
					alertFrequencySeekerBar.setProgress(0);
				}
			}
		}); 
		showHideViews();
	}

	// Switching status
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()){
		case R.id.day_light_alert_switch_status:
			//Toast.makeText(getActivity(), "The Switch is " + (isChecked ? "on" : "off"),Toast.LENGTH_SHORT).show();
			showHideViews();
			isdateshow = true;
			flag_ischange = true;
			break;
		}
	}



	@SuppressLint("SimpleDateFormat")
	public String changeTimeFormat(String time)
	{
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

	private void showHideViews() {
		if(switchStatus.isChecked() == true){
			dayLightExposureLevel.setVisibility(View.VISIBLE);
			dayLightDurationValueHour.setVisibility(View.VISIBLE);
			dayLightDurationValueMin.setVisibility(View.VISIBLE);
			dayLightStartTimeValue.setVisibility(View.VISIBLE);
			dayLightEndTimeValue.setVisibility(View.VISIBLE);
			alertFrequencySeekerBar.setVisibility(View.VISIBLE);
			alertFrequencyValue.setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_h)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_m)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_exposure_level_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_exposure_duration_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_start_time_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_end_time_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_h)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency_min)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency_max)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.fragment_day_light_alert_frequency_hint)).setVisibility(View.VISIBLE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency)).setVisibility(View.VISIBLE);

			((View) getView().findViewById(R.id.day_light_alert_line1)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.day_light_alert_line2)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.day_light_alert_line3)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.day_light_alert_line4)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.day_light_alert_line5)).setVisibility(View.VISIBLE);
			((View) getView().findViewById(R.id.day_light_alert_line6)).setVisibility(View.VISIBLE);


			((TextView) getView().findViewById(R.id.textviewMinutesCaption)).setVisibility(View.VISIBLE);
		} else {
			dayLightExposureLevel.setVisibility(View.GONE);
			dayLightDurationValueHour.setVisibility(View.GONE);
			dayLightDurationValueMin.setVisibility(View.GONE);
			dayLightStartTimeValue.setVisibility(View.GONE);
			dayLightEndTimeValue.setVisibility(View.GONE);
			alertFrequencySeekerBar.setVisibility(View.GONE);
			alertFrequencyValue.setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_h)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_m)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_exposure_level_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_exposure_duration_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_start_time_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_end_time_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_h)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency_min)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency_max)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.fragment_day_light_alert_frequency_hint)).setVisibility(View.GONE);
			((TextView) getView().findViewById(R.id.day_light_alert_frequency)).setVisibility(View.GONE);

			((View) getView().findViewById(R.id.day_light_alert_line1)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.day_light_alert_line2)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.day_light_alert_line3)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.day_light_alert_line4)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.day_light_alert_line5)).setVisibility(View.GONE);
			((View) getView().findViewById(R.id.day_light_alert_line6)).setVisibility(View.GONE);


			((TextView) getView().findViewById(R.id.textviewMinutesCaption)).setVisibility(View.GONE);
		}
	}

	// TimePicker
	private final TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String AM_PM ;
			if(hourOfDay < 12) {
				AM_PM = getString(R.string.am);

			} else {
				AM_PM = getString(R.string.pm);
				hourOfDay=hourOfDay-12;
			}

			if (hourOfDay == 0)
				hourOfDay = 12;


			if (isStartTime) {
				dayLightStartTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
				isStartTime = false;
				//compareTime();
			}
			else if (isEndTime) {
				dayLightEndTimeValue.setText(changeTimeFormat(hourOfDay + ":" + String.format("%02d", minute) + " " + AM_PM));
				isEndTime = false;
				//compareTime();
			}
		}
	};

	// OnClick Listener
	private final View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean is24hours = false;
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				is24hours = false;
				break;
			case TIME_FORMAT_24_HR:
				is24hours = true;
				break;					
			}
			switch (v.getId()) {

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
				dayLightNumberPickerHour.setMaxValue(2);       
				dayLightNumberPickerHour.setMinValue(0);         
				dayLightNumberPickerHour.setValue(Integer.parseInt(exposureDurationHourToDisplay));
				dayLightNumberPickerHour.setWrapSelectorWheel(true);
				dayLightNumberPickerHour.setOnValueChangedListener( new NumberPicker.
						OnValueChangeListener() {
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						int minVal = Integer.valueOf(exposureDurationMinToDisplay);
						int hourVal = newVal;
						LifeTrakLogger.info("Selected number is "+ exposureDurationHourToDisplay);
						if (minVal < 10 && hourVal == 0) {
							minVal = 10;
						} else if (hourVal == 2 && minVal > 0) {
							minVal = 0;
							//showAlert(getResources().getString(R.string.invalid_duration_time));
						}

						exposureDurationHourToDisplay = Integer.toString(hourVal);
						dayLightNumberPickerHour.setValue(hourVal);
						exposureDurationMinToDisplay = Integer.toString(minVal);
						dayLightNumberPickerMin.setValue(minVal);
						//dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
						//dayLightDurationValueMin.setText(exposureDurationMinToDisplay);
						LifeTrakLogger.info("Selected number is "+ exposureDurationHourToDisplay);
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
						LifeTrakLogger.info("Selected number is "+ exposureDurationHourToDisplay);
						if (minVal < 10 && hourVal == 0) {
							minVal = 10;
						} else if (hourVal == 2 && minVal > 0) {
							minVal = 0;
							//showAlert(getResources().getString(R.string.invalid_duration_time));
						}

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
			case R.id.daylight_alert_start_time:
				isStartTime = true;
				isdateshow = true;
				String timeStart  = dayLightStartTimeValue.getText().toString().trim(); 
				String[] temp = timeStart.split(" ");
				String[] timeSplitData = temp[0].split(":");
				int hour = Integer.parseInt(timeSplitData[0]);
				int mins = Integer.parseInt(timeSplitData[1]);
				if (!is24hours) {
					if (temp[1].equals(getString(R.string.pm))) {
						hour += 12;
					}
				}
				mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, hour, mins, is24hours);
				mTimePicker.show();
				break;
			case R.id.daylight_alert_end_time:
				isEndTime = true;
				isdateshow = true;
				String timeEnd  = dayLightEndTimeValue.getText().toString().trim(); 
				String[] tempEnd = timeEnd.split(" ");
				String[] timeSplitDataEnd = tempEnd[0].split(":");
				int hourEnd = Integer.parseInt(timeSplitDataEnd[0]);
				int minsEnd = Integer.parseInt(timeSplitDataEnd[1]);
				if (!is24hours) {
					if (tempEnd[1].equals(getString(R.string.pm))) {
						hourEnd += 12;
					}
				}
				mTimePicker = new TimePickerDialog(getActivity(), mOnTimeSetListener, hourEnd, minsEnd, is24hours);
				mTimePicker.show();
				break;
			case R.id.daylight_alert_exposure_level:
				showListExposureDialog();
			}

		}
	}; 

	// OnClick Listener for Custom Alert Dialog
	private final View.OnClickListener alertListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.number_picker_custom_dialog_set:
				if(exposureDurationHourToDisplay == "")
					exposureDurationHourToDisplay = "0";
				if(exposureDurationMinToDisplay == "")
					exposureDurationMinToDisplay = "00";
				dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
				dayLightDurationValueMin.setText(exposureDurationMinToDisplay);
				alertDialog.dismiss();
				break;
			case R.id.number_picker_custom_dialog_cancel:
				alertDialog.dismiss();
				break;
			}
		}
	};

	protected void compareTime() {
		noError = false;
		if (isdateshow) {
			isdateshow = false;
			flag_ischange = true;
			Date fDate = null;
			Date lDate = null;
			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				fDate = returnDate(dayLightStartTimeValue.getText().toString());
				lDate = returnDate(dayLightEndTimeValue.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				fDate = returnDate24Hours(dayLightStartTimeValue.getText().toString());
				lDate = returnDate24Hours(dayLightEndTimeValue.getText().toString());
				break;
			}


			boolean isthirtymin = false;
			int mhours, mminutes;

			Calendar fcalendar = Calendar.getInstance();
			fcalendar.setTime(fDate);
			int fhours = fcalendar.get(Calendar.HOUR_OF_DAY);
			int fminutes = fcalendar.get(Calendar.MINUTE);

			Calendar ecalendar = Calendar.getInstance();
			ecalendar.setTime(lDate);
			int ehours = ecalendar.get(Calendar.HOUR_OF_DAY);
			int ehoursfinal = ecalendar.get(Calendar.HOUR_OF_DAY);
			int eminutes = ecalendar.get(Calendar.MINUTE);
			int eminutesfinal = ecalendar.get(Calendar.MINUTE);

			mminutes = fminutes + 29;
			mhours = fhours;
			if (mminutes > 59){
				mhours = fhours + 1;
				mminutes = fminutes - 59;
			}
			if ((mminutes + (mhours*60)) >= ((ehours*60) + eminutesfinal))
				isthirtymin = true;

			//Set the Value of Exposure
			eminutes = eminutes - fminutes;
			if (eminutes < 0){
				eminutes = eminutes + 60;
				ehours = ehours -1;
			}
			//dayLightDurationValueHour.setText("" + (ehours - fhours));
			//dayLightDurationValueMin.setText("" + eminutes);

			//Checking Exposure Value
			if (fDate.getTime() > lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error1));
				setInitialDate(fhours, fminutes);
				isdateshow = true;
			}
			else if (fDate.getTime() == lDate.getTime()){
				showAlert(getString(R.string.daylight_exposure_error2));
				setInitialDate(fhours, fminutes);
				isdateshow = true;
			}
			else if (isthirtymin){
				showAlert(getString(R.string.daylight_exposure_error3));
				setInitialDate(eminutesfinal, ehoursfinal, fhours, fminutes);
				isdateshow = true;
			}
			else if ((Integer.parseInt(dayLightDurationValueHour.getText().toString()) * 60 + Integer.parseInt(dayLightDurationValueMin.getText().toString())) > ((ehoursfinal*60 + eminutesfinal) - (fhours*60 + fminutes))){
				showAlert(getString(R.string.daylight_exposure_error4));
				exposureDurationHourToDisplay = "" + (ehours - fhours);
				exposureDurationMinToDisplay = "" + eminutes;
				dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
				dayLightDurationValueMin.setText(exposureDurationMinToDisplay);
				isdateshow = true;
			}
			else if (((ehoursfinal*60 + eminutesfinal) - (fhours*60 + fminutes)) < Integer.parseInt(alertFrequencyValue.getText().toString())){//((ehours*60 + eminutes) - (fhours*60 + fminutes)) < Integer.parseInt(alertFrequencyValue.getText().toString())
				showAlert(getString(R.string.daylight_exposure_error5));
				alertFrequencyValue.setText(""+ ((ehours - fhours)*60 + eminutesfinal));
				alertFrequencySeekerBar.setOnSeekBarChangeListener(null);
				alertFrequencySeekerBar.setProgress((ehours - fhours)*60 + eminutesfinal);
				alertFrequencySeekerBar.setOnSeekBarChangeListener(this);
				isdateshow = true;
			}
			else //valid time input
			{
                nightLightDetectSetting = DataSource.getInstance(getActivity())
                        .getReadOperation()
                        .query("watchNightlightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
                        .getResults(NightLightDetectSetting.class);

                if (nightLightDetectSetting.size() > 0) {
                    LifeTrakLogger.debug("Has a data");
                    mNightLightDetectSetting = nightLightDetectSetting.get(0);

                    if (mNightLightDetectSetting.isEnabled()) {

                        Calendar calendarDLightEndTime = Calendar.getInstance();
                        calendarDLightEndTime.setTime(lDate);
                        Calendar calendarDLightStartTime = Calendar.getInstance();
                        calendarDLightStartTime.setTime(fDate);
						String nightLightEndtime = convertMinuteToTime(mNightLightDetectSetting.getEndTime());
						String nightLightStarttime = convertMinuteToTime(mNightLightDetectSetting.getStartTime());
						SimpleDateFormat input = new SimpleDateFormat("hh:mm aa");
						SimpleDateFormat output = new SimpleDateFormat("HH:mm:ss");
						Date dt = null;


						try {
							dt = input.parse(nightLightEndtime);
						} catch (ParseException e) {
							e.printStackTrace();
						}


						String formattedNightLightEndtime = output.format(dt);
						try {
							dt = input.parse(nightLightStarttime);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String formattedNightLightStartTime = output.format(dt);

						String formattedDaylightStartTime = null;
						String formattedDaylightEndTime = null;

						switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
							case TIME_FORMAT_12_HR:
								String dayLightStartTime = dayLightStartTimeValue.getText().toString();
								String dayLightEndTime = dayLightEndTimeValue.getText().toString();

								try {
									dt = input.parse(dayLightStartTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}
								formattedDaylightStartTime = output.format(dt);

								try {
									dt = input.parse(dayLightEndTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}
								formattedDaylightEndTime = output.format(dt);

								break;
							case TIME_FORMAT_24_HR:
								formattedDaylightStartTime = dayLightStartTimeValue.getText().toString();
								formattedDaylightEndTime = dayLightEndTimeValue.getText().toString();
								break;
						}

						boolean daylightStartTime = false;
						boolean daylightEndTime = false;
						try {
							daylightStartTime = isTimeBetweenTwoTime(formattedNightLightStartTime, formattedNightLightEndtime,formattedDaylightStartTime );
							daylightEndTime = isTimeBetweenTwoTime(formattedNightLightStartTime, formattedNightLightEndtime,formattedDaylightEndTime );
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e){

						}

						if (daylightEndTime == true || daylightStartTime == true){
                                    isdateshow = true;
                                    noError = false;
                                    showAlert(getString(R.string.daylight_exposure_error7));
                                }
                                else{
                                    noError = true;
                                }

//                        SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
//
//                        Calendar calendarNLightEndTime = Calendar.getInstance();
//                        try {
//                            calendarNLightEndTime.setTime(format.parse(convertMinuteToTime(mNightLightDetectSetting.getEndTime())));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        Calendar calendarNLightStartTime = Calendar.getInstance();
//                        try {
//                            calendarNLightStartTime.setTime(format.parse(convertMinuteToTime(mNightLightDetectSetting.getStartTime())));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//						Date startTime =calendarNLightStartTime.getTime();
//						Date endTime =calendarNLightEndTime.getTime();
//
//						Date x = null;
//						x = calendarDLightStartTime.getTime();
//						boolean isNightStartTimeInDayLight = x.after(endTime) && x.before(startTime);
//						x = calendarDLightEndTime.getTime();
//						boolean isNightEndTimeInDayLight =  x.after(endTime) && x.before(startTime);
//                        //boolean isNightStartTimeInDayLight =startTime.after(calendarDLightEndTime.getTime()) && startTime.after(calendarDLightStartTime.getTime());
//                        //boolean isNightEndTimeInDayLight = endTime.after(calendarDLightEndTime.getTime())&& endTime.after(calendarDLightStartTime.getTime());
//                        if (isNightStartTimeInDayLight == false || isNightEndTimeInDayLight == false){
//                                    isdateshow = true;
//                                    noError = false;
//                                    showAlert(getString(R.string.daylight_exposure_error7));
//                                }
//                                else{
//                                    noError = true;
//                                }


                    }else{
                        noError = true;
                    }
                }
                else {
                    noError = true;
                }

			}

		}
	}


	private boolean isTimeBetweenTwoTime(String argStartTime,String argEndTime, String argCurrentTime) throws ParseException {
		String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
		//
		if (argStartTime.matches(reg) && argEndTime.matches(reg)
				&& argCurrentTime.matches(reg)) {
			boolean valid = false;
			// Start Time
			java.util.Date startTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argStartTime);
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.setTime(startTime);

			// Current Time
			java.util.Date currentTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argCurrentTime);
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(currentTime);

			// End Time
			java.util.Date endTime = new SimpleDateFormat("HH:mm:ss")
					.parse(argEndTime);
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(endTime);

			//
			if (currentTime.compareTo(endTime) < 0) {

				currentCalendar.add(Calendar.DATE, 1);
				currentTime = currentCalendar.getTime();

			}

			if (startTime.compareTo(endTime) < 0) {

				startCalendar.add(Calendar.DATE, 1);
				startTime = startCalendar.getTime();

			}
			//
			if (currentTime.before(startTime)) {

				System.out.println(" Time is Lesser ");

				valid = false;
			} else {

				if (currentTime.after(endTime)) {
					endCalendar.add(Calendar.DATE, 1);
					endTime = endCalendar.getTime();

				}

				System.out.println("Comparing , Start Time /n " + startTime);
				System.out.println("Comparing , End Time /n " + endTime);
				System.out
						.println("Comparing , Current Time /n " + currentTime);

				if (currentTime.before(endTime)) {
					System.out.println("RESULT, Time lies b/w");
					valid = true;
				} else {
					valid = false;
					System.out.println("RESULT, Time does not lies b/w");
				}

			}
			return valid;

		} else {
			throw new IllegalArgumentException("Not a valid time, expecting HH:MM:SS format");
		}

	}

	private void setInitialDate(int eminutes, int ehours, int fhours,int fminutes){
		flag_ischange = true;
		eminutes = fminutes + 30;
		if (eminutes >= 60){
			eminutes = eminutes - 60;
			ehours = ehours+1;
		}
		//dayLightDurationValueHour.setText(changeTimeFormat("" + (ehours - fhours)));
		//dayLightDurationValueMin.setText(changeTimeFormat("" + (eminutes - fminutes)));

		String AM_PM ;
		if(ehours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			ehours=ehours-12;
		}

		if (ehours == 0)
			ehours = 12;

		dayLightEndTimeValue.setText(ehours + ":" + String.format("%02d", eminutes) + " " + AM_PM);
	}

	private void setInitialDate(int fhours,int fminutes){
		flag_ischange = true;
		fminutes = fminutes + 30;
		if (fminutes >= 60){
			fminutes = fminutes - 60;
			fhours = fhours+1;
		}
		exposureDurationHourToDisplay = "0";
		exposureDurationMinToDisplay = "30";
		dayLightDurationValueHour.setText(exposureDurationHourToDisplay);
		dayLightDurationValueMin.setText(exposureDurationMinToDisplay);

		String AM_PM ;
		if(fhours < 12) {
			AM_PM = getString(R.string.am);

		} else {
			AM_PM = getString(R.string.pm);
			fhours=fhours-12;
		}

		if (fhours == 0)
			fhours = 12;

		dayLightEndTimeValue.setText(changeTimeFormat(fhours + ":" + String.format("%02d", fminutes) + " " + AM_PM));
	}

	@SuppressLint("SimpleDateFormat")
	private Date returnDate(String time){
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");	
		try {
			return dateFormat.parse(time);
		} catch (ParseException e) {
			return new Date();
		}
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

	// Seeker bar
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		flag_ischange = true;
		isdateshow = true;
		minFrequencyValue = Integer.parseInt(getResources().getString(R.string.day_night_min_alert_frequency));
		if(progress < minFrequencyValue) {
			progress = minFrequencyValue;
		}

		alertFrequencyValue.setText(""+progress);
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {	
	}

	private void intializeData(){
		dayLightDetecttSettings = DataSource.getInstance(getActivity())
				.getReadOperation()
				.query("watchDaylightSetting = ?", String.valueOf(getLifeTrakApplication().getSelectedWatch().getId()))
				.getResults(DayLightDetectSetting.class);

		if (dayLightDetecttSettings.size() > 0) {
			mDayLightDetectSetting = dayLightDetecttSettings.get(0);
			mDayLightDetectSetting.update();

			int exposure = mDayLightDetectSetting.getExposureLevel();
			String exposureLvl;
			if (exposure == SALDayLightDetectSetting.EXPOSURE_LOW){
				exposureLvl = getString(R.string.low);
			} else if (exposure == SALDayLightDetectSetting.EXPOSURE_MEDIUM) {
				exposureLvl = getString(R.string.medium);
			} else {
				exposureLvl = getString(R.string.high);
			}
			dayLightExposureLevel.setText(exposureLvl);

			int startMinutes = mDayLightDetectSetting.getStartTime();
			int endMinutes = mDayLightDetectSetting.getEndTime();
			if ((startMinutes > 1440 || startMinutes < 1) || (endMinutes >1440 || endMinutes < 1)){
				startMinutes = 540;
				endMinutes = 600;
			}

			dayLightStartTimeValue.setText(changeTimeFormat(convertMinuteToTime(startMinutes)));
			dayLightEndTimeValue.setText(changeTimeFormat(convertMinuteToTime(endMinutes)));
			switchStatus.setChecked(mDayLightDetectSetting.isEnabled());
			if (mDayLightDetectSetting.getInterval()<5){
				mDayLightDetectSetting.setInterval(5);
			}
			if(mDayLightDetectSetting.getInterval()<=5){
				alertFrequencySeekerBar.setProgress(0);
			}
			alertFrequencySeekerBar.setMax(120);
			alertFrequencyValue.setText("" + mDayLightDetectSetting.getInterval());
			alertFrequencySeekerBar.setProgress(mDayLightDetectSetting.getInterval());

			setExposureDuration(mDayLightDetectSetting.getExposureDuration());

		} else 
			initialData();	
	}

	/*
	 * Set daylight exposure duration based on the current goal
	 */
	private void setExposureDuration(int expusure) {
		int hours = expusure / 60;
		int minutes = expusure % 60;

		dayLightDurationValueMin.setText(String.format("%02d", minutes));
		dayLightDurationValueHour.setText(""+ hours);
		exposureDurationHourToDisplay = ""+ hours;
		exposureDurationMinToDisplay = String.format("%02d", minutes);

	}

	private void initialData() {
		LifeTrakLogger.debug("Initial Data");
		dayLightDurationValueMin.setText(""+ 00);
		dayLightDurationValueHour.setText(""+ 5);
		dayLightStartTimeValue.setText(changeTimeFormat("" + 7 + ":" + String.format("%02d", 00) + " AM"));
		dayLightEndTimeValue.setText(changeTimeFormat("" + 12 + ":" + String.format("%02d", 00) + " PM"));
		alertFrequencyValue.setText("60");
		dayLightExposureLevel.setText(getString(R.string.medium));
		alertFrequencySeekerBar.setProgress(60);

		mDayLightDetectSetting = new DayLightDetectSetting();
		Date fDate = null;
		Date lDate = null;

		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			fDate = returnDate(dayLightStartTimeValue.getText().toString());
			lDate = returnDate(dayLightEndTimeValue.getText().toString());
			break;
		case TIME_FORMAT_24_HR:
			fDate = returnDate24Hours(dayLightStartTimeValue.getText().toString());
			lDate = returnDate24Hours(dayLightEndTimeValue.getText().toString());
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

		int startminute = (fhours*60) + fminutes;
		int endminute = (ehours*60) + eminutes;

		int exposureLevel = 0;
		if (dayLightExposureLevel.getText().toString().equals(getString(R.string.low)))
			exposureLevel = SALDayLightDetectSetting.EXPOSURE_LOW;
		else if (dayLightExposureLevel.getText().toString().equals(getString(R.string.medium)))
			exposureLevel = SALDayLightDetectSetting.EXPOSURE_MEDIUM;
		else
			exposureLevel = SALDayLightDetectSetting.EXPOSURE_HIGH;

		mDayLightDetectSetting.setEnabled(switchStatus.isChecked());
		
		mDayLightDetectSetting.setStartTime(startminute);
		mDayLightDetectSetting.setEndTime(endminute);
		mDayLightDetectSetting.setExposureDuration((Integer.parseInt(dayLightDurationValueHour.getText().toString()) *60) + Integer.parseInt(dayLightDurationValueMin.getText().toString()));
		mDayLightDetectSetting.setInterval(Integer.parseInt(alertFrequencyValue.getText()
				.toString()
				.replace(getString(R.string.minutes), "")
				.trim()));
		mDayLightDetectSetting.setExposureLevel(exposureLevel);
		saveDayLightToGoals();


			mDayLightDetectSetting.insert();
		

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

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (flag_ischange)
			syncData();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(isRemoving()) {
			if(flag_ischange)
				syncData();
			//noticeBeforeExit();
		}
	}

	private void showListExposureDialog(){
		final CharSequence[] items = {
				getString(R.string.low), 
				getString(R.string.medium), 
				getString(R.string.high)
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.daylight_exposure_select));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				flag_ischange = true;
				dayLightExposureLevel.setText(items[item]);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void syncData() {
		if (flag_ischange) {
			LifeTrakLogger.debug("sync Data");
			Date fDate = null;
			Date lDate = null;

			switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
			case TIME_FORMAT_12_HR:
				fDate = returnDate(dayLightStartTimeValue.getText().toString());
				lDate = returnDate(dayLightEndTimeValue.getText().toString());
				break;
			case TIME_FORMAT_24_HR:
				fDate = returnDate24Hours(dayLightStartTimeValue.getText().toString());
				lDate = returnDate24Hours(dayLightEndTimeValue.getText().toString());
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

			int startminute = (fhours*60) + fminutes;
			int endminute = (ehours*60) + eminutes;

			LifeTrakLogger.info("sync data switch = " + switchStatus.isChecked());
			LifeTrakLogger.info("sync data setExposureDuration = " + (Integer.parseInt(dayLightDurationValueHour.getText().toString()) *60) + Integer.parseInt(dayLightDurationValueMin.getText().toString()));
			LifeTrakLogger.info("sync data fminutes = " + fminutes);
			LifeTrakLogger.info("sync data eminutes = " + eminutes);
			LifeTrakLogger.info("sync data alertFrequencyValue = " + Integer.parseInt(alertFrequencyValue.getText()
					.toString()
					.replace(getString(R.string.minutes), "")
					.trim()));

			int exposureLevel = 0;
			if (dayLightExposureLevel.getText().toString().equals(getString(R.string.low)))
				exposureLevel = SALDayLightDetectSetting.EXPOSURE_LOW;
			else if (dayLightExposureLevel.getText().toString().equals(getString(R.string.medium)))
				exposureLevel = SALDayLightDetectSetting.EXPOSURE_MEDIUM;
			else
				exposureLevel = SALDayLightDetectSetting.EXPOSURE_HIGH;

			mDayLightDetectSetting.setEnabled(switchStatus.isChecked());
			// if switch is off All data will not be saved
			if (switchStatus.isChecked()){
				mDayLightDetectSetting.setStartTime(startminute);
				mDayLightDetectSetting.setEndTime(endminute);
				mDayLightDetectSetting.setExposureDuration((Integer.parseInt(dayLightDurationValueHour.getText().toString()) *60) + Integer.parseInt(dayLightDurationValueMin.getText().toString()));
				mDayLightDetectSetting.setInterval(Integer.parseInt(alertFrequencyValue.getText()
						.toString()
						.replace(getString(R.string.minutes), "")
						.trim()));
				mDayLightDetectSetting.setExposureLevel(exposureLevel);
				saveDayLightToGoals();
			}

			if (dayLightDetecttSettings.size() > 0) {
				mDayLightDetectSetting.update();
			} else {
				mDayLightDetectSetting.insert();
			}


		}
	}

	private void saveDayLightToGoals() {
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date());

		int day = calendar2.get(Calendar.DAY_OF_MONTH);
		int month = calendar2.get(Calendar.MONTH) + 1;
		int year = calendar2.get(Calendar.YEAR) - 1900;

		List<Goal> goals = DataSource
				.getInstance(getActivity())
				.getReadOperation()
				.query("watchGoal = ? and dateStampDay = ? and dateStampMonth = ? and dateStampYear = ?",
						String.valueOf(getLifeTrakApplication()
								.getSelectedWatch().getId()),
								String.valueOf(day), String.valueOf(month),
								String.valueOf(year)).orderBy("_id", SORT_DESC)
								.getResults(Goal.class);

		if (goals.size() > 0) {
			Goal currGoal = goals.get(0);
			int currGoalTime = currGoal.getBrightLightGoal();
			LifeTrakLogger.info("current goal: " + currGoalTime);
			int hours = Integer.parseInt(dayLightDurationValueHour.getText().toString()) * 60;
			int minutes = Integer.parseInt(dayLightDurationValueMin.getText().toString());
			int total = hours + minutes;

			currGoal.setBrightLightGoal(total);
			currGoal.update();


		}
	}


}