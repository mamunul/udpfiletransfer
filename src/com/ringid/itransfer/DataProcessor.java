package com.ringid.itransfer;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;

import com.ringid.itransfer.DataFormat.OnlineStatus;
import com.ringid.itransfer.DataFormat.StatusType;

public class DataProcessor {

	enum CommStatus {

		Activate, Send, Receive, Inactive

	};

	public DataProcessor(Context context) {

		// TODO Auto-generated constructor stub
	}

	public DataFormat createOnlineStatusData(String fromUserName, byte[] data) {

		return sendStatus(fromUserName, OnlineStatus.Online);

	}

	private InetAddress getBroadcast(InetAddress inetAddr) {

		NetworkInterface temp;
		InetAddress iAddr = null;
		try {
			temp = NetworkInterface.getByInetAddress(inetAddr);
			List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

			for (InterfaceAddress inetAddress : addresses)

				iAddr = inetAddress.getBroadcast();
			// Log.d(TAG, "iAddr=" + iAddr);
			return iAddr;

		} catch (SocketException e) {

			e.printStackTrace();
			// Log.d(TAG, "getBroadcast" + e.getMessage());
		}
		return null;
	}

	private DataFormat sendStatus(String fromUserName, OnlineStatus onlineStatus) {
		DataFormat dataFormat = new DataFormat();

		dataFormat.senderName = fromUserName;
		dataFormat.senderIp = getIpAddress();

		dataFormat.receiverIp = getBroadcast(getIpAddress());

		dataFormat.statusType = StatusType.OnlineAcivity;
		dataFormat.status = String.valueOf(onlineStatus.ordinal());

		return dataFormat;

	}

	public DataFormat createOfflineStatusData(String fromUserName, byte[] data) {

		return sendStatus(fromUserName, OnlineStatus.Offline);
	}

	public DataFormat createFileInfoStatusData(String fromUserName,
			InetAddress toUserIp, String fileFormat, int fileSize) {
		DataFormat dataFormat = new DataFormat();

		dataFormat.senderName = fromUserName;
		dataFormat.senderIp = getIpAddress();

		dataFormat.receiverIp = toUserIp;

		dataFormat.statusType = StatusType.FileInfo;

		dataFormat.status = fileFormat + ":" + fileSize;

		return dataFormat;
	}

	public DataFormat createFileReceiveReadyStatus(String fromUserName,
			InetAddress toUserIp) {

		DataFormat dataFormat = new DataFormat();

		dataFormat.senderName = fromUserName;
		dataFormat.senderIp = getIpAddress();

		dataFormat.receiverIp = getBroadcast(getIpAddress());

		dataFormat.statusType = StatusType.FileReceiverReady;

		dataFormat.status = "9";

		return dataFormat;

	}

	public DataFormat createFileData(String fromUserName, User user, byte[] data) {
		DataFormat dataFormat = new DataFormat();

		dataFormat.senderName = fromUserName;
		dataFormat.senderIp = getIpAddress();

		dataFormat.receiverIp = user.address;

		dataFormat.statusType = StatusType.File;
//		dataFormat.receiverName = user.userName;

		dataFormat.file = data;
		return dataFormat;
	}

	private InetAddress getIpAddress() {
		InetAddress myAddr = null;

		try {
			for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
					.getNetworkInterfaces(); networkInterface.hasMoreElements();) {

				NetworkInterface singleInterface = networkInterface
						.nextElement();

				for (Enumeration<InetAddress> IpAddresses = singleInterface
						.getInetAddresses(); IpAddresses.hasMoreElements();) {
					InetAddress inetAddress = IpAddresses.nextElement();

					if (!inetAddress.isLoopbackAddress()
							&& (singleInterface.getDisplayName().contains(
									"wlan0")
									|| singleInterface.getDisplayName()
											.contains("eth0") || singleInterface
									.getDisplayName().contains("ap0"))) {

						myAddr = inetAddress;
					}
				}
			}

		} catch (SocketException ex) {
		}
		return myAddr;
	}

}
