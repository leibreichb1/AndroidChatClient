package org.research.chatclient;

public class InboxItem {

	private String name;
	private String mostRecentMessage;
	private String recentTime;
	
	public InboxItem(String name, String mostRecentMessage, String recentTime){
		this.name = name;
		this.mostRecentMessage = mostRecentMessage;
		this.recentTime = recentTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMostRecentMessage() {
		return mostRecentMessage;
	}

	public void setMostRecentMessage(String mostRecentMessage) {
		this.mostRecentMessage = mostRecentMessage;
	}

	public String getRecentTime() {
		return recentTime;
	}

	public void setRecentTime(String recentTime) {
		this.recentTime = recentTime;
	}
}
