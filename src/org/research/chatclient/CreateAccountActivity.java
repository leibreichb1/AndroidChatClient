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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateAccountActivity extends Activity {
    
	private ProgressDialog mProgress;
	private SharedPreferences mPrefs;
	public static String PREFS = "prefs_file";
	public static String C2DM = "C2DM";
	public static String CREATED = "created";
	public static String USER = "user";
	private String username;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
		boolean hasCreated = mPrefs.getBoolean( CREATED, false );
		
		if(!hasCreated){
	        Button createLogin = (Button)findViewById(R.id.button_create_login);
	        
	        // Create Login button listener
	        createLogin.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
					String c2dm = mPrefs.getString( CreateAccountActivity.C2DM, "" );
					
					final EditText userText = (EditText)findViewById(R.id.username);
					username = userText.getText().toString();
					if(username != "" && c2dm != ""){
				    	try{
				    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/TestPhp/CreateNewUser.php");
				    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
				    		
				    		nameValuePairs.add(new BasicNameValuePair("username", username));
				    		nameValuePairs.add(new BasicNameValuePair("OS", "Android"));
				    		nameValuePairs.add(new BasicNameValuePair("deviceID", c2dm));
				    		
				    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				    		
				    		mProgress = new ProgressDialog(CreateAccountActivity.this);
						    mProgress.setIndeterminate(true);
						    mProgress.setCancelable(false);
						    mProgress.setMessage("Downloading...");
						    mProgress.show();
						    
						    new DownloadFilesTask().execute(httppost);
				    		
				    	}catch(UnsupportedEncodingException e){
				    		e.printStackTrace();
				    	}
					}
				}
			});
	        
	        SharedPreferences sharedPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
			String c2dm = sharedPrefs.getString( CreateAccountActivity.C2DM, "" );
	        if(c2dm.equals("")){
		        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		        registrationIntent.putExtra("app", PendingIntent.getBroadcast( this, 0, new Intent(), 0));
		        registrationIntent.putExtra("sender", "leibreichb2@gmail.com" );
		        startService(registrationIntent);
	        }
		}
		else{
			Intent inboxIntent = new Intent(CreateAccountActivity.this, InboxActivity.class);
	    	startActivity(inboxIntent);
	    	finish();
		}
    }
    
    private class DownloadFilesTask extends AsyncTask<HttpPost, Void, InputStream> {
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
	    	 
	    	 if(mProgress.isShowing())
	    		 mProgress.dismiss();
	    	 Log.d("Response", text);
	    	 if(text.trim().equals("CREATED")){
	    		 Editor editor = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE).edit();
	    		 editor.putBoolean(CREATED, true);
	    		 editor.putString(USER, username);
	    		 editor.commit();
	    		 Intent inboxIntent = new Intent(CreateAccountActivity.this, InboxActivity.class);
	    		 startActivity(inboxIntent);
	    		 finish();
	    	 }
	    	 else if(text.equals("EXISTS")){
	    		 AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccountActivity.this);
	    		 builder.setTitle("Account Exists");
	    		 builder.setMessage("That account name already exists, please choose a new username.");
	    		 builder.setPositiveButton("DONE", null);
	    	 }
	     }
	 }
}