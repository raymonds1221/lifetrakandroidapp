package com.salutron.lifetrakwatchapp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

public class FacebookUtil {
	
	/**
	 * Generates KeyHash to be used in facebook app
	 * @param context
	 * @param packageName - e.g. com.facebook.samples.hellofacebook
	 **/
	public static String generateFBKeyHash(Context context, String packageName) {
		String keyHash = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
				Log.d("KeyHash:", keyHash);
			}
			return keyHash;
		} catch (NameNotFoundException e) {
			return keyHash;
		} catch (NoSuchAlgorithmException e) {
			return keyHash;
		}
	}
}
