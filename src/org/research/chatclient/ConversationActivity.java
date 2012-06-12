package org.research.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ConversationActivity extends Activity implements Constants{
	
	private SQLiteDatabase db;
	private ProgressDialog mProgress;
	private SharedPreferences mPrefs;
	private JSONArray mUsers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		MessagesTable table = new MessagesTable(ConversationActivity.this);
		db = table.getWritableDatabase();
		mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
		mProgress = new ProgressDialog(ConversationActivity.this);
	    mProgress.setIndeterminate(true);
	    mProgress.setCancelable(false);
	    mProgress.setMessage("Getting Users...");
	    mProgress.show();
		new GetUsersTask().execute(new HttpGet("http://devimiiphone1.nku.edu/research_chat_client/testphp/get_users.php"));
	}
	
	public void sendMessage(View v){
		
		EditText messBox = (EditText)findViewById(R.id.messageText);
		String message = messBox.getText().toString();
		String sender = mPrefs.getString(CreateAccountActivity.USER, "");
		String time = "" + System.currentTimeMillis();
		if(!message.equals("")){
			insertMessage(sender, message, time);
			try{
	    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/testphp/send_message.php");
	    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
	    		
	    		nameValuePairs.add(new BasicNameValuePair("recipient", "test@test.com"));
	    		nameValuePairs.add(new BasicNameValuePair("sender", sender));
	    		nameValuePairs.add(new BasicNameValuePair("message", message));
	    		nameValuePairs.add(new BasicNameValuePair("time", "" + time));
	    		
	    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			    
			    new SendMessageTask().execute(httppost);
	    		
	    	}catch(UnsupportedEncodingException e){
	    		e.printStackTrace();
	    	}
		}
	}
	
	private class GetUsersTask extends AsyncTask<HttpGet, Void, InputStream> {
	    @Override
		 protected InputStream doInBackground(HttpGet... post) {
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
	    	 
	    	 if(mProgress.isShowing())
	    		 mProgress.dismiss();
	    	 try {
				mUsers = new JSONArray(text);
				String[] users = new String[mUsers.length()];
				for(int i = 0; i < mUsers.length(); i++)
					users[i] = mUsers.getString(i);
				Spinner personSpinner = (Spinner)findViewById(R.id.personSpin);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ConversationActivity.this, android.R.layout.simple_spinner_item, users);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				personSpinner.setAdapter(adapter);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	     }
	 }
	
	private class SendMessageTask extends AsyncTask<HttpPost, Void, InputStream> {
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
	    	 Toast.makeText(ConversationActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
	    	 Log.d("Response", text);
	     }
	 }
	
	private void insertMessage(String sender, String message, String time){
		String recipient = (String)((Spinner)findViewById(R.id.personSpin)).getSelectedItem();
		
		ContentValues values = new ContentValues();
		values.put(SENDER, sender);
		values.put(RECIPIENT, recipient);
		values.put(MESSAGE, message);
		values.put(TIMESTAMP, ""+time);
		db.insert(MESSAGE_TABLE_NAME, null, values);
	}
	
	@Override
	public void onBackPressed() {
		if(db != null && db.isOpen())
			db.close();
		super.onBackPressed();
	}
}

