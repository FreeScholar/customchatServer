package customchat.htmlutil;

//import com.sun.java.util.collections.*;
import customchat.util.*;
import java.util.*;
import java.io.*;

public class CheckBoxField extends InputField {
  private static final String MARKER = "__SET__";

  public CheckBoxField(String varName, boolean b) {
	super(varName, b ? Boolean.TRUE : Boolean.FALSE);
  }  

  public HTML print() {
	if(value == null) {
	  return new Text("<font color=\"red\"><blink>null</blink></font>");
	}
	  Container d = new Container();
	  d.addHTML(new Input(Input.CHECKBOX, varName, "ON"));
	  d.addHTML(new Input(Input.HIDDEN, varName + MARKER, "ON"));
	return d;
  }  

  public Object readValue(LookupTable lt) {
	if(lt != null) {
	  if(lt.getValue(varName) != null)
		value = Boolean.TRUE;
	  else if(lt.getValue(varName + MARKER) != null)
		value = Boolean.FALSE;
	}
	return value;
  }  
}
