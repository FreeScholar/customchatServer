package com.chatalot.server.chat;

class InvalidRoomPassException extends ChatException {
  InvalidRoomPassException(String s){
	super(s);
  }  
}
