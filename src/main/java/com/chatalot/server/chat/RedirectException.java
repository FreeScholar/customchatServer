package customchat.chat;

public class RedirectException extends ChatException {
  String link;

  public RedirectException(String location) {
	link = "Location: " + location;
  }  
  public String toString() {
	return link;
  }  
}
