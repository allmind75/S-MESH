package com.smesh.timeline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.samsung.android.example.helloaccessoryprovider.R;
import com.smesh.db.DBAdapter;
import com.smesh.helper.MyApplication;
import com.smesh.helper.SmeshPreference;
import com.smesh.main.MainActivity;
import com.smesh.network.TimeTable;

public class TimeLineActivity extends Fragment implements OnScrollListener,
		OnQueryTextListener, OnCloseListener {
	OnTimeLineCallback mCallback;
	private final String TAG = "Class::TimeLineActivity";
	private static String JSONFORMET = "[{\"timetable\":\"timetable\",\"mac\":\"mac\",\"userId\":\"userid\",\"saveDate\":\"saveDate\",\"saveTime\":\"saveTime\",\"gps\":\"gps\",\"message\":\"message\"}]";
	private static String USERId = "userId";
	private static String DATE = "saveDate";
	private static String TIME = "saveTime";
	private static String GPS = "gps";
	private static String MSG = "message";
	private static String MAC = "mac";
	private static String USED_MSG = "usedmsg";
	private static String USEDTRUE = "T";
	private static String USEDFALSE = "F";
	private static String CHECK = "C";
	private static String NOTCHECK = "N";
	private static String TIMETABLE_NUM = "timetable";

	private ListView m_ListView;
	private CustomAdapter m_Adapter;
	private ArrayList<TimelineInfo> mList;
	private DBAdapter db;
	private MyApplication myApp;
	private TimelineInfo info;
	private static int CURSOR_COUNT = 0;
	private SmeshPreference pref;
	private JSONArray sendJsonArray;
	private JSONObject sendJsonObj;
	private EditText editText;
	private ImageButton sendBtn;

	private boolean LockListView;
	private boolean isTOP = true;
	private Cursor cursor;
	private TimeTable[] mTable;

	private static final int FLAG_RED = 0;
	private static final int FLAG_WHITE = 1;
	private int check_flag = FLAG_WHITE;

	private Filter find_filter;
	private Filter check_filter;
	
	private MainActivity main; 

	public TimeLineActivity(MainActivity Context)
	{
		this.main = Context;
	}
	
	public interface OnTimeLineCallback {
		public void onSendMessage(byte[] buffer, int size, String mac);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mCallback = (OnTimeLineCallback) activity;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		myApp = (MyApplication) getActivity().getApplication();
		db = myApp.get_DB();
		pref = myApp.get_Pref();
		mTable = myApp.get_TimeTable();
		initListView();
	}

	public void initListView() {
		cursor = db.getTimeLine();
		CURSOR_COUNT = 0;
		LockListView = true;
		mList = new ArrayList<TimelineInfo>();
		m_Adapter = new CustomAdapter(getActivity(), R.layout.custom_item, mList, db);
		m_ListView.setAdapter(m_Adapter);
		m_ListView.setOnScrollListener(this);
		m_ListView.setItemsCanFocus(false);
		m_ListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		updateList(!isTOP);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_search, menu);

		SearchManager searchManager = (SearchManager) getActivity()
				.getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getActivity().getComponentName()));
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_check: {
			if (check_flag == FLAG_WHITE) {
				item.setIcon(R.drawable.ic_check_red);
				find_check("C"); // check
				check_flag = FLAG_RED;
			} else {
				item.setIcon(R.drawable.ic_check_white);
				find_check("A"); // All
				check_flag = FLAG_WHITE;

				initListView();
			}
			return true;
		}
		case R.id.action_delete: {
			db.delete_all();
			m_Adapter.refresh();
			mList.clear();
			m_Adapter.notifyDataSetChanged();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void find_check(String check) { // postm
		check_filter = m_Adapter.get_checkFilter();
		check_filter.filter(check, new FilterListener() {

			@Override
			public void onFilterComplete(int count) {
				int count_ = m_Adapter.getCount();
				if (count_ < 1) {
					Log.e(TAG, "list item empty");
				}
			}
		});
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		searchOnList(query);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		searchOnList(newText);
		return false;
	}

	private void searchOnList(String filter) {
		find_filter = m_Adapter.getFilter();
		find_filter.filter(filter, new FilterListener() {

			@Override
			public void onFilterComplete(int count) {
				int count_ = m_Adapter.getCount();
				if (count_ < 1) {
					Log.e(TAG, "list item empty");
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.timelinelistview, container, false);
		m_ListView = (ListView) v.findViewById(R.id.list);
		editText = (EditText) v.findViewById(R.id.editText);
		sendBtn = (ImageButton) v.findViewById(R.id.button_sendMsg);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String inputText = editText.getText().toString();
				String newText = inputText.replaceAll("\n", " ");
				if (newText.length() > 0)
					send_msg(newText);
				editText.setText("");

				m_ListView.setSelectionAfterHeaderView();
			}
		});

		v.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					Intent intent = new Intent(getActivity(),
							MainActivity.class);
					startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
		});
		return v;
	}

	private String compareGPS(String Date, String time) {
		String saveGPS = pref.getValue(GPS, " ");
		String saveGPSDate = pref.getValue("gps_date", "0");
		String saveGPSTime = pref.getValue("gps_time", "0");
		String compareTime = " ";
		if (!saveGPS.equals(" ") && saveGPSDate.equals(Date)) {

			int saveTimeH = Integer.parseInt(saveGPSTime.split(":")[0]);
			int curTimeH = Integer.parseInt(time.split(":")[0]);

			int saveTimeM = Integer.parseInt(saveGPSTime.split(":")[1]);
			int curTimeM = Integer.parseInt(time.split(":")[1]);

			if (curTimeH - saveTimeH >= 1) { // 1시간이상 차이날때
				compareTime = (curTimeH - saveTimeH) + "시간 전";
			} else { // 1시간이상 차이가 나지않을때
				compareTime = (curTimeM - saveTimeM) + "분 전";
			}
		}

		return compareTime;
	}

	public int change_tablenum(String M, String s) {
		int trans_time = 0;

		trans_time = (Integer.parseInt(M) % 10) * 60 + Integer.parseInt(s);

		return trans_time;
	}

	public void send_msg(String inputText) {
		sendJsonArray = new JSONArray();
		sendJsonObj = new JSONObject();
		String used = USEDFALSE;
		int timetable_num = 0;

		Date date = new Date(System.currentTimeMillis());  

		String curDate = new SimpleDateFormat("yyyy년MM월dd일").format(date);
		String curTime = new SimpleDateFormat("HH:mm:ss").format(date);
		String time[] = curTime.split(":");
		timetable_num = change_tablenum(time[1], time[2]);
		String comGPS = pref.getValue(GPS, " ") + "  -  "
				+ compareGPS(curDate, curTime);
		String tMac = pref.getValue("MAC", " ");
		try {
			sendJsonObj.put(TIMETABLE_NUM, timetable_num);
			sendJsonObj.put(MAC, tMac);
			sendJsonObj.put(USERId, pref.getValue("USERId", "GUEST"));
			sendJsonObj.put(DATE, curDate);
			sendJsonObj.put(TIME, curTime);
			sendJsonObj.put(GPS, comGPS);
			sendJsonObj.put(MSG, inputText);

			sendJsonArray.put(sendJsonObj);
		} catch (Exception e) {

		}

		if (main.client_cnt >= 1 || main.server_cnt >= 1)
			used = "T";
		else
			used = "F";

		mTable[timetable_num].Check(tMac, curTime);

		byte[] t_byte = sendJsonArray.toString().getBytes();

		mCallback.onSendMessage(t_byte, t_byte.length, tMac);

		db.inset_msg(tMac, pref.getValue("USERId", "GUEST"), curDate, curTime,
				comGPS, inputText, used + NOTCHECK);

		insertInfo(tMac, pref.getValue("USERId", "GUEST"), curDate, curTime,
				comGPS, inputText, used + NOTCHECK, isTOP);
	}

	public void updateList(boolean top) {
		LockListView = true;
		int Max = CURSOR_COUNT + 10;
		int cursorCount = cursor.getCount();
		while (CURSOR_COUNT < Max && cursorCount > CURSOR_COUNT) {

			insertInfo(cursor.getString(cursor.getColumnIndex(MAC)),
					cursor.getString(cursor.getColumnIndex(USERId)),
					cursor.getString(cursor.getColumnIndex(DATE)),
					cursor.getString(cursor.getColumnIndex(TIME)),
					cursor.getString(cursor.getColumnIndex(GPS)),
					cursor.getString(cursor.getColumnIndex(MSG)),
					cursor.getString(cursor.getColumnIndex(USED_MSG)), top);
			CURSOR_COUNT++;
			cursor.moveToNext();

		}
		LockListView = false;
	}

	public void insertInfo(String mac, String Id, String Date, String Time,
			String GPS, String MSG, String check, boolean top) {

		info = new TimelineInfo();
		info.setTo_mac(mac);
		info.setTo_userId(Id);
		info.setTo_date(Date);
		info.setTo_time(Time);
		info.setTo_location(GPS);
		info.setTo_text(MSG);
		info.setTo_used(check);

		if (top) {
			mList.add(0, info);
		} else
			mList.add(mList.size(), info);

		m_Adapter.notifyDataSetChanged(); // ListView update

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		CURSOR_COUNT = 0;
		cursor.close();
		MainActivity.isFragment = 1;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (firstVisibleItem >= (totalItemCount + 1) / 2 && totalItemCount != 0
				&& LockListView == false && cursor.getCount() > CURSOR_COUNT) {
			Log.e(TAG, "scroll");
			updateList(!isTOP);
		}
		if (totalItemCount < 10 && cursor.getColumnCount() > 0)
			updateList(!isTOP);
	}

	@Override
	public boolean onClose() {
		initListView();
		return false;
	}

}
