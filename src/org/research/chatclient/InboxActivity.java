package org.research.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InboxActivity extends Activity{
	
	private ProgressDialog mProgress;
	private SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_list);
		
		try{
    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/TestPhp/getMessages.php");
    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
    		
    		mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
    		String username = mPrefs.getString( CreateAccountActivity.USER, "" );
    		String c2dm = mPrefs.getString( CreateAccountActivity.C2DM, "" );
    		
    		nameValuePairs.add(new BasicNameValuePair("username", username));
    		nameValuePairs.add(new BasicNameValuePair("deviceID", c2dm));
    		
    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    		
    		mProgress = new ProgressDialog(InboxActivity.this);
		    mProgress.setIndeterminate(true);
		    mProgress.setCancelable(false);
		    mProgress.setMessage("Downloading...");
		    mProgress.show();
		    
		    new DownloadFilesTask().execute(httppost);
    		
    	}catch(UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
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
	     }
	 }
}
