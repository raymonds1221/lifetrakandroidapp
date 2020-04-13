package com.salutron.lifetrakwatchapp.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.salutron.lifetrak.R;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

/**
 * View for displaying error when connecting
 * 
 * @author rsarmiento
 *
 */
public class ConnectionFailedView extends FrameLayout implements View.OnClickListener {
	private ConnectionFailedListener mListener;
	@InjectView(R.id.lnrModalContainer) private View mModalContainer;
	
	private ProgressDialog mProgressDialog;

	public ConnectionFailedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		RoboGuice.getInjector(context).injectMembers(this);
		
		LayoutInflater.from(context).inflate(R.layout.view_device_not_found, this, true);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initializeObjects();
	}
	
	private void initializeObjects() {
		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnTryAgain).setOnClickListener(this);
	}
	
	public void setConnectionFailedListener(ConnectionFailedListener listener) {
		mListener = listener;
	}
	
	public static interface ConnectionFailedListener {
		public void onCancelClick();
		public void onTryAgainClick();
	}

	@Override
	public void onClick(View view) {
		if(mListener != null) {
			switch(view.getId()) {
			case R.id.btnCancel:
				mListener.onCancelClick();
				break;
			case R.id.btnTryAgain:
				mListener.onTryAgainClick();
				break;
			}
		}
	}

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }
	
	/**
	 * Method to show the error message dialog
	 */
	public void show() {
		setVisibility(View.VISIBLE);
		mModalContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_dialog));
	}
	
	/**
	 * Method to hide the error message dialog
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
}
