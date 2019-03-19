package customchat.chat;

class RoomFullException extends ChatException {
  RoomFullException(String s) {
	super(s);
  }  
}
