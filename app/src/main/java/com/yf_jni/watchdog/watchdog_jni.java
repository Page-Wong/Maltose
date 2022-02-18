package com.yf_jni.watchdog;

public class watchdog_jni {
	private static native int open();
	private static native boolean set_watchdog_on(int fd);
	private static native boolean set_watchdog_off(int fd);
	private static native boolean feed_watchdog(int fd);
	private static native boolean set_watchdog_timeout(int fd, int time);
	static {          
		System.loadLibrary("watchdog_jni");      
	}    
	public int getfd(){
		return open();
	}
	
	public boolean EnableWatchdog(int fd){
		return set_watchdog_on(fd);
	}
	
	public boolean DisableWatchdog(int fd){
		return set_watchdog_off(fd);
	}
	
	public boolean FeedWatchdog(int fd){
		return feed_watchdog(fd);
	}
	
	public boolean SetWatchdogTime(int fd, int time){
		return set_watchdog_timeout(fd,time);
	}
}
