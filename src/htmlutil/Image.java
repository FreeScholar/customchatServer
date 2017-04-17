package customchat.htmlutil;

public class Image extends Tag {
  public static final String name = "IMG";
  public Image(String URL) {
	super(name);
	if(URL == null) throw new NullPointerException("Cannot Create an Image with null for a URL.");
	this.addArgument("SRC",URL);
	addArgument("BORDER", "0");
  }  
  public Image(String URL, String alt) {
	this(URL);
	addArgument("ALT", alt);
  }  
}
