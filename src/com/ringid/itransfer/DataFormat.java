package com.ringid.itransfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class DataFormat {

	enum StatusType {

		OnlineAcivity, FileInfo, File, FileReceiverReady

	}

	enum OnlineStatus {

		Online, Offline

	}

	public final static int DefaultDataSize = 124;
	final int AppIdPosition = 0;
	final int SenderIpPosition = 12;
	final int SenderNamePosition = 26;
	final int ReceiverIpPosition = 56;
	final int ReceiverNamePosition = 70;
	final int StatusTypePosition = 100;
	final int StatusPosition = 101;

	String appId = "RingTransfer";
	InetAddress senderIp;
	String senderName = "";
	InetAddress receiverIp;
	String receiverName = "";
	StatusType statusType;
	String status = "";

	byte[] file;

	byte[] toByte() {
		byte[] data = null;
		if (statusType != StatusType.File) {
			data = new byte[DefaultDataSize];
		} else {
			data = new byte[DefaultDataSize + file.length];

		}

		byte[] receiverNameBytes = receiverName.getBytes();
		byte[] receiverIpBytes = receiverIp.getHostAddress().getBytes();
		byte[] senderNameBytes = senderName.getBytes();
		byte[] senderIpBytes = senderIp.getHostAddress().getBytes();
		byte[] AppIdBytes = appId.getBytes();

		byte[] statusBytes = status.getBytes();

		byte[] statusTypeBytes = String.valueOf(statusType.ordinal())
				.getBytes();

		System.arraycopy(AppIdBytes, 0, data, AppIdPosition, AppIdBytes.length);

		System.arraycopy(senderIpBytes, 0, data, SenderIpPosition,
				senderIpBytes.length);

		System.arraycopy(senderNameBytes, 0, data, SenderNamePosition,
				senderNameBytes.length);

		System.arraycopy(receiverIpBytes, 0, data, ReceiverIpPosition,
				receiverIpBytes.length);

		System.arraycopy(receiverNameBytes, 0, data, ReceiverNamePosition,
				receiverNameBytes.length);

		System.arraycopy(statusTypeBytes, 0, data, StatusTypePosition,
				statusTypeBytes.length);

		System.arraycopy(statusBytes, 0, data, StatusPosition,
				statusBytes.length);
		if (statusType == StatusType.File) {
			
			System.arraycopy(file, 0, data, StatusPosition, file.length);	
			
		}
		Log.i("data", "" + bytesToHex(data));
		return data;

	}

	public Boolean saveImage(String imageName, byte[] bb) {

		Bitmap bitmap = BitmapFactory.decodeByteArray(bb, 0, bb.length,
				new BitmapFactory.Options());
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
		// ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, dataFormat.file);

		File f = new File(Environment.getExternalStorageDirectory()
				+ File.separator + imageName);
		FileOutputStream fo = null;
		f.delete();

		try {
			f.createNewFile();
			fo = new FileOutputStream(f);

			fo.write(bb);
			fo.flush();
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		// readAllImage();

		return true;
	}

	void toObject(byte[] data, int fileSize) {

		byte[] AppIdBytes = new byte[SenderIpPosition];

		byte[] senderIpBytes = new byte[SenderNamePosition - SenderIpPosition];
		byte[] senderNameBytes = new byte[ReceiverIpPosition
				- SenderNamePosition];
		byte[] receiverIpBytes = new byte[ReceiverNamePosition
				- ReceiverIpPosition];
		byte[] receiverNameBytes = new byte[StatusTypePosition
				- ReceiverNamePosition];
		byte[] statusTypeBytes = new byte[StatusPosition - StatusTypePosition];
		byte[] statusBytes = new byte[data.length - StatusPosition];

		System.arraycopy(data, AppIdPosition, AppIdBytes, 0, AppIdBytes.length);
		System.arraycopy(data, SenderIpPosition, senderIpBytes, 0,
				senderIpBytes.length);
		System.arraycopy(data, SenderNamePosition, senderNameBytes, 0,
				senderNameBytes.length);
		System.arraycopy(data, ReceiverIpPosition, receiverIpBytes, 0,
				receiverIpBytes.length);
		System.arraycopy(data, ReceiverNamePosition, receiverNameBytes, 0,
				receiverNameBytes.length);
		System.arraycopy(data, StatusTypePosition, statusTypeBytes, 0,
				statusTypeBytes.length);
		System.arraycopy(data, StatusPosition, statusBytes, 0,
				statusBytes.length);

		receiverName = new String(receiverNameBytes);
		senderName = new String(senderNameBytes);

		appId = new String(AppIdBytes);

		try {
			receiverIp = InetAddress.getByName(new String(receiverIpBytes)
					.trim());
			senderIp = InetAddress.getByName(new String(senderIpBytes).trim());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		statusType = StatusType.values()[Integer.valueOf(new String(
				statusTypeBytes))];

		status = new String(statusBytes).trim();

		if (statusType == StatusType.File) {
			file = new byte[data.length - DefaultDataSize];
			System.arraycopy(data, StatusPosition, file, 0, file.length);
			
			
			System.out.println("dt"+bytesToHex(file));

		}

	}

	public String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
