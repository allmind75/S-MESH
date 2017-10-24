package com.smesh.main;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SosSendThread extends Thread {

	private int btclient_size;
	private int btserver_size;
	private final int SOS_CALL = 80;
	private Handler mSosHandler;
	
	public SosSendThread(Handler _sos, int c_size, int s_size)
	{
		this.mSosHandler = _sos;
		this.btclient_size = c_size;
		this.btserver_size = s_size;
	}
	
	public void run()
	{
		if(btclient_size >= 1 || btserver_size >= 1)
		{  
			Message msg = Message.obtain(mSosHandler, SOS_CALL);  // 연결 완료임을 알려줌.
			mSosHandler.sendMessage(msg);
			Log.e("핸들러로 메시지", "보냄");
		}
	}
}
