package com.ringid.itransfer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.itransfer.R;

public class UserAdapter extends ArrayAdapter<User> {
	private Context context;
	private List<User> usersList;

	public UserAdapter(Context context, int resource, List<User> usersList) {
		super(context, resource, usersList);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.usersList = usersList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = inflater.inflate(R.layout.user_item_layout, null);
		}

		TextView userNameTextView = (TextView) convertView
				.findViewById(R.id.username_textview);

		userNameTextView.setText(usersList.get(position).userName);

		return convertView;
	}

}
