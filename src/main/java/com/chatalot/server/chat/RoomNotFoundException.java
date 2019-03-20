package com.chatalot.server.chat;

class RoomNotFoundException extends ChatException {
  RoomNotFoundException(String s) {
	super(s);
  }  
}
