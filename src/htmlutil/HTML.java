package customchat.htmlutil;

import java.io.*;
import customchat.util.ErrorLog;

public abstract class HTML implements Serializable, Cloneable {
  public static final char QUOTE_ESCAPE = '\u00b0';

  public synchronized Object clone() {
	try {
	  return super.clone();
	} catch(CloneNotSupportedException e) {
	  ErrorLog.error(e, 207, "Error cloning an HTML");
	}
	return null;
  }  
  public static String escapeQuotes(String s) {
		//StringBuffer sb = new StringBuffer(s.length() + 10);
		//int i = 0;
		//int j = 0;
		//while( (j = s.indexOf("\"", i)) >= 0 ) {
		//	sb.append(s.substring(i,j) + "\\\"");
		//	i = j+1;
		//}
		//sb.append(s.substring(i));
	//return sb.toString();
	return s.replace('"', QUOTE_ESCAPE);
	}
	public String toString() {
	//	HTML out = format(this);
		return toText();
	}
	//private boolean centered = false;
	//public void center() { center(true); }
	//public void uncenter() { center(false);}
	//public void center(boolean centered) {
	//	this.centered = centered;
	//}

	//HTML format(HTML h) {
	//	HTML ret = h;
	//	if(centered) {
	//		ret = new ContainerTag("CENTER" , ret);
	//	}
	//	return ret;
	//}

	protected abstract String toText();
}
