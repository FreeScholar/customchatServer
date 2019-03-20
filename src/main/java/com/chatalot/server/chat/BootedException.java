package com.chatalot.server.chat;

import java.util.*;

public class BootedException extends ChatException {
  public BootedException(Calendar c, String message) {
	super("You have been booted until " + c.getTime() + "!"
		+ ((message != null)
		  ? ("<br>Reason : " + message )
		  : ""));
  }  
}
