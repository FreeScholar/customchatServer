package customchat.htmlutil;

public class Text extends HTML {
  private final String text;
	private boolean bold = false;
	private boolean blinking = false;

  public Text(String text) {
	this.text = text;
  }  
  public String toText() {
	return text;
  }  
}
