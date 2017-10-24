package com.smesh.timeline;

public class StringUtils {
	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}
		if (str.trim().length() == 0) {
			return true;
		}

		return false;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
}