package com.ringid.itransfer;

import android.content.Context;
import android.os.AsyncTask;

import com.ringid.itransfer.DataFormat.StatusType;
import com.ringid.itransfer.DataProcessor.CommStatus;
import com.ringid.itransfer.UDPProtocol.UDPProtocolListener;

class StatusAsyncTask extends AsyncTask<String, Integer, Boolean> implements
		UDPProtocolListener {

	private UDPProtocol dataSend;
	private CommStatus comStatus;
	private DataFormat data;
	private UDPStatusListener udpStatusListener;

	public interface UDPStatusListener {
		void statusReceived(DataFormat dataFormat);

		void statusSentSuccessfully(StatusType statusType);

		void statusSentError();

	}

	public void setProperties(CommStatus comStatus, DataFormat data) {
		this.comStatus = comStatus;
		this.data = data;

	}

	public StatusAsyncTask(Context context) {
		// TODO Auto-generated constructor stub

		dataSend = new UDPProtocol(context);

		dataSend.setuDPProtocolListener(this);
	}

	@Override
	protected Boolean doInBackground(String... params) {

		byte[] recvBuf = new byte[DataFormat.DefaultDataSize];

		while (comStatus == CommStatus.Receive) {
			dataSend.receiveDataUDP(recvBuf, UDPProtocol.StatusPort, 300);
		}

		if (comStatus == CommStatus.Send) {

			dataSend.sendDataUDP(data.receiverIp, data.toByte(),
					UDPProtocol.StatusPort);

		}

		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		if (dataSend != null)
			dataSend.closeConnection();
		super.onCancelled();
	}

	@Override
	protected void onCancelled(Boolean result) {
		// TODO Auto-generated method stub
		super.onCancelled(result);

	}

	public void setUdpStatusListener(UDPStatusListener udpStatusListener) {
		this.udpStatusListener = udpStatusListener;
	}

	@Override
	public void onDataReceived(DataFormat dataFormat) {
		// TODO Auto-generated method stub
		udpStatusListener.statusReceived(dataFormat);

	}

	@Override
	public void onDataSent() {
		// TODO Auto-generated method stub
		udpStatusListener.statusSentSuccessfully(data.statusType);
	}

	@Override
	public void onDataSentError() {
		// TODO Auto-generated method stub
		udpStatusListener.statusSentError();
	}

	@Override
	public void onDataReceiveError() {
		// TODO Auto-generated method stub

	}

}