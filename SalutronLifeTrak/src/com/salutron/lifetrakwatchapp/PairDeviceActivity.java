package com.salutron.lifetrakwatchapp;

import java.util.Locale;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.flurry.android.FlurryAgent;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.util.BluetoothConnected;

/**
 * Activity for confirming and displaying information for syncing to the watch
 * 
 * @author rsarmiento
 *
 */
public class PairDeviceActivity extends BaseActivity {
	private ImageView mPairDeviceSprite;
	private int mSelectedWatchModel;
	private int mImageIndex;
    private TextView pairingTextView;
    private  TextView textviewContent;
	private static final long HANDLER_DELAY = 30L;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pair_device);
		
		initializeObjects();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(0);
	}
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	   
	}
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	 
	}
	@Override
	protected void onResume() {
		super.onResume();
		  FlurryAgent.logEvent("Pairing_Page");
	};
	
	/*
	 * Initialiaze views and objects
	 */
	private void initializeObjects() {
		getSupportActionBar().setTitle(R.string.pair_device_title);
		
		mSelectedWatchModel = getIntent().getExtras().getInt(SELECTED_WATCH_MODEL);
        mPairDeviceSprite = (ImageView) findViewById(R.id.imgPairDeviceSprite);
        pairingTextView = (TextView) findViewById(R.id.pairingTextView);
        textviewContent = (TextView) findViewById(R.id.textView_content);
		String watchName = String.valueOf(mSelectedWatchModel);
		if(mSelectedWatchModel == WATCHMODEL_C300){
			watchName = "Move C300 / C320";
		}
        else if(mSelectedWatchModel == WATCHMODEL_C300_IOS){
            watchName = "Move C300 / C320";
        }
		else if (mSelectedWatchModel == WATCHMODEL_C410){
			watchName = "Zone C410 / C410w";
		} else if(mSelectedWatchModel == WATCHMODEL_R415) {
            watchName = "Brite R450";
        }
		else if(mSelectedWatchModel == WATCHMODEL_R420) {
			watchName = "Zone R420";
		}

		pairingTextView.setText(getString(R.string.pairing) + " " + watchName);

        boolean isPaired = BluetoothConnected.getInstance(PairDeviceActivity.this).deviceIsPaired();
        if (mSelectedWatchModel == WATCHMODEL_R415) {
            if (isPaired) {
                ActionBar bar = getSupportActionBar();
                bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(83, 179, 196)));
                //bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(66, 132, 230)));
                bar.setTitle(getString(R.string.pairing_sync));
//            View viewActionBar = getLayoutInflater().inflate(R.layout.actionbar_titletext_layout, null);
//            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
//                    ActionBar.LayoutParams.WRAP_CONTENT,
//                    ActionBar.LayoutParams.MATCH_PARENT,
//                    Gravity.CENTER);
//            TextView textviewTitle = (TextView) viewActionBar.findViewById(R.id.actionbar_textview);
//            textviewTitle.setText(getString(R.string.pairing_sync));
//            bar.setCustomView(viewActionBar, params);

                bar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

                pairingTextView.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.textView2)).setText(getString(R.string.pairing_sync_text));
                ((TextView) findViewById(R.id.textView2)).setTypeface(null, Typeface.BOLD);
//            ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(mPairDeviceSprite.getLayoutParams());
//            marginParams.setMargins(0, 160, 0, 0);
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//            mPairDeviceSprite.setLayoutParams(layoutParams);
                textviewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                textviewContent.setGravity(Gravity.LEFT);
                textviewContent.setText(getString(R.string.pairing_sync_description));
//                textviewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
//                textviewContent.setText("Sorry, your watch data is not syncing" +
//                        " to your app.\n" +
//                        "- Make sure the Bluetooth is active on" +
//                        " your watch (see illustration above)\n" +
//                        "- Make sure the Bluetooth is active on" +
//                        " your smartphone / tablet.\n" +
//                        "- Check to make sure your smartphone /" +
//                        " tablet has at least 20% battery power.");
            }

        }
		
		if(mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C410 || mSelectedWatchModel == WATCHMODEL_C300_IOS) {
			mPairDeviceSprite.setImageResource(R.drawable.ll_alert_devsetup_iphone4_rectangle_frame00);
		} else {
			mPairDeviceSprite.setImageResource(R.drawable.ll_alert_devsetup_iphone4_circle_frame00);
		}
		mHandler.sendMessageDelayed(Message.obtain(), 10);
	}
	
	/**
	 * Method for getting the drawable with supplied resource name
	 * 
	 * @param name	The name of the resource
	 * @return		Returns (Drawable) object.
	 */
	private Drawable drawableFromResourceName(String name) {
		int resourceId = getResources().getIdentifier(name, "drawable", getPackageName());
		
		if(resourceId > 0) {
			return getResources().getDrawable(resourceId);
		}
		return null;
	}
	
	/*
	 * Handler for animating image sprites
	 */
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			
			if(mImageIndex == 30)
				mImageIndex = 0;
			
			animatePairDeviceSprite(mImageIndex);
			mImageIndex++;
		}
	};
	
	/*
	 * Method for animating image sprites
	 */
	private void animatePairDeviceSprite(int index) {
		String resourceName = "";
		
		if(mSelectedWatchModel == WATCHMODEL_C300 || mSelectedWatchModel == WATCHMODEL_C410 || mSelectedWatchModel == WATCHMODEL_C300_IOS) {
			resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_rectangle_frame%02d", index);
		} else {{
            boolean isPaired = BluetoothConnected.getInstance(PairDeviceActivity.this).deviceIsPaired();
            if (isPaired){
                resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_circle_frame_blue%02d", index);
            }else{
                resourceName = String.format(Locale.getDefault(), "ll_alert_devsetup_iphone4_circle_frame%02d", index);
            }
        }
		}
		
		Drawable drawableImage = drawableFromResourceName(resourceName);
		
		if(drawableImage != null) {
			mPairDeviceSprite.setImageDrawable(drawableImage);
			mHandler.sendMessageDelayed(Message.obtain(), 5);
		}
	}
	
	/*
	 * Event for cancel and continue buttons
	 */
	public void onButtonClick(View view) {
		switch(view.getId()) {
		case R.id.btnCancel:
			setResult(RESULT_CANCELED);
			break;
		case R.id.btnContinue:
			setResult(RESULT_OK);
			break;
		}
		finish();
	}
}
