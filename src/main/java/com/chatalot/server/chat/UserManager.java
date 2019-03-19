package customchat.chat;

//import com.sun.java.util.collections.*;
import java.io.*;
import customchat.htmlutil.*;
import customchat.util.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class UserManager implements Serializable {
    static final long serialVersionUID = 6071217399140425525L;
    private Map users = new HashMap();
    transient protected ChatObject co;
    protected final int addCommand;
    protected final int editCommand;

    UserManager(ChatObject co, int addCommand, int editCommand) {
	this.co = co;
	this.addCommand = addCommand;
	this.editCommand = editCommand;
    }


	public abstract Page addPage(LookupTable lt);

	protected void addUser(Object key, User u) {
	users.put(key, u);
    }
	
    public Container editPage(final LookupTable lt) {
		Form f = new Form("EDITPAGE", co.commandURL(editCommand), "POST");
		Container  t = new Container("TABLE");
			
		Text welcomeText = new Text("<center><h1><font face=\"Arial, Helvetica, Geneva\">CustomChat Boot Manager</font></h1></center>") ;
		f.addHTML(welcomeText) ;
		f.addHTML(t);

		Iterator i = users.values().iterator();
		User u;

		while(i.hasNext()) {
			
			u = (User)i.next();
			u.readProperties(lt);
			if(u.deleted())
			users.remove(u.key);
			else
			t.addHTML(new Container("TR",u.printUser(null)));
		}

		f.addHTML(new Input(Input.SUBMIT, "Submit", "Submit"));
		f.addHTML(new Input(Input.RESET, "Reset", "Reset"));
		save();
		return f;
    }

    protected String extractVars(LookupTable lt) {
	//Override
	return "";
    }

    public Page frameSet(LookupTable lt) {
	Page p = new Page();
	p.addFrameSetArg("ROWS", "75%,*");
	p.addFrame("<frame src=\"" + co.commandURL(editCommand) + "\">");
	p.addFrame("<frame src=\"" + co.commandURL(addCommand) + "&"
		   + extractVars(lt) + "\">");
	return p;
    }

    protected User getUser(Object key) {
	return (key == null)
	    ? null
	    : (User)users.get(key);
    }

    protected void removeUser(Object key) {
	users.remove(key);
    }

    private void save() {
	try {
	    ObjectOutputStream oos = new ObjectOutputStream(
				    new FileOutputStream(
   				 new File(co.getPath(), co.FILE_BOOTED)));
	    oos.writeObject(this);
	    oos.close();
	} catch(Exception e) {
	    ErrorLog.error(e, 10001, "Error Saving boot manager for " + co.getHTML());
	}
    }
    public void setChatObject(ChatObject co) {
	this.co = co;
    }
}
