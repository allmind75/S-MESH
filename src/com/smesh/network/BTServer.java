package com.smesh.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smesh.main.MainActivity;

public class BTServer extends Thread { 
	private BluetoothServerSocket mmSSocket; 
	private BluetoothSocket cSocket;
	private BluetoothAdapter mBA = null;
	private ReceiveThread recvThread;
	private OutputStream mmOutStream = null;  
	private int idx;
	private Handler mHandler;  
	private final int SERVER_CONNECT = 10; 
	private final int CONNECTION_LOST = 90;
	private UUID client_uuid;
	private String client_mac;
	private int size; 
	
	public BTServer(BluetoothAdapter _mBA, int _i, Handler _handler, String _client_uuid, String _client_mac) { //  
		mHandler = _handler;
		idx = _i; 
		mBA = _mBA; 
		client_uuid = UUID.fromString(_client_uuid); // 클라이언트의 uuid와 맥을 가지고 있다.
		client_mac = _client_mac;
	} 

	public void run() { 
		try { 
			  
			mmSSocket = mBA.listenUsingInsecureRfcommWithServiceRecord(MainActivity.BLUE_NAME, client_uuid); 
			cSocket = mmSSocket.accept();  
			Message msg = Message.obtain(mHandler,SERVER_CONNECT,idx,0,client_mac); // 연결임을 알리는 상수 값 10과 idx, client_mac 주소를 날려줌.
			mHandler.sendMessage(msg);
			
			recvThread = new ReceiveThread(cSocket); 
			recvThread.start();
			
		} catch (IOException e) {
			Log.e("클라이언트가", "나간 것 같아..");
			/* 
			try {
				if (mmSSocket != null)
					mmSSocket.close();
				if (cSocket != null) {
					cSocket.close();
					
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			 */
			e.printStackTrace();
		}
	}
	
	public void all_send(String _msg)
	{ 
		byte[] tMsg = _msg.getBytes();
		recvThread.write(tMsg, tMsg.length);
	} 
	
	public void all_send(byte[] buffer, int size)
	{ 
		recvThread.write(buffer, size);
	} 

	class ReceiveThread extends Thread { 
		private final BluetoothSocket btsocket;
		private InputStream mmInStream; 
		private int size;
		
		public ReceiveThread(BluetoothSocket socket) {
			
			btsocket = socket; 
			try {
				mmOutStream = btsocket.getOutputStream(); // 소켓당 통로
				mmInStream = btsocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		public void run() { 
			try { 
				while (true) {
					byte[] buffer = new byte[1024];
					size = mmInStream.read(buffer);
					String strBuf = new String(buffer, 0, size);
					Log.e("server receive : ->", ""+strBuf); 
					if(strBuf != null)
					{
						Message msg = Message.obtain(mHandler,idx,0,0,strBuf); // what == 인덱스(몇 번째 만들어졌는지), strBuf == 내용 -> 걔 빼고 보내기 위해
						mHandler.sendMessage(msg);
					}
				}
			} catch (IOException e) { 
				Message msg = Message.obtain(mHandler,CONNECTION_LOST); // what == 인덱스(몇 번째 만들어졌는지), strBuf == 내용 -> 걔 빼고 보내기 위해
				mHandler.sendMessage(msg);
				Log.e("client", "lost");
				e.printStackTrace();
			}
		}
		
		public void write(byte[] _buffer, int size)
		{
			try {
				if(mmOutStream != null)
					mmOutStream.write(_buffer, 0, size);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	} 
}
