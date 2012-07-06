package org.research.chatclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;

public class C2DMReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
	        handleRegistration(context, intent);
	    } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
	        handleMessage(context, intent);
	    }
	 }

	private void handleRegistration(final Context context, Intent intent) {
	    final String registration = intent.getStringExtra("registration_id");
	    if (registration != null) {
	    	Editor editor = context.getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE).edit();
            editor.putString(CreateAccountActivity.C2DM, registration);
    		editor.commit();
	    }
	}

	private void handleMessage(Context context, Intent intent)
	{
		NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(android.R.drawable.stat_notify_voicemail, "Chat Client", System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_ALL;
		PendingIntent pendIntent = PendingIntent.getActivity( context, 0, new Intent(context, InboxActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(context, "Chat Client", "You have a new Message", pendIntent );
		mManager.notify(0, notification);
	}
}
