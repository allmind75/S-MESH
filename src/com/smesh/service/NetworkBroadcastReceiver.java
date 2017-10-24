package com.smesh.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.SystemClock;
import android.util.Log;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
	private boolean NetworkOn = false;
	private NetworkService networkService;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.e("LOG",action);
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {// 부팅시 서비스를 다시 켜는 작업
			Intent i = new Intent(context, NetworkService.class);
			context.startService(i);
		}
		if (NetworkOn) {
			try {
				ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				State wifi = cm.getNetworkInfo(1).getState(); // wifi 확인
				if (wifi == NetworkInfo.State.DISCONNECTED
						|| wifi == NetworkInfo.State.DISCONNECTING
						|| wifi == NetworkInfo.State.UNKNOWN) {

					State mobile = cm.getNetworkInfo(0).getState(); // mobile 확인
					if (mobile == NetworkInfo.State.DISCONNECTED
							|| mobile == NetworkInfo.State.DISCONNECTING
							|| mobile == NetworkInfo.State.UNKNOWN) {
						networkService.checkConnect();
					}
				}

			} catch (Exception e) {

			}
			if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
				// 위치를 켜거나 끌때 사용됨
				AlarmManager alarm = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				long firstTime = SystemClock.elapsedRealtime();
				Intent i = new Intent(context, NetworkService.class);
				PendingIntent pending = PendingIntent.getService(context, 3, i,
						PendingIntent.FLAG_ONE_SHOT); // 1은 여러개의 알람중에 알람
				// 구별할때 씀
				alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						firstTime + 1000 * 3, pending);

			}
		}

	}
	
	public void setService(NetworkService service){
		this.networkService = service;
	}

	public void seton(boolean networkOn) {
		this.NetworkOn = networkOn;
	}

}
