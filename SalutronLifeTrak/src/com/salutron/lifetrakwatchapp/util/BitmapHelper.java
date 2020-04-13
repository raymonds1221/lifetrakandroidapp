package com.salutron.lifetrakwatchapp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 
 * @author Ronillo Ang
 */
public class BitmapHelper {

	public Bitmap decodeSampledBitmapFromFile(File resourceFile,
			int imageMaxSize) throws IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		FileInputStream fis = new FileInputStream(resourceFile);
		BitmapFactory.decodeStream(fis, null, options);
		fis.close();

		int scale = 1;
		if (options.outHeight > imageMaxSize || options.outWidth > imageMaxSize) {
			int pow = (int) Math.round(Math.log(imageMaxSize
					/ (double) Math.max(options.outHeight, options.outWidth))
					/ Math.log(0.5));
			scale = (int) Math.pow(2, pow);
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = scale;
		fis = new FileInputStream(resourceFile);
		Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
		fis.close();

		return bitmap;
	}
}
