package com.salutron.lifetrakwatchapp.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.Watch;

public class WatchAdapter extends BaseArrayAdapter<Watch> {
	private List<Watch> mWatches;
	private int mResourceId;
	
	public WatchAdapter(Context context, int resource, List<Watch> watches) {
		super(context, resource, watches);
		mWatches = watches;
		mResourceId = resource;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder viewHolder = null;
		Watch watch = mWatches.get(position);
		
		if(view == null) {
			view = mInflater.inflate(mResourceId, null);
			
			viewHolder = new ViewHolder();
			viewHolder.watchImage = (ImageView) view.findViewById(R.id.imgWatchImage);
			viewHolder.watchName = (TextView) view.findViewById(R.id.tvwWatchName);
			viewHolder.connectToDevice = (Button) view.findViewById(R.id.btnConnectToDevice);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		switch(watch.getModel()) {
			case WATCHMODEL_C300:
				viewHolder.watchImage.setImageResource(R.drawable.watch_c300_green);
				break;
			case WATCHMODEL_C410:
				viewHolder.watchImage.setImageResource(R.drawable.watch_c410_red);
				break;
			case WATCHMODEL_R420:
				viewHolder.watchImage.setImageResource(R.drawable.r420);
				break;
			case WATCHMODEL_R415:
				viewHolder.watchImage.setImageResource(R.drawable.watch_r415_blue);
				break;
			case WATCHMODEL_R500:
				viewHolder.watchImage.setImageResource(R.drawable.watch_r500_black);
				break;

		}
		
		viewHolder.watchName.setText(watch.getName());
		viewHolder.connectToDevice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(getContext() instanceof WatchAdapterListener) {
					switch(position) {
					case 0:
						((WatchAdapterListener) getContext()).onConnectToDeviceClick(WATCHMODEL_C300);
						break;
					case 1:
						((WatchAdapterListener) getContext()).onConnectToDeviceClick(WATCHMODEL_C410);
						break;
					case 2:
						((WatchAdapterListener) getContext()).onConnectToDeviceClick(WATCHMODEL_R420);
						break;
					case 3:
						((WatchAdapterListener) getContext()).onConnectToDeviceClick(WATCHMODEL_R415);
						break;

					}
				}
			}
		});
		
		return view;
	}
	
	private class ViewHolder {
		public ImageView 	watchImage;
		public TextView 	watchName;
		public Button 		connectToDevice;
	}
	
	public static interface WatchAdapterListener {
		public void onConnectToDeviceClick(int watchModel);
	}
}
