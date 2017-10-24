package com.smesh.main;
 
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smesh.helper.SmeshPreference;

public class PostMessageThread extends Thread {
	
	private SmeshPreference pref;
	public int post_message_cnt;
	private Handler mHandler;
	private final int POST_MESSAGE = 60;
	private MainActivity main; 
	public boolean is_post_message = true; 
	 
	public PostMessageThread(MainActivity mainContext, Handler _h)
	{
		main = mainContext;
		pref = new SmeshPreference(main);
		mHandler = _h;
	}
	
	public void run() {
		while(is_post_message)
		{  
			post_message_cnt = pref.getValue("CHECK_CNT", 0); // 내가 보낼 포스트 메시지가 있나 봄
			
			if(post_message_cnt >= 1 && ((main.client_cnt >= 1) || (main.server_cnt >= 1)) )
			{
				Message msg = Message.obtain(mHandler, POST_MESSAGE);  // 연결 완료임을 알려줌.
				Log.e("포스트 메시지를 전송해라", "핸들러로");
				mHandler.sendMessage(msg);
				is_post_message = false;
			}
		}
	}
}
