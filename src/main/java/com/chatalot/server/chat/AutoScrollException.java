package customchat.chat;

public class AutoScrollException extends customchat.chat.ChatException {
  customchat.chat.Chatter c;

  AutoScrollException(customchat.chat.Chatter c) {
	this.c = c;
  }  
  public customchat.chat.Chatter getChatter() {
	return c;
  }  
}
