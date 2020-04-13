package com.salutron.lifetrakwatchapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.SlidingDrawer;
import android.graphics.Rect;

@SuppressWarnings("deprecation")
public class LifeTrakSlidingDrawer extends SlidingDrawer {
	private ViewGroup mHandle;
	private final String TAG_CLICKED_INTERCEPTED = "child_tag";
	private final Rect mRect = new Rect();

	public LifeTrakSlidingDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		
		View handle = getHandle();
		
		if(handle instanceof ViewGroup) {
			mHandle = (ViewGroup) handle;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(mHandle != null) {
			int clickX = (int)(event.getX() - mHandle.getLeft());
			int clickY = (int)(event.getY() - mHandle.getTop());
			
			if(isAnyClickableChildHit(mHandle, clickX, clickY)) {
				return false;
			}
		}
		return super.onInterceptTouchEvent(event);
	}
	
	private boolean isAnyClickableChildHit(ViewGroup viewGroup, int clickX, int clickY) {
		for(int i=0;i<viewGroup.getChildCount();i++) {
			View childView = viewGroup.getChildAt(i);
			
			if(TAG_CLICKED_INTERCEPTED.equals(childView.getTag())) {
				childView.getHitRect(mRect);
				
				if(mRect.contains(clickX, clickY)) {
					return true;
				}
			}
			
			if(childView instanceof ViewGroup && isAnyClickableChildHit((ViewGroup)childView, clickX, clickY)) {
				return true;
			}
		}
		return false;
	}
}
