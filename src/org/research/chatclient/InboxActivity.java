package org.research.chatclient;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InboxActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_list);
	}
	
	class ContactAdapter extends BaseAdapter{
		
		ArrayList<InboxItem> mList;
		
		
		
		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			
			if( v == null ){
				LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.contact_item, null);
			}
			
			InboxItem temp = mList.get(position);
			TextView name = (TextView)findViewById(R.id.contact_name);
			TextView msg = (TextView)findViewById(R.id.contact_recent_msg);
			TextView time = (TextView)findViewById(R.id.contact_time);
			
			name.setText(temp.getName());
			msg.setText(temp.getMostRecentMessage());
			time.setText(temp.getRecentTime());
			
			return v;
		}
		
	}
}
