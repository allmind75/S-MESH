package com.smesh.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter { // 디비 접근을 쉽게 만들어주는 어댑터

	private static final String DATABASE_NAME = "smeshdb.db";
	private static final String DATABASE_TABLE = "smesh_log";
	private static String DATE = "saveDate";
	private static String TIME = "saveTime";
	private static String USERId = "userId";
	private static String GPS = "gps";
	private static String MSG = "message";
	private static String MAC = "mac";
	private static String USEDMSG = "usedmsg";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDB;

	private Context mcontext;

	public DBAdapter(Context context) {
		this.mcontext = context;
	}

	public DBAdapter open() throws SQLException { // 디비 오픈
		mDbHelper = new DatabaseHelper(mcontext, 1);
		mDB = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() { // 디비 닫기
		mDbHelper.close();
	}
	

	public void update_used(String msg_check, String mac_check,
			String date_check, String time_check, String used) {

		String[] input = new String[] { mac_check, date_check, time_check,
				msg_check };

		ContentValues values = new ContentValues();
		values.put(USEDMSG, used);
		mDB.update(DATABASE_TABLE, values, MAC + "=? and " + DATE + "=? and "
				+ TIME + "=? and " + MSG + "=? ", input);

		Log.e("TAG", "db update : " + used);
	}
	
	
	public void update_send(String msg_check, String mac_check, String date_check, String time_check) {
		String used = "TC";  //전송이 되었으며 체크가 된 친구들이다.
		
		String[] input = new String[] { mac_check, date_check, time_check, msg_check };

		ContentValues values = new ContentValues();
		values.put(USEDMSG, used);
		mDB.update(DATABASE_TABLE, values, MAC + "=? and " + DATE + "=? and " + TIME + "=? and " + MSG + "=? ", input);

		Log.e("TAG", "db update send : " + used);
	}

	public void delete_msg(String mac, String msg, String date, String time) {

		String[] input = new String[] { mac, msg, date, time };

		mDB.delete(DATABASE_TABLE, MAC + " = ? and " + MSG + " = ? and " + DATE
				+ " = ? and " + TIME + " = ?", input);

	}
	
	public void delete_all(){
		mDB.delete(DATABASE_TABLE, null, null);
	}

	public Cursor getTimeLine() throws SQLException {
		
//		String sql = "select * from " + DATABASE_TABLE + " order by " + DATE
//				+ " desc , " + TIME + " desc;";
		String sql = "select * from " + DATABASE_TABLE + " order by " + "_index desc";

		Cursor mCursor = mDB.rawQuery(sql, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getPostMessage() throws SQLException {

		String sql = "select *from " + DATABASE_TABLE + " where " + USEDMSG
				+ " like '%C' order by " + DATE + " , " + TIME + " asc;";

		Cursor mCursor = mDB.rawQuery(sql, null);

		if (mCursor != null)
			mCursor.moveToFirst();

		return mCursor;

	}

	public void inset_msg(String mac, String id, String date, String time,
			String gps, String msg, String check) { // 메세지를 보내거나 받을 때 디비에 저장
		ContentValues values = new ContentValues();
		values.put(MAC, mac);
		values.put(USERId, id);
		values.put(MSG, msg);
		values.put(DATE, date);
		values.put(TIME, time);
		values.put(GPS, gps);
		values.put(USEDMSG, check);

		mDB.insert(DATABASE_TABLE, null, values);
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, int version) {
			super(context, DATABASE_NAME, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) { // index는 자동적으로 생성, id date
													// time gps msg 저장
			String sql = "create table Smesh_log("
					+ "_index INTEGER primary key autoincrement,mac text, userId text,"
					+ "saveDate text, saveTime text, gps text,usedmsg text, message text NOT NULL);";
			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "drop table if exists Smesh_log";
			db.execSQL(sql);
			onCreate(db);

		}
	}

}
