package com.salutron.lifetrakwatchapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class GraphScrollView extends HorizontalScrollView {
	public GraphScrollViewListener mListener;

	public GraphScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if(mListener != null)
			mListener.onScrollChanged(l, t, oldl, oldt);
	}
	
	public void setGraphScrollViewListener(GraphScrollViewListener listener) {
		mListener = listener;
	}
	
	public static interface GraphScrollViewListener {
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}