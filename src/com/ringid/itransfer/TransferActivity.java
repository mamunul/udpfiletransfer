package com.ringid.itransfer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itransfer.R;
import com.ringid.itransfer.DataAsyncTask.UDPDataListener;
import com.ringid.itransfer.DataFormat.OnlineStatus;
import com.ringid.itransfer.DataFormat.StatusType;
import com.ringid.itransfer.DataProcessor.CommStatus;
import com.ringid.itransfer.StatusAsyncTask.UDPStatusListener;

public class TransferActivity extends Activity implements OnClickListener,
		OnItemClickListener, UDPStatusListener, UDPDataListener {

	private static final int FILE_SELECT_CODE = 0;

	private Button refreshButton, fileSelectionButton, serverButton;

	private ListView usersListView;
	private TextView fileStatusTextView, mynameTextView;
	private ImageView imageView;

	private StatusAsyncTask statusAsyncTask;
	private DataAsyncTask dataAsyncTask;
	private DataProcessor udpCommunication;

	private Bitmap bitmap;

	private String userName;

	private ArrayList<User> userArrayList = new ArrayList<User>();
	private UserAdapter adapter;

	private User selectedUser;

	private byte[] byteArray;

	private DataFormat dataFormat;

	private Boolean isSender = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer);

		usersListView = (ListView) findViewById(R.id.users_listview);
		fileStatusTextView = (TextView) findViewById(R.id.file_status_textview);
		mynameTextView = (TextView) findViewById(R.id.myself);
		imageView = (ImageView) findViewById(R.id.transfer_imageview);

		refreshButton = (Button) findViewById(R.id.refresh_button);
		fileSelectionButton = (Button) findViewById(R.id.file_button);
		serverButton = (Button) findViewById(R.id.server_button);

		userName = getIntent().getExtras().getString("username");

		adapter = new UserAdapter(this, R.layout.user_item_layout,
				userArrayList);

		refreshButton.setOnClickListener(this);
		fileSelectionButton.setOnClickListener(this);

		dataAsyncTask = new DataAsyncTask(this);

		udpCommunication = new DataProcessor(this);

		usersListView.setOnItemClickListener(this);

		dataAsyncTask.setUdpDataListener(this);
		serverButton.setOnClickListener(this);

		mynameTextView.setText("Myself: " + userName);
		usersListView.setAdapter(adapter);

	}

	public void createWifiAccessPoint2(Context context) {

		// isSelfHotSpot = true;
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}
		Method[] wmMethods = wifiManager.getClass().getDeclaredMethods(); // Get
																			// all
																			// declared
																			// methods
																			// in
																			// WifiManager
																			// class
		boolean methodFound = false;
		for (Method method : wmMethods) {
			if (method.getName().equals("setWifiApEnabled")) {
				methodFound = true;
				WifiConfiguration netConfig = new WifiConfiguration();
				netConfig.SSID = "\"" + "iTransfer" + "\"";
				netConfig.networkId = 125679;
				netConfig.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);

				try {
					boolean apstatus = (Boolean) method.invoke(wifiManager,
							netConfig, true);
					for (Method isWifiApEnabledmethod : wmMethods) {
						if (isWifiApEnabledmethod.getName().equals(
								"isWifiApEnabled")) {
							while (!(Boolean) isWifiApEnabledmethod
									.invoke(wifiManager)) {
							}
							;
							for (Method method1 : wmMethods) {
								if (method1.getName().equals("getWifiApState")) {
									int apstate;
									apstate = (Integer) method1
											.invoke(wifiManager);
								}
							}
						}
					}
					if (apstatus) {
						System.out.println("SUCCESSdddd");

						Toast.makeText(this, "iTransfer Network is created",
								Toast.LENGTH_LONG).show();
						;

					} else {
						Toast.makeText(this, "iTransfer creation failed",
								Toast.LENGTH_LONG).show();
						;
						System.out.println("FAILED");

					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void loadUsersListView() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				adapter.notifyDataSetChanged();

				usersListView.invalidate();
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		sendStatus(udpCommunication.createOnlineStatusData(userName, null));

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// sendStatus(udpCommunication.createOfflineStatusData(userName, null));
		deactivateStatusReceiveThread();

		deActivateDataReceiveThread();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.refresh_button:
			sendStatus(udpCommunication.createOnlineStatusData(userName, null));
			break;
		case R.id.file_button:
			showFileChooser();
			break;
		case R.id.server_button:
			deactivateStatusReceiveThread();
			deActivateDataReceiveThread();
			createWifiAccessPoint2(this);

			break;
		default:
			break;
		}

	}

	void sendFile(Bitmap bitmap, User user) {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byteArray = stream.toByteArray();

		sendStatus(udpCommunication.createFileInfoStatusData(userName,
				user.address, "jpg", byteArray.length));

	}

	void activateStatusReceiveThread() {

		deactivateStatusReceiveThread();
		statusAsyncTask = new StatusAsyncTask(this);
		statusAsyncTask.setUdpStatusListener(this);
		statusAsyncTask.setProperties(CommStatus.Receive, null);

		statusAsyncTask.execute();

	}

	void deactivateStatusReceiveThread() {

		if (statusAsyncTask != null) {

			statusAsyncTask.setProperties(CommStatus.Inactive, null);
			statusAsyncTask.cancel(true);
		}

		statusAsyncTask = null;
	}

	void sendStatus(DataFormat data) {

		deactivateStatusReceiveThread();

		statusAsyncTask = new StatusAsyncTask(this);
		statusAsyncTask.setUdpStatusListener(this);

		Log.i("status",
				statusAsyncTask.getStatus() + ""
						+ data.receiverIp.getHostAddress());

		statusAsyncTask.setProperties(CommStatus.Send, data);

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				// Do something after 100ms
				if (statusAsyncTask != null)
					if (statusAsyncTask.getStatus() != AsyncTask.Status.RUNNING)
						statusAsyncTask.execute();
			}
		}, 0);

	}

	void activateDataReceiveThread(DataFormat data, int size) {

		deActivateDataReceiveThread();

		dataAsyncTask = new DataAsyncTask(this);
		dataAsyncTask.setUdpDataListener(this);
		dataAsyncTask.setProperties(CommStatus.Receive, data, size);

		dataAsyncTask.execute();

	}

	void deActivateDataReceiveThread() {
		if (dataAsyncTask != null) {

			dataAsyncTask.setProperties(CommStatus.Inactive, null, 0);
			dataAsyncTask.cancel(true);

		}

		dataAsyncTask = null;
	}

	void sendData(byte[] byteArray, User user) {

		deActivateDataReceiveThread();
		deactivateStatusReceiveThread();

		dataAsyncTask = new DataAsyncTask(this);
		dataAsyncTask.setUdpDataListener(this);

		dataAsyncTask.setProperties(CommStatus.Send,
				udpCommunication.createFileData(userName, user, byteArray),
				byteArray.length);

		dataAsyncTask.execute();

	}

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select an Image to Upload"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {

				Uri imageUri = data.getData();

				try {
					Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(
							this.getContentResolver(), imageUri);

					bitmap = Bitmap
							.createScaledBitmap(bitmap2, 120, 120, false);

					imageView.setImageBitmap(bitmap);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub

		if (bitmap != null) {
			selectedUser = userArrayList.get(position);
			sendFile(bitmap, selectedUser);

		} else {

		}
	}

	@Override
	public void statusReceived(final DataFormat dataFormat) {

		Log.i("ip", "" + dataFormat.receiverIp.getHostAddress());

		if (dataFormat.statusType == StatusType.OnlineAcivity) {

			User user = new User();

			user.address = dataFormat.senderIp;
			user.userName = dataFormat.senderName;

			if (Integer.valueOf(dataFormat.status) == OnlineStatus.Online
					.ordinal()) {
				Boolean present = false;
				for (User exuser : userArrayList) {

					if (exuser.address.getHostAddress().equals(
							user.address.getHostAddress())) {

						present = true;
					}

				}

				if (!present) {
					userArrayList.add(user);
					sendStatus(udpCommunication.createOnlineStatusData(
							userName, null));
				}

			} else {

				int i = 0;
				for (User exuser : userArrayList) {

					if (exuser.address == user.address) {

						userArrayList.remove(i);
					}
					i++;
				}

			}

		} else if (dataFormat.statusType == StatusType.FileInfo) {

			this.dataFormat = dataFormat;
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// Do something after 100ms

					sendStatus(udpCommunication.createFileReceiveReadyStatus(
							userName, dataFormat.senderIp));
				}
			}, 10);

		} else if (dataFormat.statusType == StatusType.FileReceiverReady
				&& isSender) {

			Log.i("ready", "y");
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					fileStatusTextView.setText("File is sending to "
							+ dataFormat.receiverName);
				}
			});
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// Do something after 100ms
					sendData(byteArray, selectedUser);
				}
			}, 200);

		}
		loadUsersListView();
	}

	@Override
	public void fileReceived(final DataFormat dataFormat) {
		// TODO Auto-generated method stub

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bitmap bitmap = BitmapFactory.decodeByteArray(dataFormat.file,
						0, dataFormat.file.length);

				int l = dataFormat.file.length;

				Log.i("file", "" + l);
				
				for(int i = 0; i< dataFormat.file.length;i++){
					
					
//					System.out.println("");
					
				}

				imageView.setImageBitmap(bitmap);

				fileStatusTextView.setText("File is received from "
						+ dataFormat.senderName);
			}
		});

		deActivateDataReceiveThread();
		activateStatusReceiveThread();

	}

	@Override
	public void statusSentSuccessfully(StatusType statusType) {
		// TODO Auto-generated method stub

		if (statusType == StatusType.FileReceiverReady) {
			final int bytesize = Integer
					.valueOf(dataFormat.status.split(":")[1]);

			Log.i("byte size", "" + bytesize);
			deactivateStatusReceiveThread();
			activateDataReceiveThread(dataFormat, bytesize);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					fileStatusTextView.setText("File (image size: " + bytesize
							+ "byte) is receiving from "
							+ dataFormat.senderName);
				}
			});

		} else if (statusType == StatusType.FileInfo) {

			isSender = true;

			activateStatusReceiveThread();
		} else {

			activateStatusReceiveThread();
		}

	}

	@Override
	public void statusSentError() {
		// TODO Auto-generated method stub
		activateStatusReceiveThread();
	}

	@Override
	public void fileSentSuccessfully() {
		// TODO Auto-generated method stub
		isSender = false;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				fileStatusTextView.setText("File sending complete");
			}
		});

		deActivateDataReceiveThread();
		activateStatusReceiveThread();
	}

	@Override
	public void fileSentError() {
		// TODO Auto-generated method stub
		isSender = false;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				fileStatusTextView.setText("File sending error");
			}
		});

		deActivateDataReceiveThread();
		activateStatusReceiveThread();
	}

	@Override
	public void fileReceivedError() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				fileStatusTextView.setText("File receiving error");
			}
		});
		deActivateDataReceiveThread();
		activateStatusReceiveThread();
	}

}
