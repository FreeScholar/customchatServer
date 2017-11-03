package customchat.chat;

public class AutoScrollException extends ChatException {
  Chatter c;

  AutoScrollException(Chatter c) {
	this.c = c;
  }  
  public Chatter getChatter() {
	return c;
  }  
}
