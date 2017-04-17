package customchat.htmlutil;

import java.io.*;

public class Argument implements Serializable {
	//added by ritchie
	  static final long serialVersionUID = -7561022089980875527L;
  protected String name;
  private Object value;

  public Argument(String name, Object value) {
	if(name == null) throw new NullPointerException("Cannot construct an argument with null for a name");
	this.name = name;
	if(value == null) throw new NullPointerException("Cannot construct an argument with null for a value.");
	this.value = value;
  }  
  public boolean equals(Object o) {
	return (o != null) &&
	(o instanceof Argument) &&
	( o == this || ((Argument) o).name.equalsIgnoreCase(name));
  }  
  public String toString() {
	return ' ' + name.toUpperCase() + "=\"" + HTML.escapeQuotes(value.toString()) + "\"";
  }  
}
