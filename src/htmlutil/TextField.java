package customchat.htmlutil;

import com.sun.java.util.collections.*;
import customchat.util.*;
import java.util.*;
import java.io.*;

public class TextField extends InputField {

  public TextField(String s) {
	super("", s);
	value = s;
  }  

  public HTML print() {
	return new Text(value == null
					? "<font color=\"red\"><blink>null</blink></font>"
					: value.toString());
  }  
  
  public Object readValue(LookupTable lt) {
	return value;
  }  
}
