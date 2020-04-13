package com.salutron.lifetrakwatchapp.fragment;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Class for creating a new fragment instance
 * 
 * @author rsarmiento
 *
 */
public final class FragmentFactory {
	
	/**
	 * Create a new instance of fragment
	 * 
	 * @param cls 	Class of the fragment
	 * @return 		Returns <T>
	 */
	public static final <T extends SherlockFragment> T newInstance(Class<? extends T> cls) {
		try {
			return cls.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create a new instance of fragment with arguments
	 * 
	 * @param cls		Class of the fragment
	 * @param bundle	Bundle object
	 * @return			Returns <T>
	 */
	public static final <T extends SherlockFragment> T newInstance(Class<? extends T> cls, Bundle bundle) {
		try {
			T t = cls.newInstance();
			t.setArguments(bundle);
			return t;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
