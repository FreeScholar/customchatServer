package customchat.chat;

import customchat.htmlutil.*;
import customchat.util.*;
import java.util.Calendar;

public class BootManager extends UserManager {
	static final long serialVersionUID = 2511488076172447290L;
	public static final String BOOT_HANDLE_VAR = "BootMe";

	public BootManager(ChatObject co) {
		super(co, co.BOOT_ADD, co.BOOT_EDIT);
	}
	public Page addPage(LookupTable lt) {
		StringBuffer error = new StringBuffer(10);;

		// read in new user if there is one
		if(lt != null) {
			// read in bootee properties
			String sHandle = lt.getValue("BOOT_MANAGER_HANDLE");
			String reason  = lt.getValue("BOOT_MANAGER_REASON");
			Calendar until;
			try{
			  until = HTMLDate.getCalendar("BOOT_MANAGER_UNTIL", lt);
			} catch (NumberFormatException e) {
			  until = null;
			}
			// if info is missing give an error message
			if(sHandle == null || "".equals(sHandle) || until == null) {
				if(sHandle == null || "".equals(sHandle)) {
					error.append( "<font face=\"Arial, Helvetica, Geneva\">You Must Specify a Handle.</font><br>\n");
				}
				if(until == null)
					error.append("<font face=\"Arial, Helvetica, Geneva\">You Must Specify a Date.</font><br>\n");
			} else {
				Chatter c = UserRegistry.getChatter(sHandle);
				if(c == null)
				  error.append("<font face=\"Arial, Helvetica, Geneva\">Chatter not Found.</font><br>\n");
				else if(co.isAdmin(c.getLogin()))
				  error.append("<font face=\"Arial, Helvetica, Geneva\">You cannot boot Admins.</font><br>\n");
				else {
				  try {
					UserRegistry.endSession(sHandle);
				  } catch(ChatException e) {
					error.append("<font face=\"Arial, Helvetica, Geneva\">"+e.getMessage() + "</font><br>\n");
				  }
				  Bootee b = new Bootee(c.getLogin(), sHandle, until, reason);
				  addUser(b.key, b);
				}
			}
		}

		// print out the form
		Form f = new Form("BOOT_FORM", co.commandURL(addCommand), "POST");
		String handle = lt.getValue(BOOT_HANDLE_VAR);
		f.addHTML(new Input(Input.TEXT, "BOOT_MANAGER_HANDLE", handle != null ? handle : "" ));
		f.addHTML(new HTMLDate("BOOT_MANAGER_UNTIL"));
		f.addHTML(new Input(Input.TEXT,"BOOT_MANAGER_REASON", "See the site rules."));
		f.addHTML(new Input(Input.SUBMIT, "submit", "submit"));
		f.addHTML(new Input(Input.RESET, "reset", "reset"));
		Page p = new Page(new Body("FFFFFF",null,null,null,null));
		p.addHeadHTML("<script>\n"
					+ "<!--\n"
					+   "parent.frames[0].location.href=\""
						  + co.commandURL(editCommand) + "\";\n"
					+ "//-->\n"
					+ "</script>\n");
		p.addHTML(error + "<br>");
		p.addHTML(f);

		return p;
	}
	protected String extractVars(LookupTable lt) {
	  String bootHandle;

	  if(lt == null)
		return "";

	  if((bootHandle = lt.getValue(BOOT_HANDLE_VAR)) == null)
		return "";

	  return BOOT_HANDLE_VAR + "="
		   + java.net.URLEncoder.encode(bootHandle);
	}
	public void isBooted(Login l)
	throws BootedException {
		Bootee b = (Bootee)getUser(l);
		if(b != null && !b.hasExpired()) {
		  throw new BootedException(
			(Calendar)((InputField)b.inputFields.get(2)).getValue(),
			(String)  ((InputField)b.inputFields.get(3)).getValue());
		}
	}
}
