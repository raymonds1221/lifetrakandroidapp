package com.salutron.lifetrakwatchapp.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.salutron.lifetrak.R;

/**
 * View for displaying syncing status
 * 
 * @author rsarmiento
 *
 */
public class SyncStatusView extends RelativeLayout {
	private TextView mSyncStatusText;
	private ImageView mSyncStatusImage;
	private int[] mImageResourceIds = {
			R.drawable.ll_preloader_default,
			R.drawable.ll_preloader_arrow_01,
			R.drawable.ll_preloader_arrow_02,
			R.drawable.ll_preloader_arrow_03,
			R.drawable.ll_preloader_arrow_04,
			R.drawable.ll_preloader_arrow_05,
			R.drawable.ll_preloader_arrow_06,
			R.drawable.ll_preloader_arrow_07,
			R.drawable.ll_preloader_radar_01,
			R.drawable.ll_preloader_radar_02,
			R.drawable.ll_preloader_radar_03,
			R.drawable.ll_preloader_radar_04,
			R.drawable.ll_preloader_radar_05,
			R.drawable.ll_preloader_radar_06,
			R.drawable.ll_preloader_radar_07,
			R.drawable.ll_preloader_radar_08,
			R.drawable.ll_preloader_radar_09,
			R.drawable.ll_preloader_radar_10,
			R.drawable.ll_preloader_radar_11,
	};
	private int mImageResourceIndex;
	private Context mContext;

	public SyncStatusView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.view_sync_data, this, true);
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		initializeObjects();
	}
	
	private void initializeObjects() {
		mSyncStatusText = (TextView) findViewById(R.id.tvwSyncStatus);
		mSyncStatusImage = (ImageView) findViewById(R.id.imgSyncStatus);
	}
	
	/**
	 * Method to set the status text
	 * 
	 * @param arg0 The message to display in sync status view
	 */
	public void setStatusText(CharSequence arg0) {
		mSyncStatusText.setText(arg0);
	}
	
	/**
	 * Start the preloader animation
	 */
	public void startAnimating() {
		setVisibility(View.VISIBLE);
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {}

			@Override
			public void onAnimationRepeat(Animation arg0) {}

			@Override
			public void onAnimationEnd(Animation arg0) {
				animatePreloaderSprite(0);
			}
		});
		startAnimation(animation);
	}
	
	/**
	 * Stop the preloader animation
	 */
	public void stopAnimating() {

		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_animation);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) { }
			
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			
			@Override
			public void onAnimationEnd(Animation arg0) {

				setVisibility(View.GONE);
			}
		});
		startAnimation(animation);
	}
	
	private void animatePreloaderSprite(int index) {

		setScaledImage(mSyncStatusImage, mImageResourceIds[index]);
		//mSyncStatusImage.setImageResource();
		mHandler.sendMessageDelayed(Message.obtain(), 100);
	}
	
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message messag) {
			mImageResourceIndex++;
			
			if(mImageResourceIndex == mImageResourceIds.length) {
				mImageResourceIndex = 0;
			}

			animatePreloaderSprite(mImageResourceIndex);
		}
	};
	
	/**
	 * Display success page
	 */
	public void showSuccess() {
		mHandler.removeMessages(0);
		mSyncStatusImage.setImageResource(R.drawable.ll_preloader_sync_success);
		setStatusText(mContext.getString(R.string.sync_success));

	}
	
	public void showFail() {
        mHandler.removeMessages(0);
		mSyncStatusImage.setImageResource(R.drawable.asset_connect_4failed);
        setStatusText(mContext.getString(R.string.sync_failed));

        mHandler.postDelayed(new Runnable() {
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) { }

                    @Override
                    public void onAnimationRepeat(Animation arg0) { }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        setVisibility(View.GONE);
                    }
                });
                startAnimation(animation);
            }
        }, 3000);
	}
	
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
		startAnimation(animation);
	}

	private void setScaledImage(ImageView imageView, final int resId) {
		final ImageView iv = imageView;
		ViewTreeObserver viewTreeObserver = iv.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				iv.getViewTreeObserver().removeOnPreDrawListener(this);
				int imageViewHeight = iv.getMeasuredHeight();
				int imageViewWidth = iv.getMeasuredWidth();
				iv.setImageBitmap(
						decodeSampledBitmapFromResource(getResources(),
								resId, imageViewWidth, imageViewHeight));
				return true;
			}
		});
	}

	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
														  int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
