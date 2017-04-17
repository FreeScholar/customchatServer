package customchat.htmlutil;

import com.sun.java.util.collections.*;
import customchat.util.*;
import java.util.*;
import java.io.*;

public class DateField extends InputField {

  public DateField(String varName, Calendar c) {
	super(varName, c);
  }  
  
  public HTML print() {
	if(value == null)
	  return new HTMLDate(varName);
	else
	  return new HTMLDate(varName, (Calendar) value);
  }  

  public Object readValue(LookupTable lt) {
	if(lt != null) {
	  try {
		value = HTMLDate.getCalendar(varName, lt);
	  } catch (NumberFormatException ignore) {}
	}
	return value;
  }  
}
