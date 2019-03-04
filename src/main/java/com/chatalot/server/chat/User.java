package customchat.chat;

//import com.sun.java.util.collections.*;
import customchat.htmlutil.*;
import customchat.util.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;


abstract class  User implements Serializable {
	static final long serialVersionUID = 4053076139390121L;
	List inputFields;
	Object key;
	protected static Form NewUser;
	
	User(Object key, List inputFields) {
		this.key = key;
		this.inputFields = inputFields;
	}
	public abstract boolean deleted();
	
	public boolean equals(Object o) {
		return (o != null && this.getClass().isInstance(o)) && key.equals(((User)o).key);
	}
	
	HTML printUser(LookupTable lt) {
		Iterator i = inputFields.iterator();
		InputField in;
		Container tr = new Container("TR");
		while(i.hasNext()) {
			in = (InputField)i.next();
			tr.addHTML(new Container("TD", in.print()));
		}
		return tr;
	
	}
	
	void readProperties(LookupTable lt) {
		Iterator i = inputFields.iterator();
		InputField in;
		while(i.hasNext()) {
			in = (InputField)i.next();
			in.readValue(lt);
		}    
	}
}
