package com.smesh.dialog;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.samsung.android.example.helloaccessoryprovider.R;

public class TimeLineDelete_Popup extends PopupWindow {

	public interface OnClickItem {
		public void onClickItem(boolean check);
	}

	public static final int ACTION_Delete = 1;
	public static final int ACTION_NO = 2;

	private Context mContext;

	private LayoutInflater inflater;
	private View mRootView;
	private LinearLayout linear;
	private ImageButton imagebtn;
	private OnClickItem mCallback;

	public TimeLineDelete_Popup(Context context) {
		super(context);
		this.mContext = context;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootView = (ViewGroup) inflater.inflate(R.layout.popup_delete, null);
		linear = (LinearLayout)mRootView.findViewById(R.id.popup_layout);
		imagebtn = (ImageButton) mRootView.findViewById(R.id.btn_delete_yes);
		imagebtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onClickItem(true);
					dismiss();
				}

			}
		});
		setTouchable(true);
		setFocusable(true);
		setOutsideTouchable(true);
		setContentView(mRootView);

		setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					dismiss();
					return true;
				}
				return false;
			}
		});
	}

	public void show(View view) {
		int[] location = new int[2];

		view.getLocationOnScreen(location);
		int viewh = view.getHeight();
		int LcdSizeW = getLcdSizeWidth();

		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int mviewW = mRootView.getMeasuredWidth()+30;
		int mviewH = mRootView.getMeasuredHeight()+30;
		setWidth(mviewW);
		setHeight(mviewH);

		int xPos = LcdSizeW / 2 - mviewW / 2;
		int yPos = location[1] + mviewH / 3;

		showAtLocation(view, Gravity.NO_GRAVITY, xPos, yPos);
	}

	public void setOnClickItem(OnClickItem mCallback) {
		this.mCallback = mCallback;
	}

	public int getLcdSizeWidth() {
		return ((WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getWidth();
	}
}
