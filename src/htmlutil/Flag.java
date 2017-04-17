package customchat.htmlutil;

import java.io.*;

public class Flag extends Argument {

  public static final Flag CHECKED = new Flag("CHECKED");
  public static final Flag SELECTED = new Flag("SELECTED");

  public Flag(final String name) {
	super(name, new Boolean(true));
  }  
  public String toString() {
	return ' ' + name;
  }  
}
