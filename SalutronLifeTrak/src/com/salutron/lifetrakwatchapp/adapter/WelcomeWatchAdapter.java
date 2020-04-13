package com.salutron.lifetrakwatchapp.adapter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.Watch;
import com.salutron.lifetrakwatchapp.util.Gallery;

public class WelcomeWatchAdapter extends BaseArrayAdapter<Watch> {
	private int mResourceId;
	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();
	private List<Watch> mWatches;
	private Gallery mGallery;

	public WelcomeWatchAdapter(Context context, int resource, List<Watch> objects) {
		super(context, resource, objects);
		
		mResourceId = resource;
		mWatches = objects;
		mDateFormat.applyPattern("MMMM dd, yyyy hh:mm aa");
		mGallery = new Gallery();
	}
	
	public void setItems(List<Watch> objects) {
		clear();
		addAll(objects);
		mWatches = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(mResourceId, null);
			holder = new ViewHolder(convertView);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		Watch watch = mWatches.get(position);
		holder.watch = watch;
		
		int mod = position % 2;
		
		if(mod == 0) {
			holder.watchImage.setBackgroundResource(R.drawable.ll_welcome02_bg_orange);
			int colorWelcomeItem1 = getContext().getResources().getColor(R.color.color_welcome_item_1);
			convertView.setBackgroundColor(colorWelcomeItem1);
			holder.watchName.setBackgroundColor(colorWelcomeItem1);
			holder.lastSyncDate.setBackgroundColor(colorWelcomeItem1);
			convertView.findViewById(R.id.lnrWatchInfoContainer).setBackgroundColor(colorWelcomeItem1);
		} else {
			holder.watchImage.setBackgroundResource(R.drawable.ll_welcome02_bg_blue);
			int colorWelcomeItem2 = getContext().getResources().getColor(R.color.color_welcome_item_2);
			convertView.setBackgroundColor(colorWelcomeItem2);
			holder.watchName.setBackgroundColor(colorWelcomeItem2);
			holder.lastSyncDate.setBackgroundColor(colorWelcomeItem2);
			convertView.findViewById(R.id.lnrWatchInfoContainer).setBackgroundColor(colorWelcomeItem2);
		}
		
		if(watch.getImage() != null) {
			File file = new File(watch.getImage());
			
			if(file.exists()) {
				try {
					mGallery.addItem(file.getAbsolutePath());
					holder.watchImage.setImageBitmap(mGallery.getItem(file.getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				switch(watch.getModel()) {
				case WATCHMODEL_C300:
					holder.watchImage.setImageResource(R.drawable.watch_c300_green);
					holder.watchName.setText(WATCHNAME_C300);
					break;
				case WATCHMODEL_C410:
					holder.watchImage.setImageResource(R.drawable.watch_c410_red);
					holder.watchName.setText(WATCHNAME_C410);
					break;
				case WATCHMODEL_R415:
					holder.watchImage.setImageResource(R.drawable.watch_r415_blue);
					holder.watchName.setText(WATCHNAME_R415);
					break;
				case WATCHMODEL_R420:
					holder.watchImage.setImageResource(R.drawable.r420);
					holder.watchName.setText(WATCHNAME_R420);
					break;
				case WATCHMODEL_R500:
					holder.watchImage.setImageResource(R.drawable.watch_r500_black);
					holder.watchName.setText(WATCHNAME_R500);
					break;
				}
			}
		} else {
			switch(watch.getModel()) {
			case WATCHMODEL_C300:
				holder.watchImage.setImageResource(R.drawable.watch_c300_green);
				holder.watchName.setText(WATCHNAME_C300);
				break;
			case WATCHMODEL_C410:
				holder.watchImage.setImageResource(R.drawable.watch_c410_red);
				holder.watchName.setText(WATCHNAME_C410);
				break;
			case WATCHMODEL_R415:
				holder.watchImage.setImageResource(R.drawable.watch_r415_blue);
				holder.watchName.setText(WATCHNAME_R415);
				break;
			case WATCHMODEL_R420:
					holder.watchImage.setImageResource(R.drawable.r420);
					holder.watchName.setText(WATCHNAME_R420);
				break;
			case WATCHMODEL_R500:
				holder.watchImage.setImageResource(R.drawable.watch_r500_black);
				holder.watchName.setText(WATCHNAME_R500);
				break;
			}
		}
		
		holder.watchName.setText(watch.getName());
		holder.lastSyncDate.setText(mDateFormat.format(watch.getLastSyncDate()));
		
		return convertView;
	}
	
	class ViewHolder implements OnClickListener{
		public ImageButton watchImage;
		public TextView watchName;
		public TextView lastSyncDate;
		public ImageButton delete;
		public Watch watch;
		
		public ViewHolder(View view) {
			watchImage = (ImageButton) view.findViewById(R.id.imgWatchImage);
			watchName = (TextView) view.findViewById(R.id.tvwWatchName);
			lastSyncDate = (TextView) view.findViewById(R.id.tvwLastSyncDate);
			delete = (ImageButton) view.findViewById(R.id.btnDelete);
			view.setOnTouchListener(touchListener);
			view.setOnClickListener(this);
			view.setTag(this);
			delete.setOnClickListener(this);
		}
		
		private OnSwipeTouchListener touchListener = new OnSwipeTouchListener(getContext()){
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
			
			@Override
			public void swipeRight() {
				//delete.setVisibility(View.GONE);
			}
			
			public void swipeLeft() {
				//delete.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void singleTapUp() {
				if(getContext() instanceof WelcomeWatchAdapterListener) {
					((WelcomeWatchAdapterListener) getContext()).onWatchSelected(watch);
				}
			}
		};

		@Override
		public void onClick(View view) {
			if(view.getId() == R.id.btnDelete) {
				if(getContext() instanceof WelcomeWatchAdapterListener) {
					((WelcomeWatchAdapterListener) getContext()).onDeleteWatch(watch);
				}
			}
		}
	}
	
	public static interface WelcomeWatchAdapterListener {
		public void onWatchSelected(Watch watch);
		public void onDeleteWatch(Watch watch);
	}
	
	private class OnSwipeTouchListener implements View.OnTouchListener {
		protected GestureDetector mGestureDetector;
		
		private final int SWIPE_THRESHOLD = 100;
		private final int SWIPE_VELOCITY_THRESHOLD = 100;
		
		public OnSwipeTouchListener(Context context) {
			mGestureDetector = new GestureDetector(context, new GestureListener());
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			return false;
		}
		
		private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
				float diffY = event2.getY() - event1.getY();
				float diffX = event2.getX() - event1.getX();
				
				if(Math.abs(diffX) > Math.abs(diffY)) {
					if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						if(diffX > 0) {
							swipeRight();
						} else {
							swipeLeft();
						}
					}
				}
				
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				singleTapUp();
				return false;
			}
		}
		
		protected void swipeRight() { }
		protected void swipeLeft() { }
		protected void singleTapUp() { }
	}
}
