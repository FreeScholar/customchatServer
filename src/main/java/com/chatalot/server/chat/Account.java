package customchat.chat;

import java.util.*;
import customchat.util.* ;
// This is a comment added for intial commit
/**
 * An account represents one active user account.  It keeps track of all
 * active sessions for this user as well as preferences that persist across
 * all sessions.
 *
 * @Author CustomChat Server <info@customchat.com>
 * @see UserRegistry
 * @version 1.5
 */

public class Account {
  // The owner of this account
  private final Login lOwner;
  // A map by handle of all the active sessions this user is engaged in
  private Hashtable sessions = new Hashtable(); // Most users will have only one handle
  // The set of valid handles
  private AuthInfo a = null;

    //  private static Auth dataBase;
  static {
      //if(System.getProperty("chat.registration") != null) {
      //			dataBase =  new Auth("jdbc:odbc://localhost:1114/CHATALOT",
      //						   "user",
      //						   "password");
      //	}
      //	else
      //	  dataBase = null;
  }


  /**
   * Construct an account belonging to l
   * @param l the owner of the account to be constructed.
   */
  public Account(Login l)
  throws DataBaseException {
	if( l == null ) throw new NullPointerException();
	lOwner = l;

	// Get account info from database
	//	if(dataBase != null)
	//  a = dataBase.Verify(l);
  }  
 // /** Returns a <code>Vector</code> of Sessions belonging to this account.*/
 // public Vector getSessions() {
 //   return new Vector(sessions);
 // }


  /**
   * Calls the <code>end</code> method for session owned by the given handle
   *
   * @param handle of the
   */
  public int endSession(String handle)
  throws ChatException {
	Chatter c = (Chatter)sessions.remove(handle);
	if(c != null && c.location() != null)
	  c.location().removeChatter(c);
	return sessions.size();
  }  
  /** Returns a <code>Vector</code> of handle Strings belonging to this account.*/
  public String[] getHandles() {
	return (a == null ? null : a.sHandles);
  }  
  public final boolean isOwnedBy(Login l) {
	return lOwner.equals(l);
  }  
  /**
   * Calls the <code>endSession</code> method on all active sessions.
   */
  public void logOut()
  throws ChatException {
	Enumeration i = sessions.elements();
	while (i.hasMoreElements()) {
	  endSession((String)i.nextElement());
	}
  }  
  public Chatter makeChatter(String handle, ChatObject dest)
  throws ChatException, DataBaseException {

// 	if(dataBase != null) {
// 	  if(a == null)
// 		a = dataBase.Verify(lOwner);
// 	  if(a == null || !a.verifyHandle(handle))
// 		throw new ChatException("You must register your handle before you can use it");
// 	}

	Chatter c = (Chatter)sessions.get(handle);

	if(c == null) {
	  c = new Chatter(lOwner, handle);
	  sessions.put(handle, c);
	} else // if they are alread somewhere exit them out
	  if(dest != c.location() && c.location() != null)
		c.location().removeChatter(c);

	// put them in the new place
	// dest.addChatter(c);
	return c;
  }  
}
