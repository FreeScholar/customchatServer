package customchat.htmlutil;

import com.sun.java.util.collections.*;
import customchat.util.*;
import java.util.*;
import java.io.*;

public class InputField implements Serializable {
    //added by ritchie
    static final long serialVersionUID = 3261841957700663347L;
    String varName;
    Input  input;

    HTML   lable;
    Object value;

    public InputField(String varName, Object value) {
	this.varName  = varName;
	this.value    = value;
    }
	
    public Object getValue() {
	return readValue(null);
    }
    /**
     * Converts the data into html for display.
     *
     * @return The HTML Representaton of the data.
     */
    public HTML print() {
	if(value == null) {
	    return new Text("<font color=\"red\"><blink>null</blink></font>");
	}
	return new Input(Input.TEXT, varName, value.toString());
    }
    /**
     * Parces the information in the Lookuptable
     * of cgi variables and sets the value of the
     * <code>InputField</code>.
     *
     * @param lt A lookuptable of cgi name value pairs.
     * @return the value of the <code>InputField</code>.
     */
    public Object readValue(LookupTable lt) {
	if(lt != null) {
	    Object temp = lt.getValue(varName);
	    if(temp != null)
		value = temp;
	}
	return value;
    }
}
