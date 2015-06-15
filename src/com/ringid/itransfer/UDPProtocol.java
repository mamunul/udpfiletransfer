package com.ringid.itransfer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class UDPProtocol {

	public interface UDPProtocolListener {

		void onDataReceived(DataFormat dataFormat);

		void onDataReceiveError();

		void onDataSent();

		void onDataSentError();

	}

	Context context;

	private UDPProtocolListener uDPProtocolListener;

	final public static int StatusPort = 8887;
	final public static int DataPort = 8888;

	DatagramSocket sendDatagramSocket = null;
	DatagramSocket receiveDatagramSocket = null;

	public UDPProtocol(Context context) {
		// TODO Auto-generated constructor stub

		this.context = context;
	}

	Boolean isSelfHotSpot = false;
	final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {

			WifiManager mWifiManager = (WifiManager) c
					.getSystemService(Context.WIFI_SERVICE);

			if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
				List<ScanResult> mScanResults = mWifiManager.getScanResults();

				for (ScanResult scanResult : mScanResults) {

					System.out.println(scanResult.BSSID + ":"
							+ scanResult.level + ":" + scanResult.SSID + ":"
							+ scanResult.capabilities + ":");

				}

				// add your logic here
			}
		}
	};

	public void discoverNetworks(Context context) {
		final WifiManager mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		context.registerReceiver(mWifiScanReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		mWifiManager.startScan();

	}

	public void createWifiAccessPoint2(Context context) {

		isSelfHotSpot = true;
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
				netConfig.SSID = "\"" + "TinyBox" + "\"";
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

					} else {
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

	public void connectWithHotspot(Context context) {
		WifiConfiguration conf = new WifiConfiguration();
		conf.SSID = "\"\"" + "TinyBox" + "\"\"";
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.addNetwork(conf);

		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration i : list) {
			if (i.SSID != null && i.SSID.equals("\"\"" + "TinyBox" + "\"\"")) {
				try {
					wifiManager.disconnect();
					wifiManager.enableNetwork(i.networkId, true);
					System.out.print("i.networkId " + i.networkId + "\n");
					wifiManager.reconnect();
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	public void sendDataUDP(InetAddress toIpAddress, byte[] data, int port) {

		Boolean errorOccured = false;

		Log.i("send", "" + new String(data));

		try {
			sendDatagramSocket = new DatagramSocket(port);

			sendDatagramSocket.setBroadcast(true);
			DatagramPacket sendPacket = null;

			sendPacket = new DatagramPacket(data, data.length, toIpAddress,
					port);

			try {
				sendDatagramSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				errorOccured = true;
				e.printStackTrace();
			} finally {
				sendDatagramSocket.close();

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			errorOccured = true;

			e.printStackTrace();
		}
		if (!errorOccured)
			uDPProtocolListener.onDataSent();
		else
			uDPProtocolListener.onDataSentError();

	}

	public DataFormat receiveDataUDP(byte[] recvBuf, int port, int timeOut) {

		Boolean errorOccured = false;

		DataFormat dataFormat = new DataFormat();
		try {
			receiveDatagramSocket = new DatagramSocket(port);

			if (timeOut > 0)
				receiveDatagramSocket.setSoTimeout(timeOut);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			errorOccured = true;
			e1.printStackTrace();
		}

		DatagramPacket receivePacket = new DatagramPacket(recvBuf,
				recvBuf.length);

		try {

			receiveDatagramSocket.receive(receivePacket);
			int length = receivePacket.getLength();
			byte[] data = Arrays.copyOf(receivePacket.getData(), length);
			
			Log.i("receive udp data",""+length+":"+recvBuf.length+":"+port);

			dataFormat.toObject(data, 0);

		} catch (IOException e) {
			errorOccured = true;
			e.printStackTrace();
		} finally {

			receiveDatagramSocket.close();
		}

		if (!errorOccured)
			uDPProtocolListener.onDataReceived(dataFormat);
		else
			uDPProtocolListener.onDataReceiveError();

		return dataFormat;

	}

	public void refresh(Context context) {

		context.unregisterReceiver(mWifiScanReceiver);

	}

	public void setuDPProtocolListener(UDPProtocolListener uDPProtocolListener) {
		this.uDPProtocolListener = uDPProtocolListener;
	}

	public void closeConnection() {

		if (receiveDatagramSocket != null) {
			receiveDatagramSocket.disconnect();
			receiveDatagramSocket.close();
		}

	}

}
