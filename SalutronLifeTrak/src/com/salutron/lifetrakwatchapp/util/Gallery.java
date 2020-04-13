package com.salutron.lifetrakwatchapp.util;

import static android.media.ExifInterface.ORIENTATION_ROTATE_180;
import static android.media.ExifInterface.ORIENTATION_ROTATE_270;
import static android.media.ExifInterface.ORIENTATION_ROTATE_90;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;

public class Gallery {

	public static final int IMAGE_W = 405;
	public static final int IMAGE_H = 274;

	private HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
	private BitmapHelper bitmapHelper = new BitmapHelper();

	public int getCount() {
		return bitmaps.size();
	}

	public void addItem(String imagePath) throws IOException {
		if (bitmaps.containsKey(imagePath))
			return;
		
		Bitmap scaledBitmap = bitmapHelper.decodeSampledBitmapFromFile(
				new File(imagePath), IMAGE_W);

		ExifInterface exif = new ExifInterface(imagePath);
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
		int rotate = 0;
		switch (orientation) {
		case ORIENTATION_ROTATE_270:
			rotate += 270;
			break;
		case ORIENTATION_ROTATE_180:
			rotate += 180;
			break;
		case ORIENTATION_ROTATE_90:
			rotate += 90;
		}

		final Matrix matrix = new Matrix();
		matrix.postRotate(rotate);

		/*scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, IMAGE_W,
				IMAGE_H, true);*/
		scaledBitmap = createScaledBitmap(scaledBitmap, IMAGE_W, IMAGE_H);
		Bitmap rotatedBitmap = Bitmap
				.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),
						scaledBitmap.getHeight(), matrix, true);

		bitmaps.put(imagePath, rotatedBitmap);
	}

	public Bitmap getItem(String imagePath) {
		return bitmaps.get(imagePath);
	}

	public long getItemId(int position) {
		return position;
	}
	
	protected Bitmap createScaledBitmap(Bitmap originalBitmap, float width, float height) {
		int originalWidth = originalBitmap.getWidth();
		int originalHeight = originalBitmap.getHeight();
		
		Bitmap bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		float scale = width / originalWidth;
		float xtranslation = 0.0f;
		float ytranslation = (height - originalHeight * scale) / 2.0f;
		Matrix matrix = new Matrix();
		
		if(originalWidth > originalHeight)
			matrix.setRotate(90, originalWidth / 2, originalHeight / 2);
		
		matrix.postTranslate(xtranslation, ytranslation);
		matrix.postScale(scale, scale);
		
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalBitmap, matrix, paint);
		
		return bitmap;
	}
}
