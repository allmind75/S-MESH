package com.smesh.helper;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.smesh.db.DBAdapter;
import com.smesh.main.MainActivity;
import com.smesh.network.BTClient;
import com.smesh.network.BTServer;
import com.smesh.network.TimeTable;

public class MyApplication extends Application {
	private static DBAdapter db;
	private static String USERId = "userId";
	private static String DATE = "saveDate";
	private static String TIME = "saveTime";
	private static String GPS = "gps";
	private static String MSG = "message";
	private static String MAC = "mac";
	private static String USED_MSG = "usedmsg";
	private static int HRM = 0;
	private static SmeshPreference pref; 
	private int eq_cnt = 0; 
	private boolean gearConnect = false;
	private TimeTable[] mTable;   
	
	public void set_TimeTable(TimeTable[] _mTable)
	{
		mTable = _mTable;
	} 
	
	public TimeTable[] get_TimeTable()
	{
		return mTable;
	}  
	
	public void set_Pref(SmeshPreference _pref){
		this.pref= _pref;
	}

	public SmeshPreference get_Pref(){
		return pref;
	} 
	
	public void set_DB(DBAdapter db) {
		this.db = db;
	}

	public DBAdapter get_DB() {
		return db;
	} 
	
	public void set_HRM(int hrm) {
		this.HRM = hrm;
	}
	
	public int get_HRM(){
		return HRM;
	}
	
	public void set_gear(boolean connect) {
		Log.e("MYapp", "Set Gear : " + connect);
		this.gearConnect = connect;
	}
	
	public boolean get_gear() {
		return gearConnect;
	}
}
