package org.research.chatclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessagesTable extends SQLiteOpenHelper implements Constants{
	
	private static final String DATABASE_NAME = "messages.db" ;
	private static final int DATABASE_VERSION = 1;
	
	/** Create a helper object for the Events database */
	public MessagesTable( Context ctx ) {
		super( ctx, DATABASE_NAME, null, DATABASE_VERSION );
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE " + MESSAGE_TABLE_NAME + " (" + _ID
	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+ SENDER + " TEXT, " + RECIPIENT + " TEXT, " + OTHER_MEMBER + " TEXT, " + MESSAGE + " TEXT, " + TIMESTAMP + " TEXT);" );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
	int newVersion) {
	db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
	onCreate(db);
	}
}
