package com.smesh.main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.example.helloaccessoryprovider.R;
import com.smesh.db.DBAdapter;
import com.smesh.dialog.InputID_Dialog;
import com.smesh.dialog.SOS_Dialog;
import com.smesh.helper.MyApplication;
import com.smesh.helper.SmeshPreference;
import com.smesh.main.AccessoryProviderService.LocalIBinder;
import com.smesh.network.BTClient;
import com.smesh.network.BTServer;
import com.smesh.network.CheckClient;
import com.smesh.network.CheckServer;
import com.smesh.network.SearchStruct;
import com.smesh.network.TimeTable;
import com.smesh.setting.Setting;
import com.smesh.timeline.TimeLineActivity;

public class MainActivity extends FragmentActivity implements
		Fragment_Main.OnFragmentMainListener,
		TimeLineActivity.OnTimeLineCallback {

	private static final int ACTION_ENABLE_BT = 101;
	public static final String BLUE_NAME = "BluetoothEx";
	public static boolean gearSoS = false;
	public static int gearHrm = 0;
	public static boolean stopSoS = true;

	private static String USERId = "userId";
	private static String DATE = "saveDate";
	private static String TIME = "saveTime";
	private static String GPS = "gps";
	private static String MSG = "message";
	private static String MAC = "mac";
	private static String NOTCHECK = "N";
	private static String TIMETABLE_NUM = "timetable";

	public final static int FRAGMENT_ONE = 1;
	public final static int FRAGMENT_TWO = 2;
	public final static int FRAGMENT_THREE = 3;

	private Fragment_Main fragment_main;
	private static TimeLineActivity timelineActivity;
	private Setting settingActivity;
	private static MediaPlayer mPlayer;
	private MyApplication myApp;
	private InputID_Dialog id_dialog;
	private SOS_Dialog sos_dialog;
	public SmeshPreference pref;
	public DBAdapter db;

	public static int isFragment = 1;
	public static int client_cnt = 0, server_cnt = 0;
	private final int UPDATE_TIME = 1000 * 60 * 1; // 1초가 60개면 1분인데 그게 60개니까 10분
	private final int SERVER_CONNECT = 10;
	private final int CLIENT_CHECK = 20; // 클라이언트가 서버로 일단 연결되었고, 자신의 uuid를 날릴 준비가 되었다. 
	private final int REAL_CHECK_CONNECT = 30;  
	private final int NEXT_CONNECT = 40;
	private final int SOCKET_FAILED = 50; // 소켓 연결 실패
	private final int POST_MESSAGE = 60;
	private final int SERVER_OKAY = 70;
	private final int SOS_CALL = 80;
	private final int CONNECTION_LOST = 90; 

	private ArrayList<BTServer> btServer;
	private ArrayList<BTClient> btClient;

	public int server_check_connect[], client_check_connect[]; 
	private SearchStruct temp_class[], search_class[];
	private int connect_idx_list[];

	private Timer timer, update_timer;

	private TimeTable[] mTable = new TimeTable[604];

	private BluetoothAdapter mBA;
	private BluetoothDevice mDevice, checkDevice;

	private CheckServer checkServer = null;
	private CheckClient checkClient = null;

	private int temp_cnt = 0;
	private int connect_idx = 0;
	private int current_eq, after_eq;
	private int search_eq; // 검색된 장비
	private short rssi;

	private String my_uuid, my_mac;
	private String recv_client_uuid, recv_client_mac = null;

	private boolean first_connect = true;
	private boolean is_tempclass_init = false;

	private ArrayList<String> listServerMac, listClientMac;

	private BTServer mServer = null;
	private BTClient mClient = null;

	private SosSendThread sos_thread = null;
	private PostMessageThread postmessage_thread = null;

	private JSONArray sendJsonArray;
	private JSONObject sendJsonObj;

	private AccessoryProviderService m_accessoryService;
	private boolean m_bound = false;

	public static boolean is_first = false;
	private Date SosStartDate;

	private boolean is_blue_search_register = false;
	private boolean is_blue_state_register = false;
	private boolean is_connecting = false;

	public boolean sos_flag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		is_first = false;

		if (findViewById(R.id.main_fragment_view) != null) {
			if (savedInstanceState != null)
				return;
			fragment_main = new Fragment_Main(MainActivity.this);
			fragment_main.setArguments(getIntent().getExtras());
			FragmentTransaction trans = getSupportFragmentManager()
					.beginTransaction();
			trans.add(R.id.main_fragment_view, fragment_main).commit();
		}

		timelineActivity = new TimeLineActivity(MainActivity.this);
		settingActivity = new Setting();
		myApp = (MyApplication) getApplication();

		btServer = new ArrayList<BTServer>();
		btClient = new ArrayList<BTClient>();
		listServerMac = new ArrayList<String>();
		listClientMac = new ArrayList<String>();

		temp_class = new SearchStruct[30]; 
		connect_idx_list = new int[30];
		server_check_connect = new int[10]; // 연결 소켓 확인
		client_check_connect = new int[10]; // 연결 소켓 확인

		db = new DBAdapter(MainActivity.this);
		db.open(); // db open

		myApp.set_DB(db);
		pref = new SmeshPreference(this);
		myApp.set_Pref(pref);

		mBA = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 구한다

	}

	public void init() {
		MakeTable(); // 타임테이블 만들기

		boolean isBlue = canUseBluetooth();
		if (isBlue)
			StartCheckServer();

		is_blue_search_register = true;
		is_blue_state_register = true;
		this.registerReceiver(mBlueRecv, new IntentFilter(
				BluetoothDevice.ACTION_FOUND)); // 원격 디바이스 검색 이벤트 리시버 등록
		this.registerReceiver(mBlueCheckRecv, new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED));

		postmessage_thread = new PostMessageThread(MainActivity.this, mPostMessageHandler);
		postmessage_thread.start();

		timer = new Timer();
		timer.schedule(time_task, 5000, 5000); // 5초 후에 5초 간격으로 실행

		update_timer = new Timer();
		timer.schedule(update_task, UPDATE_TIME, UPDATE_TIME); // 10분 마다 갱신하는거.

		String id = pref.getValue("USERId", " ");
		if (id.equals(" ")) { // 처음에 메인에서 아이디 받아서 저장해야함
			pref.putValue("CHECK_CNT", 0);
			id_dialog = new InputID_Dialog(MainActivity.this);
			id_dialog.setCancelable(false);
			id_dialog.show();
			pref.putValue("service", false);

		}
		String mac = pref.getValue("MAC", " "); // 맥주소 저장하기
		if (mac.equals(" ")) {
			WifiManager mng = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo info = mng.getConnectionInfo();
			pref.putValue("MAC", info.getMacAddress());
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, AccessoryProviderService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (m_bound) {
			unbindService(mConnection);
			m_bound = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBA.isDiscovering())
			stopFindDevice();
	}

	@Override
	public void onBackPressed() {

		if (isFragment == 1) {
			moveTaskToBack(true);
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopFindDevice();
		if (is_blue_state_register) {
			unregisterReceiver(mBlueCheckRecv); // 브로드캐스트 리시버를 등록 해제한다
			mBlueCheckRecv = null;
		}
		if (is_blue_search_register) {
			unregisterReceiver(mBlueRecv);
			mBlueRecv = null;
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (isFragment == 1) {
			fragment_main.set_client_text(client_cnt);
			fragment_main.set_server_text(server_cnt);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBA != null) {
			if (mBA.isDiscovering())
				mBA.cancelDiscovery();
			mBA.startDiscovery();
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			m_bound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalIBinder binder = (LocalIBinder) service;
			m_accessoryService = binder.getService();
			m_bound = true;
			new HeartBeet().start();
		}
	};

	private void StartCheckServer() {

		if (checkServer != null)
			checkServer = null;
		checkServer = new CheckServer(mBA, mCheckServerHandler);
		checkServer.start();

		if (mBA.isDiscovering())
			mBA.cancelDiscovery();
		mBA.startDiscovery();

		setDiscoverable(); // 다른 디바이스에 자신을 노출
	}

	private void startFindDevice() {
		if (mBA.isDiscovering())
			stopFindDevice();
		mBA.startDiscovery(); // 디바이스 검색 시작
	}

	private void stopFindDevice() {
		if (mBA.isDiscovering()) { // 현재 디바이스 검색 중이라면 취소한다
			mBA.cancelDiscovery();
		}
	}

	private void setDiscoverable() { // 발견할 수 있게 함.
		if (mBA.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			return;
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivity(intent);
	}

	public boolean canUseBluetooth() {

		if (mBA == null) {
			Log.e("블루투스가 ", "안됨");
			return false; // 블루투스 어댑터가 null 이면 블루투스 장비가 존재하지 않는다.
		}
		if (mBA.isEnabled()) {
			return true; // 블루투스 활성화 상태라면 함수 탈출
		}

		Log.e("블루투스를", "켜야지");
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, ACTION_ENABLE_BT);
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			StartCheckServer();

		} else if (resultCode == RESULT_CANCELED) {
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void MakeTable() {

		mTable = null;
		mTable = new TimeTable[604];

		for (int i = 0; i < 604; i++) {
			mTable[i] = new TimeTable();
		}

		myApp.set_TimeTable(mTable);

	}

	@Override
	public void onMainButtonClick(int position) {
		switch (position) {
		case FRAGMENT_ONE: // sos
			startSOS();
			break;
		case FRAGMENT_TWO:// timeline
			changeFragment_timeline();
			isFragment = 2;
			break;
		case FRAGMENT_THREE:// setting
			changeFragment_setting();
			isFragment = 3;

			break;
		}
	}

	public void startSOS() {
		SosStartDate = new Date(System.currentTimeMillis());
		mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.warning); // 경고음
																				// 틀기위해서
		mPlayer.setLooping(true);
		mPlayer.start();
		// 지속적으로 sos 메세지를 주변에 날리거나 post 메세지를 날리는거 넣어야함
		sos_dialog = new SOS_Dialog(MainActivity.this);

		sos_thread = new SosSendThread(mSosHandler, btClient.size(),
				btServer.size());
		Log.e("연결 client size : " + btClient.size(), "연결 server size : "
				+ btServer.size());
		sos_thread.start();
		sos_dialog.show(); // 다이얼로그 보여줌

		sos_dialog.setOnDismissListener(new OnDismissListener() { // 다이얼로그가 꺼지면
																	// 호출되는 메소드.
					@Override
					public void onDismiss(DialogInterface dialog) {
						new Thread(sos_thread).interrupt();
						mPlayer.stop();
						mPlayer.release();
						stopSoS = true;
					}
				});
	}

	public void changeFragment_timeline() {
		timelineActivity.setArguments(getIntent().getExtras());
		FragmentTransaction trans = getSupportFragmentManager()
				.beginTransaction();
		trans.replace(R.id.main_fragment_view, timelineActivity);
		trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		trans.addToBackStack(null);
		trans.commit();
	}

	public void changeFragment_setting() {
		settingActivity = new Setting();
		settingActivity.setArguments(getIntent().getExtras());
		FragmentTransaction trans = getSupportFragmentManager()
				.beginTransaction();
		trans.replace(R.id.main_fragment_view, settingActivity);
		trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		trans.addToBackStack(null);
		trans.commit();
	}

	public int get_eq() {
		return search_eq;
	}

	public void set_eq() {
		search_eq++;
	}

	// 메소드가 다른 핸들러에서 재귀적으로 불리니까 다음 장비가 연결 안되는지는 걱정하지 말자.
	// 클라이언트로서 서버로 연결 시도하는 메소드
	public synchronized void Bluetooth_Connect() {
		mBA = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 구한다
		try {
			if (connect_idx < search_eq && search_class[connect_idx].get_address() != null) {
				checkDevice = mBA.getRemoteDevice(search_class[connect_idx].get_address());
				if (checkDevice != null) {
					if (search_class[connect_idx].get_address().equals(recv_client_mac) == false) {
						if(connect_idx_list[connect_idx] == 0) {
							connect_idx_list[connect_idx] = 2;
							if (mBA.isDiscovering())
								mBA.cancelDiscovery();

							if (!is_connecting) {
								Log.e("체크용 현재 연결 시도 장비 : ", "" + search_class[connect_idx].get_name());
								checkClient = new CheckClient(checkDevice, mCheckClientHandler);
								checkClient.start();
								is_connecting = true;
							}
						}
						else
							return;
					}
					else
						return;
				}
				else
					return;
			}
			else
				return;
		} catch (Exception e) {
		}
	}

	public String make(Cursor cursor, int index, int flag) {

		if (flag == 2) {
			SosStartDate = new Date(System.currentTimeMillis());
		}
		Date date = new Date(System.currentTimeMillis());
		String pushDate = new SimpleDateFormat("yyyy년MM월dd일")
				.format(SosStartDate);
		String pushTime = new SimpleDateFormat("HH:mm:ss").format(SosStartDate);
		String curDate = new SimpleDateFormat("yyyy년MM월dd일").format(date);
		String curTime = new SimpleDateFormat("HH:mm:ss").format(date);

		sendJsonArray = new JSONArray();
		sendJsonObj = new JSONObject();
		int timetable_num = 0;
		String time[] = curTime.split(":");

		String comGPS = pref.getValue(GPS, " ") + "  -  "
				+ compareGPS(curDate, curTime);
		pref.putValue("CHECK_CNT", cursor.getCount());

		timetable_num = (index + 1) + 600;
		try {
			sendJsonObj.put(TIMETABLE_NUM, timetable_num);
			sendJsonObj.put(MAC, cursor.getString(cursor.getColumnIndex(MAC)));
			sendJsonObj.put(USERId, pref.getValue("USERId", "GUEST"));
			sendJsonObj.put(DATE, curDate);
			sendJsonObj.put(TIME, curTime);
			sendJsonObj.put(GPS, comGPS);

			if (flag == 1) {

				sendJsonObj.put(
						MSG,
						"    <<긴급 구조 요청 메시지 입니다>>\n\n"
								+ cursor.getString(cursor.getColumnIndex(MSG))
								+ "   -  " + pushDate + " " + pushTime);
			} else if (flag == 2) {
				sendJsonObj.put(
						MSG,
						"    <<연결 전 과거 메시지 입니다>>\n\n"
								+ cursor.getString(cursor.getColumnIndex(MSG))
								+ "   -  " + pushDate + " " + pushTime);
			}
			sendJsonArray.put(sendJsonObj);
			db.update_send(cursor.getString(cursor.getColumnIndex(MSG)),
					cursor.getString(cursor.getColumnIndex(MAC)),
					cursor.getString(cursor.getColumnIndex(DATE)),
					cursor.getString(cursor.getColumnIndex(TIME)));
		} catch (Exception e) {
		}

		return sendJsonArray.toString();
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

	public String make() {
		Date date = new Date(System.currentTimeMillis());
		String pushDate = new SimpleDateFormat("yyyy년MM월dd일")
				.format(SosStartDate);
		String pushTime = new SimpleDateFormat("HH:mm:ss").format(SosStartDate);

		String curDate = new SimpleDateFormat("yyyy년MM월dd일").format(date);
		String curTime = new SimpleDateFormat("HH:mm:ss").format(date);

		sendJsonArray = new JSONArray();
		sendJsonObj = new JSONObject();
		int timetable_num = 0;
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
			sendJsonObj.put(MSG, "SOS 메세지 입니다. 도와주세요   -  " + pushDate + " "
					+ pushTime);
			sendJsonArray.put(sendJsonObj);
		} catch (Exception e) {
		}

		return sendJsonArray.toString();
	}

	public String get_make_string(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(MSG));
	}

	public int change_tablenum(String M, String s) {
		int trans_time = 0;

		trans_time = (Integer.parseInt(M) % 10) * 60 + Integer.parseInt(s);

		return trans_time;
	}

	public String JSONparse(String jsonString) {

		if (jsonString == null)
			return " ";
		int receiveTableTime = 0;
		String receiveMac = null;
		String receiveId = null;
		String receiveDate = null;
		String receiveTime = null;
		String receiveGPS = null;
		String receiveMSG = null;

		try {
			JSONArray receiveJson = new JSONArray(jsonString);
			JSONObject receiveObj = receiveJson.getJSONObject(0);

			receiveTableTime = receiveObj.getInt(TIMETABLE_NUM);
			receiveMac = receiveObj.getString(MAC);
			receiveId = receiveObj.getString(USERId);
			receiveDate = receiveObj.getString(DATE);
			receiveTime = receiveObj.getString(TIME);
			receiveGPS = receiveObj.getString(GPS);
			receiveMSG = receiveObj.getString(MSG);
		} catch (Exception e) {
		}

		if (!mTable[receiveTableTime].Check(receiveMac, receiveTime)) {
			String used = "T";

			db.inset_msg(receiveMac, receiveId, receiveDate, receiveTime,
					receiveGPS, receiveMSG, used + NOTCHECK);

			if (isFragment == 2)
				timelineActivity.insertInfo(receiveMac, receiveId, receiveDate,
						receiveTime, receiveGPS, receiveMSG, used + NOTCHECK,
						true);

			// Send Gear Message
			m_accessoryService.sendMsg("내용 : " + receiveMSG);
			m_accessoryService.sendMsg("I  D : " + receiveId);
			

			return receiveMac;
		} else {
			return " ";
		}
	}

	public void Me_Send(byte[] buffer, int size, String _mac) { // 연결된 모두에게 전부 다
																// 보냄
		String msg = new String(buffer, 0, size);
		All_Send(-1, -1, msg, _mac);
	}

	public void All_Send(int server_index, int client_index, String _msg,
			String _remac) {

		// 연결된 서버들로 전송한다.
		for (int i = 0; i < btServer.size(); i++) {
			if ((i != server_index) && (server_check_connect[i] == 1)
					&& (!checkClientMacList(_remac))) // 연결된 애들은 빼고( i != ),
														// 연결되었다고 알려진 list in
														// index만 (i)
			{
				BTServer mBtserver = btServer.get(i);
				if (mBtserver != null)
					mBtserver.all_send(_msg); // 리스트에 들어있는 서버 모두 전송
			}
		}

		// 연결된 클라이언트들로 전송한다.
		for (int i = 0; i < btClient.size(); i++) {
			if ((i != client_index) && (client_check_connect[i] == 1)
					&& (!checkServerMacList(_remac))) {
				BTClient mBtclient = btClient.get(i);
				if (mBtclient != null)
					mBtclient.all_send(_msg);
			}
		}
	}

	public boolean checkServerMacList(String _mac) // 같은게 검색되면 true
	{
		for (int i = 0; i < listServerMac.size(); i++) {
			if (listServerMac.get(i).equals(_mac))
				return true;
			else
				return false;
		}
		return false;
	}

	public boolean checkClientMacList(String _mac) {
		for (int i = 0; i < listClientMac.size(); i++) {
			if (listClientMac.get(i).equals(_mac)) // 같은게 검색되면
				return true;
			else
				return false;
		}
		return false;
	}

	public String CreateBaseUUID() { // 고유 UUID 생성하기
		final TelephonyManager mTelephonyManager = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidID;
		tmDevice = "" + mTelephonyManager.getDeviceId();
		tmSerial = "" + mTelephonyManager.getSimSerialNumber();
		androidID = ""
				+ android.provider.Settings.Secure.getString(
						getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidID.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();

		return deviceId;
	}

	public String CreateClientMac() {
		String _my_mac = pref.getValue("MAC", " "); // 자기 맥 주소 가져 옴.
		return _my_mac;
	}

	TimerTask time_task = new TimerTask() {
		public void run() {

			if (first_connect == true) // 첫 x초 일 때는
			{
				if (search_eq == 0) // x초가 경과해도 장비가 아직 없으면
				{
					stopFindDevice();
					startFindDevice(); // 중간에 다시 들어올 수도 있기 때문에
				} else {
					search_class = new SearchStruct[search_eq]; // 갯수만큼 새로 만듦.
					if (search_class != null && is_tempclass_init) {
						for (int i = 0; i < search_eq; i++) {
							if ((temp_class[i].get_name() != null)
									&& (temp_class[i].get_address() != null)
									&& (temp_class[i].get_rssi() != 0)) {
								search_class[i] = new SearchStruct(
										temp_class[i].get_name(),
										temp_class[i].get_address(),
										temp_class[i].get_rssi());
							}
						}
						try {
							Arrays.sort(search_class); // 오름차순(음수 기준)으로 정렬 완료. 가장가까운 것이 0번 배열에 들어감.
							if (search_eq > 0 && connect_idx < search_eq)
								Bluetooth_Connect(); // 연결이 끝난 이후에는
							first_connect = false;
						} catch (Exception e) {
						}
					} else
						return;
				}
				current_eq = get_eq();
			} // 처음 5초 일 때
			else {
				after_eq = get_eq(); // 총 x초 뒤에

				if (current_eq != after_eq) // 장비가 다르면
				{
					search_class = new SearchStruct[search_eq]; // 갯수만큼 새로 만듦.

					// search 클래스는 다시 만들 되
					for (int i = 0; i < search_eq; i++)
						search_class[i] = new SearchStruct(temp_class[i].get_name(), temp_class[i].get_address(), temp_class[i].get_rssi());

					if (search_eq > 0 && connect_idx < search_eq)
						Bluetooth_Connect(); // 연결이 끝난 이후에는
				} else { // 계속 장비가 같으면 연결을 다시 시도해야 한다. 
					
					if (search_eq > 0 && connect_idx < search_eq)
						Bluetooth_Connect();
				}
				current_eq = get_eq(); // 다시 시간 구하는 중
			}
		}
	};

	BroadcastReceiver mBlueRecv = new BroadcastReceiver() {
		public void onReceive(android.content.Context context,
				android.content.Intent intent) {
			mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
				temp_cnt = search_eq;
				try {
					rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
							Short.MIN_VALUE);
					String this_address = mDevice.getAddress();
					temp_cnt++;
					boolean is_same = false;

					if (temp_cnt == 1) {
						temp_class[0] = new SearchStruct(mDevice.getName(),
								mDevice.getAddress(), rssi);
						is_tempclass_init = true;
						set_eq();
					} else if (temp_cnt >= 2) {
						for (int i = 0; i < temp_cnt - 1; i++) // 같은 장비가 검색되는 것을
																// 걸러줌.
						{
							if (temp_class[i].get_address()
									.equals(this_address)) { 
								is_same = true;
								break;
							}
						}
						if (!is_same) { 
							temp_class[temp_cnt - 1] = new SearchStruct(mDevice.getName(), mDevice.getAddress(), rssi);
							set_eq(); // 검색한 결과 리스트에 스트럭쳐에 없으면 증가시켜 줌
							is_same = false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // end of if
		}
	};
	BroadcastReceiver mBlueCheckRecv = new BroadcastReceiver() { // 블루투스가 꺼지면
		public void onReceive(android.content.Context context,
				android.content.Intent intent) {

			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int actualState = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.STATE_OFF);

				if (actualState == BluetoothAdapter.STATE_OFF) {
					if (checkServer != null) {
						Log.e("체크 서버", "인터럽트");
						checkServer.cancel();
						checkServer.interrupt();
						checkServer = null;
					}
					stopFindDevice();
					Toast.makeText(MainActivity.this,
							"블루투스가 꺼졌습니다. 블루투스를 켜 주세요.", Toast.LENGTH_SHORT)
							.show();
				} else if (actualState == BluetoothAdapter.STATE_ON) {
					Toast.makeText(MainActivity.this, "블루투스가 켜졌습니다.",
							Toast.LENGTH_SHORT).show(); 
				}
			}
		}

	};

	// Real Socket(n번 소켓) 연결이 성공하든, 실패하든 불러지는 핸들러
	Handler nextConnectHandler = new Handler() { // 0번 소켓이 연결이 실패하거나(다음꺼 하기 위해)
													// n 번 소켓 연결이 성공하면 들어오는
													// 핸들러(n+1꺼 하기 위해)
		public void handleMessage(Message msg) {

			int is_connect = msg.arg2;
			if (is_connect == NEXT_CONNECT) // 클라이언트가 연결되었으면 서버의 맥 주소를 가져올 수 있다.
			{ 
				if(postmessage_thread.post_message_cnt == 0)
				{
					postmessage_thread.is_post_message = false;
					postmessage_thread.interrupt(); 
				}
				
				if (checkClient != null) {
					checkClient.cancel();
					checkClient = null;
				}

				if (mClient != null)
					btClient.add(mClient);

				int _idx = msg.what;
				client_check_connect[_idx] = 1;
				connect_idx_list[connect_idx] = 1;
				
				client_cnt = client_cnt + 1;

				if (isFragment == 1) {
					fragment_main.set_client_text(client_cnt);
				} 

				add_serverMacList(search_class[connect_idx].get_address());
			}

			connect_idx++; // ( 소켓 연결에 실패하거나 성공해도 다음꺼 연결하기 위해)

			if (checkClient != null) // 클라이언트가 안 만들어지면 종료할 필요 없음.
				checkClient = null;

			// 연결 장비 < 검색 장비
			if (connect_idx < search_eq) {
				Bluetooth_Connect(); // 연결이 끝난 이후에는
			}
		}
	};

	Handler mCheckServerHandler = new Handler() {
		public void handleMessage(Message msg) {
			int check = msg.what; 
			
			if (check == REAL_CHECK_CONNECT) {
				
				String client_uuidmac = msg.obj.toString();
				String recv_client_uuid = client_uuidmac.substring(0, 36);
				String recv_client_mac = client_uuidmac.substring(36);
				// fragment_main.set_recvmac(recv_client_mac);
				 
				mServer = new BTServer(mBA, btServer.size(), mServerHandler, recv_client_uuid, recv_client_mac); 
				mServer.start(); // 0-n 소켓 연결
				
				recv_client_uuid = null;
				recv_client_mac = null;
				
				if(checkServer != null)
					checkServer.ServerReady(); // 서버가 준비되었다고 알림
			}
		}
	};

	// 체크 소켓이 연결되거나 실패하면 불러지는 핸들러
	Handler mCheckClientHandler = new Handler() {
		public void handleMessage(Message msg) {
			int check = msg.what;

			if (check == CLIENT_CHECK) { // 체크용 클라이언트가 연결 되었으면
				is_connecting = false;
				my_uuid = CreateBaseUUID(); // 내 UUID를 받는다.
				my_mac = CreateClientMac(); // 내 맥 주소를 받는다.

				try {
					Thread.sleep(100);
					checkClient.UuidMacSend(my_uuid, my_mac);
				} catch (Exception e) {
					Log.e("Main uuid Send", "erorr");
					e.printStackTrace();
				}
			} else if (check == SERVER_OKAY) {
				is_connecting = false;
				try {
					Log.e("Client 연결 ", "시도");
					stopFindDevice();
					mClient = new BTClient(checkDevice, mClientHandler,
							btClient.size(), my_uuid, my_mac,
							nextConnectHandler);
					mClient.start(); // 클라이언트를 붙여야지

				} catch (Exception e) {
					e.printStackTrace();
					Log.e("체크 Client 연결 ", "실패");
				}
			} else if (check == SOCKET_FAILED) {
				is_connecting = false;
				connect_idx++; // ( 소켓 연결에 실패해도 다음꺼 연결하기 위해)

				if (checkClient != null) // 클라이언트가 안 만들어지면 종료할 필요 없음.
					checkClient = null;

				// 연결 장비 < 검색 장비
				if (connect_idx < search_eq) {
					Bluetooth_Connect(); // 연결이 끝난 이후에는
				}
			}
		}
	};

	Handler mServerHandler = new Handler() { // 서버에서는 받은 애 빼고, 모든 클라이언트에게 전송
		public void handleMessage(Message msg) {
			int idx = msg.what;
			if (idx == CONNECTION_LOST) {
				server_cnt = server_cnt - 1;

				if (server_cnt <= 0)
					server_cnt = 0;

				if (server_cnt == 0) {
					postmessage_thread = new PostMessageThread(MainActivity.this, mPostMessageHandler);
					postmessage_thread.start();
				}

				if (isFragment == 1)
					fragment_main.set_server_text(server_cnt);

				return;
			} else if (idx == SERVER_CONNECT) // 서버는 연결 되었다고 알려졌으면
			{
				if(postmessage_thread.post_message_cnt == 0)
				{
					postmessage_thread.is_post_message = false;
					postmessage_thread.interrupt(); 
				}
				
				if (mServer != null)
					btServer.add(mServer);

				if (isFragment == 1) {
					server_cnt = server_cnt + 1;
					fragment_main.set_server_text(server_cnt);
				}

				add_clientMacList(msg.obj.toString()); // 서버에서는 클라이언트의 Mac주소 넣어야
														// 함.

				int _idx = msg.arg1; // 연결된 애는 1로
				server_check_connect[_idx] = 1; // 서버에서 연결된 애들 인덱스 관리

				// 또 체크 서버 객체를 만들어서 입력을 대기해야 한다.
				// mBA = BluetoothAdapter.getDefaultAdapter();
				if (checkServer != null) {
					checkServer.cancel();
					checkServer = null;
				}
				checkServer = new CheckServer(mBA, mCheckServerHandler);
				checkServer.start();
				
			} else // 서버가 연결된 상태가 아니고 수신 상태의 구문 (서버에서 수신받은 메시지를 모든 애들한테 전송하기 위해서
					// -1을 넣어줌)
			{
				String message = msg.obj.toString();
				Log.e("핸들러가 받은 서버 수신 메시지", "" + message);
				if (message != null) {
					String recv_mac = JSONparse(message); // 보낸 사람의 맥 주소가 반환됨.
					if (!recv_mac.equals(" ")) // true 안받았던거
						All_Send(idx, -1, message, recv_mac); // 1 : server_index, 2 : client_index
				}
			}
		}
	};

	Handler mClientHandler = new Handler() { // 클라이언트에서는 모든 server와 받은 애 빼고 보낸다.
		public void handleMessage(Message msg) {
			int idx = msg.what;
			if (idx == CONNECTION_LOST) {
				client_cnt = client_cnt - 1;
				if (client_cnt <= 0)
					client_cnt = 0;

				if (client_cnt == 0) {
					postmessage_thread = new PostMessageThread(MainActivity.this, mPostMessageHandler);
					postmessage_thread.start();
				}

				if (isFragment == 1)
					fragment_main.set_client_text(client_cnt);

				return;

			} else if (msg.obj.toString() != null) {
				String message = msg.obj.toString();
				if (message != null) {
					String recv_mac = JSONparse(message); // 맥 주소를 반환.
					if (!recv_mac.equals(" "))
						All_Send(-1, idx, message, recv_mac);
				}
			}
		}
	};

	Handler mSosHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;

			if (what == SOS_CALL) {
				Log.e("SOS CALL", "불러졌어");
				Cursor cursor = db.getPostMessage();
				int count = 0;
				if (cursor != null)
					count = cursor.getCount();
				All_Send(-1, -1, make(), "MAC");

				try {
					Thread.sleep(100);
					for (int i = 0; i < count; i++) {
						String send = make(cursor, i, 1);
						All_Send(-1, -1, send, "MAC");
						cursor.moveToNext();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	Handler mPostMessageHandler = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == POST_MESSAGE) {
				if (postmessage_thread != null) {

					postmessage_thread.is_post_message = false;
					postmessage_thread.interrupt(); 

					Cursor cursor = db.getPostMessage();
					int count = 0;
					if (cursor != null) {
						count = cursor.getCount();
						Log.e("get count : ", "" + count);
					}

					for (int i = 0; i < count; i++) {
						try {
							String send = make(cursor, i, 2);
							Log.e("메시지 : ", "" + send);
							All_Send(-1, -1, send, "MAC");
							cursor.moveToNext();
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
	};

	TimerTask update_task = new TimerTask() {
		public void run() { 
			
			if (mTable != null)
				mTable = null;

			MakeTable();

			// 주기적 심박수 확인
			HrmCheck();
			// fragment_main.set_hrm(myApp.get_HRM());

		}
	};

	public ArrayList<String> get_serverMacList() {
		return listServerMac;
	}

	public void add_serverMacList(String _mac) {
		listServerMac.add(_mac);
	}

	public ArrayList<String> get_clientMacList() {
		return listClientMac;
	}

	public void add_clientMacList(String _mac) {
		listClientMac.add(_mac);
	}

	@Override
	public void onSendMessage(byte[] buffer, int size, String _mac) {
		Me_Send(buffer, size, _mac);
	}

	public void HrmCheck() {

		if (myApp.get_gear()) {
			m_accessoryService.mMyConnection.onSend("H");
		}
	}

	class HeartBeet extends Thread {

		@Override
		public void run() {
			while (true) {
				if (gearHrm > 85 || (gearHrm > 0 && gearHrm < 50)) {
					sos_flag = true;
					mHandler.sendEmptyMessage(1);
					gearHrm = -1;
					
				}
				
				if (gearSoS) {
					mHandler.sendEmptyMessage(1);
					gearSoS = false;
				}
			}
		}
	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(stopSoS) {
				startSOS();
				stopSoS = false;
			}
		}
	};

}
