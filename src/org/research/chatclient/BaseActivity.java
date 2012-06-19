package org.research.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

public class BaseActivity extends Activity implements Constants{
	
	private SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MessagesTable table = new MessagesTable(BaseActivity.this);
		db = table.getWritableDatabase();
	}
	protected class SendMessageTask extends AsyncTask<HttpPost, Void, InputStream> {
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
	    	 Toast.makeText(BaseActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
	    	 Log.d("Response", "TIME: " + text);
	     }
	 }
	
	protected void insertMessage(String sender, String message, String time){
		String recipient = (String)((Spinner)findViewById(R.id.personSpin)).getSelectedItem();
		
		ContentValues values = new ContentValues();
		values.put(SENDER, sender);
		values.put(RECIPIENT, recipient);
		values.put(OTHER_MEMBER, recipient);
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
