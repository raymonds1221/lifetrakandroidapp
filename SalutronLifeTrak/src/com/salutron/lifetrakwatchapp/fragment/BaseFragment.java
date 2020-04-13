package com.salutron.lifetrakwatchapp.fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.MainActivity;
import com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility;
import com.salutron.lifetrakwatchapp.util.PreferenceWrapper;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Doubles;

import roboguice.RoboGuice;

public abstract class BaseFragment extends SherlockFragment implements SalutronLifeTrakUtility {
	private LifeTrakApplication mLifeTrakApplication;
	protected PreferenceWrapper mPreferenceWrapper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RoboGuice.getInjector(getActivity()).injectMembersWithoutViews(this);
		mPreferenceWrapper = PreferenceWrapper.getInstance(getActivity());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RoboGuice.getInjector(getActivity()).injectViewMembers(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	protected LifeTrakApplication getLifeTrakApplication() {
		if (mLifeTrakApplication == null) {
			mLifeTrakApplication = (LifeTrakApplication) getActivity().getApplicationContext();
		}
		return mLifeTrakApplication;
	}

	protected double getYWithMaxValue(double y, double maxY, double minRangeY, double maxRangeY) {
		if (maxY > 0.0) {
			double percent = y / maxY;
			double value = percent * (maxRangeY - minRangeY);
			return value;
		}
		return 0.0;
	}

	protected int orientation() {
		int orientation = getResources().getConfiguration().orientation;
		return orientation;
	}

	protected Number getMaxValue(List<Number> values) {
		Ordering<Number> ordering = new Ordering<Number>() {
			@Override
			public int compare(@Nullable Number left, @Nullable Number right) {
				int value1 = left.intValue();
				int value2 = right.intValue();

				return Ints.compare(value1, value2);
			}
		};
		return ordering.max(values);
	}

	protected Double getMaxValueWithDoubles(List<Double> values) {
		Ordering<Double> ordering = new Ordering<Double>() {
			@Override
			public int compare(@Nullable Double value1, @Nullable Double value2) {
				return Doubles.compare(value1, value2);
			}
		};
		return ordering.max(values);
	}

	protected void hideActionBarAndCalendar() {
		MainActivity activity = (MainActivity) getActivity();
		activity.getSupportActionBar().hide();
		activity.hideCalendar();
	}

	protected void showActionBarAndCalendar() {
		MainActivity activity = (MainActivity) getActivity();
		activity.getSupportActionBar().show();
		activity.showCalendar();
	}

	protected void hideCalendar() {
		MainActivity activity = (MainActivity) getActivity();
		activity.hideCalendar();
	}

	protected void showCalendar() {
		MainActivity activity = (MainActivity) getActivity();
		activity.showCalendar();
	}

	protected float dpToPx(float dp) {
		if (!isAdded())
			return 1;
		return dp * (getResources().getDisplayMetrics().densityDpi / 160.0f);
	}

	protected Date getYesterdayForDate(Date date) {
		Calendar calYesterday = Calendar.getInstance();
		calYesterday.setTime(date);
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);

		return calYesterday.getTime();
	}

	protected Date getTomorrowForDate(Date date) {
		Calendar calTomorrow = Calendar.getInstance();
		calTomorrow.setTime(date);
		calTomorrow.add(Calendar.DAY_OF_MONTH, 1);

		return calTomorrow.getTime();
	}

	public String getApiUrl() {
		return API_URL;
	}

	/**
	 * Shrinks and rotates (if necessary) a passed Bitmap.
	 * 
	 * @param bm
	 * @param maxLengthOfEdge
	 * @param rotateXDegree
	 * @return Bitmap
	 */
	public static Bitmap shrinkBitmap(Bitmap bm, int maxLengthOfEdge, int rotateXDegree) {
		if (maxLengthOfEdge > bm.getWidth() && maxLengthOfEdge > bm.getHeight()) {
			return bm;
		} else {
			// shrink image
			float scale = (float) 1.0;
			if (bm.getHeight() > bm.getWidth()) {
				scale = ((float) maxLengthOfEdge) / bm.getHeight();
			} else {
				scale = ((float) maxLengthOfEdge) / bm.getWidth();
			}
			// CREATE A MATRIX FOR THE MANIPULATION
			Matrix matrix = new Matrix();
			// RESIZE THE BIT MAP
			matrix.postScale(scale, scale);
			matrix.postRotate(rotateXDegree);

			// RECREATE THE NEW BITMAP
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);

			matrix = null;
			System.gc();
			if (bm != null)
				return bm;
			else
				return null;
		}
	}

	protected Bitmap createScaledBitmap(Bitmap originalBitmap, float width, float height) {
		int originalWidth = originalBitmap.getWidth();
		int originalHeight = originalBitmap.getHeight();

		Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		float scale = width / originalWidth;
		float xtranslation = 0.0f;
		float ytranslation = (height - originalHeight * scale) / 2.0f;
		Matrix matrix = new Matrix();

		/*if (originalWidth > originalHeight)
			matrix.setRotate(90, originalWidth / 2, originalHeight / 2);*/

		//matrix.postTranslate(xtranslation, ytranslation);
		//matrix.postScale(scale, scale);

		float scalex = width / originalWidth;
		float scaley = height / originalHeight;

		matrix.postScale(scalex, scalex);

		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalBitmap, matrix, paint);

		return bitmap;
	}

	protected Bitmap makeRoundedBitmap(Bitmap bitmap, int width, int height) {
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(width / 2, height / 2, height / 2, Path.Direction.CCW);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight()), null);
		return targetBitmap;
	}

	protected Date getExpirationDate() {
		long millis = mPreferenceWrapper.getPreferenceLongValue(EXPIRATION_DATE) * 1000;
		Date date = new Date(millis);
		return date;
	}

	protected void changeFramentView(LayoutInflater inflater, int layoutId, ViewGroup parent) {
		parent.removeAllViewsInLayout();
		inflater.inflate(layoutId, parent);
	}

	protected void toggleNavigationMenu() {
		MainActivity mainActivity = (MainActivity) getActivity();

		if (mainActivity.getSlidingMenu().isMenuShowing()) {
			mainActivity.getSlidingMenu().toggle();
		}
	}
}
