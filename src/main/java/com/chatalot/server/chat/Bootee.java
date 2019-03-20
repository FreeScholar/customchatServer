package com.chatalot.server.chat;

//import com.sun.java.util.collections.*;
import com.chatalot.server.htmlutil.*;
import com.chatalot.server.util.*;
import java.util.Calendar;
import java.util.LinkedList;

class Bootee extends User {
	// added by ritchie
	 static final long serialVersionUID = 5892873866212750857L;
	private static final String EXPIRES = "Expires";
	private static final String HANDLE  = "Handle";
	private static final String MESSAGE = "Message";
	private static final String DELETE   = "Delete";
	private String prefix;

	public String IP = null ;

	Bootee(Login l, String handle, Calendar until, String message) {
		super(l, new LinkedList());
		prefix = l.getLogin() + String.valueOf(l.hashCode());

		inputFields.add(new TextField(l.getLogin()));
		inputFields.add(new TextField(handle));
		inputFields.add(new DateField(prefix + EXPIRES, until));
		inputFields.add(new InputField(prefix + MESSAGE, message));
		inputFields.add(new CheckBoxField(prefix + DELETE, false));
	}
	public boolean deleted() {
	  return ((Boolean)((InputField)inputFields.get(4)).getValue()).booleanValue()
			  || hasExpired();
	}
	public Calendar expires() {
		return (Calendar)((InputField)inputFields.get(2)).getValue();
	}
	public boolean hasExpired() {
		return expires().before(Calendar.getInstance());
	}
}
