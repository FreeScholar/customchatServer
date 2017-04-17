package customchat.htmlutil;

import java.util.Vector;

public class PullDown extends Container {

  public PullDown(String s) {
	super("SELECT");
	addArgument("NAME", s);
  }  
  public PullDown(String s, Vector v) {
	this(s);
	for (int i = 0; i < v.size(); i++) {
	  addOption((String)v.elementAt(i));
	}
  }  
  public PullDown(String s, Vector v, final String selected) {
	this(s);
	for (int i = 0; i < v.size(); i++) {
	  String entry = (String)v.elementAt(i);
	  addOption(entry, (selected != null) && selected.equals(entry));
	}
  }  
   public PullDown(String s, String[] sa) {
	this(s);
	for (int i = 0; i < sa.length; i++) {
	  addOption(sa[i]);
	}
  }  
  public PullDown(String s, String[] sa, final String selected) {
	this(s);
	for (int i = 0; i < sa.length; i++) {
	  addOption(sa[i], selected != null && selected.equals(sa[i]));
	}
  }  
  public void addOption(String text) {
	addOption(text, text);
  }  
  public void addOption(String text, String value) {
	addOption(text, value, false);
  }  
  public void addOption(String text, String value, boolean selected) {
	Tag option = new Tag("OPTION");
	option.addArgument(new Argument("VALUE", value));
	if(selected)
	  option.addArgument(Flag.SELECTED);
	addHTML(option);
	addHTML(new Text(text));
  }  
  public void addOption(String text, boolean selected) {
	addOption(text, text, selected);
  }  
	protected String toText() {
	return "<FONT SIZE=2>" + super.toText() + "</FONT>";
  }  
  public synchronized String toText(int option) {
	String s;
	try {
	  Tag temp = ((Tag)contents.elementAt(option*2));
	  temp.addArgument(Flag.SELECTED);
	  s = toText();
	  temp.removeArgument(Flag.SELECTED);
	} catch(ArrayIndexOutOfBoundsException ignore) {
	  s = toText();
	}
	return s;
  }  
}
