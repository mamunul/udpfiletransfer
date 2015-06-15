package com.ringid.itransfer;

import android.content.Context;
import android.os.AsyncTask;

import com.ringid.itransfer.DataProcessor.CommStatus;
import com.ringid.itransfer.UDPProtocol.UDPProtocolListener;

public class DataAsyncTask extends AsyncTask<String, Integer, Boolean>
		implements UDPProtocolListener {

	private UDPProtocol dataSend;
	private CommStatus comStatus;
	private DataFormat data;
	private int dataSize;

	private UDPDataListener udpDataListener;

	public interface UDPDataListener {

		void fileReceived(DataFormat dataFormat);

		void fileReceivedError();

		void fileSentSuccessfully();

		void fileSentError();

	}

	public void setProperties(CommStatus comStatus, DataFormat data,
			int dataSize) {
		this.comStatus = comStatus;
		this.data = data;
		this.dataSize = dataSize;

	}

	public DataAsyncTask(Context context) {
		// TODO Auto-generated constructor stub
		dataSend = new UDPProtocol(context);
		dataSend.setuDPProtocolListener(this);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		byte[] recvBuf = new byte[dataSize + DataFormat.DefaultDataSize];
		if (comStatus == CommStatus.Receive) {

			dataSend.receiveDataUDP(recvBuf, UDPProtocol.DataPort, 5000);

		}
		if (comStatus == CommStatus.Send) {

			dataSend.sendDataUDP(data.receiverIp, data.toByte(),
					UDPProtocol.DataPort);

		}

		return null;
	}

	public void setUdpDataListener(UDPDataListener udpDataListener) {
		this.udpDataListener = udpDataListener;
	}

	@Override
	public void onDataReceived(DataFormat dataFormat) {
		// TODO Auto-generated method stub
		udpDataListener.fileReceived(dataFormat);
	}

	@Override
	public void onDataSent() {
		// TODO Auto-generated method stub
		udpDataListener.fileSentSuccessfully();
	}

	@Override
	public void onDataSentError() {
		// TODO Auto-generated method stub
		udpDataListener.fileSentError();
	}

	@Override
	public void onDataReceiveError() {
		// TODO Auto-generated method stub
		udpDataListener.fileReceivedError();
	}

}
