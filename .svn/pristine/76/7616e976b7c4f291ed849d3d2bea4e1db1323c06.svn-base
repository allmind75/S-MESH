package com.smesh.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.samsung.android.example.helloaccessoryprovider.R;
import com.smesh.helper.MyApplication;
import com.smesh.helper.SmeshPreference;
import com.smesh.main.MainActivity;

public class Setting extends Fragment implements OnCheckedChangeListener {

	private RadioGroup radiogroup, notigroup;
	private RadioButton radiobtn_true, radiobtn_false;
	private RadioButton noti_true, noti_false;
	private SmeshPreference pref;
	private boolean first_serviceOn = false;
	private boolean Last_serviceOn = false;
	private boolean first_notiOn = false;
	private boolean Last_notiOn = false;
	private MyApplication myApp;

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MainActivity.isFragment = 1;

		if (Last_serviceOn != first_serviceOn) {
			if (Last_serviceOn) {
				pref.putValue("service", true);// 서비스 false
				Intent starti = new Intent("SmeshNetworkService");
				getActivity().startService(starti);
			} else {
				// 서비스 종료하는 코드
				pref.putValue("service", false);// 서비스 false
				Intent stopi = new Intent("NetworkService");
				getActivity().stopService(stopi);
			}
		}

		if (Last_notiOn != first_notiOn) {
			if (Last_notiOn) {
				pref.putValue("NOTI", true);// 서비스 false
			} else {
				pref.putValue("NOTI", false);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.activity_setting, container, false);
		radiobtn_true = (RadioButton) v.findViewById(R.id.radiobtn_true);
		radiobtn_false = (RadioButton) v.findViewById(R.id.radiobtn_false);
		noti_true = (RadioButton) v.findViewById(R.id.noti_radiobtn_true);
		noti_false = (RadioButton) v.findViewById(R.id.noti_radiobtn_false);

		myApp = (MyApplication) getActivity().getApplication();
		pref = myApp.get_Pref();
		first_serviceOn = pref.getValue("service", true);
		Last_serviceOn = first_serviceOn;

		first_notiOn = pref.getValue("NOTI", true);
		Last_notiOn = first_notiOn;

		firstcheck();

		radiogroup = (RadioGroup) v.findViewById(R.id.radiogroup_service);
		radiogroup.setOnCheckedChangeListener(this);

		notigroup = (RadioGroup) v.findViewById(R.id.radiogroup_noti);
		notigroup.setOnCheckedChangeListener(this);

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

	public void firstcheck() {

		if (first_serviceOn) // service
			radiobtn_true.setChecked(true);
		else
			radiobtn_false.setChecked(true);

		if (first_notiOn) // noti
			noti_true.setChecked(true);
		else
			noti_false.setChecked(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radiobtn_true:
			Last_serviceOn = true;
			break;
		case R.id.radiobtn_false:
			Last_serviceOn = false;
			break;
		case R.id.noti_radiobtn_true:
			Last_notiOn = true;
			break;
		case R.id.noti_radiobtn_false:
			Last_notiOn = false;
			break;
		}

	}

}
