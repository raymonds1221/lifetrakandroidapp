package com.salutron.lifetrakwatchapp.view;

import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.salutron.lifetrak.R;

public class DashboardLighExposureView extends View {
	private double mGoal;
	private double mValue;
	private int mColorLightExposure;
	private int mColorWhite;
	private int mColorLightGray;
	private Paint mPaint;
	private RectF mRectF;
	private Bitmap mFullwheelBitmap;

	public DashboardLighExposureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		
		mColorWhite = Color.rgb(255, 255, 255);
		mColorLightExposure = getResources().getColor(R.color.color_light_exposure);
		mFullwheelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lt_icon_lightgraph);
		mColorLightGray = Color.rgb(180, 184, 188);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		double value = 0;
		double percent = 0;
		
		if(mGoal > 0 && mValue > 0) {
			value = mValue / mGoal;
			percent = value * 100;
		}
		
		mPaint.setAntiAlias(true);
		
		if((int)percent >= 100) {
			canvas.drawBitmap(mFullwheelBitmap, 0, 0, mPaint);
		} else {
			mPaint.setColor(mColorLightGray);
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 3, mPaint);
			mPaint.setColor(mColorWhite);
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 10, mPaint);
			
			if((int)percent > 0) {
				mPaint.setColor(mColorLightExposure);
				canvas.drawArc(mRectF, 270, (float) value * 360.0f, true, mPaint);
			}
			
			mPaint.setColor(mColorWhite);
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 13, mPaint);
			
			if((int)percent <= 0) {
				mPaint.setColor(mColorLightGray);
			} else {
				mPaint.setColor(mColorLightExposure);
			}
			
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 25, mPaint);
			
			mPaint.setColor(mColorWhite);
			mPaint.setTextSize(50);
			String text = (int) percent + "%";
			canvas.drawText(text, (this.getMeasuredWidth() / 2) - (mPaint.measureText(text) / 2), (this.getMeasuredHeight()) / 2 + 15, mPaint);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mRectF = new RectF(0, 0, w, h);
	}
	
	public void setGoal(double goal) {
		mGoal = goal;
	}
	
	public void setValue(double value) {
		mValue = value;
	}
}
