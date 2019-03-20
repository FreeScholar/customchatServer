package com.chatalot.server.chat;

import java.util.*;
import java.io.*;
import com.chatalot.server.util.* ;

// For java 1.1 compatablility
//import com.sun.java.util.collections.*;


/**
 * This class wakes up every 60 sec and removes all chatters idle for more than
 * 10 minutes.
 *
 * @author CustomChat Server
 * @version 1.5
 */
class TimeOut extends Thread implements Serializable {
    private Floor fFloor;
    private static long lTimeOut;
    static {
	try {
	    lTimeOut = Long.parseLong(System.getProperty("chat.timeout"));
	} catch (Exception e) {
	    lTimeOut = 600000;  //10 min * 60 secs * 1000 ms
	}
    }
    TimeOut() {
	this.setDaemon(true);
	this.start();
    }
    public synchronized void run() {
	Date dNow;
	long expires;
	Chatter c;
	Enumeration i;

	while(true) {
	    try { sleep(60000); } catch (InterruptedException ignored) {;}
	    try {
		com.chatalot.server.util.Timer.start();
		i = UserRegistry.getChatters();
		expires = new Date().getTime() - lTimeOut;

		while(i.hasMoreElements()) {
		    c = (Chatter)i.nextElement();
		    if((c != null) && (c.GetLastCheck().getTime() <  expires)) {
			try {
			    UserRegistry.endSession(c.sHandle);
			} catch(ChatException ex) {
			    ErrorLog.error(ex, 310, "Error timing out user.");
			}
		    }
		}
		//Debug.println("timeout: " + String.valueOf(customchat.util.Timer.stop()));
	    } catch(Exception ex) {
		ErrorLog.error(ex, 310, "Ignored in time out thread.");
	    }
	}
    }
}
