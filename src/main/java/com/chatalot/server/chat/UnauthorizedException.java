package com.chatalot.server.chat;

public class UnauthorizedException extends ChatException {
  public UnauthorizedException(String s) {
	super(s);
  }  
}
