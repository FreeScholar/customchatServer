package com.chatalot.server.chat;

public class BadLanguageException extends ChatException {

  public BadLanguageException(String word) {
	super("You can't say " + word + " in this room.");
  }  
}
