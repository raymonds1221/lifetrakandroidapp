package com.salutron.lifetrakwatchapp.view;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.salutron.lifetrak.R;

/**
 * Custom view for displaying the progress of the metric
 * 
 * @author rsarmiento
 *
 */
public class DashboardMetricView extends View {
	private double mGoal;
	private double mValue;
	private int mColorLightGray;
	private int mColor25Percent;
	private int mColor50Percent;
	private int mColor75Percent;
	private int mColor100Percent;
	private int mColorWhite;
	private Paint mPaint;
	private RectF mRectF;
	private Bitmap mFullwheelBitmap;

	public DashboardMetricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		
		mColorLightGray = Color.rgb(180, 184, 188);
		mColor25Percent = Color.rgb(217, 189, 55);
		mColor50Percent = Color.rgb(229, 210, 80);
		mColor75Percent = Color.rgb(144, 204, 41);
		mColor100Percent = Color.rgb(31, 178, 103);
		mColorWhite = Color.rgb(255, 255, 255);
		mFullwheelBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.asset_dash_3_fullwheel);
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
				mPaint.setColor(colorFromPercent(percent));
				canvas.drawArc(mRectF, 270, (float) value * 360.0f, true, mPaint);
			}
			
			mPaint.setColor(mColorWhite);
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 13, mPaint);
			
			if((int)percent <= 0) {
				mPaint.setColor(mColorLightGray);
			} else {
				mPaint.setColor(colorFromPercent((int)percent));
			}
			
			canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredHeight() / 2 - 25, mPaint);
			
			mPaint.setColor(mColorWhite);
			mPaint.setTextSize(50);
			String text = (int) percent + "%";
			canvas.drawText(text, (this.getMeasuredWidth() / 2) - (mPaint.measureText(text) / 2), (this.getMeasuredHeight()) / 2 + 15, mPaint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int desiredWidth = 200, desiredHeight = 200;
		
		int width = 0;
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int height = 0;
		int heightMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		switch(widthMode) {
		case MeasureSpec.EXACTLY:
			width = widthSize;
			break;
		case MeasureSpec.AT_MOST:
			width = Math.min(desiredWidth, widthSize);
			break;
		default:
			width = desiredWidth;
			break;
		}
		
		switch(heightMode) {
		case MeasureSpec.EXACTLY:
			height = heightSize;
			break;
		case MeasureSpec.AT_MOST:
			height = Math.min(desiredHeight, heightSize);
			break;
		default:
			height = desiredHeight;
			break;
		}
		
		width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		height = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mRectF = new RectF(0, 0, w, h);
	}
	
	/**
	 * Goal of the metric per day
	 * 
	 * @param goal The goal of the metric
	 */
	public void setGoal(double goal) {
		mGoal = goal;
	}
	
	/**
	 * The actual value of the metric
	 * 
	 * @param value The actual value of the metric
	 */
	public void setValue(double value) {
		mValue = value;
	}
	
	private int colorFromPercent(double percent) {
		if(percent >= 75) {
			return mColor100Percent;
		} else if(percent >= 50) {
			return mColor75Percent;
		} else if(percent >= 25) {
			return mColor50Percent;
		} else if(percent > 0) {
			return mColor25Percent;
		}
		
		return mColorLightGray;
	}
}
