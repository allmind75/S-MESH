package com.smesh.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.samsung.android.example.helloaccessoryprovider.R;

public class SOS_Dialog extends Dialog {
	private LinearLayout dialoglayout;

	public SOS_Dialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_sos);
		this.setCanceledOnTouchOutside(true); // 다이얼로그 밖 클릭 종료
		this.setCancelable(true); //취소버튼 클릭 종료

		dialoglayout = (LinearLayout) findViewById(R.id.sos_layout);
		dialoglayout.setOnClickListener(new View.OnClickListener() {

			@Override 
			public void onClick(View v) {
				dismiss();

			}
		});

	}

}
