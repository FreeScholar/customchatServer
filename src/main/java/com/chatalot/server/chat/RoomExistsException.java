package com.chatalot.server.chat;

class RoomExistsException extends ChatException{
  RoomExistsException(String s) {
	super(s);
  }  
}
