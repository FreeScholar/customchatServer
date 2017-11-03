package customchat.chat;

public class BadLanguageException extends ChatException {

  public BadLanguageException(String word) {
	super("You can't say " + word + " in this room.");
  }  
}
