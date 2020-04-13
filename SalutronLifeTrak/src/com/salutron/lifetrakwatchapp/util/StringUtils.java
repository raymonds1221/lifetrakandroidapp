package com.salutron.lifetrakwatchapp.util;

public class StringUtils {

	/**
	 * Checks if a String is empty ("") or null.
	 * 
	 * StringUtils.isEmpty(null) =true StringUtils.isEmpty("") = true
	 * StringUtils.isEmpty(" ") = false StringUtils.isEmpty("bob") = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * 
	 * @param: string - the String to check, may be null
	 * @return true if the String is empty or null
	 **/
	public static boolean isEmpty(String string) {
		if (null == string) {
			return true;
		} else if ("".equals(string)) {
			return true;
		} else if (string.length() > 0) {
			return false;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * Checks if a String is whitespace, empty ("") or null.
	 **/
	public static boolean isBlank(String string) {
		if (null == string) {
			return true;
		} else if ("".equals(string)) {
			return true;
		} else if (string.trim().length() == 0) {
			return true;
		} else if (string.trim().length() > 0) {
			return false;
		} else {
			return false;
		}
	}
}
