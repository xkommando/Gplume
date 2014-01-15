package com.caibowen.gplume.event;

public class AppStartedEvent extends AppEvent {

	private static final long serialVersionUID = 6239892164123250715L;
	
	private long time;

	public AppStartedEvent(Object arg0) {
		super(arg0);
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
