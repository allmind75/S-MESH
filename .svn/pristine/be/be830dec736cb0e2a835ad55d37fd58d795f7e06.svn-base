package com.smesh.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SmeshPreference {// SharedPreferences 를 쉽게 쓰기 위한 클래스
	
	private static String PR_NAME = "s_mesh";
	static Context mContext;

	public SmeshPreference(Context context) {
		mContext = context;
	}

	public void putValue(String key, String value) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value); 
		editor.commit();
	}

	public void putValue(String key, boolean value) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(key, value);
		editor.commit();
	}

	public String getValue(String key, String defalut) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME, Activity.MODE_PRIVATE);

		try {
			return pref.getString(key, defalut);
		} catch (Exception e) {
			return defalut;
		}
	}

	public boolean getValue(String key, boolean defalut) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME,
				Activity.MODE_PRIVATE);
		try {
			return pref.getBoolean(key, defalut);
		} catch (Exception e) {
			return defalut;
		}
	}
	
	public void putValue(String key, int value) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt(key, value);
		editor.commit();
	}

	public int getValue(String key, int defalut) {
		SharedPreferences pref = mContext.getSharedPreferences(PR_NAME,
				Activity.MODE_PRIVATE);

		try {
			return pref.getInt(key, defalut);
		} catch (Exception e) {
			return defalut;
		}
	}
	
}
