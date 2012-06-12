package org.research.chatclient;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
	
	public static final String MESSAGE_TABLE_NAME = "Messages";
	
	// Columns in the Events database
	public static final String SENDER = "sender";
	public static final String RECIPIENT = "recipient";
	public static final String MESSAGE = "message";
	public static final String TIMESTAMP = "timestamp";
}