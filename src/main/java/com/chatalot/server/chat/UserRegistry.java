package com.chatalot.server.chat;

import java.util.*;
import com.chatalot.server.util.*;

public abstract class UserRegistry {
    public static Hashtable accounts = new Hashtable();
    private static Hashtable sessions = new Hashtable();
    private static boolean NODATABASE = true;
    private static TimeOut to = new TimeOut();

    public static Chatter endSession(String handle)
	throws ChatException {
	Chatter c = (Chatter)sessions.remove(handle);
	if(c != null) {
	    Account a = (Account)accounts.get(c.getLogin());
	    if(a != null) {
		int active = a.endSession(handle);
		if( active <= 0)
		    accounts.remove(c.getLogin());
	    }
	}
	return c;
    }  

    private static Account getAccount(Login l)
	throws ChatException, DataBaseException {
	if (l == null)
	    throw new UnauthorizedException("Please enter your username and password.");
	// if an account already exists, get it.
	Account a = (Account)accounts.get(l);

	// if not there get account from database
	if(a == null) {
	    a = new Account(l);
	    accounts.put(l, a);
	}

	// Check to see if l owns the account
	if(!a.isOwnedBy(l))
	    throw new UnauthorizedException("You cannot access that account.");

	return a;
    }  

    public static Chatter getChatter(Login l, String handle, ChatObject destination)
	throws ChatException, DataBaseException {
	// get the account
      	Account a = getAccount(l); // checks for owner ship
	Chatter cExists = (Chatter) sessions.get(handle);
	if(cExists != null)
	    if( !cExists.verify (l, handle))
		throw new InvalidHandleException("That handle is already in use.");
	//else
	//return cExists;

	Chatter c = a.makeChatter(handle, destination);
	sessions.put(handle, c);

	return c;
    }  

    public static Chatter getChatter(String handle) {
	return (Chatter)sessions.get(handle);
    }  

    public static Enumeration getChatters() {
	return sessions.elements();
    }  

    public static String[] getHandles(Login l)
	throws ChatException, DataBaseException {
	    Account a = getAccount(l);
	    if(a == null)
	    throw new ChatException("That user was not found.");

	    return a.getHandles();
	}  

    public static Login getLogin(String handle) {
	Chatter c = getChatter(handle);
	if(c == null) return null;
	return c.getLogin();
    }  

    public static void sendMessage(Message m) {
	String[] recips = m.recips();
	Chatter c;
	for(int i = 0; i < recips.length; i++) {
	    c = (Chatter)sessions.get(recips[i]);
	    if (c == null) {
		if (m.getFrom() != null)
		    sendMessage(new Message("<FONT FACE=Arial, Helvetica>Your Message was "
					    + "not received by " + recips[i]
					    + "because they have logged out!</FONT>",
					    House.ctDaemon,
					    m.getFrom(),
					    Message.SYSTEM));
	    } else
		    c.addMessage(m);
	}
    }  
}
