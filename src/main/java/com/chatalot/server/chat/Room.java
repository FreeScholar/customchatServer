package com.chatalot.server.chat;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import com.chatalot.server.util.*;
import com.chatalot.server.htmlutil.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

/**
 * This class implements one chat room.  The class keeps track of users in room, messages, and options
 * for the room.  Based on these options, the class generates HTML customized for a given chatter.
 *
 * 
 * @author CustomChat Inc.
 * @version 1.5
 */
public class Room extends ChatObject {
    //	static final long SERIAL_VERSION_UID = 8311762447962493048L;
    // added by ritchie
    static final long SERIAL_VERSION_UID = 6448109445035551597L;

    public static final int ADD = 1001;
    public static final int MANUAL = 1002;
    public static final int SCROLL_TOP = 1003;
    public static final int SCROLL_MESSAGES = 1004;
    public static final int SCROLL_SEND = 1005;
    public static final int SCROLL_MIDDLE = 1006;
    public static final int SCROLL_LIST = 1007;
    public static final int OPTIONS = 1008;
    public static final int SWITCH = 1009;
    public static final int EXIT = 1010;
    public static final int COM_POST_IMAGES = 1011;
    public static final int COM_POST_SOUNDS = 1012;
    
    public static final int COM_REMOVE_IMAGES = 1013;
    public static final int COM_REMOVE_SOUNDS = 1014;
    
    public static final int SET_OPTIONS = 1015 ;
    
	public static final int PASTE_POP = 1016 ;
	public static final int PASTE_SEND = 1017 ;

    public static final String DESTINATION_VAR = "Dest";
    public static final String ROOM_PASS_VAR = "RoomPass";
    public static final String SCROLL_VAR = "bScroll";
    public static final String MESSAGE_VAR = "Message";
    public static final String VAR_POST_IMAGE = "postimage";
    public static final String VAR_POST_SOUND = "postsound";
    public static final String VAR_OPTIONS_SUBMITTED = "OptionsFormSubmitted";
    

    public static final String FRAME_LIST = "WhoListFrame";
    public static final String FRAME_MIDDLE   = "MiddleFrame";
    public static final String FRAME_MESSAGES = "MessagesFrame";
    public static final String FRAME_TOP      = "TopFrame";
    public static final String FRAME_SEND     = "SendFrame";

    public static final String FORM_WHO_LIST  = "PMForm";
    public static final String FORM_POST_IMAGES = "postimageform";
    public static final String FORM_POST_SOUNDS = "postsoundform";

    public static final String DONE = "Done";
    

    /**
     * Hash table implementing a ring buffer of lenght mesg_limit messages posted in the room.
     * The hashkey is the current value of auto_inc which then increases by one.
     * When auto_inc reaches mesg_limit, it is set to 0.
     */
    private final Hashtable htMessages = new Hashtable();
    private int auto_inc = 0; //Next open position in the message buffer.
    private static final int MESSAGE_LIMIT = 300; //Max number of messages in the message buffer.

//    private transient Vector vChatters = new Vector();  //  List of current chatters
    private Filter fl = new Filter(); // The HTML filter used for this room

    // ----- Variables for the rooms made with the creator tool -----
    
    public boolean bHide = false;       // True if room does not appear on live list
    public boolean bCtrName = false;  	// True to center the room name
    public String sPass = null; 	// passphrase for the room (null if no passphrase)

    public String sEntryImg = null; 	// url for image on html entry page to chatroom
    public boolean bCtrImg = false; 	// true to center image
    
    public String sEntryText = "";  // Text to appear between entry image and login fields on the entry page
    public boolean bCtrEntryText = false;       // true to center sEntryText
    public String sEntryBottom = null;         	// Html at the bottom of entry page
    public boolean bCtrEntryBottom = false;    	// If true center sEntryBottom

    public int iShowRecent = 0; 		// Number of recent messages to display on the entry page
    public boolean bEntryWho = true; 		// true to show who's inside on entry page

    // Links on entry page.  Link is text and Anchor is the url.
    public String sEntryLink1 = null;
    public String sEntryAnchor1 = null;
    public String sEntryLink2 = null;
    public String sEntryAnchor2 = null;

    public String sTop = "";  		// Image url at the top of the page inside the room.
    public boolean bCtrTop = false;     // If true center sTop
    public String sRoomText = null;     // text inside room
    public boolean bCtrRoomText = false;  // True to center sRoomText

    // Links inside of room. Link is text and Anchor is the url.
    public String sRoomLink1 = null;
    public String sRoomAnchor1 = null;
    public String sRoomLink2 = null;
    public String sRoomAnchor2 = null;

    // Signature of room creator displayed at the bottom of entry and room pages.
    // String sTagline = null;
    public String sHome = null;
    public String sOwnerHandle = null;
    public String sEmail = null;
    public String sTitle = null;
    public String sSpeak = "Speak";             //text on the public message button
    public String sWhisper = "Whisper";         //text on the private message button
    public String sDefaultSwitchKey = "";

    public int iMaxChatters = 30; //limit for number of chatters (but always lets in admins)
    private static final String BOTTOM_FORM = "Bottom"; //NAME of the bottom Form
    protected final static String NEW_HTML =  htmlFile("NewRoom.html");
    public boolean bCtrVid = false;

    public static String buttonJS = null;
    public static String signature = null;
    
    private Page roomPage;
    private DeliveryThread thread;

   class DeliveryThread extends Thread {
	private final Room r;

	public DeliveryThread(Room r) {
	    this.r = r;
	    setDaemon(true);
	    start();
	}

	public void run() {
	    Enumeration e;
	    Chatter c;
	    while(true) {
		e = r.GetChatters();
		while(e.hasMoreElements()) {
		    c = (Chatter) e.nextElement();
                    
		    if(c.HasScroll()) {
                        c.deliverMessages();
                    }
                }
                
		try { sleep(1000L); } catch(InterruptedException ie){}
	    }
	}
    } // End DeliveryThread

    public Room() {}

    public Room(Login l, LookupTable lt, ChatObject parent) throws ChatException {
	//This sets the owner, parent ChatObject, the keyword, and the full name.
	super(l, parent, lt.getValue("sKeyWord"), lt.getValue("sName"));
	// Updates all of the web settable variables
        update(lt);
        
	thread = new DeliveryThread(this);
        
    }  
    
    
    public void start(){
        thread.start();
    }
    /**
     * This is used for when you are editing a room to pass the current chatters to the new room.
     *
     * @return the vector conating current chatter objects.
     */
    public Vector GetChatterV() {
	return vChatters;
    }  
    /**
     * @return an Enumeration of the Chatters in the Room
     */
    public Enumeration GetChatters() {
	return vChatters.elements();
    }  
    // --------------------HTML methods------------------
    /**
     * Get the name of the room and do not link it to a url.
     *
     * @return The HTML name of the room filtered with images escaped.
     */
    public String GetHTML() { return this.GetHTML(null);}
    /**
     * Escapes images in the room name and filters the tags.  If sHREF is not null,
     * links the room name to the url sHREF
     *
     * @param sHREF the url to link the name to.  null means no link.
     * @return the html name of the room.
     */
    public String GetHTML(String sHREF) {
	Filter myF = new Filter();
	return ((sHREF == null) ? "" : "<A HREF=\"" + sHREF + "\">") +
	    myF.FilterHTML(
			   //comment next line and line after sNameSub [one with only ")"] to remove
			   //image filter. This remove images from user created chatroom names
			   Filter.ImgFilter(
					    sName
					    )
			   + ((sHREF == null) ? "" : "</A>") + " - " +
			   String.valueOf(NumChatters()) + " of " + String.valueOf(MaxChatters()) +
			   " slots full");
    }  
    /**
     * @return The human readable name of the room.
     */
    public String GetName() {
	return sName;
    }  
    /**
     * @return The current room passphrase.
     */
    public String GetPass() {
	return sPass;
    }  

    /**
     * Prints out the entry page for the room.
     *
     * @param sHandle The default handle to present to the user or if registration is on the select dropdown list of the chatter's handles.
     * @param iID the current id of the Chatter on the floor.
     * @param out the output stream to write the entry page to.
     */
    Page HTMLEntryPage(Login l, String sMessage, String sDefaultHandle) throws ChatException {
      
	if(l == null) {
            throw new UnauthorizedException("Please enter a username and passphrase.");
        }
        
	Page p = (Page) pageTemplate.clone();
	p.addHeadHTML("<script language = \"Javascript\">" +
		      "if (top.location != location) top.location.href = location.href; " +
		      "</script>\n");
	p.addHeadHTML("<script language = \"Javascript\">" +
		      "\nfunction focusinput() {\n " +
		      "}\n" +
		      "</script>\n");

	//line below to prevents the room name from appearing on the room entry page in the default area ZZ
        // changed to use brackets G.T. 11/30/2017
	if(System.getProperty("chat.roomname") != null) {
            p.addHTML(Filter.ctrHTML("<H1><FONT FACE='Arial, Helvetica'>" + sName + "</FONT></H1>\n", bCtrName));
        }
        
        // changed to use brackets G.T. 11/30/2017
	if((sEntryImg != null) && !sEntryImg.equals("")) {
            p.addHTML(Filter.ctrHTML("<IMG SRC=\"" + sEntryImg + "\">", bCtrImg));
        }

	p.addHTML(Filter.ctrHTML(sEntryText, bCtrEntryText));

	if((sEntryLink1 != null) && !sEntryLink1.equals("")) {
	    p.addHTML("<UL><FONT FACE=Arial, Helvetica><LI><A HREF=\"" + sEntryLink1 + "\" TARGET=\"_blank\">" + sEntryAnchor1 + "</A>");
            
            // changed to use brackets G.T. 11/30/2017
	    if((sEntryLink2 != null) && !sEntryLink2.equals("")) {
                p.addHTML("<FONT FACE=Arial, Helvetica><LI><A HREF=\"" + sEntryLink2 + "\" TARGET=\"_blank\">" + sEntryAnchor2 + "</A>");
            }

	    p.addHTML("</UL>\n");
	}

	Form f = form(true);
	p.addHTML(f);
	f.addHTML (new Input(Input.HIDDEN, SET_ONE_VAR + SCROLL_VAR, "ON"));
	f.addHTML("<CENTER><TABLE WIDTH=450 ALIGN=CENTER BORDER=0><TR><TD VALIGN=TOP COLSPAN=2><FONT SIZE=2 FACE=ARIAL>" +
		  "</FONT>\n");
	f.addHTML(ChatObject.printCheckBox(Room.SCROLL_VAR,
					   "<FONT SIZE=2 FACE=ARIAL><B>Real Time Chat</B></FONT>",
					   System.getProperty("chat.defaultrealtime") != null));
	// NO auto scroll check box. uncomment this line for the AUTOSCROLL ONLY areas like the ZineZone ZZ
	//f.addHTML("<INPUT TYPE=\"HIDDEN\" NAME=\"" + Room.SCROLL_VAR + "\"></TD></TR><TR><TD>");

	//if(sLastKey != null) // tack on the old floor name
	//  f.addHTML("<INPUT TYPE='HIDDEN' NAME='" + LAST_ROOM_VAR + "' VALUE='" + sLastKey + "'>\n");

	f.addHTML("<P><B><FONT SIZE=2 FACE=ARIAL>" + sMessage + "</FONT></B></P></TD></TR>");
	f.addHTML("<TR><TD><CENTER><TABLE><TR>");
	f.addHTML("<TD Width=200><FONT SIZE=2 FACE=ARIAL><B>Your Handle or Name:</B><BR> ");

	if(sDefaultHandle == null) {
            sDefaultHandle = l.getLogin();
        }

	try {
	    String[] handles = UserRegistry.getHandles(l);

	    if(handles == null) {
                f.addHTML("<INPUT NAME=\"" + Chatter.HANDLE_VAR + "\" VALUE=\"" + sDefaultHandle + "\">");
            } else {
                f.addHTML( new PullDown(Chatter.HANDLE_VAR,
					UserRegistry.getHandles(l),
					sDefaultHandle));
            }	
	} catch (DataBaseException e) {
	    throw new ChatException("We are experiencing technical difficulties.  Please try back later.");
	}

	f.addHTML("</FONT></TD>");
	f.addHTML("<TD Width=200><B><FONT SIZE=2 FACE=ARIAL>Your Home Page URL:</FONT><BR><FONT SIZE=2>"
		  + "<INPUT NAME=\"" + Chatter.HOMEPAGE_VAR + "\" SIZE=\"25\"></FONT></TD></tr>");
	f.addHTML("<TR><TD><FONT SIZE=2 FACE=ARIAL><B>Tagline: </B><br>");
	f.addHTML("<Input name=\"" + Chatter.TAGLINE_VAR +"\"></FONT>");
	f.addHTML("</TD>");
	// sChatPic
	f.addHTML("<td width=200>");
        
	if (!bHtmlDis) {
            f.addHTML("<B><FONT SIZE=2 FACE=ARIAL>Your Handle Image:</FONT><BR><FONT SIZE=2><INPUT NAME=\"sChatPic\" SIZE=\"25\"></FONT>");
        }

	if(sPass != null && sPass.length() > 0) {
	    f.addHTML("<TD><FONT SIZE=2 FACE=ARIAL><B>Room Password:</B></FONT><BR>");
	    f.addHTML(new Input(Input.PASSWORD, Room.ROOM_PASS_VAR, "").addArgument("SIZE","15"));
	    f.addHTML("</TD>");
	}

	f.addHTML("</td>");
	f.addHTML("</TR><TR><TD COLSPAN=2 ALIGN=CENTER>");
	f.addHTML("<P>");
	f.addHTML(new Input(Input.IMAGE, "Enter " + sName, "ENTER").addArgument("src", buttonURL("enter.gif")));
	f.addHTML("</TD></TR></TABLE></CENTER></TR></TABLE></CENTER>");

	//print text or HTML on bottom of room entry page under the login fields
	if(sEntryBottom != null && sEntryBottom.length()>0) {
            p.addHTML(Filter.ctrHTML(sEntryBottom, bCtrEntryBottom));
        }

	p.addHTML("<CENTER><TABLE WIDTH=\"600\"><TR VALIGN=\"TOP\">");

	//Print out last few PUBLIC messages posted inside the room on the room entry page
	if(iShowRecent > 0) {
	    p.addHTML("<TD width=" + (bEntryWho ? "\"300\"" : "\"600\"") + ">" +
		      "<FONT SIZE=\"3\" FACE=\"Arial\">" +
		      "<B>The most recent PUBLIC messages posted inside this room:</B></FONT>");
	    int i;
	    boolean bFound = false;
            // this might be where the public message error is
	   // for(i = (auto_inc - iShowRecent) % mesg_limit; i < auto_inc; i = (i + 1) % mesg_limit) {
	    for(i = iShowRecent; i > 0; i--) {
		Message m = (Message)htMessages.get((auto_inc - i) % MESSAGE_LIMIT);
		if(m != null && m.is(Message.PUBLIC)) {
		    bFound = true;
		    p.addHTML("<FONT SIZE=\"2\" FACE=\"Arial\">" + m.getHTML(null) + "</FONT>");
	  	}
	    }
            
	    if(!bFound) {
                p.addHTML("<I>No recent messages</I>");
            }
            
	    p.addHTML("</FONT></TD>");
	}

	if(bEntryWho) {
	    p.addHTML("<TD><FONT SIZE=\"3\" FACE=\"Arial\">" +
		      "<B>Who is currently in this room:</B></FONT>" +
		      "<FONT SIZE=\"2\" FACE=\"Arial\">");

	    Enumeration e = GetChatters();
	    while(e.hasMoreElements()) {
		p.addHTML(((Chatter) e.nextElement()).getHTML(true,bIdleTimes));
		p.addHTML("<BR>");
	    }

	    p.addHTML("</FONT></TD>");
	}

	p.addHTML("</TR></TABLE></CENTER>");
	//the CustomChat signature on the room entry pages
	p.addHTML("<P><center>" + HTMLGetBottom(null) + "</center>");
	p.addHTML("<CENTER><FONT SIZE=\"1\" FACE=Arial,Helvetica,Geneva>Powered by " +
		  "<A HREF=\"http://customchat.com\"><B>CustomChat Server</B></A> since 1997</FONT></CENTER></P>");

	return p;
    }
    /**
     * @param ct the chatter to customize this display for.
     * @return the HTML code for the very bottom of the room.
     */
    protected String HTMLGetBottom(Chatter ct) {

	String s = "<B>";
        // changed to use brackets G.T. 11/30/2017 (all 3 if statements)
	if((sOwnerHandle != null) && ! sOwnerHandle.trim().equals("")) {
            s += "<FONT FACE=Arial, Helvetica SIZE=1>Room created by:</FONT> <FONT FACE=Arial, Helvetica SIZE=2>" + sOwnerHandle + " </FONT>&nbsp;&nbsp; ";
        }

	if((sEmail != null) && ! sEmail.trim().equals("")) {
            s += "<FONT FACE=Arial, Helvetica SIZE=1>Email:</FONT> <FONT FACE=Arial, Helvetica SIZE=2><A HREF=\"mailto:" + sEmail + "\">" + sEmail + "</A></FONT><BR> ";
        }

	if((sHome != null) && ! sHome.trim().equals("")) {
            s += "<FONT FACE=Arial, Helvetica SIZE=1>Homepage:</FONT> <FONT FACE=Arial, Helvetica SIZE=2><A HREF=\"" + sHome + "\" TARGET=\"_blank\">" + sHome + "</A></FONT>";
        }

	return s + "</B>";
    }  

    private String HTMLGetPullDown(String sFieldName) {
	StringBuilder sb;
        sb = new StringBuilder("");

	//should change these to use PullDown() !!!
	if (getSoundList() != null)
	    sb.append("&nbsp;<FONT SIZE=2><SELECT NAME=\"Sound").append(sFieldName).append("\" onchange='if(this.selectedIndex != 0) document.Bottom.").append(sFieldName).append(".value += " + "\"<embed src=\\\"\" + this.options[" + "this.selectedIndex].value +" + "\"\\\" hidden=\\\"False\\\" autostart=\\\"true\\\" align border=\\\"0\\\"" + "width=\\\"140\\\" height=\\\"30\\\" >\";" + "document.Bottom.").append(sFieldName).append(".focus();'>" + "<option>Sounds").append(getString(getSoundList())).append("</select>\n");

	if (getImageList() != null) {
            sb.append("<SELECT NAME=\"Picture").append(sFieldName).append("\" onchange='if(this.selectedIndex != 0) document.Bottom.").append(sFieldName).append(".value += " + "\"<img src=\\\"\" + this.options[" + "this.selectedIndex].value +\"\\\" border=\\\"0\\\">\";" + "document.Bottom.").append(sFieldName).append(".focus();'><option>Images").append(getString(getImageList())).append("</select></FONT>\n");
        }
        
	return sb.toString();
    }
    //------------------- Data Abstraction -------------------
    /**
     * Uses the room name as the hash key.
     *
     * @return a unique identifying Object for use in a hash table.
     */
    public String HashKey() {
	return sName;
    }  
    /**
     * @return the maximum number of normal chatters. i.e. non-admins
     */
    public int MaxChatters() {
	return iMaxChatters;
    }  
    /**
     * @return returns the current number of chatters in the room including admins.
     */
    public int NumChatters() {


	return vChatters.size();
    }  
    /**
     * Changes the name of the room
     *
     * @param s the new name of the room.
     */
    public synchronized void SetName(String s) {
	sName = s;
    }  
    /** -----------------------------------------------------------------------------------
     * Tries to add the user to the room.  Fails if the room is full and the chatter is not
     * an admin or the owner.  Also fails if the room password is incorrect.
     * On success send the arrival message to the room.
     *
     * @param ct the Chatter to add to the room.
     * @param sRoomPass the room passphrase supplied by the user.
     * @return the chatter object if successful or null otherwise.
     */
    
    /**
     * -----------------------------------------------------------------------------------
 Tries to add the user to the room.Fails if the room is full and the chatter is not
 an admin or the owner.  Also fails if the room password is incorrect.
 On success send the arrival message to the room.
     * @param l
     * @param lt
     * @return the chatter object if successful or null otherwise.
     * @throws customchat.chat.ChatException
     */
    protected Page add(Login l, LookupTable lt) throws ChatException {
	
	if(l == null) throw new UnauthorizedException("Please enter your username and passphrase.");
	if(lt == null)
	    return HTMLEntryPage(l, "Please choose a handle.  This will be your persona in the room.", null);

	
	// use this "infrequent" opportunity to delete the admin transcript if it's over 250K
	deleteTranscript() ;	 

	  
	String sRoomPass = lt.getValue(ROOM_PASS_VAR);
	bScroll  = lt.getValue(SCROLL_VAR) != null;
	String handle = lt.getValue(Chatter.HANDLE_VAR);
	// Need to filter the handle before hashing because Chatter filters it in update.
	if (handle != null)
	    handle = (new Filter()).FilterHTML(handle);

	if (sPass != null && !sPass.equals("") && !sPass.equals(sRoomPass) && !isAdmin(l))
	    return HTMLEntryPage(l, "This is a private room.\n"
				 + "Please enter the room password below.",
				 handle);

	if(handle == null)
	    return HTMLEntryPage(l, "Please choose a handle.  This will be your persona in the room.", handle);

	Chatter c = null ;

	try {
	    c = UserRegistry.getChatter(l, handle, this); 
	} catch (DataBaseException e) {
	    ErrorLog.error(e, 401, "Database Malfunction");
	    throw new ChatException("We are experiencing technical difficulties<br>/n"
       			    + "Please try back in a few minutes.");
	}
	
	try {
      	    c.update(lt);
	    addChatter(c);
	} catch (RoomFullException e) {
	    Container con = new Container() ;
	    String m = "<p><CENTER><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">This Room is FULL or" +
		       " PASSWORD PROTECTED.<B>" +
		       "<p><A HREF=\"" + commandURL(SWITCH, c)+"&"+DESTINATION_VAR+"="+parent.sKeyWord+"\">Back to Live List" +
		       "</A></FONT></CENTER>" ;
	    Text t = new Text(m) ;
	    con.addHTML(t) ;
	    Page p = PageFactory.makeUtilPage(con);
	    return p ;
	}catch (ChatException e) {
	    return HTMLEntryPage(l, e.getMessage(), handle);
	}
	// We now have a chatter with the correct passwords in the room

	if(bScroll || c.HasScroll()) { // what happens to old autoscroll if already in room
	    return scrollPage(c);
	}

	return manual(c);
    }      

    /**
     *
     * @param c
     * @throws ChatException
     */
    @Override
    public void addChatter(Chatter c)
	throws ChatException {
	if(!inRoom(c)) {
	    synchronized(vChatters) {
		boolean isAdm = isAdmin(c.getLogin()) ;
		if((NumChatters() < MaxChatters()) || isAdm) {
		    super.addChatter(c);
		    c.isAdmin = isAdm ;
		    vChatters.addElement(c);
		} else {
		    System.out.println("Throwing RoomFullException") ;
		    throw new RoomFullException("This room is limited to " + MaxChatters() + " users.");
		}
	    }

	    String m  = c.getFullHTML() + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">has arrived...</FONT>" ;
	    	
	    this.send(new Message(m,
				  House.ctDaemon,
				  (String)null,
				  Message.ADDCHATTER));
	    

	    c.setLocation(this);

	    if(isOwner(c.getLogin())) {
		InetAddress addr = null ;
		try {
		addr = InetAddress.getLocalHost() ;
		} catch (java.net.UnknownHostException exc) {
		}
		c.addMessage(new Message(
			 "<B><FONT FACE=\"Arial,Helvetica,Geneva,Verdana\" size=\"2\">Welcome Room Owner! <BR>"
			 + "To create a LINK to your CustomChat Room, just copy the code"
			 + " below.:</FONT><BR>"
			 + "<FONT FACE=\"Arial,Helvetica,Geneva,Verdana\" SIZE=\"1\">"
			 + "&lt;A&nbsp;HREF=\"http://"+addr.getHostAddress()+":"+Server.getPort() + "/" + sKeyWord + "\">"
			 + "My CustomChat Room&lt;/A></FONT>"
			 + "<BR><FONT FACE=\"Arial,Helvetica,Geneva,Verdana\" size=\"2\">There are buttons at the top of "
			 + "the room to Remove/Boot unruly chatters, Edit or Delete this room.</B>",
			 House.ctDaemon,
			 c.sHandle,
			 Message.SYSTEM));
	    }
	}
    }                    

    /**
     *
     * @return
     * @throws ChatException
     */
    @Override
    protected String createChildPage() throws ChatException {
	String s = fileToString(Room.NEW_HTML);
	s = replace(s, "#ParentName#", sName);
	s = replace(s, "#ParentKey#", sKeyWord);
	return s;
    }  

    /**
     *
     * @return
     */
    @Override
    protected String getChildName() {
	return "Inner Room";
    }  
    @Override
    public synchronized void cleanup() {
	for(int i = 0; i < vChatters.size(); i++)
	    try {
		UserRegistry.endSession(((Chatter) vChatters.elementAt(i)).HashKey());



	    } catch (ChatException ignored) {}
	super.cleanup();
    }  

    /**
     *
     * @param command
     * @return
     */
    @Override
    public String commandButton(final int command) {
	String sURL;

	switch(command) {
	case MANUAL :
	    sURL = buttonURL("getnewmessages.gif");
	    break;
	case SWITCH :
	    sURL = buttonURL("switchrooms.gif");
	    break;
	case EXIT :
	    sURL = buttonURL("exit.gif");
	    break;
	case SCROLL_LIST :
	case OPTIONS :
	    sURL = buttonURL("setoptions.gif");
	    break;
	default :
	    return super.commandButton(command);
	}

	return sURL;
    }  
    private String commandURL(int iCommand, Chatter c) {
	return super.commandURL(iCommand) + "&" + Chatter.HANDLE_VAR + "="
	    + URLEncoder.encode(c.sHandle);
    }  

    /**
     *
     * @param l
     * @param lt
     * @return
     * @throws ChatException
     */
    @Override
    public Page defaultCommand(Login l, LookupTable lt)
	throws ChatException {
	return add(l, lt);
    }
	
    /**
     *
     * @param l
     * @param m
     * @return
     * @throws ChatException
     */
    @Override
    public Page doBroadcast(Login l, Message m)
	throws ChatException {
	super.doBroadcast(l, m);
	send(m);
	return parent.defaultCommand(l, null);
    } 


	
    protected Container doCommand(Login lUser, LookupTable lt, PrintWriter out, int iCommand)
	throws ChatException, IOException {
	
	Container con;
	Chatter c = getChatter(lUser, lt.getValue(Chatter.HANDLE_VAR));


	if (c != null)
	    c.MakeContact();

	try {
	    switch(iCommand) {
	    case ADD:
		con = add(lUser, lt);
		break;
            case MANUAL:
		if (lt.getValues().length > 3) {
                    c.update(lt);
                    
                    if (lt.getValue("bNoPic") != null) {
                        c.bNoPic = false ;
                    } else {
                        c.bNoPic  = true ;
                    }

                    if (lt.getValue("bNoPicHandle") != null) {
                        c.bNoPicHandle = false ;
                    } else {
                        c.bNoPicHandle  = true ;
                    }

                    if (lt.getValue("bNoSound") != null) {
                        c.bNoSound = false ;
                    } else {
                        c.bNoSound  = true ;
                    }

                    if (lt.getValue("bIgEntry") != null) {
                            c.bIgEntry = true;
                    } else  {
                            c.bIgEntry  = false ;
                    }

                    if (lt.getValue("bUserDing") != null) {
                            c.bUserDing = true ;
                    } else  {
                            c.bUserDing  = false ;
                    }

                    if (lt.getValue("bIgOutside") != null) {
                            c.bIgOutside = true ;
                    } else  {
                            c.bIgOutside  = false ;
                    }

                    String v ;
                    if ((v = lt.getValue("XPublic")) != null && !v.equals("")) {
                            lt.put("Public",v) ;
                    }
		}

		send(c, lt);
		con = manual(c);
		break;
            case SCROLL_TOP:
		con = getTop(c, true);
		break;
	    case SCROLL_MIDDLE:
		con = doMiddleFrame(c);
		break;
	    case SCROLL_LIST:
		c.update(lt);
		con = doScrollList(c, lt.getValue("refreshtime"));
		break;
	    case SCROLL_MESSAGES:
		if(c == null) {
                    throw new ChatterNotFoundException();
                }

		throw new AutoScrollException(c);

	    case SCROLL_SEND:
		send(c, lt);
               con = scrollSend(c);
		break;
	    case OPTIONS:
		// options() throws ChatException if Chatter not in room (because it's
		// in its own window), but send throws ChatterNotFoundException, effectively
		// opening the room in the little window.  So, this order is important.
		if(lt.getValue(VAR_OPTIONS_SUBMITTED) != null) {
		    con = new Page();
		    con.addHTML("<script>\n <!-- \nself.close();\n//-->\n</script>");
		} else {
		    con = options(c);
		}
		break;
            case PASTE_POP:
		con = pastePop(c) ;	
		break ;
            case PASTE_SEND:
		con = null ;
		send(c, lt);
		con = new Page();
		con.addHTML("<script>\n <!-- \nself.close();\n//-->\n</script>");
		break ;
	    case SET_OPTIONS:
		c.update(lt);
		if (lt.getValue("bNoPic") != null) {
		    c.bNoPic = false ;
		} else {
		    c.bNoPic  = true ;
		}

		if (lt.getValue("bNoPicHandle") != null) {
		    c.bNoPicHandle = false ;
		} else {
		    c.bNoPicHandle  = true ;
		}

		if (lt.getValue("bNoSound") != null) {
		    c.bNoSound = false ;
		} else {
		    c.bNoSound  = true ;
		}

		if (lt.getValue("bIgEntry") != null) {
		    c.bIgEntry = true ;
		} else {
		    c.bIgEntry  = false ;
		}

		if (lt.getValue("bUserDing") != null) {
		    c.bUserDing = true ;
		} else {
		    c.bUserDing  = false ;
		}

		con = new Page();
		con.addHTML("<script>\n <!-- \nself.close();\n//-->\n</script>");
		break ;
	    case SWITCH:
		con = doSwitch(lUser, lt);
		break;
	    case EXIT:
		con = doExit(c, lt);
		break;
	    case CREATE_PAGE:
                /* falls through */
	    case CREATE :
		throw new ChatException("Not Implemented");
                /* falls through */
	    case COM_POST_IMAGES:
		con = doPostImages(lUser, lt.getFullValue(VAR_POST_IMAGE),lt);
		break;
	    case COM_POST_SOUNDS:
	     	con = doPostSounds(lUser, lt.getFullValue(VAR_POST_SOUND),lt);
		break;
	    case COM_REMOVE_SOUNDS:
		con = doRemoveSounds(lUser,lt);
		break;
	    case COM_REMOVE_IMAGES:
		con= doRemoveImages(lUser,lt);
		break;
	    default:
		con = super.doCommand(lUser, lt, out, iCommand);
                break;
	    }
	} catch(ChatterNotFoundException e) {
	    con = popPage(ADD, lt);
	} 
	return con;
    }      

    public Page doRemoveImages(Login l, LookupTable lt) throws ChatException {
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }

	clearImageList();
	saveImageList();

	Container c = new Container() ;
	Text t = new Text("<center><b>The postable image dropdown is removed.</b></center>");
	c.addHTML(t) ;
	Page p = PageFactory.makeUtilPage(c);

	return p ;
    }

    public Page doRemoveSounds(Login l, LookupTable lt) throws ChatException {
        // changed to use brackets G.T. 11/30/2017
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }

	clearSoundList();
	saveSoundList();
	Container c = new Container() ;
	Text t = new Text("<center><b>The postable sound dropdown is removed.</b></center>") ;
	c.addHTML(t) ;
	Page p = PageFactory.makeUtilPage(c);

	return p ;
    }

    protected Page doExit(Chatter c, LookupTable lt)
	throws ChatException {
	if(c != null) {
	    UserRegistry.endSession(c.HashKey());
	    doScrollList(c,"");
	}
        
	throw new RedirectException(exitPage());
    }  

    Page doMiddleFrame(Chatter c) throws ChatterNotFoundException  {
        // changed to use brackets G.T. 11/30/2017
	if(!inRoom(c)) {
            throw new ChatterNotFoundException();
        }

 	Page p = new Page();
	//p.addFrameSetArg("COLS","75%,*");
	//p.addFrameSetArg("BORDER","1");
	p.addHTML("<FRAME NAME=\"" + FRAME_MESSAGES + "\" SRC=\""
		   + commandURL(SCROLL_MESSAGES, c)
		   + "\" MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\" "
		   + "SCROLLING=\"auto\" BORDER=\"1\" FRAMEBORDER=\"1\">");
	p.addHTML("<FRAME NAME=\"" + FRAME_LIST + "\" SRC=\""
		   + commandURL(SCROLL_LIST, c)
		   + "\" MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\" "
		   + "SCROLLING=\"auto\" BORDER=\"1\" FRAMEBORDER=\"1\">");
	return p;
    }  
    private Page doPostImages(Login l, String sa[]) throws ChatException {
        // changed to use brackets G.T. 11/30/2017
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }

        // removed unused int j variable
	int i;
        // removed unused String sDesc variable
	String sURL;

	if (sa != null) {
	    clearImageList();
            // changed to use brackets (for and if statements) G.T. 11/30/2017
	    for (i = 0; i < sa.length; i++) {
                if (sa[i] != null && (sURL = (String)House.POST_IMAGES.get(sa[i])) != null) {
                    addImage(sa[i], sURL);
                }
            }

	    saveImageList();
	    throw new RedirectException(URL_PREFIX + getKeyWord());
	} 
	
	House.readImages();

	Form f = form(FORM_POST_IMAGES, COM_POST_IMAGES);
	f.addHTML("<font face=\"Helvetica,Arial,Geneva\">Click an image to preview</font><p>") ;
	f.addHTML("<IMG NAME=\"eg\" HEIGHT=100 WIDTH=200><BR>");
	f.addHTML("<TABLE>\n");
	Enumeration e = House.POST_IMAGES.keys();
	i = 0;
	while (e.hasMoreElements()) {
            // changed to use brackets G.T. 11/30/2017
	    if (i % 6 == 0 && i != House.POST_IMAGES.size() - 1) {
                f.addHTML((i != 0 ? "</TR>\n" : "") + "<TR>");
            }
            
	    i++;

	    f.addHTML("<TD>");
	    String sName = (String)e.nextElement();
	    Input box = new Input(Input.CHECKBOX, VAR_POST_IMAGE, sName);
            // changed to use brackets G.T. 11/30/2017
	    if (haveImage(sName)) {
                box.addArgument(Flag.CHECKED);
            }
	
	    f.addHTML(box);
	    f.addHTML("<A HREF=\"\" onCLICK=\"document.images.eg.src='"
		      + House.POST_IMAGES.get(sName) + "';return false\">"
		      + sName + "</A>");
	    f.addHTML("</TD>\n");
	}
	f.addHTML("</TR></TABLE>\n");
	f.addHTML(new Input(Input.SUBMIT, "", "Set"));

	return PageFactory.makeUtilPage(f);
    }            

    private Page doPostImages(Login l, String sa[],LookupTable lt) throws ChatException {
        // changed to use brackets G.T. 11/30/2017
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }

        // removed unused int j variable
	int i;
        // removed unsused String sDesc variable
	String sURL;	

	// switch is a value which is added as a field to the form to indicate the call came from the form, rather than the
	// postale image manager call ..
  
	if (lt.getValue("switch") != null ) {
	    // do this strange clear save in case the user is trying to switch everything off
	    clearImageList();
	    if (sa != null) {
		for (i = 0; i < sa.length; i++) {
                    // changed to use brackets G.T. 11/30/2017
		    if (sa[i] != null && (sURL = (String)House.POST_IMAGES.get(sa[i])) != null) {
                        addImage(sa[i], sURL);
                    }
		}
	 
		//Page bottomPage = new Page();
		//Container script = new Container("SCRIPT", "\n<!--\nself.close();\n//-->\n");
		//p.addHTML(script);

		//return bottomPage;
	    }
	    // in either case throw us back to the list ....
	    saveImageList();
	    throw new RedirectException(URL_PREFIX + getKeyWord());
	}

	// this is actually called from the list and the Postable Image Manager drop down 
	House.readImages();
	Container con = new Container() ;

	Form f = form(FORM_POST_IMAGES, COM_POST_IMAGES);
	f.addHTML("<input type = \"hidden\" name=\"switch\" value=\"1\">") ;
	f.addHTML("<font face=\"Helvetica,Arial,Geneva\">Click an image to preview</font><p>") ;
	f.addHTML("<IMG NAME=\"eg\" HEIGHT=100 WIDTH=200><BR>");
	f.addHTML("<TABLE>\n");
	Enumeration e = House.POST_IMAGES.keys();
	i = 0;
	while (e.hasMoreElements()) {
            // changed to use brackets G.T. 11/30/2017
	    if (i % 6 == 0 && i != House.POST_IMAGES.size() - 1) {
                f.addHTML((i != 0 ? "</TR>\n" : "") + "<TR>");
            }
		
	    i++;

	    f.addHTML("<TD>");
	    String sName = (String)e.nextElement();
	    Input box = new Input(Input.CHECKBOX, VAR_POST_IMAGE, sName);
	    if (haveImage(sName))
		box.addArgument(Flag.CHECKED);
	    f.addHTML(box);
	    f.addHTML("<A HREF=\"\" onCLICK=\"document.images.eg.src='"
		      + House.POST_IMAGES.get(sName) + "';return false\">"
		      + sName + "</A>");
	    f.addHTML("</TD>\n");
	}
	f.addHTML("</TR></TABLE>\n");
	f.addHTML(new Input(Input.SUBMIT, "", "Set"));

	con.addHTML(f) ;
	con.addHTML(doMakeRemoveForm()) ;

	Page p = PageFactory.makeUtilPage(con);

	return p ;
    }                                          

    private Form doMakeRemoveForm() {
	Form f = form(FORM_POST_IMAGES, COM_REMOVE_IMAGES);
	f.addHTML(new Input(Input.SUBMIT, "", "Remove All"));
	return f ;
    }

    private Page doPostSounds(Login l, String sa[]) throws ChatException {
        // changed to use brackets G.T. 11/30/2017
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }

        // removed unused int j variable
	int i;
        // removed unused String sDesc variable
	String sURL;

	if (sa != null) {
	    clearSoundList();
            // changed to use brackets (for and if statements) G.T. 11/30/2017
	    for (i = 0; i < sa.length; i++) {
                if (sa[i] != null && (sURL = (String)House.POST_SOUNDS.get(sa[i])) != null) {
                    addSound(sa[i], sURL);
                }
            }

	    saveSoundList();
	    throw new RedirectException(URL_PREFIX + getKeyWord());

	    //Page bottomPage = new Page();
	    //Container script = new Container("SCRIPT", "\n<!--\nself.close();\n//-->\n");
	    //p.addHTML(script);

	    //return bottomPage;
	}

	House.readSounds();

	Container con = new Container() ;

	Form f = form(FORM_POST_SOUNDS, COM_POST_SOUNDS);
	f.addHTML("<TABLE>\n");
	Enumeration e = House.POST_SOUNDS.keys();
	i = 0;
	while (e.hasMoreElements()) {
            // changed to use brackets G.T. 11/30/2017
	    if (i % 6 == 0 && i != House.POST_SOUNDS.size() - 1) {
                f.addHTML((i != 0 ? "</TR>\n" : "") + "<TR>");
            }
		
	    i++;

	    f.addHTML("<TD>");
	    String sName = (String)e.nextElement();
	    Input box = new Input(Input.CHECKBOX, VAR_POST_SOUND, sName);
            // changed to use brackets G.T. 11/30/2017
	    if (haveSound(sName)) {
                box.addArgument(Flag.CHECKED);
            }
		
	    f.addHTML(box);
	    f.addHTML("<A HREF=\"" + House.POST_SOUNDS.get(sName) + "\" TARGET=_blank>"
		      + sName + "</A>");
	    f.addHTML("</TD>\n");
	}
	f.addHTML("</TR></TABLE>\n");
	f.addHTML(new Input(Input.SUBMIT, "", "Set"));

	con.addHTML(f) ;

	Form f2 = form(FORM_POST_IMAGES, COM_REMOVE_SOUNDS);
	f2.addHTML(new Input(Input.SUBMIT, "", "Remove All"));
	con.addHTML(f2) ;

	return PageFactory.makeUtilPage(con);
    }  

    private Page doPostSounds(Login l, String sa[], LookupTable lt) throws ChatException {
        // changed to use brackets G.T. 11/30/2017
	if (!hasPrivilege(l, CAN_EDIT)) {
            throw new UnauthorizedException("You do not have edit privileges for " + sName);
        }
	    
        // removed unused int j variable
	int i;
        // removed unused String sDesc variable
	String sURL;

	// switch is a value which is added as a field to the form to indicate the call came from the form, rather than the
	// sound manager call ..

	if (lt.getValue("switch") != null) {
	    // do this strange clear save in case the user is trying to switch everything off
	    clearSoundList();
	    if (sa != null) {
		for (i = 0; i < sa.length; i++) {
                    // changed to use brackets G.T. 11/30/2017
		    if (sa[i] != null && (sURL = (String) House.POST_SOUNDS.get(sa[i])) != null) {
                        addSound(sa[i], sURL);
                    }
		}
	    }
	    saveSoundList();
	    throw new RedirectException(URL_PREFIX + getKeyWord());

	    //Page bottomPage = new Page();
	    //Container script = new Container("SCRIPT", "\n<!--\nself.close();\n//-->\n");
	    //p.addHTML(script);

	    //return bottomPage;
	}

	Container con = new Container() ;

	House.readSounds();
	Form f = form(FORM_POST_SOUNDS, COM_POST_SOUNDS);
	f.addHTML("<input type = \"hidden\" name=\"switch\" value=\"1\">");
	f.addHTML("<TABLE>\n");
	Enumeration e = House.POST_SOUNDS.keys();
	i = 0;
	while (e.hasMoreElements()) {
            // changed to use brackets G.T. 11/30/2017
	    if (i % 6 == 0 && i != House.POST_SOUNDS.size() - 1) {
                f.addHTML((i != 0 ? "</TR>\n" : "") + "<TR>");
            }
            
	    i++;
	    f.addHTML("<TD>");
	    String sNameSub;
            sNameSub = (String) e.nextElement();
	    Input box = new Input(Input.CHECKBOX, VAR_POST_SOUND, sNameSub);
            // changed to use brackets G.T. 11/30/2017
	    if (haveSound(sNameSub)) {
                box.addArgument(Flag.CHECKED);
            }
		
	    f.addHTML(box);
	    f.addHTML("<A HREF=\"" + House.POST_SOUNDS.get(sNameSub) + "\" TARGET=_blank>" + sNameSub + "</A>");
	    f.addHTML("</TD>\n");
	}
	f.addHTML("</TR></TABLE>\n");
	f.addHTML(new Input(Input.SUBMIT, "", "Set"));

	con.addHTML(f);

	Form f2 = form(FORM_POST_IMAGES, COM_REMOVE_SOUNDS);
	f2.addHTML(new Input(Input.SUBMIT, "", "Remove All"));

	con.addHTML(f2);
        
	return PageFactory.makeUtilPage(con);
    }

    Page doScrollList(Chatter c, String sRefresh) {
	if (sRefresh == null || sRefresh.length() == 0) {
            sRefresh = "";
        }

	try {
	    double d;
            d = (new Double(sRefresh));
            // changed to use brackets (both if statements) G.T. 11/30/2017
	    if (d < 5) {
                sRefresh = "5";
            }
            
	    if (d > 600) {
                sRefresh = "";
            }	
	} catch (NumberFormatException e) {
	    sRefresh = "";
	}

	Page p = (Page) pageTemplate.clone();
	Form f = form(FORM_WHO_LIST, true, SCROLL_LIST, c);

	// add a dummy javascript function focusinput in here ...
	// focusinput is only meant to be used with the bottom input frame but because of 
	// cloning (which I don't want to mess with cos it will probably have 
	// repercussions throughout)  the call to the function turns up in here too, in the body load
	// so simply add a dummy function body and walk away ;)

	String df = "\n<SCRIPT>\n" +
	    "function focusinput() { \n" +
	    " }\n" +
	    "</SCRIPT>\n" ;
	
	p.addHeadHTML(df) ;
	p.addHTML(f);
	f.addHTML("<TABLE BORDER=0><tr><td>");
	f.addHTML("<FONT FACE=Arial, Helvetica SIZE=3><B>" + String.valueOf(numChatters())
		  + " active chatter" + (numChatters() != 1 ? "s" : "") + ".</B></font>");
	f.addHTML("</TD></TR>");
	//Print Chatters
	f.addHTML("<TR><TD>");
	
	if (!bPMDis || isAdmin(c.lUser)) {
	    f.addHTML(new Image(buttonURL("pmcheck.gif"), "Private Message Recipient"));
	}
	
	f.addHTML(new Image(buttonURL("igcheck.gif"), "Ignore Public Messages"));
	
        // changed to use brackets G.T. 11/30/2017
	if (!bPMDis || isAdmin(c.lUser)) {
            f.addHTML(new Image(buttonURL("igpmcheck.gif"), "Ignore Private Messages"));
        }

	f.addHTML("</TD></TR><TR><TD>\n");

	Enumeration e = GetChatters();

	if (!bPMDis || isAdmin(c.lUser)) {
	    f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.PM_RECIPIENT_VAR, "TRUE"));
	}

	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.PUBLIC_IGNORE_VAR, "TRUE"));
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.PRIVATE_IGNORE_VAR, "TRUE"));

	while(e.hasMoreElements()) {
	    Chatter cCurr =(Chatter)e.nextElement();
	    Input i;
	    
	    if (!bPMDis || isAdmin(c.lUser)) {
		// Print Private Message Recip Check Box
		f.addHTML(i = new Input(Input.CHECKBOX, Chatter.PM_RECIPIENT_VAR, cCurr.HashKey()));
                // changed to use brackets G.T. 11/30/2017
		if(c.isPMRecip(cCurr.HashKey())) {
                    i.addArgument("CHECKED");
                }
                    
		i.addArgument("ONCLICK", "submit()");
	    }

	    f.addHTML(i = new Input(Input.CHECKBOX, Chatter.PUBLIC_IGNORE_VAR, cCurr.HashKey()));
            
            // changed to use brackets G.T. 11/30/2017
	    if(c.isIgPub(cCurr.HashKey())) {
                i.addArgument("CHECKED");
            }

	    i.addArgument("ONCLICK", "submit()");

	    if (!bPMDis || isAdmin(c.lUser)) {
		f.addHTML(i = new Input(Input.CHECKBOX, Chatter.PRIVATE_IGNORE_VAR, cCurr.HashKey()));
                // changed to use brackets G.T. 11/30/2017
		if(c.isIgPriv(cCurr.HashKey())) {
                    i.addArgument("CHECKED");
                }
		    	
		i.addArgument("ONCLICK", "submit()");
	    }

	    if(isAdmin(c.getLogin())) {
                f.addHTML(bootHTML(cCurr));
            }
		
	    String text = cCurr.getText();

	    f.addHTML("<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2>"
		      + (text.length() > 15
			 ?  text.substring(0,15)
			 + "<img src=\"" + buttonURL("dot.gif") + "\" alt=\"" + text + "\">"
			 :  text)
		      + "</FONT><BR>\n");
	}

	f.addHTML("<TR><TD>");

	Input update = new Input(Input.IMAGE, "Save your options and refresh the frame", "Save/Refresh");
	update.addArgument("SRC",buttonURL("refresh.gif"));
	f.addHTML(update);

	f.addHTML("<P><SCRIPT>\n<!--\n"
		  + "function setRefresh(refreshtime) {\n"
		  + "\tif(refreshtime > 0) setTimeout('document." + FORM_WHO_LIST
		  + ".submit();', refreshtime*1000)\nelse\n\tclearTimeout();\n}\n"
		  + "setRefresh(" + sRefresh + ");\n//-->\n</SCRIPT>");
/*
	f.addHTML("<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=1>"
		  + "Refresh every <input size=3 name=refreshtime value=\""
		  + sRefresh
		  + "\" onchange=\"setRefresh(this.value)\"> "
		  + "seconds <input type=image src=\"" + buttonURL("set.gif") + "\"value=set border=0></FONT>");
*/
	f.addHTML("</TD></TR></TABLE>\n");

	return p;
    }    

    protected Page doSwitch(Login l, LookupTable lt) throws ChatException{
        String destKey = lt.getValue(DESTINATION_VAR);
        // changed to use brackets G.T. 11/30/2017
	if(destKey == null) {
            destKey = sDefaultSwitchKey;
        }
			
	ChatObject dest = registry.get(destKey);
	return dest.defaultCommand(l, lt);
    }  

    private Form form(String name, Chatter c){
	return form(name, false, c);
    }  

    private Form form(String name, boolean bGet, int iCommand, Chatter c) {
	Form f = form(name, bGet, c);
	f.addHTML(new Input(Input.HIDDEN, COM_VAR, String.valueOf(iCommand)));
	return f;
    }  

    private Form form(String name, boolean bGet, Chatter c){
		Form f = new Form(name, URL_PREFIX + getKeyWord() + "?" + (new Date()).getTime(), bGet ? "GET" : "POST");
		if(c != null) {
                    f.addHTML(new Input(Input.HIDDEN, Chatter.HANDLE_VAR, c.sHandle));
                    if(c.HasScroll()) {
                        f.addHTML(new Input(Input.HIDDEN, SCROLL_VAR, "1"));
                    }
		}
		return f;
    }  

    Chatter getChatter(Login l, String sHandle) {
	Chatter c = null;
	Enumeration e = vChatters.elements();
	while(e.hasMoreElements()) {
            if((c = (Chatter)e.nextElement()).verify(l, sHandle)) {
                return c;
            }
        }
                            
        return null;
    }  

    Chatter getChatter(Object o) {
	Enumeration e = GetChatters();
	Chatter c = null;
        // changed to use brackets (both white and if statements) 11/30/2017
	while(e.hasMoreElements()) {
	    if((c = (Chatter)e.nextElement()).hashKeyIs(o)) {
                return c;
            }
        }
       
	return null;
    }  
	
    public Page getTop(Chatter c, boolean bScrolling) throws ChatException {
	
        // changed to use brackets 11/30/2017
	if(c == null) {
            throw new ChatterNotFoundException();
        } 
	//Make Form and Buttons and the suchlike
	Page p = (Page) pageTemplate.clone();
	//Print Title Image

	
	p.addHeadHTML("<SCRIPT>\n" + "<!--\n" + "function focusinput() {\n" + "   } \n" + "// -->\n" + "</SCRIPT>\n");
	
	if (video != null && !video.equals("")) {
	    if (video.equals("webcam")) {
		p.addHeadHTML("<meta http-equiv=\"Pragma\" content=\"no-cache\">\n");
      		p.addHeadHTML("<meta http-equiv=\"REFRESH\" content=\"90\">\n");
	    }
	}

	p.addHTML("<section id='top-content'>");
	if((sTop!=null) && !sTop.equals("")) {
	    p.addHTML("<div class='top-image' style='float:left;'>") ;
	    p.addHTML(Filter.ctrHTML("<IMG SRC=\"" + sTop + "\">", bCtrTop));
	    p.addHTML("</div>") ;
	}

	p.addHTML("<div class='room-video'>");
	if (video != null && !video.equals("")) {
	    
	    if (bCtrVid) {
		p.addHTML("<CENTER>");
	    }
	    
	    if (video.equals("winvid")) {
		    p.addHTML("<EMBED src=\""+sVideo+"\" AUTOSTART=NO HEIGHT=175 WIDTH=180>");
	    }

	    if (video.equals("realvid")) {
		p.addHTML("\n<EMBED TYPE=\"audio/x-pn-realaudio-plugin\"\n");
		p.addHTML("SRC=\""+sVideo+"\" WIDTH=150 HEIGHT=116\n");
		p.addHTML("CONTROLS=IMAGEWINDOW\n"); 
		p.addHTML("AUTOSTART=FALSE CONSOLE=CLIP1>") ;
		p.addHTML("<BR>");
	    }

	    if (video.equals("webcam")) {
		p.addHTML("<IMG SRC=\""+sVideo+"\">");
	    }
	   
            // changed to use brackets 11/30/2017
	    if (bCtrVid) {
                p.addHTML("</CENTER>") ;
            }	   

	}

	p.addHTML("</div><div class='room-text-and-links'>") ;
	//the Creator - set entry page text then set users chosen links
	if((sRoomText != null) && !sRoomText.equals("")) {
	    p.addHTML(Jsoup.parseBodyFragment(sRoomText).toString());
	}

	if((sRoomLink1 != null) && !sRoomLink1.equals("")) {
	    p.addHTML("<UL><LI><A HREF=\"" + sRoomLink1 + "\" TARGET=\"_blank\">" + sRoomAnchor1 + "</A>");
            // changed to use brackets 11/30/2017
	    if((sRoomLink2 != null) && !sRoomLink2.equals("")) {
                p.addHTML("<LI><A HREF=\"" + sRoomLink2 + "\" TARGET=\"_blank\">" + sRoomAnchor2 + "</A>");
            }

	    p.addHTML("</UL>");

	}
        
	p.addHTML("</div></section>") ;

	Form f = form("TopAdminForm", c);
	p.addHTML(f);

	//-------- buttons ----------
	if(!bScrolling) {
	    f.addHTML("<CENTER>");

	    //get new messages or Refresh in Autoscroll
	    f.addHTML(new Link(commandURL(MANUAL, c), new Image(commandButton(MANUAL)), CHAT_FRAME));

	    //select box for floor to switch to
	    PullDown pd = new PullDown( DESTINATION_VAR );
	    f.addHTML(pd);

	    House h = null;
	    try {
		h = (House)registry.get(""); // HACKEY !!!
	    } catch (ChatException ce) {
		ErrorLog.error(ce, 0, "House was not found in the registry.");
		throw ce;
	    }
	    Vector chldn = h.children;
	    ChatObject co;
	    for(int i = 0; i < chldn.size(); i++) {
		co = (ChatObject) chldn.elementAt(i);
		pd.addOption(co.sName, co.sKeyWord, co == parent);
	    }

	    f.addHTML(commandInput(Input.HIDDEN, SWITCH));
	    Input switchB = new Input(Input.IMAGE, "", "");
	    switchB.addArgument("SRC", commandButton(SWITCH));
	    switchB.addArgument("BORDER", "0");
	    f.addHTML(switchB);
	    //Exit
	    f.addHTML(new Link(commandURL(EXIT, c), new Image(commandButton(EXIT))));
	} else {
	    f.addArgument("TARGET",CHAT_FRAME);
	}

	Login l = c.getLogin();
	// Admin tools
	if(isAdmin(l)) {
	    //Edit the room
            //changed to use brackets 11/30/2017
	    f.addHTML("<BR>");
	    if(hasPrivilege(l, CAN_EDIT)) {
                f.addHTML(new Link(commandURL(MODIFY_PAGE, c), new Image(commandButton(MODIFY_PAGE)), CHAT_FRAME));
            }

	    //Delete the room
            //changed to use brackets 11/30/2017
	    if(hasPrivilege(l, CAN_DELETE)) {
                f.addHTML(new Link(commandURL(DELETE, c), new Image(commandButton(DELETE)), CHAT_FRAME));
            }	
	}

	f.addHTML("</CENTER>");
	return p;
    }


    boolean inRoom(Chatter c) {
	return c != null && vChatters.contains(c);
    }  

    boolean inRoom(Login l) {
	return getChatter(l) != null;
    }  

    protected HTML liveList0(int level, String handle, boolean showPics, Container ct) throws ChatException {
	Container list = new Container();

	list.addHTML(super.liveList0(4, handle, showPics, ct));

	Container innerList = new Container("DL");
	list.addHTML(innerList);
	for (int i = 0; i < vChatters.size(); i++) {
            Chatter cCurr = (Chatter)vChatters.elementAt(i);
            Container item = new Container("DD");
            innerList.addHTML(item);
            
            if (!bPMDis || isAdmin(lOwner)) {
                item.addHTML(PMHTML(cCurr));
            }

            item.addHTML(cCurr.getHTML(!showPics,bIdleTimes));
	}
        
	return list;
    }  

    /**
     * in manual chat, returns the middle of the room, including the top picture and
     * form, the messages and the bottom form.
     *
     * @param ct the chatter to customize this display for.
     * @return HTML code for the message entry area of room.
     */
    protected Page manual(Chatter ct) throws ChatException {

	Page p = getTop(ct,false);
	Enumeration eChatters = null;

	if (buttonJS == null) {
            buttonJS = getResource("button.js");
        }
		
	//express messages form in the NON scrolling area
	Form f = form(BOTTOM_FORM, ct);
	p.addHTML(f);
	p.addHeadHTML("<script>") ;
	p.addHeadHTML(buttonJS) ;
	p.addHeadHTML("</script>") ;

	f.addHTML("<CENTER><FONT FACE=Arial, Helvetica SIZE=2>Express Public Message:</FONT>");
	Input i = new Input(Input.TEXT, XPUBLIC_MESSAGE_VAR, "");
	i.addArgument("SIZE","35");
        i.addArgument("class", "xPublic");
	f.addHTML(i);
	f.addHTML("</CENTER>");

	p.addHTML("<SCRIPT><!--\ndocument.Bottom." + XPUBLIC_MESSAGE_VAR + ".focus();\n//--></SCRIPT>");
	//new messages
	f.addHTML(ct.HTMLGetNewMessages());

	//Bottom Form
	//f = form(sBottomForm, ct);
	//p.addHTML(f);
        
	//message boxes
	f.addHTML(commandInput(Input.HIDDEN, MANUAL));
	f.addHTML(new Input(Input.HIDDEN, SET_VAR, "TRUE"));
	// Sound option for chatters in the NON SCROLLING area
	f.addHTML("<TABLE><TR><TD VALIGN=\"TOP\">");

	if (bHtmlDis) {
            f.addHTML(HTMLGetPullDown(PUBLIC_MESSAGE_VAR));
        }
            
	f.addHTML("<BR>\n");
	f.addHTML("<FONT FACE=Arial, Helvetica SIZE=2>Type a Public Message to everyone in the room:</FONT><BR><TEXTAREA ROWS=\"4\" COLS=\"65\" " +
		  "WRAP=\"SOFT\" NAME=\"" + PUBLIC_MESSAGE_VAR);
	f.addHTML("\"></TEXTAREA><BR>\n"); 
	f.addHTML(new Input(Input.SUBMIT, "", sSpeak));

	if (!bHtmlDis && !ct.bNoButtons) {
            f.addHTML("<A href=\"javascript:bold('Speak')\"><IMG alt=\"Bold\" border=\"0\" SRC=\""+buttonURL("bold.gif")+"\"></A>") ;
            f.addHTML("<A href=\"javascript:italicize('Speak')\"><IMG alt=\"Italics\" border=\"0\" SRC=\""+buttonURL("italicize.gif")+"\"></A>") ;
            f.addHTML("<A href=\"javascript:underline('Speak')\"><IMG alt=\"Underline\" border=\"0\" SRC=\""+buttonURL("underline.gif")+"\"></A>") ;
            f.addHTML("<A href=\"javascript:center('Speak')\"><IMG alt=\"Center\" border=\"0\" SRC=\""+buttonURL("center.gif")+"\"></A>") ;
            f.addHTML("<A href=\"javascript:hyperlink('Speak')\"><IMG alt=\"Hyperlink\" border=\"0\" SRC=\""+buttonURL("url.gif")+"\"></A>") ;
            f.addHTML("<A href=\"javascript:image('Speak')\"><IMG alt=\"Image\" border=\"0\" SRC=\""+buttonURL("image.gif")+"\"></A>") ;
            f.addHTML(getFontDrops("Speak"));
	}
        
	f.addHTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
	f.addHTML("<BR><FONT FACE=Arial, Helvetica SIZE=2>Type a Private Message to anyone you have selected below:</FONT><BR><TEXTAREA ROWS=\"2\" COLS=\"65\" " +
		  "WRAP=\"SOFT\" NAME=\"" + PRIVATE_MESSAGE_VAR);
	f.addHTML("\"></TEXTAREA><BR>\n");
	f.addHTML(new Input(Input.SUBMIT, "", sWhisper));
	
	if (!bHtmlDis && !ct.bNoButtons) {
		f.addHTML("<A href=\"javascript:bold('Whisper')\"><IMG alt=\"Bold\" border=\"0\" SRC=\""+buttonURL("bold.gif")+"\"></A>") ;
		f.addHTML("<A href=\"javascript:italicize('Whisper')\"><IMG alt=\"Italics\" border=\"0\" SRC=\""+buttonURL("italicize.gif")+"\"></A>") ;
		f.addHTML("<A href=\"javascript:underline('Whisper')\"><IMG alt=\"Underline\" border=\"0\" SRC=\""+buttonURL("underline.gif")+"\"></A>") ;
		f.addHTML("<A href=\"javascript:center('Whisper')\"><IMG alt=\"Center\" border=\"0\" SRC=\""+buttonURL("center.gif")+"\"></A>") ;
		f.addHTML("<A href=\"javascript:hyperlink('Whisper')\"><IMG alt=\"Hyperlink\" border=\"0\" SRC=\""+buttonURL("url.gif")+"\"></A>") ;
		f.addHTML("<A href=\"javascript:image('Whisper')\"><IMG alt=\"Image\" border=\"0\" SRC=\""+buttonURL("image.gif")+"\"></A>") ;
		f.addHTML(getFontDrops("Whisper"));
	}

	f.addHTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        
	if (!bHtmlDis) {
            f.addHTML(HTMLGetPullDown(PRIVATE_MESSAGE_VAR));
        }
        
	f.addHTML("<BR>");
	//no picture checkboxes
	f.addHTML("<TABLE>\n");
	f.addHTML("<TR><TD>\n");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_IMAGE_VAR +
		  "\" VALUE=1 " + (ct.bNoPic ? "":"CHECKED") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>See Posted Images</FONT>&nbsp;&nbsp;&nbsp;&nbsp;\n");
	f.addHTML("</td><td>\n");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_HANDLE_IMAGE_VAR +
		  "\" VALUE=0 " + (ct.bNoPicHandle ? "":"CHECKED") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>See Handle Images</FONT>&nbsp;&nbsp;&nbsp;&nbsp;\n");
	f.addHTML("</td></tr>\n");
	f.addHTML("<TR><TD>\n");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_SOUND_VAR +
		  "\" VALUE=1 " + (ct.NoSound() ? "" : "CHECKED") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>Hear Sounds</FONT>\n");
	f.addHTML("</td><td>\n");
	// -------
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.IG_OUTSIDE_VAR +
		  "\" VALUE=1 " + (ct.bIgOutside ? "CHECKED" : "") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>Ignore Outside Messages</FONT>\n");
	f.addHTML("</td></tr>\n");
	f.addHTML("</TABLE>");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.IG_ENTRY_VAR +
		  "\" VALUE=1 " + (ct.bIgEntry ? "CHECKED" : "") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>Ignore Entry/Exit Messages</FONT>\n");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_BUTTONS +
		  "\" VALUE=1 " + (ct.bNoButtons ? "CHECKED" : "") +
		  "><FONT FACE=\"Arial, Helvetica\" SIZE=2>No Buttons</FONT>\n");
	f.addHTML("</td></tr>\n");
	f.addHTML("</TABLE>");

	//Print Chatters
	f.addHTML("<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\" "
		  + "FRAME=\"BOX\"><TR><TD VALIGN=\"TOP\">");
	f.addHTML(new Image(buttonURL("pmcheck.gif"), "Private Message Recipient"));
	f.addHTML("</td><TD VALIGN=\"TOP\" >");
	f.addHTML(new Image(buttonURL("igcheck.gif"), "Ignore Public Messages"));
	f.addHTML("</td><TD VALIGN=\"TOP\">");
	f.addHTML(new Image(buttonURL("igpmcheck.gif"), "Ignore Private Messages"));
	f.addHTML("</td><TD COLSPAN=\"2\" VALIGN=\"TOP\" ALIGN=\"LEFT\">");
	f.addHTML(String.valueOf(numChatters()) + " Chatter"
		  + (numChatters() == 1 ? "" : "s"));
	f.addHTML("</td></tr>");

	eChatters = GetChatters();
	Chatter ctCurr = null;
	while(eChatters.hasMoreElements()) {
	    ctCurr = (Chatter)eChatters.nextElement();
	    Input input;
	    f.addHTML("<TR><TD VALIGN=\"TOP\">");
	    f.addHTML(input = new Input(Input.CHECKBOX, Chatter.PM_RECIPIENT_VAR, ctCurr.HashKey()));
	    if(ct.isPMRecip(ctCurr.HashKey())) {
                input.addArgument("CHECKED");
            }
	
	    f.addHTML("</TD><TD VALIGN=\"TOP\">");
	    f.addHTML(input = new Input(Input.CHECKBOX,
					Chatter.PUBLIC_IGNORE_VAR,
					ctCurr.HashKey()));
	    if(ct.isIgPub(ctCurr.HashKey())) {
                input.addArgument("CHECKED");
            }

	    f.addHTML("</TD><TD VALIGN=\"TOP\">");
	    f.addHTML(input = new Input(Input.CHECKBOX,
					Chatter.PRIVATE_IGNORE_VAR,
					ctCurr.HashKey()));
	    if(ct.isIgPriv(ctCurr.HashKey())) {
                input.addArgument("CHECKED");
            }

	    f.addHTML("</TD><TD>");
	    if(isAdmin(ct.getLogin())) {
                f.addHTML(bootHTML(ctCurr));
            }

	    f.addHTML("</TD><TD>");
	    f.addHTML(ctCurr.getHTML(ct.NoPicHandle(),bIdleTimes));
	    f.addHTML("</TD></TR>");
	}
        
	f.addHTML("</TABLE>");
	f.addHTML(new Input(Input.SUBMIT, "", sSpeak));
	f.addHTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
	f.addHTML(new Input(Input.SUBMIT, "", sWhisper));
	return p;
    }    

    /**
     *
     * @return
     * @throws ChatException
     */
    @Override
    protected String modifyPage() throws ChatException {
	return fileToString(NEW_HTML);
    }  

    /**
     *
     * @param lOwner
     * @param lt
     * @return
     * @throws ChatException
     */
    @Override
    protected ChatObject newChild(Login lOwner, LookupTable lt) throws ChatException {
	return new Room(lOwner, lt, this);
    }  

    protected Page pastePop(Chatter ct) throws ChatException {
	if (!inRoom(ct)) {
            throw new ChatException("You are no longer in " + GetHTML());
        }
	
	//Make Form and Buttons and the suchlike
	Page p = (Page)pageTemplate.clone();

	p.addHeadHTML("\n<SCRIPT>\n" + "function focusinput() {\n" + "}\n" + "</SCRIPT>\n");
	// (String name, boolean bGet, int iCommand, Chatter c) {

	Form f = form("pastePop",false,PASTE_SEND,ct);
	p.addHTML(f);
	f.addHTML("<center><FONT SIZE=\"2\" FACE=\"Arial, Helvetica, Verdana\">Paste Text Here:</FONT><BR>");
	f.addHTML("<br><textarea rows=10 cols=45 wrap=virtual name=Public></textarea>") ;	
	f.addHTML("<input type=submit value=Done></center>");
	return p;
    } 

	/**
     * Duplicates code in HTMLGetMiddle !!!
     *
     * @param ct the chatter to customize this display for.
     * @return HTML code for popup window for private messages and setting options in real time chat.
     */

    protected Page options(Chatter ct) throws ChatException {
	if (!inRoom(ct)) {
            throw new ChatException("You are no longer in " + GetHTML());
        }
	//Make Form and Buttons and the suchlike
	Page p = (Page)pageTemplate.clone();

	p.addHeadHTML("\n<SCRIPT>\n" + "function focusinput() {\n" + "}\n" + "</SCRIPT>\n") ;
		
	Form f = form(BOTTOM_FORM, ct);
	p.addHTML(f);

	f.addHTML(new Input(Input.HIDDEN, VAR_OPTIONS_SUBMITTED, "ON"));
	//no Images checkbox
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_IMAGE_VAR
		  + "\" VALUE=1 " + (ct.bNoPic ?  "":"CHECKED") + ">") ;
	//f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.NO_IMAGE_VAR, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>See Posted Images"
		  + "</FONT><BR>\n");

	//no Handle Images checkbox
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_HANDLE_IMAGE_VAR
		  + "\" VALUE=0 " + (ct.bNoPicHandle ? "" : "CHECKED") + ">");
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.NO_HANDLE_IMAGE_VAR, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>See Handle Images"
		  + "</FONT><BR>\n");
	//no Embeded Sounds checkbox
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.NO_SOUND_VAR
		  + "\" VALUE=1 " + (ct.NoSound() ? "":"CHECKED") + ">");
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.NO_SOUND_VAR, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>Hear Sounds"
		  + "</FONT><BR>\n");
	//no Outside messages
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.IG_OUTSIDE_VAR
		  + "\" VALUE=1 " + (ct.bIgOutside ? "CHECKED" : "") + ">");
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.IG_OUTSIDE_VAR, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>Ignore Outside Messages"
		  + "</FONT><BR>\n");
	//rmt: no entry/exit messages
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.IG_ENTRY_VAR
		  + "\" VALUE=1 " + (ct.bIgEntry ? "CHECKED" : "") + ">");
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.IG_ENTRY_VAR, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>Ignore Entry/Exit"
		  + "</FONT><BR>\n");
	f.addHTML("<INPUT TYPE=CHECKBOX NAME=\"" + Chatter.USER_DING
		  + "\" VALUE=1 " + (ct.bUserDing ? "CHECKED" : "") + ">");
	f.addHTML(new Input(Input.HIDDEN, SET_ONE_VAR + Chatter.USER_DING, "1"));
	f.addHTML("<FONT FACE=\"Arial, Helvetica,Verdana,Geneva\" SIZE=2>Play Sound on Entry/Exit"
		  + "</FONT><BR>\n");
	f.addHTML(commandInput(Input.HIDDEN, SET_OPTIONS));
	f.addHTML(new Input(Input.IMAGE, "", DONE).addArgument("src", buttonURL("done.gif")));

	return p;
    }    

    private void readObject(ObjectInputStream in) throws NotActiveException, ClassNotFoundException, IOException {
	in.defaultReadObject();
	if (fl == null) {
            fl = new Filter();
        }
        
	if (vChatters == null) { 
            vChatters = new Vector(iMaxChatters); 
        }
        
	new DeliveryThread(this);
    }

    public void removeChatter(Chatter ct) throws ChatException {
	removeChatter(ct, "has left the room");
    }  
    /**
     * Remove the Chatter ct from this room and send exit message sMessage.
     *
     * @param ct the chatter to be removed
     * @param sMessage
     */
    public void removeChatter(Chatter ct, String sMessage) throws ChatException {
	super.removeChatter(ct);
	if(ct == null) { return; }
	vChatters.removeElement(ct);
	ct.doExit();

	this.send(new Message(ct.getHTML() + ' ' + sMessage,
			      House.ctDaemon,
			      (String)null,
			      Message.REMOVE));
    }  
    
    private Page scrollPage(Chatter ct) throws ChatException {

	roomPage = new Page();
        Container style = new Container("style");
        Container main = new Container();
        Container row = new Container("div");
        row.addArgument("class", "row full-page");

        main.addHTML(row);
        main.addArgument("id", sName + "-page");
        main.addArgument("class", "full-page");
        roomPage.addHTML(main);
        // bootstrap
        
        roomPage.addHeadHTML("<title>" + sName + "</title>");
	roomPage.addHeadHTML(style);
        roomPage.addHeadHTML(
		      "<SCRIPT>\n"
		      //	+ "<!--\n"
		      + "function focusinput() {\n"
		      + "self.focus() ;\n"
		      + "document.getElementById('user-message').focus();\n"
		      + "return true ;\n" 
		      +"}\n\n"
	
		      + "function doSubmit() {\n"
		      // if they are the admin reload the top frame
		      // + (isAdmin(ct.getLogin) ? "parent." + FRAME_TOP + ".location.reload();\n" : "")
		      // + "parent." + FRAME_LIST + ".document.location.reload();\n"
		      + "return true;\n"
		      + "}\n"
		      + "\n"
		      + "function openOptions() {\n"
		      + "  if(parent.winRef && !parent.winRef.closed) \n"
		      + "     parent.winRef.close();\n"
		      + "  parent.winRef = open(\"" + commandURL(OPTIONS, ct) + "&\" + (new Date()).getTime(),\n"
		      + "                       \"other_win\",\"menubar=no,scrollbars=auto,"
		      +                         "resizable=yes,width=240,height=180\");\n"
		      + "  parent.winRef.focus(); \n"
		      + "  return false;\n"
		      + "}\n\n"
			  + "function openPopPaste() {\n"
		      + "  if(parent.winRef && !parent.winRef.closed) \n"
		      + "     parent.winRef.close();\n"
		      + "  parent.winRef = open(\"" + commandURL(PASTE_POP, ct) + "&\" + (new Date()).getTime(),\n"
		      + "                       \"other_win\",\"menubar=no,scrollbars=auto,"
		      +                         "resizable=yes,width=400,height=250\");\n"
		      + "  parent.winRef.focus(); \n"
		      + "  return false;\n"
		      + "}\n\n"

			  + buttonJS
		      + "</script>\n");

        
        style.addHTML("html, body, .full-page {min-height: 100vh; overflow-x: hidden}");
        style.addHTML("#room-messages-frame {min-height:50vh;}");
       
        
	row.addHTML("<iframe id='room-top-frame' class='full-frame col-12 frame' name='" + FRAME_TOP + "' src='"+ commandURL(SCROLL_TOP, ct) + "'></iframe>");
        //row.addHTML("<iframe id='room-messages-frame' class='col-12 col-md-9 frame' name='" + FRAME_MESSAGES+ "' src='"+ commandURL(SCROLL_MESSAGES, ct) + "'></iframe>");
        row.addHTML("<div id='room-messages-frame' class='col-12 col-md-3 frame' name='" + FRAME_MESSAGES + "'></div>");
		row.addHTML("<iframe id='room-user-list-frame' class='col-12 col-md-3 frame' name='" + FRAME_LIST + "' src='"+ commandURL(SCROLL_LIST, ct) + "'></iframe>");
        row.addHTML("<iframe id='room-send-frame' class='col-12 frame' name='" + FRAME_SEND + "' src='"+ commandURL(SCROLL_SEND, ct) + "'></iframe>");
		row.addHTML("<script src='https://code.jquery.com/jquery-3.3.1.min.js' integrity='sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=' crossorigin='anonymous'></script>");
        row.addHTML("<script>var chatRoom = '" + commandURL(SCROLL_MESSAGES, ct) + "';</script>");
		row.addHTML("<script type='text/javascript' src='/resources/scripts/autoscroll.js'></script>");
	return roomPage;
    }



	// ------------------------------------------------------------------------------------------
    //START CODE FOR BOTTOM FRAME IN AUTOSCROLLING ROOMS
    /**
     * Includes message entry area and buttons to set options, send private messages and exit.
     * Duplicates code in HTMLGetMiddle !!!
     *
     * @param ct the chatter to customize this display for.
     * @return the HTML code for the bottom frame in real time chat
     */
    
    protected Page scrollSend(Chatter ct) throws ChatException {
	if(ct == null) { 
            throw new ChatterNotFoundException(); 
        }

	if (buttonJS == null) {
            buttonJS = getResource("button.js");
        }

	if (signature == null) {
            signature = getResource("signature.html");
        }

	Page p = (Page)pageTemplate.clone();
	p.addArgument("onload=\"return focusinput();\"") ;	
	//Script defining functions used below
	//p.addHeadHTML("<script src=\"button.js\" language=\"javascript\"></script>") ;
	p.addHeadHTML(
		      "<SCRIPT>\n"
		      //	+ "<!--\n"

		      + "function focusinput() {\n"
		      + "self.focus() ;\n"
		      + "document.getElementById('user-message').focus();\n"
		      + "return true ;\n" 
		      +"}\n\n"
	
		      + "function doSubmit() {\n"
		      // if they are the admin reload the top frame
		      // + (isAdmin(ct.getLogin) ? "parent." + FRAME_TOP + ".location.reload();\n" : "")
		      // + "parent." + FRAME_LIST + ".document.location.reload();\n"
		      + "return true;\n"
		      + "}\n"
                      + "function checkSubmit(e) {\n" 
                      + " if(e && e.keyCode == 13) {\n" 
                      + "    document.forms[0].submit();\n" 
                      + "  }\n" 
                      + "}"
		      + "\n"
		      + "function openOptions() {\n"
		      + "  if(parent.winRef && !parent.winRef.closed) \n"
		      + "     parent.winRef.close();\n"
		      + "  parent.winRef = open(\"" + commandURL(OPTIONS, ct) + "&\" + (new Date()).getTime(),\n"
		      + "                       \"other_win\",\"menubar=no,scrollbars=auto,"
		      +                         "resizable=yes,width=240,height=180\");\n"
		      + "  parent.winRef.focus(); \n"
		      + "  return false;\n"
		      + "}\n\n"
			  + "function openPopPaste() {\n"
		      + "  if(parent.winRef && !parent.winRef.closed) \n"
		      + "     parent.winRef.close();\n"
		      + "  parent.winRef = open(\"" + commandURL(PASTE_POP, ct) + "&\" + (new Date()).getTime(),\n"
		      + "                       \"other_win\",\"menubar=no,scrollbars=auto,"
		      +                         "resizable=yes,width=400,height=250\");\n"
		      + "  parent.winRef.focus(); \n"
		      + "  return false;\n"
		      + "}\n\n"

			  + buttonJS
		      + "</script>\n");

	//Bottom Form
	Form tBottomForm = form(BOTTOM_FORM, ct);

        tBottomForm.addArgument("onKeyPress", "return checkSubmit(event)");
	tBottomForm.addArgument("onSUBMIT", "doSubmit()");
	tBottomForm.addHTML(commandInput(Input.HIDDEN, SCROLL_SEND));

	p.addHTML(tBottomForm);

	//Begin the table
	tBottomForm.addHTML("<TABLE WIDTH=700 CELLPADDING=\"0\" CELLSPACING=\"0\" VALIGN=\"TOP\" BORDER=0><TR>" +
			    "<TD NOWRAP VALIGN=\"TOP\">\n");

	//Sounds and images pulldown menus
	if (!bHtmlDis) {
            tBottomForm.addHTML(HTMLGetPullDown(PUBLIC_MESSAGE_VAR));
        }

	if (!bHtmlDis) {
            tBottomForm.addHTML(getFontDrops("RealTime"));	
        }	
	
	//Start new column
	tBottomForm.addHTML("</TD><TD>");

	//Java Script for Toggling Scrolling
	tBottomForm.addHTML(//"</FONT>\n"
			    "<SCRIPT LANGUAGE=\"JavaScript1.1\">\n" +
			    "\n" +
			    "  <!--\n" +
			    "    var scrolling = true;\n" +
			    "    function toggleScrolling( )\n" +
			    "    {\n" +
			    "        if ( scrolling == true )\n" +
			    "        {\n" +
			    "            scrolling = false;\n" +
			    "            parent." + FRAME_MESSAGES + ".clearTimeout();\n" +
			    "            parent." + FRAME_MESSAGES + ".autoScrollOn = 0;\n" +
			    "            parent." + FRAME_MESSAGES + ".onblur = parent." + FRAME_MESSAGES + ".scrollOffFunction;\n" +
			    "        } else {\n" +
			    "            scrolling = true;\n" +
			    "            parent." + FRAME_MESSAGES + ".autoScrollOn = 1;\n" +
			    "            parent." + FRAME_MESSAGES + ".onblur = parent." + FRAME_MESSAGES + ".scrollOnFunction;\n" +
			    "            parent." + FRAME_MESSAGES + ".scroll(0, 65000);\n" +
			    "            parent." + FRAME_MESSAGES + ".setTimeout('scrollWindow()', 200);\n" +
			    "        }  // end if\n" +
			    "    }  // end toggleScrolling\n" +
			    "    if ( parent." + FRAME_MESSAGES + " != null  &&  parent." + FRAME_MESSAGES + ".autoScrollOn != null )\n" +
			    "    {\n" +
			    "        document.write('<INPUT NAME=AUTOSCROLL TYPE=CHECKBOX onclick=\"toggleScrolling()\"');\n" +
			    "        if ( parent." + FRAME_MESSAGES + ".autoScrollOn == 1 )\n" +
			    "        {\n" +
			    "            document.write(' CHECKED');\n" +
			    "        }  // end if\n" +
			    "        document.write('> <FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=1>Scroll - Uncheck to scroll back</FONT>');\n" +
			    "        scrolling = ( parent." + FRAME_MESSAGES + ".autoScrollOn == 1 );\n" +
			    "    }  // end if\n" +
			    "  //  -->\n" +
			    "\n" +
			    "</SCRIPT>");

	//Start a new row in the table
	tBottomForm.addHTML("</TD></TR><TR><TD>\n");

	//Message box
	Input inp = new Input(Input.TEXT, PUBLIC_MESSAGE_VAR, "");
        // new class added to message box
        inp.addArgument("id", "user-message");
        inp.addArgument("class", "message-box");
	inp.addArgument("SIZE", "60") ;
	tBottomForm.addHTML(inp);

	//Instructions
	tBottomForm.addHTML(signature) ;

	//New column
	tBottomForm.addHTML("</TD><TD VALIGN=\"TOP\">");

	tBottomForm.addHTML("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" ><tr>") ;
	tBottomForm.addHTML("<td>");

	// Whisper Button for bottom frame of AUTOSCROLL room
	if (!bPMDis || isAdmin(ct.lUser)) {
        //tBottomForm.addHTML("<input type=image src=\"" + buttonURL("whisper.gif") + "\" set border=0 value=\"Whisper\" name=\""+PRIVATE_CHECKBOX_VAR+"\">") ;
	    Input iWhisper = new Input(Input.SUBMIT, PRIVATE_CHECKBOX_VAR, sWhisper);
            //Input iSpeak = new Input(Input.SUBMIT, PUBLIC_MESSAGE_VAR, sSpeak);
            
	    iWhisper.addArgument("ALIGN", "TOP");
	    iWhisper.addArgument("VSPACE", "0");
            //tBottomForm.addHTML(iSpeak);
	    tBottomForm.addHTML(iWhisper);
	}

	tBottomForm.addHTML("</td>");
	if (!bHtmlDis) {
		tBottomForm.addHTML("<td><A href=\"javascript:bold('RealTime')\"><IMG alt=\"Bold\" border=\"0\" SRC=\""+buttonURL("bold.gif")+"\"></A>") ;
		tBottomForm.addHTML("<A href=\"javascript:italicize('RealTime')\"><IMG alt=\"Italics\" border=\"0\" SRC=\""+buttonURL("italicize.gif")+"\"></A>") ;
		tBottomForm.addHTML("<A href=\"javascript:underline('RealTime')\"><IMG alt=\"Underline\" border=\"0\" SRC=\""+buttonURL("underline.gif")+"\"></A>") ;
		tBottomForm.addHTML("<A href=\"javascript:center('RealTime')\"><IMG alt=\"Center\" border=\"0\" SRC=\""+buttonURL("center.gif")+"\"></A>") ;
		tBottomForm.addHTML("<A href=\"javascript:hyperlink('RealTime')\"><IMG alt=\"Hyperlink\" border=\"0\" SRC=\""+buttonURL("url.gif")+"\"></A>") ;
		tBottomForm.addHTML("<A href=\"javascript:image('RealTime')\"><IMG alt=\"Image\" border=\"0\" SRC=\""+buttonURL("image.gif")+"\"></A>") ;
		//tBottomForm.addHTML("<A href=\"javascript:quote('RealTime')\"><IMG border=\"0\" SRC=\""+buttonURL("quote.gif")+"\"</A></td>") ;
		tBottomForm.addHTML("<A HREF=\"\" onClick='return openPopPaste()'>" + "<IMG alt=\"Text Editor\" SRC=\"" + buttonURL("quote.gif")  + "\" ALT=\"\" BORDER=\"0\"></A>");
	}

	tBottomForm.addHTML("</tr>") ;
	tBottomForm.addHTML("<tr>") ;
	tBottomForm.addHTML("<td nowrap colspan=\"2\">") ;

	//Set options button
	tBottomForm.addHTML("<A HREF=\"\" onClick='return openOptions()'>"
			    +	"<IMG SRC=\"" + commandButton(OPTIONS)
			    + "\" ALT=\"\" BORDER=\"0\"></A>");

	// Switch rooms button
	if (!bSwitchDis || ct.isAdmin) {
	    tBottomForm.addHTML("<A HREF=\"" + commandURL(SWITCH, ct)+"&"+DESTINATION_VAR+"="+parent.sKeyWord
			    + "\" TARGET=\"_top\"><IMG SRC=\""
			    + buttonURL("switchrooms.gif") + "\""
			    + " BORDER=0></A>");
	}
	
	//Exit button
	tBottomForm.addHTML("<A HREF=" + commandURL(EXIT, ct)
			    + " TARGET=_top class='exit-link'><IMG SRC=\"" + buttonURL("exit.gif")
			    + "\" ALT=\"\" BORDER=0></A>");
	tBottomForm.addHTML("</td></tr>") ;
	tBottomForm.addHTML("</table>") ;
	//Put cursor in the message box
	tBottomForm.addHTML("<SCRIPT><!--\ndocument.getElementById('user-message').focus();\n//--></SCRIPT>");
	//End table
	tBottomForm.addHTML("</TD></TR></TABLE>");
	tBottomForm.addHTML("<FONT SIZE=\"1\" FACE=Arial,Helvetica,Geneva> powered by " +
			    "<A target=\"new\" HREF=\"http://customchat.com\"><B>CustomChat Server</B></A> since 1997</FONT><P>");
        
	return p;
    }

	// ------------------------------------------------------------------------------------------
    protected void send(Chatter c, LookupTable lt) throws ChatException {
	if(c == null) {
            throw new ChatterNotFoundException();
        }    

	String sMessage;

	// AUTOSCROLL MESSAGES and MANUAL PUBLIC MESSAGES
	// PUBLIC_MESSAGE_VAR has the public message from manual
	// and either the public or privat message in autoscroll
	// depending on if they hit the speak or the whisper button
	if(((sMessage = lt.getValue(PUBLIC_MESSAGE_VAR)) != null) && !sMessage.equals("")) {
            if(lt.getValue(PRIVATE_CHECKBOX_VAR) == null) {
                send(new Message(sMessage, c.HashKey(), (String)null, Message.PUBLIC));
            } else {
		Message m = c.privateMessage(sMessage);
		if(m != null) {
                    UserRegistry.sendMessage(m);
                }    
	    }
        }
	    

	// MANUAL PUBLIC MESSAGES
	if(((sMessage = lt.getValue(PRIVATE_MESSAGE_VAR)) != null) && !sMessage.equals("")) {
            Message m = c.privateMessage(sMessage);
	    if(m != null) {
		UserRegistry.sendMessage(m);
            }
	}
    }  
    /**
     * Adds message m to the buffer of messages to the room and the pending message queues of its
     * recipients.
     *
     * @param m the message to me sent
     * @throws customchat.chat.BadLanguageException
     */
    public void send(Message m)	throws BadLanguageException {

        m.Filter(fl);//filter text

        ChatObject p = this ;
        ChatObject last = this ;

        // if a bad language filter is not defined in this chatobject then
        // step up to the parents IF the user requires a dictionary, i.e.
        // iWordser != -1

        if (!blf.defined) {
            if (iWordSet != -1) {
                while (p != null) {
                    if (p.blf.defined) {
                        last = p ;
                        break ;
                    }

                    last = p ;
                    p = p.parent ;
                }
            }
        }

        if (last != null && this.iWordSet != -1) {
            m.languageFilter(this.iWordSet,last);
        }

        //Add the message to the hashtable
        htMessages.put(auto_inc % MESSAGE_LIMIT, m);
        auto_inc++;

        //Add message to queues of recips
        Enumeration eTo = GetChatters();
        while(eTo.hasMoreElements()) {
            Chatter ct = (Chatter)eTo.nextElement();
            ct.addMessage(m);
        }

        if(bLogging) {
            log(m);
        }      
    }
    
    @Override
    public String toString() {
        return(this.sName);
    }

    public String getResource(String fn) {
        String res = null;
        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( new File(fn))));
            StringBuilder sb;
            sb = new StringBuilder();
            String l ;
            while ((l = in.readLine()) != null) {
                if (l != null) {
                    sb.append(l) ;
                    sb.append('\n');
                }
            }
            
            res = sb.toString();
        } catch (IOException ioe) {
            System.out.println("Room:getResource:"+ioe);
        }
        return res;
    }

    public String getFontDrops(String ibox) {
        StringBuilder sb; 
        sb = new StringBuilder();
        sb.append("<SELECT onchange=showfont(this.options[this.selectedIndex].value,'"+ibox+"') name=font>");
        sb.append("<OPTION value=Arial selected>Arial</OPTION>");
        sb.append("<OPTION value=\"Century\">Century</OPTION>");
        sb.append("<OPTION value=\"Comic\">Comic</OPTION>");
        sb.append("<OPTION value=\"Courier\">Courier</OPTION>");
        sb.append("<OPTION value=Harrington>Harrington</OPTION>");
        sb.append("<OPTION value=\"Helvetica\">Helvetica</OPTION>");
        sb.append("<OPTION value=\"Halloween\">Halloween</OPTION>");
        sb.append("<OPTION value=Impact>Impact</OPTION>");
        sb.append("<OPTION value=Tahoma>Tahoma</OPTION>");
        sb.append("<OPTION value=\"Times\">Times</OPTION>");
        sb.append("<OPTION value=Stencil>Stencil</OPTION>");
        sb.append("<OPTION value=Verdana>Verdana</OPTION>");
        sb.append("<OPTION value=\"Lucida\">Lucida</OPTION></SELECT>");
        sb.append("<SELECT onchange=showsize(this.options[this.selectedIndex].value,'"+ibox+"') name=size>");
        sb.append("<OPTION value=\"1\">1</OPTION>");
        sb.append("<OPTION value=\"2\">2</OPTION>");
        sb.append("<OPTION value=\"3\" selected>3</OPTION>");
        sb.append("<OPTION value=\"4\">4</OPTION>");
        sb.append("<OPTION value=\"5\">5</OPTION>");
        sb.append("<OPTION value=\"6\">6</OPTION></SELECT>");
        sb.append("<SELECT onchange=showcolor(this.options[this.selectedIndex].value,'"+ibox+"') name=color>");
        sb.append("<OPTION value=\"White\">White</OPTION>");
        sb.append("<OPTION value=\"Black\" selected>Black</OPTION>");
        sb.append("<OPTION value=\"Red\">Red</OPTION>");
        sb.append("<OPTION value=\"Yellow\">Yellow</OPTION>");
        sb.append("<OPTION value=\"Pink\">Pink</OPTION>");
        sb.append("<OPTION value=\"Green\">Green</OPTION>");
        sb.append("<OPTION value=\"Orange\">Orange</OPTION>");
        sb.append("<OPTION value=\"Purple\">Purple</OPTION>");
        sb.append("<OPTION value=\"Blue\">Blue</OPTION>");
        sb.append("<OPTION value=\"Beige\">Beige</OPTION>");
        sb.append("<OPTION value=\"Brown\">Brown</OPTION>");
        sb.append("<OPTION value=\"Teal\">Teal</OPTION>");
        sb.append("<OPTION value=\"Navy\">Navy</OPTION>");
        sb.append("<OPTION value=\"Maroon\">Maroon</OPTION>");
        sb.append("<OPTION value=\"Lime\">Lime</OPTION></SELECT>");

        return sb.toString() ;
    }
}

