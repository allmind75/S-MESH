package com.smesh.timeline;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.samsung.android.example.helloaccessoryprovider.R;
import com.smesh.db.DBAdapter;
import com.smesh.dialog.TimeLineDelete_Popup;
import com.smesh.dialog.TimeLineDelete_Popup.OnClickItem;
import com.smesh.helper.SmeshPreference;

public class CustomAdapter extends ArrayAdapter<TimelineInfo> {

	private final String TAG = "Class::CustomAdapter";
	
	private static String USERId = "userId";
	private static String DATE = "saveDate";
	private static String TIME = "saveTime";
	private static String GPS = "gps";
	private static String MSG = "message";
	private static String MAC = "mac";
	private static String USED_MSG = "usedmsg";
	
	private ArrayList<TimelineInfo> mOriginalValues;
	private ArrayList<TimelineInfo> mList;
	private Object mLock = new Object();
	private Context mContext;
	private LayoutInflater mInflater;
	private SmeshPreference pref;
	private static DBAdapter db;
	private int cnt = 0;
	private String keyword = "";
	private Array_FindFilter mFilter;
	private CheckFilter mFilter_check;
	private TimeLineDelete_Popup delete_popup;

	public CustomAdapter(Context context, int layoutResource,
			ArrayList<TimelineInfo> objects, DBAdapter db) {
		super(context, layoutResource, objects);
		this.db = db;
		pref = new SmeshPreference(context);
		this.mContext = context;
		this.mList = objects;
		this.mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	// 현재 아이템의 수 리턴
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void refresh(){
		pref.putValue("CHECK_CNT", 0);
	}

	// 출력 될 아이템 관리
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final int pos = position;
		// final Context context = parent.getContext();
		TimelineInfo info = mList.get(position);
		
		TextView userId = null;
		TextView date = null;
		TextView time = null;
		TextView location = null;
		TextView text = null;
		LinearLayout used = null;
		CheckBox check = null;
		CustomHolder holder = null;

		// 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 convertView가 null인 상태로 돌아 옴
		if (convertView == null) {
			// view가 null일 경우 커스텀 레이아웃 얻어 옴
			convertView = mInflater
					.inflate(R.layout.custom_item, parent, false);

			userId = (TextView) convertView
					.findViewById(R.id.custom_item_userId);
			date = (TextView) convertView.findViewById(R.id.custom_item_date);
			time = (TextView) convertView.findViewById(R.id.custom_item_time);
			location = (TextView) convertView
					.findViewById(R.id.custom_item_location);
			text = (TextView) convertView.findViewById(R.id.custom_item_text);
			used = (LinearLayout) convertView
					.findViewById(R.id.custom_item_used);
			check = (CheckBox) convertView
					.findViewById(R.id.custom_item_checkbox);

			holder = new CustomHolder();
			holder.userId = userId;
			holder.date = date;
			holder.time = time;
			holder.location = location;
			holder.text = text;
			holder.used = used;
			holder.check = check;
			convertView.setTag(holder);

		} else {

			// convertView.setBackgroundResource(R.drawable.ic_message_box);
			// //list item 배경 변경,
			holder = (CustomHolder) convertView.getTag();
			userId = holder.userId;
			date = holder.date;
			time = holder.time;
			location = holder.location;
			text = holder.text;
			used = holder.used;
			check = holder.check;
		}

		if (info != null) {

		}

		holder.userId.setText(spannableString(mList.get(pos).getTo_userId()),
				BufferType.SPANNABLE);
		holder.date.setText(mList.get(pos).getTo_date());
		holder.time.setText(mList.get(pos).getTo_time());
		holder.location.setText(
				spannableString(mList.get(pos).getTo_location()),
				BufferType.SPANNABLE);
		holder.text.setText(spannableString(mList.get(pos).getTo_text()),
				BufferType.SPANNABLE);

		String myMAC = pref.getValue("MAC", " ");

		if (myMAC.equals(mList.get(pos).getTo_mac())) { // 내가 보낸건지 아닌건지 확인
			check.setVisibility(View.VISIBLE); // 체크박스가 보여야함
			if ("T".equals(mList.get(pos).getTo_used())) { // 이게 다른 사람에게 보내진거라면
				holder.used.setBackgroundColor(Color.BLUE); // 파란색으로 확인
			} else { // 이게 다른 사람에게 보내지지 못한거라면
				holder.used.setBackgroundColor(Color.RED); // 빨간색으로 확인
			}
			
			if ("C".equals(mList.get(pos).getTo_check())) { // 그런데 이게 또 체크가 된거라면
				
				holder.check.setChecked(true); // 체크 시킴
			} else { // 그런데 이게 체크가 안된거라면
				holder.check.setChecked(false); // 체크 안함
			}


			convertView.setOnClickListener(new OnClickListener() {  //내가 보낸것만 됨

				@Override
				public void onClick(View v) {
					cnt = pref.getValue("CHECK_CNT", 0);
					//Log.e(TAG, "click   cnt : " + cnt);
					
					if ("N".equals(mList.get(pos).getTo_check())) {  //체크가 안됬는데 눌렀을때
						if (cnt >= 0 && cnt < 3) {
							pref.putValue("CHECK_CNT", cnt + 1);
							db.update_used(mList.get(pos).getTo_text(),	mList.get(pos).getTo_mac(), mList.get(pos)
											.getTo_date(), mList.get(pos).getTo_time(), mList.get(pos).getTo_used() + "C");
							mList.get(pos).setTo_check("C");
							CheckBox cbox = (CheckBox) v
									.findViewById(R.id.custom_item_checkbox);
							cbox.setChecked(true);

							Log.e(TAG, "check msg : " + mList.get(pos).getTo_text()
									+ " pos : " + pos + " check : "
									+ mList.get(pos).getTo_check());
						} else {
							CheckBox cbox = (CheckBox) v
									.findViewById(R.id.custom_item_checkbox);
							cbox.setChecked(false);
							Toast.makeText(mContext, "중요 메세지 선택은 최대 3개까지 가능",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						if (cnt > 0 && cnt <= 3) {
							
							pref.putValue("CHECK_CNT", cnt - 1);
							db.update_used(mList.get(pos).getTo_text(),
									mList.get(pos).getTo_mac(), mList.get(pos)
											.getTo_date(), mList.get(pos)
											.getTo_time(), mList.get(pos)
											.getTo_used() + "N");
							mList.get(pos).setTo_check("N");
							CheckBox cbox = (CheckBox) v
									.findViewById(R.id.custom_item_checkbox);
							cbox.setChecked(false);
						}

					}
				}
			});

			
			
		} else { // 다른 사람에게 받은것
			holder.used.setBackgroundColor(Color.YELLOW); // 노란색
			holder.check.setVisibility(View.GONE); // 체크 박스가 없음
			mList.get(pos).setTo_check("N");
		}
		
		cnt = pref.getValue("CHECK_CNT", 0);
		holder.check.setClickable(false);
		holder.check.setFocusable(false);

		convertView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				delete_popup = new TimeLineDelete_Popup(mContext);
				delete_popup.show(v);
				delete_popup.setOnClickItem(new OnClickItem() {

					@Override
					public void onClickItem(boolean check) {
						if (check) { // yes 누르면
							if ("C".equals(mList.get(pos).getTo_check())) {
								cnt = pref.getValue("CHECK_CNT", 0);
								pref.putValue("CHECK_CNT", cnt - 1);
							}
							db.delete_msg(mList.get(pos).getTo_mac(), mList
									.get(pos).getTo_text(), mList.get(pos)
									.getTo_date(), mList.get(pos).getTo_time());
							mList.remove(pos);
							notifyDataSetChanged();
						}

					}
				});
				return false;
			}
		});

		return convertView;
	}

	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new Array_FindFilter();
		}
		return mFilter;
	}

	public Filter get_checkFilter() {
		if (mFilter_check == null) {
			mFilter_check = new CheckFilter();
		}
		return mFilter_check;
	}

	private SpannableString spannableString(String code) {
		SpannableString text = new SpannableString(code);
		text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, code.length(), 0);
		if (StringUtils.isNotBlank(keyword)) {
			int index;
			if ((index = code.toLowerCase().indexOf(keyword)) != -1) {
				try {
					text.setSpan(new ForegroundColorSpan(Color.RED), index,
							index + keyword.length(), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	private class CheckFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (prefix.equals("A")) {
				ArrayList<TimelineInfo> values;
				synchronized (mLock) {
					values = new ArrayList<TimelineInfo>(mOriginalValues);
					// 이전에 있던 값들을 다시 들고옴
				}
				results.values = values;
				results.count = values.size();
			} else {

				if (mOriginalValues == null) {
					synchronized (mLock) {
						mOriginalValues = new ArrayList<TimelineInfo>(mList);
						// 이전값들 저장시켜둠
					}
				}
				

				ArrayList<TimelineInfo> newValues = new ArrayList<TimelineInfo>();
				newValues = checkList();
			
				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mList = (ArrayList<TimelineInfo>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();

			} else {
				notifyDataSetInvalidated();
			}
		}
	}
	public ArrayList<TimelineInfo> checkList(){
		ArrayList<TimelineInfo> checkList = new ArrayList<TimelineInfo>();
		int count =0;
		
		Cursor cursor = db.getPostMessage();
		
		if(cursor != null)
			count = cursor.getCount();
			
		pref.putValue("CHECK_CNT", count);
		cursor.moveToLast();
		for(int i=0; i<count; i++){
			TimelineInfo info = new TimelineInfo();
			info.setTo_mac(cursor.getString(cursor.getColumnIndex(MAC)));
			info.setTo_userId(cursor.getString(cursor.getColumnIndex(USERId)));
			info.setTo_date(cursor.getString(cursor.getColumnIndex(DATE)));
			info.setTo_time(cursor.getString(cursor.getColumnIndex(TIME)));
			info.setTo_location(cursor.getString(cursor.getColumnIndex(GPS)));
			info.setTo_text(cursor.getString(cursor.getColumnIndex(MSG)));
			info.setTo_used(cursor.getString(cursor.getColumnIndex(USED_MSG)));
			checkList.add(info);
			cursor.moveToPrevious();
		}
		return checkList;
	}

	
	
	private class Array_FindFilter extends Filter { // find 할때 찾는것
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<TimelineInfo>(mList);
				}
			}

			if (prefix == null || prefix.length() == 0) {
				keyword = "";
				ArrayList<TimelineInfo> list;
				synchronized (mLock) {
					list = new ArrayList<TimelineInfo>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();
				keyword = prefixString;

				ArrayList<TimelineInfo> values;
				synchronized (mLock) {
					values = new ArrayList<TimelineInfo>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<TimelineInfo> newValues = new ArrayList<TimelineInfo>();

				for (int i = 0; i < count; i++) {
					final TimelineInfo value = values.get(i);
					final String valueID = value.getTo_userId().toLowerCase();
					final String valueMSG = value.getTo_text().toLowerCase();
					final String valueGPS = value.getTo_location()
							.toLowerCase();

					if (valueID.contains(prefixString)
							|| valueMSG.contains(prefixString)
							|| valueGPS.contains(prefixString)) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mList = (ArrayList<TimelineInfo>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private class CustomHolder {
		TextView userId;
		TextView date;
		TextView time;
		TextView location;
		TextView text;
		LinearLayout used;
		CheckBox check;
	}

}
