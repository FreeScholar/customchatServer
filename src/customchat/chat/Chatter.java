package customchat.chat;

import java.util.*;
import java.io.*;
import java.net.*;
import customchat.util.*;


/**
 * Class to store the state for a single user logged into the system,
 * and provides HTML formatted for this chatter.
 *
 * @author CustomChat Server
 * @version 1.5
 */

public class Chatter extends Object implements Serializable {
    static final long serialVersionUID 				= 6448109445035551597L;
    public static final String PRIVATE_IGNORE_VAR 	= "IgPriv";
    public static final String PUBLIC_IGNORE_VAR 	= "IgPub";
    public static final String NO_IMAGE_VAR 		= "bNoPic";
    public static final String NO_HANDLE_IMAGE_VAR 	= "bNoPicHandle";
    public static final String NO_SOUND_VAR 		= "bNoSound";
    public static final String IG_OUTSIDE_VAR 		= "bIgOutside";
    public static final String IG_ENTRY_VAR 		= "bIgEntry";
    public static final String USER_DING 			= "bUserDing";
    public static final String PM_RECIPIENT_VAR 	= "PM";
    public static final String HANDLE_VAR 			= "sHandle";
    public static final String HOMEPAGE_VAR 		= "sHomePage";
    public static final String TAGLINE_VAR 			= "sTagline";
    public static final String USING_IE_VAR 		= "bIE";
	public static final String NO_BUTTONS			= "bNoButtons" ;

    // -------------------Private Variables ----------------------------
    // Configuration variables for the chat session.
    public  Login lUser;
    public String sHandle;   // Handle displayed in the room
    public String sHomePage; // Home page of the chatter, optional.
    // This is used to link the users name in a room to their home page.
    public String sTagline;  // Optional user tagline.

 // User Options
    public boolean bNoPic = true;        // Don't display <img> tags in messages.
    public boolean bNoPicHandle = true;  // Don't display <img> tags in handles.
    public boolean bNoSound = true;      // Don't play sound in messages.
    public boolean bIgOutside = false;   // Ignore messages sent from outside the room.
    public boolean bIgEntry = false;   // Ignore entry/exit messages
    public boolean bUserDing = false;   // Ding on entry/exit
    public boolean bNoButtons = false ; 
	boolean bIE = false;	// using IE browser

    //who was last sent a private message(PM)
    private String[] aPMRecips = null;
    //who's being ignored for public messages
    private String[] aIgPub = null;
    //who's being ignored for private
    private String[] aIgPriv = null;
    public ChatObject loc = null;

    public boolean isAdmin = false ;
 // State Variables

    private Date dLastCheck = new Date();  // Time of last activity
    private transient AutoScroll scroll = null;
    //private final transient Thread outThread = null; //thread associated with chatter

    // Messages waiting for chatter
    private final Vector vPubBox = new Vector();  // Public message queue.
    private final Vector vPrivBox = new Vector(); // Private message queue.

	public String sChatPic = "DEF";

    public Chatter(final Login l, String handle) {
	lUser = l;
	sHandle = (new Filter()).FilterHTML(handle);
    }  

    // -------------------Public Interface ----------------------------

    public Chatter(final Login l, String handle, final LookupTable lt) {
	this(l, handle);
	update(lt);
	// bNoPic = bNoPicHandle = bNoSound = true; // Must set default true's outside of update(lt)
    }


    /**
	 * Adds a message to the public or private message queue.
	 *
	 * @param m The message to be added.
	 */
    public void addMessage(Message m) {
	//Sort messages into public and private boxes (vectors)
	switch(m.getType()) {
	case Message.PUBLIC:
	    //check if sender is being ignored publicly
	    if(isIgPub(m.getFrom())) {
                break;
            }
            /* otherwise, falls through */
	case Message.BROADCAST :
            /* falls through again */
	case Message.SYSTEM:
	    synchronized(vPubBox) {
		vPubBox.addElement(m);
	    }
	    break;
	case Message.PRIVATE:
	    //check if sender is being ignored privately
	    if(isIgPriv(m.getFrom())) {
                break;
            }
            
	    synchronized(vPrivBox) {
		vPrivBox.addElement(m); //if not, add the message
	    }
	    break;
	case Message.INSTANT:
	    //check if chatter is ignoring outside messages
            // changed to use brackets G.T. 11/30/2017
	    if(bIgOutside) {
                break;
            }
            
	    synchronized(vPrivBox) {
		vPrivBox.addElement(m);
	    }
	    break;
	case Message.REMOVE: 
	    if (loc != null) {
                // changed to use brackets G.T. 11/30/2017
		if (loc.bScroll) {
                    m.append("\n<script>parent.WhoListFrame.document.location.reload();</script>\n");
                }
	    }

	    if (!bIgEntry) {
		if (bUserDing) {
                    m.append("<embed src=\"/resources/sounds/doorbell.wav\" autostart=\"true\" hidden=\"true\">") ;
		    //System.out.println("doing sound for user leaving");
		}	
		
		synchronized(vPubBox) {
		    vPubBox.addElement(m);
		}	
            }
	    break;
	case Message.ADDCHATTER:
	    if (loc != null) {
                // changed to use brackets G.T. 11/30/2017
		if (loc.bScroll) {
                    m.append("\n<script>parent.WhoListFrame.document.location.reload();</script>\n");
                }  
	    }
	
	    if (!bIgEntry) {
		if (bUserDing) {
		    m.append("<embed src=\"/resources/sounds/doorbell.wav\" autostart=\"true\" hidden=\"true\">") ;
		    //System.out.println("doing sound for user arriving");
		}
                
		synchronized(vPubBox) {
		    vPubBox.addElement(m);
	    	}
	    }
            break;
	default:
	    ErrorLog.error(new ChatException(), 12321, "Invalid message type");
            /* I added a break statement even though redundant in most cases. 
              It can fix fall through errors. G.T. 11/30/2017 */
            break;
	}
    }
    /**
	 * @return a String containing CGI variables to uniquely identify the user (for use in a URL)
	 */
    protected String GetGetVars() {
	return (sHandle == null ? "" : HANDLE_VAR + "=" + java.net.URLEncoder.encode(sHandle)) +
	    (HasScroll() ? "&" + Room.SCROLL_VAR : "");
    } 

    /**
	 * @return Last time the chatter interacted with the server.
	 */

    public Date GetLastCheck() {
	return dLastCheck;
    }

    /**
     * Calls HTMLGetNewMessages(false) which means not real time chat.
     */

    public String HTMLGetNewMessages() {
	return HTMLGetNewMessages(false);
    }

    /**
     *
     * @param bScroll true if the user is using real time chat.
     * @return a string with the HTML formatted messages pending in the public and private queues.
     */

    public String HTMLGetNewMessages(boolean bScroll) {
	StringBuilder out;
        out = new StringBuilder("");
	Message mCurr;
	Enumeration e;

	if(vPrivBox.size() > 0) {
          
	    if(!bScroll) {
                out.append("<H2><FONT FACE=Arial, Helvetica>Private Messages</FONT></H2>\n");
            }

	    //Print out the private messages
	    e = vPrivBox.elements();
	    while(e.hasMoreElements()) {
		mCurr = (Message)e.nextElement();
                // changed to use brackets G.T. 11/30/2017
		if(mCurr.is(Message.INSTANT)) {
                    out.append("<H3><FONT FACE=Arial, Helvetica>Outside Message:</FONT></H3>");
                } else if(bScroll) {
                    out.append("<H3><FONT FACE=Arial, Helvetica>Private Message:</FONT></H3>");
                }
					
		out.append(mCurr.getHTML(this));
	    }


	    if(!bScroll) {
                out.append("<HR>\n");
            }

	    vPrivBox.removeAllElements(); //delete messages
	}

	if(!bScroll) {
            out.append("\n<H2><FONT FACE=Arial, Helvetica>Public Messages</FONT></H2>\n");
        }

	if(vPubBox.size() > 0) {
	    //Print out the public messages
	    e = vPubBox.elements();
	    while(e.hasMoreElements()) {
		mCurr = (Message)e.nextElement();
		out.append(mCurr.getHTML(this));
	    }
	    vPubBox.removeAllElements();//delete them
	} else if(!bScroll) {
	    out.append("<FONT FACE=Arial, Helvetica SIZE=2><I>None</I></FONT>");
	}
        
	return out.toString();
    }
    //---------------------- HTML methods ------------------------------
    /**
     *
     * @return a String containing HTML hidden variables to uniquely identify the user.
     */

    protected String HTMLGetVars() {
	return sHandle == null ? "" : "<INPUT TYPE=\"HIDDEN\" NAME=\"" + HANDLE_VAR +
	    "\" VALUE=\"" + sHandle + "\">\n" +
	    (this.HasScroll() ? "<INPUT TYPE=\"HIDDEN\" NAME=\"" + Room.SCROLL_VAR +
	     "\" VALUE=TRUE>\n" : "");
    }

    /**
     * Used to determine if the chatter is using real time chat.
     *
     * @return True if there is an AutoScroll Object associated with the chatter.
     */
    
    public boolean HasScroll() {
	return scroll != null;
    }
    
    //----------------Data Abstraction Functions ----------------------------
    public String HashKey() {
        // changed to use brackets G.T. 11/30/2017
	if(!hasHandle()) {
            return null;
        }
        
	return sHandle;
    }
    
    /**
     * Perform tasks needed every time a chatter makes contact with the server,
     * like setting time of last check, and checking to see if he's booted.
     */
    
    void MakeContact() {
	dLastCheck = new Date();
    } 

    /**
     * @return True if the user is ignoring <img> tags in handles.
     */
    public boolean NoPicHandle() {
	return bNoPicHandle;
    }

    /**
     * @return True if the user is ignoring sounds in messages.
     */
    
    public boolean NoSound() {
	return bNoSound;
    }
    
    /**
     * Associates the chatter with the given AutoScroll object.
     * Used in real time chat.
     *
     * @param as An AutoScoll object that outputs to the user's Web Browser.
     */
    
    public synchronized void SetScroll(AutoScroll as) {
        // changed to use brackets G.T. 11/30/2017
	if(scroll != null) {
            scroll.Exit();
        }
	scroll = as;
    }
    
    public void deliverMessages() {
	if(scroll != null) {
            
	    synchronized(scroll) {
		scroll.print(HTMLGetNewMessages(true));
	    }
	}
    }
    /**
     * Cleans up when the chatter leaves the system.  Currently calls AutoScroll.exit().
     *
     * @return the this pointer.
     */
    
    public Chatter doExit() {
	if(scroll != null) {
	    scroll.Exit();
	}
	setLocation(null);
	return this;
    }
    /**
	 * Calls getFullHTML(false) which means with images.
	 */
    public String getFullHTML() {
	return getFullHTML(false);
    }
    /**
	 * Generates the HTML code for the user handle to be displayed in the browser.
	 * Makes the name bold, links it to the home page (if provided) and adds the idle
	 * time.
	 *
	 * @return a string with the handle as it should be printed in html
	 */

    public String getFullHTML(boolean bNoPic) {
	return (!bNoPicHandle ? ((sChatPic != "DEF") ? "<IMG src=\"" + sChatPic +"\" width=\"144\" height=\"120\">" : "") : "") +
				getHTML(bNoPic,(loc==null) ? "None" : loc.bIdleTimes) + "<BR>"
	    // include tagline
	    + (sTagline != null ?
	       "<B><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">" + (bNoPic ? Filter.ImgFilter(sTagline) : sTagline) + "</font></B>\n" :
	       "");
    }
    //((!bNoPic) ? ("<IMG src=\"" + sChatPic +"\">") : "" ) +
	/**
	 * Calls getHTML(false) which means with images.
	 */
    public String getHTML() {
	return getHTML(false,(loc==null) ? "None" : loc.bIdleTimes);
    }
    /**
	 * Generates the HTML code for the user handle to be displayed in the browser.
	 * Makes the name bold, links it to the home page (if provided) and adds the idle
	 * time.
	 *
	 * @param bNoPic true means replace images in handle with a small default image.
     * @param noIdleTimes
	 * @return a string with the handle as it should be printed in html
	 */
    public String getHTML(boolean bNoPic,String noIdleTimes) {
	Date dNow = new Date();
	long time = (dNow.getTime() - this.dLastCheck.getTime()) / 1000;
	
	String idleTimes = "";

	if (!noIdleTimes.equals("0")) {
		if (noIdleTimes.equals("1")) {	
		    idleTimes = " (#" +  String.valueOf(this.getLogin().hashCode()) + ")"
			+ "</B></A>; idle " + String.valueOf(time) + " seconds\n";
		}

		if (noIdleTimes.equals("2")) {
			idleTimes = " (" + this.getLogin().IP + ")"
			+ "</B></A>; idle " + String.valueOf(time) + " seconds\n";
		}
	}
	
	return ((sHomePage != null || "".equals(sHomePage) ) ?
		"<A HREF=\"" + sHomePage + "\" TARGET=\"_blank\">" : "")
	    + "<B>" + (bNoPic ? Filter.ImgFilter(this.sHandle) : this.sHandle)
	    + idleTimes ;
    }

    public Login getLogin() {
	return lUser;
    }

    public String getText() {
	return Filter.stripHTML(sHandle);
    }
    
    boolean hasHandle() {
	return sHandle != null;
    }
    
    boolean hashKeyIs(Object o) {
	return((HashKey() != null) && (HashKey().equals(o)));
    }
    
    public boolean isIgPriv(final String handle) {
        // changed to use brackets G.T. 11/30/2017 both statements (2 ifs and 1 for)
	if(handle == null || aIgPriv == null) {
            return false;
        }

	for(int i = 0; i < aIgPriv.length; i++) {
            if(aIgPriv[i].equals(handle)) {
                return true;
            }
        }

	return false;
    }
    
    public boolean isIgPub(final String handle) {
        // changed to use brackets G.T. 11/30/2017 both statements (2 ifs and 1 for)
	if(handle == null || aIgPub == null) {
            return false;
        }

	for(int i = 0; i < aIgPub.length; i++) {
            if(aIgPub[i].equals(handle)) {
                return true;
            }
        }

	return false;
    }
    
    public boolean isPMRecip(final String handle) {
        // changed to use brackets G.T. 11/30/2017 both statements (2 ifs and 1 for)
	if(handle == null || aPMRecips == null) {
            return false;
        }

	for(int i = 0; i < aPMRecips.length; i++) {
            if(aPMRecips[i].equals(handle)) {
                return true;
            }
        }

	return false;
    }

    public final ChatObject location() {
	return loc;
    }  

    Message privateMessage(String s) {
        // changed to use brackets G.T. 11/30/2017 
	if(aPMRecips != null && aPMRecips.length > 0) {
            return new Message(s, this.HashKey(), aPMRecips, Message.PRIVATE);
        }
        
	return null;
    }

    public final ChatObject setLocation(ChatObject dest) {
	return loc=dest;
    }  
    /**
	 * Overrides the default toString method of Object class.
	 *
	 * @return A string with the chatters handle.
	 */

    public String toString() {
	return sHandle;
    }

    void update(final LookupTable lt) {
	//set options
	ChatObject.setFields(this, lt);
        // changed to use brackets G.T. 11/30/2017 
	if(lt.getValue(ChatObject.SET_ONE_VAR + Room.SCROLL_VAR) != null) {
            if(lt.getValue(Room.SCROLL_VAR) == null) {
                scroll = null;
            }
        }

        // changed to use brackets G.T. 11/30/2017 
	if(lt.getValue(HANDLE_VAR) != null) {
            sHandle = (new Filter()).FilterHTML(sHandle);
        }

        // changed to use brackets G.T. 11/30/2017 
	if(lt.getValue(TAGLINE_VAR) != null) {
            sTagline = (new Filter()).FilterHTML(sTagline);
        }

	if(lt.getValue(ChatObject.SET_VAR) != null) {
	    aPMRecips = lt.getFullValue(PM_RECIPIENT_VAR);// Handles of private message recipients.
	    aIgPub = lt.getFullValue(PUBLIC_IGNORE_VAR);  // ""      public message ignorees
	    aIgPriv = lt.getFullValue(PRIVATE_IGNORE_VAR);//""     private message ignorees
	} else {
            // changed to use brackets G.T. 11/30/2017 
	    if(lt.getValue(ChatObject.SET_ONE_VAR + PM_RECIPIENT_VAR) != null) {
                // duplicate line of code found. Might serve purpose. G.T. 11/30/2017
                // if(lt.getValue(ChatObject.SET_ONE_VAR + PM_RECIPIENT_VAR) != null)
                aPMRecips = lt.getFullValue(PM_RECIPIENT_VAR);
            }
            
            // changed to use brackets G.T. 11/30/2017 
	    if(lt.getValue(ChatObject.SET_ONE_VAR + PUBLIC_IGNORE_VAR) != null) {
                aIgPub = lt.getFullValue(PUBLIC_IGNORE_VAR);
            }
	
	    if(lt.getValue(ChatObject.SET_ONE_VAR + PRIVATE_IGNORE_VAR) != null) {
                aIgPriv = lt.getFullValue(PRIVATE_IGNORE_VAR);
            }
	}
    }
    public final boolean usingIE() {
	return bIE;
    }  

    /**
	 * Verifies the given login name and passphrase.
	 *
	 * @param primary login name
	 * @param pass passphrase
	 * @return true for good login name and passphrase.
    */
    public boolean verify(Login l, String sHandle) {
	return lUser.equals(l) && hashKeyIs(sHandle);
    }
}
