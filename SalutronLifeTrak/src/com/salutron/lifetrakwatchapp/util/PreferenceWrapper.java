package com.salutron.lifetrakwatchapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class for managing Shared Preferences
 * @author Raymond Sarmiento
 *
 */
public class PreferenceWrapper implements SalutronLifeTrakUtility {
	private static Object LOCK_OBJECT = PreferenceWrapper.class;
	private static PreferenceWrapper sPrefsWrapper;
	private SharedPreferences mSharedPrefs;
	private SharedPreferences.Editor mEditor;
	
	private PreferenceWrapper(Context context) {
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		mEditor = mSharedPrefs.edit();
	}
	
	/**
	 * Singleton method for getting the instance of the class
	 * @param context 	The Context object to be pass
	 * @return			Returns (PreferenceWrapper) object
	 */
	public static PreferenceWrapper getInstance(Context context) {
		synchronized(LOCK_OBJECT) {
			if(sPrefsWrapper == null)
				sPrefsWrapper = new PreferenceWrapper(context);
			return sPrefsWrapper;
		}
	}
	
	/**
	 * Method for saving string preference
	 * @param key		The key of the String value to be save
	 * @param value		The String value to be save
	 * @return			Returns (PreferenceWrapper) object
	 */
	public PreferenceWrapper setPreferenceStringValue(String key, String value) {
		mEditor.putString(key, value);
		return this;
	}
	
	/**
	 * Method for saving boolean preference
	 * @param key		The key of the boolean value to be save
	 * @param value		The boolean value to be save
	 * @return			Returns (PreferenceWrapper) object
	 */
	public PreferenceWrapper setPreferenceBooleanValue(String key, boolean value) {
		mEditor.putBoolean(key, value);
		return this;
	}
	
	/**
	 * Method for saving int preference
	 * @param key		The key of the int value to be save
	 * @param value		The int value to be save
	 * @return			Returns (PreferenceWrapper) object
	 */
	public PreferenceWrapper setPreferenceIntValue(String key, int value) {
		mEditor.putInt(key, value);
		return this;
	}
	
	/**
	 * Method for saving long preference
	 * @param key		The key of the int value to be save
	 * @param value		The int value to be save
	 * @return			Returns (PreferenceWrapper) object
	 */
	public PreferenceWrapper setPreferenceLongValue(String key, long value) {
		mEditor.putLong(key, value);
		return this;
	}
	
	/**
	 * Method for getting saved string preference
	 * @param key		The key of the string value to be retrieve
	 * @return			Returns (String) object
	 */
	public String getPreferenceStringValue(String key) {
		return mSharedPrefs.getString(key, null);
	}
	
	/**
	 * Method for getting saved boolean preference
	 * @param key		The key of the boolean value to be retrieve
	 * @return			Returns (boolean) value
	 */
	public boolean getPreferenceBooleanValue(String key) {
		return mSharedPrefs.getBoolean(key, false);
	}
	
	public boolean getPreferenceBooleanValueDefaultTrue(String key) {
		return mSharedPrefs.getBoolean(key, true);
	}
	
	/**
	 * Method for getting saved int value
	 * @param key		The key of the int value to be retrieve
	 * @return			Returns (int) value
	 */
	public int getPreferenceIntValue(String key) {
		return mSharedPrefs.getInt(key, 0);
	}
	
	/**
	 * Method for getting saved long value
	 * @param key		The key of the int value to be retrieve
	 * @return			Returns (long) value
	 */
	public long getPreferenceLongValue(String key) {
		return mSharedPrefs.getLong(key, 0);
	}
	
	/**
	 * Method for committing changes to the SharedPreferences
	 */
	public void synchronize() {
		if(mEditor != null)
			mEditor.commit();
	}
	
	/**
	 * Cloning is not supported for Singleton object
	 */
	@Override
	public Object clone() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("clone is not permitted");
	}
	
	/**
	 * Clear sharedpref 
	 **/
	public void clearSharedPref() {
		//mSharedPrefs.edit().clear().commit();
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.remove(MAC_ADDRESS);
		editor.remove(FIRST_INSTALL);
		editor.remove(ACCESS_TOKEN);
		editor.remove(REFRESH_TOKEN);
		editor.remove(EXPIRATION_DATE);
		editor.commit();
	}

	/**
	 * Removes all Google Fit preferences for last sync tracking
	 */
	public void clearGoogleFitPrefs() {
		final SharedPreferences.Editor editor = mSharedPrefs.edit();
		for (String key : mSharedPrefs.getAll().keySet()) {
			if (key.startsWith(GOOGLE_FIT_LAST_SYNCED_DATA_TIME_PREFIX)) {
				editor.remove(key);
			}
		}
		editor.commit();
	}
}
