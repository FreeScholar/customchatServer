package com.chatalot.server.chat;

public class ShutDownException extends ChatException {
  private boolean bRestart = false;

  ShutDownException(boolean b) {
	super();
	bRestart = b;
  }  
  public boolean restart() {
	return bRestart;
  }  
}
