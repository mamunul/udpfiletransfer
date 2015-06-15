package com.ringid.itransfer;

import com.example.itransfer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	private EditText userNameEditText;

	private Button startButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		userNameEditText = (EditText) findViewById(R.id.user_name_edittext);

		startButton = (Button) findViewById(R.id.start_button);

		startButton.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String userName = userNameEditText.getText().toString().trim();
		if (v.getId() == R.id.start_button) {

			if (userName.length() > 0) {

				Intent intent = new Intent(this, TransferActivity.class);

				intent.putExtra("username", userName);

				startActivity(intent);

			} else {

				new AlertDialog.Builder(this).setTitle("Alert!")
						.setMessage("Enter NickName.").setCancelable(false).setPositiveButton("OK", null)
						.show();

			}

		}
	}

}
