package com.salutron.lifetrakwatchapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;

/**
 * View for displaying the search device dialog
 *  
 * @author rsarmiento
 *
 */
public class SearchDeviceView extends FrameLayout implements View.OnClickListener, SalutronLifeTrakUtility {
	private TextView mSearchDeviceText;
	private View mModalContainer;
	private SearchDeviceListener mListener;

	public SearchDeviceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_searching_for_device, this, true);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initializeObjects();
	}
	
	private void initializeObjects() {
		mSearchDeviceText = (TextView) findViewById(R.id.tvwSearchingDevice);
		mModalContainer = findViewById(R.id.lnrModalContainer);
		
		findViewById(R.id.imgClose).setOnClickListener(this);
	}
	
	/**
	 * Method to set the selected watch
	 * 
	 * @param watchModel the model number of the watch
	 */
	public void setWatchModel(int watchModel) {
		mSearchDeviceText.setText(getContext().getString(R.string.searching_device, 
												modelNameFromWatchModel(watchModel)));
	}
	
	/**
	 * Method to show the search device dialog
	 */
	public void show() {
		setVisibility(View.VISIBLE);
		mModalContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), 
														R.anim.scale_in_dialog));
	}
	
	/**
	 * Method to hide the search device dialog
	 */
	public void hide() {
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_out_dialog);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				setVisibility(View.GONE);
			}
		});
		mModalContainer.startAnimation(animation);
	}
	
	private String modelNameFromWatchModel(int model) {
		switch(model) {
		case WATCHMODEL_C300:
			return WATCHNAME_C300;
		case WATCHMODEL_C410:
			return WATCHNAME_C410;
		case WATCHMODEL_R415:
			return WATCHNAME_R415;
		case WATCHMODEL_R500:
			return WATCHNAME_R500;
		}
		return "LifeTrak Watch";
	}
	
	public void setSearchDeviceListener(SearchDeviceListener listener) {
		mListener = listener;
	}
	
	public static interface SearchDeviceListener {
		public void onCloseClick();
	}

	@Override
	public void onClick(View view) {
		if(mListener != null && view.getId() == R.id.imgClose) {
			mListener.onCloseClick();
		}
	}
}
