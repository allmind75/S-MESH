package com.smesh.dialog;

import com.samsung.android.example.helloaccessoryprovider.R;
import com.smesh.helper.SmeshPreference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class InputID_Dialog extends Dialog {
	private ImageButton btn_ok;
	private EditText edit_id;
	private SmeshPreference pref;

	public InputID_Dialog(final Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_inputid);

		pref = new SmeshPreference(context);
		edit_id = (EditText) findViewById(R.id.edit_dialog_id);
		btn_ok = (ImageButton) findViewById(R.id.btn_dialog_ok);
		btn_ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String inputID = edit_id.getText().toString();
				String newText = inputID.replaceAll(" ", "");
				newText = newText.replaceAll("\n", "");
				if (newText.length() > 0) {
					pref.putValue("USERId", inputID);
					dismiss();
				} else {
					Toast.makeText(context, "이름을 넣어 주세요", Toast.LENGTH_SHORT)
							.show();
				}
				edit_id.setText("");

			}
		});

	}

}
