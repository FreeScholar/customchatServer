package customchat.chat;

class ChatterNotFoundException extends ChatException {
  ChatterNotFoundException() {
	super();
  }  
  ChatterNotFoundException(String s) {
	super(s);
  }  
}
