package customchat.chat;

import java.util.*;
import java.io.*;
import java.net.*;
import customchat.util.*;
import customchat.htmlutil.*;

/**
 * This class represents the entire house it stores the floors and
 * the global variables such as commands and CGI variable names.
 *
 * CustomChat Server
 * @version 1.5
 */
public class House extends ChatObject {

	// added by ritchie
 static final long serialVersionUID = -4116962984823438181L;
  public static final int KILL = 1001;
  public static final int RESTART = 1002;
  public static final int BAD_WORDS = 1101;

  public static final String FILE_SEND = "send.html";

    public String sFileName;

  public static Hashtable POST_IMAGES;
  public static Hashtable POST_SOUNDS;

  static {
	readImages();
	readSounds();
  }

    /**
	 * the default chatter who never expires with id 0; all messages from the server come from this user.
	 */
    public static String ctDaemon = null; 
    //new Chatter("", 0, "CustomChat Server", "", Chatter.ADMIN, null);

    //Server Commands
    public static final String sAddFromDirCom = "AddRooms";
    public static final String sBootCom = "Boot User";

 //cgi variable name
    public static final String sRoomDirVar = "Dir";

    protected static final String sNewHTML = htmlFile("NewServer.html");

	/**
	 * sets the mode to s, sets up the file paths.
	 *
	 * @param s the mode or null for default
	 */
    public House() throws ChatException {
	super(lDefaultOwner, null, "", "CustomChat Server");

	if(System.getProperty("chat.loadtest") != null) {
	    Floor f = new Floor(getOwner(), this, "test1", "Test Area 1");
	    addChild(f);
	    int rooms = 200;
	    try{
		rooms = Integer.parseInt(System.getProperty("chat.loadtest"));
	    } catch (NumberFormatException e) {
		System.err.println("Improper format for number of loadtest Rooms.");
		System.err.println("Defaulting to : " + String.valueOf(rooms) + " rooms.");
	    }

	    for(int i = 1; i <= rooms; i++) {
		try {
		    f.addChild( new Room(getOwner(),
			 (new QueryStringParser("sName=Test+Room+" + String.valueOf(i)
				+ "&sKeyWord=" + String.valueOf(i))).parse(), f));

		} catch (WeirdSyncException ignored) {}
		catch (RoomExistsException ignored) {}
	    }
	}

	update((new QueryStringParser("")).parse());

	try {
	    String sHeader = fileToString(htmlFile("header.html"));
	    pageTemplate = new Page(sHeader);
	} catch(ChatException e) {
	    ErrorLog.error(e, 390341029, "Could not open header.html");
	    pageTemplate = new Page();
	}
    }

    protected String childCreationPage() throws ChatException {
	return fileToString(Floor.sNewHTML);
    }  

    protected String childName() {
	return "Area";
    }  

    public Page doCommand(Login lUser, LookupTable lt, PrintWriter out, int iCommand)
	throws ChatException, IOException {
	boolean bRestart = false;

	switch(iCommand) {
	case SEND :
	    return doSend(lUser, lt.getValue(Chatter.HANDLE_VAR),
			  lt.getValue(RECIPIENT_VAR), lt.getValue(PUBLIC_MESSAGE_VAR));

	case FIND :
	    return doFind(lUser, lt.getValue(SEARCH_VAR),lt);

	case BAD_WORDS :
	    return PageFactory.makeUtilPage(blf.update(lt,this));

	case RESTART :
	    bRestart = true;

	case KILL :
	    if(!isAdmin(lUser))
		throw new UnauthorizedException("Action requires Server Admin Privileges.");
	    throw new ShutDownException(bRestart);

	    /*
	      else if(sCommand.equals(sAddFromDirCom))
	      sReturn = (doAddFromDir(lUser, lt.getValue(sRoomDirVar)));
	      // Return live list
	      else if(sCommand.equals(sBootCom))
	      sReturn = (boot(lUser, f, r, lt.getValue(this.sBootSelect)));
	      // Commands below require that the chatter be in the room
	      // A frame to the right hand side of middle frame with list of users etc.
	      else if(sCommand.equals(sMiddleFrameCom))
	      sReturn = (r.doMiddleFrame(lUser));
	      // Get popup window with private messages in real time chat
	      else if(sCommand.equals(sOptionCom)) {
	      f.doChat(lUser, r, lt);
	      sReturn = (r.HTMLGetOption(lUser));
	      }
	      // Get list of who is in the room.
	      else if(sCommand.equals(sWhoListCom))
	      sReturn = (r.HTMLGetWhoList(lUser));
	      else
	      sReturn = (f.doChat(lUser, r, lt));

	      out.println(Server.sHTTPOK);
	    */
	default :
	    return super.doCommand(lUser, lt, out, iCommand);
	}
    }  


    private Page doSend(Login l, String sHandle, String sRecip, String sMessage)
	throws ChatException {
	if (sMessage != null && sMessage.length() > 0
	    && sRecip != null && sRecip.length() > 0) {
	    if (sHandle == null)
		throw new ChatException("Invalid Handle");
	    try {
		UserRegistry.getChatter(l, sHandle, null);
	    } catch (DataBaseException e) {
		throw new ChatException("Database Error: " + e.getMessage());
	    }
	    UserRegistry.sendMessage(new Message(sMessage,
						 sHandle,
						 sRecip,
						 Message.INSTANT));
	    // Close Window
	    Page p = new Page();
	    Container script = new Container("SCRIPT", "\n<!--\nself.close();\n//-->\n");
	    p.addHTML(script);

	    return p;
	}

	String sFile = fileToString(htmlFile(FILE_SEND));
	if (sHandle == null)
	    sHandle = "";
	sFile = replace(sFile, "#sHandle#", sHandle);
	if (sRecip == null)
	    sRecip = "";
	sFile = replace(sFile, "#Recip#", HTML.escapeQuotes(sRecip));
	if (sMessage == null)
	    sMessage = "";
	sFile = replace(sFile, "#Public#", sMessage);

	return new Page(sFile);
    }  

    static int lastSpace(String s) {
	int iLast = -1;
	for (int i = 0; i < s.length(); i++)
	    if (Character.isWhitespace(s.charAt(i)))
		iLast = i;

	return iLast;
    }  
    /**
   * Read the rooms in for this house and this mode.  And add them to the proper floors.
   */
   /*
	private String doAddFromDir(Login l, String sDir) throws UnauthorizedException {
		if(!isAdmin(l)) throw new UnauthorizedException("You must be house admin to read in new rooms.");
		Floor f;
		if(sDir == null) sDir = sRootDataDir;

		StringBuffer sb = new StringBuffer("Reading in rooms from " + sDir + " ...<br>\n");

		File fDir = new File(sDir);
		String [] sRooms = fDir.list();
		if(sRooms == null)
				return "No rooms in " + sDir + "<br>\n";
		int i = 0;

		while(i < sRooms.length) {
				String sFloor = "";
				String sPrimary = "";
				String sPassword = "";
				String sVariables = "";
			try {
				sb.append("reading in room " + sRooms[i] + "<br>\n");
				BufferedReader d = new BufferedReader(new FileReader(new File(sDir, sRooms[i++])));
				sFloor = d.readLine();
				sPrimary = d.readLine();
				sPassword = d.readLine();
				sVariables = d.readLine();

				f = GetFloor(sFloor);

				//Parse the CGI variables
				QueryStringParser qsp = new QueryStringParser(sVariables);
				LookupTable lt = qsp.parse();

				f.AddRoom(new Room(new Login(sPrimary, sPassword), lt));
			} catch(Exception e) {
				sb.append("error reading room " + sRooms[i-1] + ":" + e + "<br>\n");
				continue;
			}

		}

		Enumeration eFloors = this.GetFloors();
		while(eFloors.hasMoreElements()) {
			f = (Floor)eFloors.nextElement();
			try {
	BufferedReader d = new BufferedReader(new FileReader(new File(sDir, "Rooms" + (new Integer(f.HashKey().hashCode())).toString())));
	String sKey = null;
	int iCurr = 0;
	while((sKey = d.readLine()) != null)
		f.insertRoom(f.GetRoom(sKey), iCurr++);
			} catch(Exception e) {
				sb.append("error :" + e + "<br>\n");
			}
		}
		return sb.toString();
	}
  */


    protected HTML liveList0(int level, String handle, boolean showPics,
			     Container ct)
	throws ChatException {
	return super.liveList0(6, handle, showPics, ct);
    }  

    protected String modifyPage() throws ChatException {
	return fileToString(sNewHTML);
    }  

    protected ChatObject newChild(Login lOwner, LookupTable lt) throws ChatException {
	return new Floor(lOwner, lt, this);
    }  

    static void readImages() {
	POST_IMAGES = new Hashtable();
	readResourceList("/images/post/cclist", POST_IMAGES);
    }  

    static void readResourceList(String sFile, Hashtable ht) {
	try {
	    BufferedReader in = new BufferedReader(
						   new FileReader(DIR_RESOURCE + sFile));

	    String line;
	    while ((line = in.readLine()) != null) {
		if (line.startsWith("#"))
		    continue;
		int iURL = lastSpace(line) + 1;
		if (iURL <= 0 || iURL >= line.length())
		    continue;
		ht.put(line.substring(0, iURL - 1), line.substring(iURL));
	    }
	} catch (IOException ignored) { }
    }  

    static void readSounds() {
	POST_SOUNDS = new Hashtable();
	readResourceList("/sounds/cclist", POST_SOUNDS);
    }  
}
