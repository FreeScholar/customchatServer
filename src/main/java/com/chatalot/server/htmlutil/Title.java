package customchat.htmlutil;

public class Title extends Container {
  public static final String name = "TITLE";

  public Title(String title) {
	super(name);
	if(title == null) throw new NullPointerException("Cannot construct a Title with null for a name.");
	addHTML(new Text(title));
  }  
}
