package com.chatalot.server.chat;

class RoomFullException extends ChatException {
  RoomFullException(String s) {
	super(s);
  }  
}
