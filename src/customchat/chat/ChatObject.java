package customchat.chat;

import customchat.util.*;
import customchat.htmlutil.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;


public abstract class ChatObject extends Object implements Serializable {

    static final long serialVersionUID = 6680373933489036655L;
    public static final Registry registry = new Registry();
    public static long maxChatters = 0;
   
    public static final int DEFAULT = 0;
    /*
     * Session Commands:
     * Commands that have to do with normal chatting.
     * All session commands are between 100 and 199.
     */
    public static final int SEND = 100;
    public static final int LIST = 101; // List contents of this object
    public static final int FIND = 102; // Find a user room or area
    public static final int VIEW_LOG = 103; //View chat log
    public static final int BROADCAST = 104;
    public static final int SUBMIT = 105;
    public static final int NEW_ROOM = 106;
    public static final int WHISPER = 107;
    /*
     * Construction Commands:
     * Commands that have to do with modifying objects.
     * All Construction commands are between 200 and 299.
     */
    public static final int CREATE = 200;
    public static final int MODIFY = 201;
    public static final int DELETE = 202;
    public static final int REORDER = 203;
    public static final int CREATE_PAGE = 204;
    public static final int MODIFY_PAGE = 205;
    public static final int GALLERY_PAGE = 206;

    /*
     * User Manager Commands:
     * Commands used to manage users.
     * All User Manager Commands are between 300 and 399
     */
    public static final int BOOT_MANAGER = 300;
    public static final int BOOT_ADD     = 301;
    public static final int BOOT_EDIT    = 302;
    public static final int ADD_ADMIN    = 303;
    public static final int ADD_ADMIN_PAGE = 304;
    public static final int UPDATE_ADMINS  = 305;
    public static final int PRIVILEGES_PAGE = 306;
    public static final int ADMIN_MANAGER = 307;
    public static final int ADMIN_PASSWORD = 308;
    public static final int TRANSCRIPT_DELETE = 309;
    public static final int PRIVATE_ROOMS = 310 ;
    public static final int ADD_USER_ADMIN = 311 ;
    public static final int IP_LOG = 312;
    public static final int IP_LOG_UPDATE = 313;
	
    /*
     * Names of variables in the CGI lookup table
     *
     */
    public static final String SET_ONE_VAR = "Set";
    public static final String PUBLIC_MESSAGE_VAR = "Public";
    public static final String XPUBLIC_MESSAGE_VAR = "XPublic";
    public static final String PRIVATE_MESSAGE_VAR = "Private";
    public static final String PRIVATE_CHECKBOX_VAR = "PrivCheckbox";
    public static final String RECIPIENT_VAR = "Recip";
    public static final String SEARCH_VAR = "ss";
    public static final String COM_VAR = "Action";
    public static final String SHOW_PIC_VAR = "Pics";
    public static final String LAST_ROOM_VAR = "LastRoomKey";
    public static final String ORDER_VAR = "Order";
    public static final String TYPE_VAR = "Type";
    public static final String SET_VAR = "Set Variables";
    private static final String sAdminStartVar = "sAdminStart";
    private static final String sAdminPassVar = "sAdminPassphrase";
    private static final String sAdminNameVar = "sAdminsName";
    private static final String sAdminStopVar = "sAdminStop";

    // are we in manual or realtime mode
    public boolean bScroll;

    protected static Login lDefaultOwner;
    static {
	String root = System.getProperty ("chat.owner");
	if(root == null || root.indexOf(":") <= 0) {
	    lDefaultOwner = new Login("admin", "gofi$h");
	    System.err.println("No Super User User name or Password.");
	    System.err.println("Defaulting to \"admin\" \"gofi$h\".");
	}
	else {
	    int i = root.indexOf(":");
	    String user = root.substring(0,i);
	    String pass = root.substring(i+1);
	    if(user.length() <= 4 || pass.length() <= 4) {
		lDefaultOwner = new Login("admin", "gofi$h");
		System.err.println("Super user User name or Password is too short.");
		System.err.println("Defaulting to \"admin\" \"gofi$h\".");
	    } else
		lDefaultOwner = new Login(user, pass);
	}
    }

    protected transient Vector vChatters = new Vector();  //  List of current chatters
    protected static final String[] sPrivilegeNames = {"Create", "Edit", "Delete"};
    protected static final int CAN_CREATE = 0, CAN_EDIT = 1, CAN_DELETE = 2;
    protected static final String CHAT_FRAME = "_top";
    public static String URL_PREFIX;
    static {
	if ((URL_PREFIX = System.getProperty("chat.urlprefix")) == null)
	    URL_PREFIX = "/";
    }
    // Variables for file names
    protected static final String FILE_PREFIX = "cc";
    protected static final String FILE_DATA = FILE_PREFIX + "data";
    protected static final String FILE_TRANSCRIPT = FILE_PREFIX + "messages.html";
    protected static final String FILE_ADMINS = FILE_PREFIX + "admins";
    public static final String FILE_BOOTED = FILE_PREFIX + "booted";
    protected static final String FILE_ORDER = FILE_PREFIX + "order";
    protected static final String FILE_POST_IMAGES = "ccimages";
    protected static final String FILE_POST_SOUNDS = "ccsounds";
    protected static final String FILE_BLOCKED_IP = FILE_PREFIX + "ipblock" ;

    // Default directories for data and HTML (see below)
    public static String DIR_DATA;
    public static String DIR_HTML;
    // new css code (needs testing)
    public static String DIR_CSS;
    public static String URL_IMG;
    public static String URL_RESOURCE;
    public static String DIR_RESOURCE;

    static {
	DIR_DATA = System.getProperty("chat.datadir");
	if(DIR_DATA == null)
	    DIR_DATA = "." + File.separator + "data" + File.separator;
	//	  DIR_DATA = "." + File.separator + "data" ;

	DIR_HTML = System.getProperty("chat.htmldir");
	if(DIR_HTML == null)
	    DIR_HTML = "." + File.separator + "html" + File.separator;

	DIR_RESOURCE = System.getProperty("chat.resourcedir");
	if (DIR_RESOURCE == null)
	    DIR_RESOURCE = "." + File.separator + "resources" + File.separator;

	URL_RESOURCE = System.getProperty("chat.resourceurl");
	if (URL_RESOURCE == null)
	    URL_RESOURCE = "/resources/";

	URL_IMG = System.getProperty("chat.imgurl");
	if(URL_IMG == null)
	    URL_IMG = URL_RESOURCE + "images/";
    }

    protected static final int SEARCH_RESULTS_LIMIT = 250;

    // Variables set from the web
    public String sKeyWord;
    public String sName;
    public String sExitURL = null;
    public int iDefaultDepth = 2;
    public String sBgType = "Color";        // Background type
    public String sBgURL = null;            //choose a background image by supplying a URL
    public String sBg = "000000";           //select a background color
    public String sTextColor = "FFFFFF";    // text color
    public String sLinkColor = "0000FF";    // link color
    public String sVLinkColor = "660099";   // Visited link color
    public boolean bHidden = false;         // This Object is hidden
    public boolean areaHidden = false;         // ritchie: This Object is hidden
    public boolean bLogging = false;
    public String sButtonURL = URL_IMG + "buttons/lightgray/";
    public int iWordSet = BadLanguageFilter.NONE;
    public String video;
    public String sVideo;
    public boolean bDing = false;
    public boolean bUserDing = false;
    public boolean bHtmlDis = false;
    public boolean bSwitchDis = false;
    public boolean bPMDis = false;
    public boolean bPrivateRooms = false;
    public String bIdleTimes = "1";


    // every object can get one of these ...
    public  BadLanguageFilter blf = new BadLanguageFilter() ;

    // The object that contain this object. null means it is the root object
    public ChatObject parent = null;

    // A list of all children in the order they will be displayed
    protected Vector children = new Vector(16);
    Vector vSounds = null;
    Vector vImages = null;

    protected Page pageTemplate;
    public Login lOwner;
    private Hashtable htAdmins = new Hashtable();
    private BootManager booted = new BootManager(this);
 

    private transient long activeCount = 0L;
    private transient PrintWriter logOut;


    public ChatObject() {}
    /**
     * Constructs a new chat object.
     *
     * @param l is the owner of this object
     * @param parent is the parent ChatObject that contains this Object.
     * @param sKey the unique keyword identifying this ChatObject in the
     *        chat object registry.
     * @see Registry
     */

    ChatObject(Login l, ChatObject parent, String sKey, String sName) throws ChatException {
	lOwner = l;
	if (sName == null)
	    throw new ChatException("You must specify a name.");
	this.sName = sName;
	if (sKey == null)
	    sKey = sName.trim().toLowerCase();
	this.sKeyWord = sKey;
	this.parent = parent;
	registry.add(sKey, this);
    }


    HTML PMHTML(Chatter c)
	throws ChatException {

	House h = (House) registry.get("");

	//new Link(h.commandURL(SEND) + "&" + RECIPIENT_VAR + "="
	//+ URLEncoder.encode(c.HashKey()),
	//"<IMG SRC=\"" + buttonURL("pmcheck.gif") + "\" ALT=\"Send "
	//+ c.getText() + " a private message\" BORDER=0>",
	//   "_blank");
	Link popup = new Link("javascript:openPM('"+h.commandURL(SEND) + "&" + RECIPIENT_VAR + "=" + URLEncoder.encode(c.HashKey())+"')",
			      "<IMG SRC=\"" + buttonURL("pmcheck.gif") + "\" ALT=\"Send " + c.getText() + " a private message\" BORDER=0>") ;
		
	return popup;
	
    }            

    public void addAdmin(Admin a) {
	htAdmins.put(a.getLogin(), a);
    }  

    public Page addAdmin(Login l, LookupTable lt) throws UnauthorizedException{
	HTML error = null;
	Page page;
	if (!isOwner(l) && !bPrivateRooms) throw new UnauthorizedException("Only owners can add Admins");
	try {
	    String sName = lt.getValue(sAdminNameVar);
	    String sPass = lt.getValue(sAdminPassVar);
	    Calendar start = HTMLDate.getCalendar(sAdminStartVar, lt);
	    Calendar stop = HTMLDate.getCalendar(sAdminStopVar, lt);
	    addAdmin( new Admin(sName, sPass, start, stop));
	    save(htAdmins, FILE_ADMINS);
	} catch (Exception e) {
	    ErrorLog.error(e, 204, "Could not create new admin");
	    error = new Text("Could not create new admin. " + e.getMessage());
	}
	page = addAdminPage(l);

	if(error != null)
	    addAdminPage(l).addHTML(error);
	return page;
    }  


    public Page addAdminManually(Login l, String name,String pass) throws UnauthorizedException{
	HTML error = null;
	Page page;
	try {
	    String sName = name ;
	    String sPass = pass ;
	    Calendar start = HTMLDate.getCalendarDate(1,1,2000);
	    Calendar stop = HTMLDate.getCalendarDate(1,1,2010);
	    addAdmin( new Admin(sName, sPass, start,stop));

	    save(htAdmins, FILE_ADMINS);
	} catch (Exception e) {
	    ErrorLog.error(e, 204, "Could not create new admin");
	    error = new Text("Could not create new admin. " + e.getMessage());
	}

	page = addAdminPage(l);

	if(error != null)
	    addAdminPage(l).addHTML(error);

	return page;
    }  

    public Page addAdminPage(Login l) throws UnauthorizedException {
	if (!isOwner(l) && !bPrivateRooms) throw new UnauthorizedException("Only owners can add Admins");
	
	Page p = new Page(new Body("FFFFFF",null,null,null,null));
	
	// Reloads the top frame when you load in the bottomframe
	p.addHeadHTML(new Container("SCRIPT", "<!--\ntop.EditFrame.location.href=\""
				       + URL_PREFIX + getKeyWord() + "?" + COM_VAR + "=" + PRIVILEGES_PAGE + "\";\n//-->"));

	Form f = new Form("AddAdminForm", URL_PREFIX + getKeyWord(), "POST");
	Container tr;

	f.addHTML(new Input(Input.HIDDEN, COM_VAR, String.valueOf(ADD_ADMIN)));
	Container table = new Container("TABLE");

	table.addArgument("WIDTH","650");
	f.addHTML(table);
	table.addHTML(tr = new Container("TR"));

	tr.addHTML("<TD WIDTH=100 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>"
		   + "User Name:</B></FONT></TD>");
	tr.addHTML("<TD WIDTH=100 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>"
		   + "Passphrase:</B></FONT></TD>");
	tr.addHTML("<TD WIDTH=190 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>"
		   + "Becomes Active:</B></FONT></TD>");
	tr.addHTML("<TD WIDTH=190 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>"
		   + "Expires:</B></FONT></TD>");
	tr.addHTML("<TD WIDTH=60 BGCOLOR=\"#A4C6F4\">&nbsp;</TD>");

	table.addHTML(tr = new Container("TR"));
	tr.addHTML(new Container("TD",new Input(Input.TEXT, sAdminNameVar, "").addArgument("Size","12")));
	tr.addHTML(new Container("TD",new Input(Input.TEXT, sAdminPassVar, "").addArgument("Size","12")));
	tr.addHTML(new Container("TD",new HTMLDate(sAdminStartVar)));
	tr.addHTML(new Container("TD",new HTMLDate(sAdminStopVar)));
	tr.addHTML(new Container("TD",new Input(Input.SUBMIT, "SubmitButton", "Add Admin")));

	p.addHTML("<CENTER>");
	p.addHTML(f);
	p.addHTML("</CENTER>");
	return p;
    }          

    public void addChatter(Chatter c) throws ChatException {
	if(activeCount >= maxChatters && !c.getLogin().equals(lOwner)) {
	    ChatException ce = new ChatException("User limit has been reached.  Try back later.");
	    ErrorLog.error(ce, 0, "Upgrade to a larger license at http://customchat.com");
	    throw new ChatException("User limit has been reached.  Try back later.");
	}
	if(parent != null)
	    parent.addChatter(c);
	activeCount++;
    }      

    public final void addChild(ChatObject chld) throws ChatException {
	synchronized (children) {
	    synchronized(chld) {
		if(children.contains(chld))
		    throw new ChatException(chld.sName + " already exists. <br>" +
					    "Click here to edit"); // put in url!!!
		children.addElement(chld);
	    }
	    saveOrder();
	}
    }  
    void addImage(String sDesc) {
	if(vImages == null) vImages = new Vector();
	vImages.addElement(sDesc);
    }
    void addImage(String sName, String sURL) {
	addImage(getDescription(sName, sURL));
    }  
    void addSound(String sDesc) {
	if(vSounds == null) vSounds = new Vector();
	vSounds.addElement(sDesc);
    }
    void addSound(String sName, String sURL) {
	addSound(getDescription(sName, sURL));
    }  

    public Page adminManager(Login l) throws UnauthorizedException {
	Page p = new Page();
	if (!isOwner(l)) throw new UnauthorizedException("Only owners can add/edit Admins");
	p.addFrameSetArg("ROWS", "80%,*");

	// Top Frame
	Container top = new Container("FRAME");
	top.addArgument("NAME", "EditFrame");
	top.addArgument("SRC", URL_PREFIX + getKeyWord() + "?" + COM_VAR + "="
			+ PRIVILEGES_PAGE);
	p.addFrame(top);

	// Bottom Frame
	Container bottom = new Container("FRAME");
	bottom.addArgument("SRC", URL_PREFIX + getKeyWord() + "?" + COM_VAR + "="
			   + ADD_ADMIN_PAGE);
	bottom.addArgument("NAME", "AddFrame");
	p.addFrame(bottom);
	return p;
    }  

    protected Form adminPullDown(Login l) {
	if (!isAdmin(l)) {
	    return null;
	}

	Form f = form("admin" + sName);
       	PullDown pdCommand = new PullDown(COM_VAR);
	f.addHTML(pdCommand);

	if (parent != null && parent.hasPrivilege(l, CAN_EDIT)) {
	    PullDown pdSlot = new PullDown(ORDER_VAR);
	    f.addHTML(pdSlot);
	    for (int i = 0; i < parent.children.size(); i++) {
		pdSlot.addOption(String.valueOf(i+1), String.valueOf(i),
				 this.equals(parent.children.elementAt(i)));
	    }
	    pdCommand.addOption("Set Position", String.valueOf(REORDER), true);
	}

	if (hasPrivilege(l, CAN_CREATE)
	    && (this instanceof House || this instanceof Floor))
	    pdCommand.addOption("New " + childName(), String.valueOf(CREATE_PAGE));


	if (hasPrivilege(l, CAN_EDIT) && parent != null ) {
	    pdCommand.addOption("Edit", String.valueOf(MODIFY_PAGE));
	}

	if(hasPrivilege(l, CAN_DELETE) && parent != null )
	    pdCommand.addOption("Delete", String.valueOf(DELETE));

	if(isOwner(l))
	    pdCommand.addOption("Admin Manager", String.valueOf(ADMIN_MANAGER));

	if(isAdmin(l))
	    pdCommand.addOption("Boot Manager", String.valueOf(BOOT_MANAGER));

	//if(isAdmin(l) && parent == null)
	if (isOwner(l))
	    pdCommand.addOption("Bad Word Manager", String.valueOf(House.BAD_WORDS));

	if(isAdmin(l) && (new File(getPath() + FILE_TRANSCRIPT)).exists())
	    pdCommand.addOption("View Chat Transcript", String.valueOf(VIEW_LOG));

	if(isAdmin(l) && (new File(getPath() + FILE_TRANSCRIPT)).exists())
	    pdCommand.addOption("Delete Transcript", String.valueOf(TRANSCRIPT_DELETE));	

	if(isAdmin(l))
	    pdCommand.addOption("IP Log & Block", String.valueOf(IP_LOG));	
	
	if(hasPrivilege(l, CAN_EDIT) && this instanceof Room) {
	    pdCommand.addOption("Postable Image Manager", String.valueOf(Room.COM_POST_IMAGES));
	    pdCommand.addOption("Postable Sound Manager", String.valueOf(Room.COM_POST_SOUNDS));}
	

	
	f.addHTML(new Input(Input.SUBMIT, "", "Submit"));
	return f;
    }            

    HTML bootHTML(Chatter c) {
	return
	    new Link(commandURL(BOOT_MANAGER) + "&"
		     + BootManager.BOOT_HANDLE_VAR
		     + "=" + URLEncoder.encode(c.sHandle),
		     "<IMG SRC=\"" + buttonURL("boot.gif") + "\" ALT=\"Boot "
		     + c.getText() + " from " + sName + "\" BORDER=0>",
		     "_blank");
    }  

    public String buttonURL(String s) {
	return sButtonURL + s;
    }  

    void checkBooted(Login l)
	throws BootedException {
	if(!isAdmin(l))
	    booted.isBooted(l);
	if(parent != null)
	    parent.checkBooted(l);
    }

    protected abstract String childCreationPage() throws ChatException;  
    protected abstract String childName();  

    public synchronized void cleanup() {
	File fDir = new File(getPath());
	String[] files = fDir.list();
	for (int i = 0; i < files.length; i++) {
	    //Debug.println(files[i]);
	    if (files[i].startsWith(FILE_PREFIX)) {
		//Debug.println("deleted");
		(new File(getPath(), files[i])).delete();
	    }
	}
	fDir.delete();
    }  

    void clearImageList() {
	vImages = null;
    }  

    void clearSoundList() {
	vSounds = null;
    }  

    public String commandButton(final int command) {
	String sURL;

	switch(command) {
	case SEND :
	    sURL = buttonURL("send.gif");
	    break;
	case DELETE :
	    sURL = buttonURL("delete.gif");
	    break;
	case MODIFY_PAGE :
	    sURL = buttonURL("edit.gif");
	    break;
	case BOOT_EDIT :
	    sURL = buttonURL("bootuser.gif");
	    break;
	case FIND :
	    sURL = buttonURL("searchglass.gif");
	    break;
	case SUBMIT:
	    sURL = buttonURL("submit.gif");
	    break ;
	case NEW_ROOM:
	    sURL = buttonURL("newRoom.gif");
	    break ;
	case WHISPER:
	    sURL = buttonURL("whisper.gif");
	    break ;
	default :
	    sURL = buttonURL("default.gif");
	}

	return sURL;
    }  

    public final static Input commandInput(final short commandType,final int commandNumber) {
	return  new Input(commandType, COM_VAR, String.valueOf(commandNumber));
    }  

    public String commandURL(int commandNumber) {
	return URL_PREFIX + getKeyWord() + "?"
	    + COM_VAR + "=" + String.valueOf(commandNumber) ;
	    //+ "&" + String.valueOf(Math.random());
    }  

    public Page createPage(Login l) throws ChatException {
	if(!hasPrivilege(l, CAN_CREATE))
	    throw new UnauthorizedException("You do not have create privileges for "
					    + sName + ".");

	String sFile = childCreationPage();
	sFile = replace(sFile, "#sKeyWord#", getKeyWord());
	sFile = replace(sFile, "#Command#", String.valueOf(CREATE));
	sFile = replace(sFile, "#sName#", sName);
	//sFile = replace(sFile, "#URL_IMG#", URL_IMG);
	sFile = replace(sFile, "#GalleryPullDown#",
			PageFactory.makeDirectoryPulldown(new File(DIR_RESOURCE + "images/backgrounds")).toString());

	return new Page( sFile );
    }  

    public Page defaultCommand(Login lUser, LookupTable lt)
	throws ChatException {
	return list(lUser,
		    (lt == null) ? null  : lt.getValue(Chatter.HANDLE_VAR),
		    (lt == null) ? false : lt.getValue(SHOW_PIC_VAR) != null);
    }  

    public void deleteTranscript() {
	// check the transscript log size and delete if approp
	if (logOut != null) {
	    File tf = new File(getPath() + FILE_TRANSCRIPT)	 ;
	    if (tf.length() >= 2048000L) {
		ErrorLog.error(null,1000,"Deleting transcript file at 2MB") ;
		synchronized (logOut) {
		    logOut.close() ;
		    logOut = null ;
		}
		tf.delete() ;
	    }
	}	
    }

    public Page doBroadcast(Login l, Message m)
	throws ChatException {
	if (!isAdmin(l))
	    throw new UnauthorizedException("Only Admins may broadcast messages.");

	for (int i = 0; i < children.size(); i++)
	    ((ChatObject) children.elementAt(i)).doBroadcast(l, m);

	return defaultCommand(l, null);
    }  

    protected Page doCommand(Login lUser, LookupTable lt, PrintWriter out, int iCommand)
	throws ChatException, IOException {
	Container con = new Container(); 
	Page p = null;
	// handle the command

	if (isIPBlocked(lUser) && iCommand != IP_LOG) 
	    throw new UnauthorizedException("Your IP has been blocked from this room.");
		
	switch(iCommand) {
	case LIST :
	    p = list(lUser, lt.getValue(Chatter.HANDLE_VAR), lt.getValue(SHOW_PIC_VAR) != null);
	    break;
	case BROADCAST :
	    p = doBroadcast(lUser, new Message(lt.getValue(PUBLIC_MESSAGE_VAR),
					       House.ctDaemon,
					       (String)null,
					       Message.BROADCAST));
	    break;
	case VIEW_LOG :
	    p = viewLog();
	    break;
	    //Construction Commands:
	case CREATE :
	    throw new RedirectException(doCreate(lUser, lt));
	case MODIFY :
	    p = doModify(lUser, lt);
	    break;
	case DELETE :
	    throw new RedirectException(doDelete(lUser));
	case REORDER :
	    throw new RedirectException(doReorder(lUser, lt));
	case CREATE_PAGE :
	    p = createPage(lUser);
	    break;
	case MODIFY_PAGE :
	    p = modifyPage(lUser);
	    break;
	case GALLERY_PAGE :
	    String dir       =  lt.getValue(PageFactory.VAR_GALLERY_DIR),
		fieldName =  "opener.document.Creator.sBgURL.value";
	    p = PageFactory.makeGalleryPage(new File(DIR_RESOURCE, dir),
					    URL_RESOURCE + dir,
					    fieldName);
	    break;
	    //Admin Commands
	case ADD_ADMIN :
	    p = addAdmin(lUser, lt);
	    break;
	case ADD_ADMIN_PAGE :
	    p = addAdminPage(lUser);
	    break;
	case UPDATE_ADMINS :
	    p = updateAdmins(lUser, lt);
	    break;
	case PRIVILEGES_PAGE :
	    p = privilegesPage(lUser);
	    break;
	case ADMIN_MANAGER :
	    p = adminManager(lUser);
	    break;
	case BOOT_ADD :
	    if(!isAdmin(lUser))
		throw new UnauthorizedException("Only admins can boot users.");
	    p = booted.addPage(lt);
	    break;
	case BOOT_EDIT :
	    if(!isAdmin(lUser))
		throw new UnauthorizedException("Only admins can boot users.");
	    p = PageFactory.makeUtilPage(booted.editPage(lt));
	    break;
	case BOOT_MANAGER :
	    if(!isAdmin(lUser))
		throw new UnauthorizedException("Only admins can boot users.");
	    p = booted.frameSet(lt);
	    break;

	case ADMIN_PASSWORD:
	    if(!isAdmin(lUser))
		throw new UnauthorizedException("Only admins can change the admin password.");
	    // first change the disk based password  ...
	    // ritchie:I don't know why the system properties collection is used for this purpose ;

	    Properties props = System.getProperties() ;
	    props.put("chat.owner","admin:"+lt.getValue("p1")) ;
	    lOwner = new Login("admin",lt.getValue("p1")) ;
	    props.save(new FileOutputStream("." + File.separator + "chat.properties"),"customchat properties.");	
	    Container c = new Container() ;
	    c.addHTML("<p><center><font face=\"Arial,Helvetical,Geneva\" size=\"4\">New Password Set</font></center>") ;
	    p = PageFactory.makeUtilPage(c);
		
	    break ;

	case TRANSCRIPT_DELETE:
	    File tsf = new File(getPath() + FILE_TRANSCRIPT) ;
  	    if(isAdmin(lUser) && tsf.exists()) {
		synchronized(logOut) {
	  	    if (logOut != null) {		  	    
			logOut.close() ;
			logOut = null ;
		    }
		}
	  	    
		tsf.delete() ;
		Container ct = new Container() ;
		ct.addHTML("<p><center><font face=\"Arial,Helvetical,Geneva\" size=\"4\">Transcript Deleted</font></center>") ;
		p = PageFactory.makeUtilPage(ct);
  	    }
	    break ;
	   

        case PRIVATE_ROOMS:
	    con = new Container("CENTER") ;

	    con.addHTML(privateRoomPage) ;
	    Form newpassword = new Form("Password",null,null);
	    p = PageFactory.makeUtilPage(con);
	    p.addHeadHTML(getJSPassword()) ;
	    break ;
	
	case ADD_USER_ADMIN:
	    con = new Container("CENTER") ;
	    Login l = new Login (lt.getValue("user"),lt.getValue("p1")) ;
	    String key = doCreate(l, lt);
	    ChatObject co = registry.get(key.substring(1)) ;
	    co.addAdminManually(l,lt.getValue("user"),lt.getValue("p1")) ;
	    InetAddress addr = null ;
	    try {
		addr = InetAddress.getLocalHost() ;
	    } catch (java.net.UnknownHostException exc) {}

	    String newRoom = 
		"<P>" +
		"<B><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">Your chat room has been "+
		"created.</FONT></B>" +
		"<P>" +
		"<B><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">Log in with the SAME User "+
		"Name and Password you entered on the last screen, <BR>if you wish to have "+
		"the Administrative options inside the room."+
		"</FONT></B>"+
		"<P>"+
		"<B><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\"><a href=\""+key+"?Reset\">CLICK HERE</a> to enter your "+
		"room now.</FONT></B>" +
		"<P>" +
		"<B><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">The URL to your private "+
		"room is:" + "&lt;A&nbsp;HREF=\"http://"+addr.getHostAddress()+":"+Server.getPort() + key + "\"&gt;<BR>" + 
		"</FONT></B>" ;

	    con.addHTML(newRoom) ;

	    p = PageFactory.makeUtilPage(con);
	    break ;

	case FIND :
	    return doFind(lUser, lt.getValue(SEARCH_VAR),lt);

	case House.BAD_WORDS :
	    String s = sKeyWord;
	    return PageFactory.makeUtilPage(blf.update(lt,this));

	case IP_LOG:
		Container cont = logList(lUser, (String)lt.getValue(Chatter.HANDLE_VAR),lt);
	    p = PageFactory.makeUtilPage(cont);
	break ;

	case IP_LOG_UPDATE:
	
	
	break ;


	case DEFAULT :
	    p = defaultCommand(lUser, lt);
	    break ;
		  
	default :   throw new ChatException("Not Implemented");
	}
	return p;
    }                                                  

	public boolean isIPBlocked(Login l) {
		try {
				File blocked = new File(getPath() + FILE_BLOCKED_IP) ;
				if (blocked.exists()) {
					java.io.BufferedReader is = new java.io.BufferedReader(new FileReader(blocked)); 
					String st = null ;
					while ((st = is.readLine()) != null) {
						int i = st.indexOf(':') ;
						if (l.IP.equals(st.substring(0,i)))
							return true ;
					}
					is.close() ;
				}
		} catch(Exception e) {
			System.out.println(e) ;
		}
		return false ;
	}

    public final void doCommand(Login lUser, LookupTable lt, PrintWriter out, boolean bPrintHeader)
	throws ChatException {
		
		if(lUser != null)
			checkBooted(lUser);
		 
		// get the command variable COM_VAR
		String sCommand = lt.getValue(COM_VAR);
		int iCommand;
		Page p;

		if(sCommand == null)
			iCommand = DEFAULT;
		else
			try {
			iCommand = Integer.parseInt(sCommand);
			} catch (NumberFormatException e) {
			throw new ChatException(sCommand + " is an invalid Action");
			}

		try {
			Object key = customchat.util.Timer.start(null);
			//Debug.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>") ;
			//Debug.println("sKeyWord:"+sKeyWord );
			//Debug.println("iCommand:"+ String.valueOf(iCommand));
			//Debug.println("Timer:"+String.valueOf(customchat.util.Timer.stop(key)));
			//Debug.println("-------------------------------------") ;
			p = doCommand(lUser, lt, out, iCommand);
			
		} catch(ChatException e) {
			throw e;
		} catch(Exception e) {
			ErrorLog.error(e, 204, "Unexpected Exception in doCommand");
			throw new ChatException("An unexpected Error ocurred. If the problem persists contact Tech. Support.");  // Bug!!!!
		}

		if (bPrintHeader) {
			out.print("HTTP/1.0 " + HTTP.STATUS_OKAY + " "
				  + HTTP.getCodeMessage(HTTP.STATUS_OKAY) + "\r\n");
			out.print("Content-type: text/html\r\n");
			out.println();
		}
		//System.out.println(p.toString()) ;
		out.print(p.toString());
		out.close();

    }      

    public String doCreate(Login lOwner, LookupTable lt)
	throws ChatException {
		if(!hasPrivilege(lOwner, CAN_CREATE) && !bPrivateRooms)
			throw new UnauthorizedException("You do not have creation privileges for "
							+ sName + ".");
		ChatObject chld = newChild(lOwner, lt);
		addChild(chld);
		return URL_PREFIX + chld.getKeyWord();
    }  

    public synchronized String doDelete(Login lUser) throws ChatException {
		if(!hasPrivilege(lUser, CAN_DELETE))
			throw new
			UnauthorizedException("You do not have deletion privileges for " + sName);

		ChatObject p = parent;

		// Check that we are not the root node in the tree;
		if( p == null )
			throw new ChatException("You cannot delete a root object");

		// Remove us from the parents children list
		parent.remove(this);

		// Remove is from the registry
		registry.remove(sKeyWord);

		for(int i = 0; i < children.size(); i++) {
			((ChatObject)children.elementAt(i)).doDelete(lUser);
		}

		// Do any cleanup that is necessary.  For example booting chatters.
		cleanup();

		// Remove link back to parent
		parent = null;

		return URL_PREFIX + p.getKeyWord();
    }  

    public Page doModify(Login l, LookupTable lt)
	throws ChatException {
		if (!hasPrivilege(l, CAN_EDIT))
			throw new
			UnauthorizedException("You do not have edit privileges for " + sName);
		update(lt);
		return defaultCommand(l, null);
    }  

    protected Page doFind(Login l, String sSearch,LookupTable lt)
	throws ChatException {
		if(sSearch == null || sSearch.length() <= 0)
			throw new ChatException("Please Enter a Search String");
		
		sSearch = sSearch.toLowerCase();

		Container con = new Container("Center") ;

		con.addHTML("<H1><font face=\"Helvetica,Arial,Geneva\">Search Results</font></H1><TABLE>");
		Enumeration e = UserRegistry.getChatters();
		int iSoFar = 0;
		while(e.hasMoreElements() && iSoFar++ < SEARCH_RESULTS_LIMIT) {
			Chatter c = (Chatter) e.nextElement();


			//If the search string is a substring of their handle
			if(c.sHandle != null && c.sHandle.toLowerCase().indexOf(sSearch) >= 0) {
			con.addHTML("<TR>");

			//Print link to room
			if(c.location() != null)
				con.addHTML("<TD>" + c.location().getHTML() + "</TD>\n");

			if (!bPMDis || isAdmin(l)) {
				//Print PM button
				con.addHTML("<TD>");
				con.addHTML(PMHTML(c));
				con.addHTML("</TD>\n");
			}

			//Boot button for admins
			if(isAdmin(l)) {
				con.addHTML("<TD>");
				con.addHTML(bootHTML(c));
				con.addHTML("</TD>\n");
			}

			//Print chatter's handle
			con.addHTML("<TD NOWRAP>" + c.getHTML() + "</TD>\n");

			con.addHTML("</TR>\n");
			}
		}
		con.addHTML("</TABLE>");

		if(iSoFar >= SEARCH_RESULTS_LIMIT) {
			con.addHTML("<P><font face=\"Helvetica,Arial,Geneva\">Search results are limited to "
				  + String.valueOf(SEARCH_RESULTS_LIMIT)
				  + ". Please use a more specific search string.</font>");
		} else if (iSoFar == 0)
			con.addHTML("<I><font face=\"Helvetica,Arial,Geneva\">no matches</font></I>");

		Page p = PageFactory.makeUtilPage(con);
		p.addHeadHTML(getPMScript()) ;

		return p;
    }      

    public String doReorder(Login l, LookupTable lt)
		throws ChatException, IOException {
		String option = lt.getValue(ORDER_VAR);

		if(parent == null)
			throw new ChatException("That action can not be performed on root objects.");
		try {
			int position = Integer.parseInt(option);

			if(!parent.hasPrivilege(l, CAN_EDIT))
			throw new UnauthorizedException("You do not have Edit Privileges on " +
							this.sName + "Admin Privileges.");

			Vector temp = (Vector) parent.children.clone();
			temp.removeElement(this);
			temp.insertElementAt(this, position);
			parent.children = temp;

			parent.saveOrder();

		} catch(NumberFormatException e) {
			throw new ChatException("Your browser sent an improper option.  " +
						"If this problem persists contact technical support.");
		}

		return URL_PREFIX + parent.getKeyWord();
    }  

    String exitPage() {
		if(sExitURL == null || "".equals(sExitURL)) {
			if(parent == null)
			return(URL_PREFIX);
			return parent.exitPage();
		}
		return sExitURL;
    }

    public static String fileToString(String sFileName) throws ChatException {
		FileReader f = null;
		try {
			File fFile = new File(sFileName);
			if(fFile == null) throw new IOException();

			BufferedReader br = new BufferedReader(f = new FileReader(fFile));
			String sLine;
			StringBuffer sb = new StringBuffer((int)fFile.length());
			while((sLine = br.readLine()) != null)
			sb.append(sLine+"\n");

			String sFile = sb.toString();
			sFile = replace(sFile, "#URL_IMG#", URL_IMG);
			sFile = replace(sFile, "#URL_PREFIX#", URL_PREFIX);
			return sFile;
		} catch( IOException e) {
			throw new ChatException("Error Reading file " + sFileName + " : " + e);
		} catch( NullPointerException e) {
			throw new ChatException("Tried to open null file " + e);
		} finally {
			try {
			if( f != null ) f.close();
			} catch(IOException e) {}
		}
    }

    public Form form() {
		return form("Form");
    }  

    public Form form(String sName) { return form(sName, false); }  

    public Form form(String sName, int iCommand) {
		return form(sName, false, iCommand);
    }  

    public Form form(String sName, boolean bGet) {
		return new Form(sName, URL_PREFIX + getKeyWord() + "?" + (new Date()).getTime(), bGet ? "GET" : "POST");
    }  

    public Form form(String sName, boolean bGet, int iCommand) {
		Form f = form(sName, bGet);
		f.addHTML(commandInput(Input.HIDDEN, iCommand));
		return f;
    }  

    public Form form(boolean bGet) { return form("Form", bGet); }  

    private String getDescription(String sName, String sURL) {
		return "<OPTION VALUE=\"" + sURL + "\">" + sName + "\n";
    }  

    public HTML getHTML() {
		return getHTML(null);
    }  

    public HTML getHTML(String handle) {
		String url = URL_PREFIX + getKeyWord() + 
			((handle != null) ? ("?" + Chatter.HANDLE_VAR + "=" + URLEncoder.encode(handle)) : "");

		return new Link(url,sName + " - " + String.valueOf(numChatters()));
    }  

    Vector getImageList() {
		if(vImages == null && (vImages = (Vector)open(FILE_POST_IMAGES)) == null)
			if (parent != null)
			return parent.getImageList();
			else
			return null;
		return vImages;
    }

    /**
     * getKeyWord returns the URLEncoded keyword that identifies this object in
     * the registry;
     */
    String getKeyWord() {
		if (sKeyWord.equals("/"))
			return URLEncoder.encode("") ;
		else
			return URLEncoder.encode(sKeyWord) ;
    }  

    Login getOwner() {
		return new Login(lOwner);
    }

    public final String getPath() {
		return ((parent != null)   ? parent.getPath()  : DIR_DATA )	+ sKeyWord + File.separator;
    }    

    public static ChatObject getRoot() {
		try {
			return registry.get("");
		} catch (ChatException e) {
			ErrorLog.error(e, 502, "Root object not found");
			return null;
		}
    }  

    Vector getSoundList() {
		if(vSounds == null && (vSounds = (Vector)open(FILE_POST_SOUNDS)) == null)
			if (parent != null)
			return parent.getSoundList();
			else
			return null;
		return vSounds;
    }

    static String getString(final Vector v) {
		StringBuffer sb = new StringBuffer();
		Enumeration e = v.elements();
		while(e.hasMoreElements())
			sb.append(e.nextElement());
		return sb.toString();
    }

    public Page getTemplate() {
		return (Page)pageTemplate.clone();
    }  

    public boolean hasPrivilege(Login l, int iPrivilege) {
		if(l == null) return false;
		Admin a = (Admin)htAdmins.get(l.getLogin());
		return (isOwner(l) || (a != null && a.hasPrivilege(iPrivilege)));

    }  

    boolean haveImage(String sName) {
		if (vImages != null)
			for (int i = 0; i < vImages.size(); i++)
			if (((String)vImages.elementAt(i)).indexOf(sName) >= 0)
				return true;
		return false;
    }  

    boolean haveSound(String sName) {
		if (vSounds != null)
			for (int i = 0; i < vSounds.size(); i++)
			if (((String)vSounds.elementAt(i)).indexOf(sName) >= 0)
				return true;
		return false;
    }  

    public static String htmlFile(String s) {
		return DIR_HTML + s;
    }  

    public boolean isAdmin(Login lUser) {

		return lUser != null
			&& (lUser.equals(htAdmins.get(lUser.getLogin()))
			|| isOwner(lUser));
					// isOwner is true for parrent admins
					//|| (parent != null && parent.isAdmin(lUser)));
    }  

    public boolean isOwner(Login lUser) {
		return lOwner.equals(lUser) ||
			(parent != null && parent.isAdmin(lUser));
    }


    public String getJSPassword() {
		StringBuffer sb = new StringBuffer();

		sb.append("\n<SCRIPT language=\"JavaScript\">");
		sb.append('\n');
		sb.append("<!--");
		sb.append('\n');
		sb.append("function checkPass(theField) {\n");
		sb.append(" 	if (document.forms.Password.p1.value.length >0) { \n");
		sb.append("   if (theField.value != document.forms.Password.p1.value) {\n");
		sb.append("      theField.value = \"\" ; \n");
		sb.append("		alert(\"Passwords must be identical!\")\n");
		sb.append("		return false; \n");
		sb.append("	}\n");
		sb.append("	return  true ;\n");
		sb.append("  } else { ");
		sb.append("       alert('Please type in a password!') ; \n");
		sb.append("		return false ;");
		sb.append("  }\n");
		sb.append("}\n");
		sb.append("// -->\n");
		sb.append("</SCRIPT>\n");

		return sb.toString() ;
    }

    public Page list(Login lUser, String handle, boolean showPics) throws ChatException {

		Page p = (Page)pageTemplate.clone() ;
		StringBuffer sb = new StringBuffer();

		sb.append(getPMScript()) ;
		sb.append(getJSPassword()) ;

		// search form

		Form f = form("Search", true, FIND);

		f.addHTML("<TABLE><TR><TD><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"1\">Search for Someone</FONT><BR></TD></TR><TR><TD valign=\"middle\">");
		f.addHTML(" ");
		f.addHTML(new Input(Input.TEXT, SEARCH_VAR, ""));
		f.addHTML("</TD></TR></TABLE>");
		Form newpassword = null ;
		if (isAdmin(lUser)) {
			newpassword = form("Password", true, ADMIN_PASSWORD);
			newpassword.addArgument("onSubmit=\"return checkPass(document.forms.Password.p2)\"");

			String c1 = "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">New Super Admin Password:</font>" ;
			String c2 = "<input type=\"password\" name=\"p1\" size=\"15\">" ;
			String c3 = "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">Confirm New Password:</font>" ;
			String c4 = "<input type=\"password\" name=\"p2\" size=\"15\" onBlur=\"checkPass(this)\">" ;
			String c5 = "<input type=\"image\" border=0 src =\""+commandButton(SUBMIT)+"\" name=\"Submit\" value=\"Submit\">" ;

			newpassword.addHTML("<table cellspacing=0 cellpadding=0><tr><td>"+c1+"</td><td>"+c2+"</td></tr><tr><td valign=\"top\">"+c3+"</td><td>"+c4+"<br>"+c5+"</td></tr></table>") ;
		}

		if (newpassword != null)
			sb.append("<table cellspacing=0 cellpadding=0><tr><td colspan=\"2\"><hr noshade></td></tr><tr><td>"+f.toString()+"</td><td>"+newpassword.toString()+"</td><tr><tr><td colspan=\"2\"><hr noshade></td></tr></table>") ;
		else
			sb.append("<table><tr><td>"+f.toString()+"</td><tr></table>") ;

		p.addHTML(sb.toString());
		p.addHTML(liveList(lUser, handle, showPics, iDefaultDepth));

		return p;
    }


    public String getPMScript() {
		StringBuffer sb = new StringBuffer();
		sb.append("<SCRIPT language=\"JavaScript\">\n");
		sb.append("<!--\n");
		sb.append("function openPM(link) {\n");
		sb.append(" PMwin = window.open(link, 'pmwin','width=650,height=450,scrollbars=no,menubar=no,resize=no')\n");
		sb.append("}\n");
		sb.append("// -->\n");
		sb.append("</SCRIPT>\n");
		return sb.toString() ;
    }

    /**
     * Prints out the live list page for the ChatObject.
     *
     * @param showPics True means show images

     protected HTML liveList(Login l, String handle, boolean showPics)
     throws ChatException {
     return liveList(l, handle, showPics, iDefaultDepth);
     }
    */

    HTML liveList(Login l, String handle, boolean showPics, int depth)
	throws ChatException  {
		
		int level = (depth + 1)*2;
		if (depth == 0) return liveList0(level, handle, showPics, null);

		Container list = new Container("DL");

		if (!areaHidden  || isAdmin(l)) {
			Container c =new Container("DT", liveList0(level, handle, showPics, adminPullDown(l))) ;
			list.addHTML(c);

			if (isAdmin(l)) {
			Form fBroadcast = form("BroadcastForm", BROADCAST);
			list.addHTML(fBroadcast);

			Container message = new Container("TEXTAREA");
			fBroadcast.addHTML(message);
			message.addArgument("NAME", PUBLIC_MESSAGE_VAR);
			message.addArgument("ROWS", String.valueOf("3"));
			message.addArgument("COLS", String.valueOf("45"));
			fBroadcast.addHTML("<BR>");
			fBroadcast.addHTML(new Input(Input.SUBMIT, "", "Broadcast Message to " + sName));
			} 
			//else
			//	  list.addHTML("<BR>");

			// Print out the children
			for(int i = 0; i < children.size(); i++)
			try {
				ChatObject child = (ChatObject)children.elementAt(i);
				if(!child.bHidden || child.isAdmin(l)) {
				list.addHTML(new Container("DD",child.liveList(l, handle,showPics,depth - 1)));
				}
			} catch(ArrayIndexOutOfBoundsException ignore) {}


			if (bPrivateRooms) {
				Form f = form("CPR", true, FIND);
				list.addHTML(f);
				f.addHTML("<A HREF=\"" + commandURL(PRIVATE_ROOMS)+"\"><img border=0 src="+commandButton(NEW_ROOM)+"></a>");
			}
		}
		return list;
    }                      

    protected HTML liveList0(int level, String handle, boolean showPics,
			     Container ct) throws ChatException {
		
		if (ct == null)
			ct = new Container();

		if (bHidden || areaHidden)
			ct.addHTML("<I>(Hidden)</I>");

		Container font = new Container("FONT", getHTML(handle));
		ct.addHTML(new Container("B", font));
		font.addArgument("SIZE", String.valueOf(level-2));
		font.addArgument("FACE","Helvetica, Arial, Geneva") ;

		return ct;
    }          

	// this is the single calling function ... Log and Block by IP address
	public Container logList(Login l, String handle,LookupTable lt){
		Container newcon = new Container();
		newcon.addHTML("<form name=\"blah\" method=\"post\" action=\""+commandURL(IP_LOG)+"\">");
		
		try {
			newcon.addHTML(IPLogList(l,handle,2,lt));
		} catch (ChatException ce) {
			System.out.println(ce);
		}

		newcon.addHTML("<p><input type=\"submit\" value=\"Do it\">") ;
		newcon.addHTML("</form>");
		return newcon ;
	}

	Container IPLogList(Login l, String handle,int depth,LookupTable lt) throws ChatException  {
		int level = 3 - (depth);
		if (depth == 0) return IPLogList0(level, handle,lt);

		Container list = new Container("DL");
	//	if (!areaHidden  || isAdmin(l)) {
			Container c =new Container("DT", IPLogList0(level, handle,lt)) ;
			list.addHTML(c);
			
			for(int i = 0; i < children.size(); i++)
			try {
				ChatObject child = (ChatObject)children.elementAt(i);
				if(!child.bHidden || child.isAdmin(l)) {
					list.addHTML(new Container("DD",child.IPLogList(l, handle,depth - 1,lt)));
				}
			} catch(ArrayIndexOutOfBoundsException ignore) {}

	//	}
		return list;
    } 

    protected Container IPLogList0(int level, String handle,LookupTable lt) 
	throws ChatException {
		Container ct  = new Container();
		ct = ipLog(lt,ct,level) ;
	return ct;
    }          


	public Container ipLog(LookupTable lt,Container newcon,int level) {
		try {
		File blocked = new File(getPath()+FILE_BLOCKED_IP) ;
		// get a hash of the currently blocked IPs ...
		Hashtable current = new Hashtable();		
		if(blocked.exists()) {
			java.io.BufferedReader is = new java.io.BufferedReader(new FileReader(blocked)); 
			String st = null ;
			while ((st = is.readLine()) != null) {
				int hi = st.indexOf(':');
				current.put(st.substring(0,hi),st.substring(hi+1));
			}
			is.close() ;
		}
				
		// get a list of new ips to (un)block from the user ...
		Hashtable unblock = new Hashtable() ;
		Hashtable block = new Hashtable();
		
		String[] names = lt.getNames() ;
		if (names != null) {
			for (int n = 0 ; n < names.length ; n++) {
				String t = names[n] ;
				int startname = t.indexOf(":",4);
				if (startname > -1) {
					String key = t.substring(4,startname);
					// process the block for this chatobject ...
					if (key.equals(sKeyWord)){
						if (t.startsWith("chk")) {
							block.put(lt.getValue(t),t.substring(t.lastIndexOf(':')+1));
						} else {
							if (names[n].startsWith("unp")) {
								unblock.put(lt.getValue(t),t.substring(t.lastIndexOf(':')+1));
							}
						}	
					}
				}
			}
		}

		// now work out the filtered list ...
		Enumeration e = current.keys() ;
		while (e.hasMoreElements()){
			String ip = (String)e.nextElement() ;
			if (!unblock.containsKey(ip)) {
				block.put(ip,current.get(ip));
			}
		}
		
	    Enumeration ec = vChatters.elements() ;
		StringBuffer sb = new StringBuffer() ;
		sb.append("<h"+level+">"+sName+"</h"+level+">");
		if (vChatters.size() > 0) {
		sb.append("<b>WHO'S HERE</b><p>");
		sb.append("<ul>");
		}
		
		while (ec.hasMoreElements()) {
			Chatter ch = (Chatter)ec.nextElement();
			String Handle = ch.toString() ;
			sb.append("<li>");
			sb.append(	"<input type=checkbox name=\"chk:"+sKeyWord+":"+Handle+"\""+
					  	" value=\""+ch.lUser.IP.toString()+"\">");
			sb.append(ch.lUser.IP.toString()) ;
			sb.append(":") ;
			sb.append(Handle) ;
			sb.append("<p>") ;
			sb.append("</li>");
		}
		sb.append("</ul>");

		java.io.BufferedWriter os = new java.io.BufferedWriter(new FileWriter(blocked)); 
		
		if (block.size() > 0) {
		sb.append("<b>WHO'S BLOCKED</b><p>");
		Enumeration eb = block.keys();
		sb.append("<ul>") ;
		while (eb.hasMoreElements()) {
			String ip = (String)eb.nextElement();
			String handle = (String)block.get(ip);
			// write the html ...
			sb.append("<li>");
			sb.append(ip) ;
			sb.append(':');
			sb.append(handle);
			sb.append("<input type=checkbox name=\"unp:"+sKeyWord+":"+handle+"\" value=\""+ip+"\">");
			sb.append("</li>");
			// and then the new blocked file ...
			os.write (ip,0,ip.length());
			os.write (':') ;
			os.write (handle,0,handle.length());
			os.newLine () ;
		}
		sb.append("</ul>");
		os.close() ;
		}
	
		newcon.addHTML(sb.toString()) ;
		} catch (Exception exp) {
			System.out.println(exp);
		}
	    return newcon ;
	}	

   public static ChatObject load(String sDir, ChatObject parent) {
	/*  ritchie: removed the ErrorLogs here are the system pumped them out even when it doesn't need to
	** i.e. when a file validly shouldn't be there 
	*/
	  
	File fDir = new File(sDir);

	if(!fDir.isDirectory())
	    return null;

	ObjectInputStream ois;
	try {

	    ois = new ObjectInputStream(
		  new FileInputStream(
		  new File(sDir, FILE_DATA)));

	} catch (IOException e) {
	    ErrorLog.error(e, 211, "Could not open file in ChatObject load");
	    return null;
	}

	Login lOwner;
	String type;
	LookupTable lt;

	try {
	    type = (String)ois.readObject();
	    lt = (LookupTable)ois.readObject();
	    lOwner = (Login)ois.readObject();
	} catch(IOException e) {
	    ErrorLog.error(e, 212 , "Could not load the class in ChatObject.load");
	    return null;
	} catch(ClassNotFoundException e) {
	    ErrorLog.error(e, 209 , "Could not load the class in ChatObject.load");
	    return null;
	} finally {
	    try {
		ois.close();
	    } catch(IOException ignore) {}
	}

	ChatObject co;
	try {
	    if(parent == null)
		co = new House();
	    else
		co = parent.newChild(lOwner, lt);
	} catch (ChatException e) {
	    ErrorLog.error(e, 210, "File may be corrupted");
	    return null;
	}

	// read in the admin table
	try {
	    ois = new ObjectInputStream(
			new FileInputStream(
		    new File(sDir, FILE_ADMINS)));
	    try {
		co.htAdmins = (Hashtable) ois.readObject();
	    } catch(ClassNotFoundException e) {
		//ErrorLog.error(e, 214 , "Could not load the class in ChatObject.load");
	    }
	} catch (IOException e) {
	    //  ErrorLog.error(e, 211, "Could not open file in ChatObject load");
	} finally {
	    if(co.htAdmins == null)
		co.htAdmins = new Hashtable();
	}

	// read in the booted user table
	try {
	    ois = new ObjectInputStream(
			new FileInputStream(
		    new File(sDir, FILE_BOOTED)));
	    try {
		co.booted = (BootManager) ois.readObject();
		co.booted.setChatObject(co);
	    } catch(ClassNotFoundException e) {
		//ErrorLog.error(e, 214 , "Could not load the class in ChatObject.load");
	    }
	} catch (IOException e) {
	    ErrorLog.error(e, 211, "Could not open file in ChatObject load");
	} finally {
	    if(co.booted == null)
		co.booted = new BootManager(co);
	}

	// read in the children
	// read in the order
	Vector v = null;
	try {

	    ois = new ObjectInputStream(
			new FileInputStream(
		    new File(sDir, FILE_ORDER)));
	    try {
		v = (Vector) ois.readObject();
	    } catch(ClassNotFoundException e) {
		//ErrorLog.error(e, 214 , "Could not load the class in ChatObject.load");
	    }

	} catch (IOException e) {
	    ErrorLog.error(e, 211, "Could not open file in ChatObject load");
	}

	// first try those in the vector
	ChatObject child;
	if (v != null)
	    for (int i = 0; i < v.size(); i++) {
		child = load(sDir + v.elementAt(i) + File.separator, co);
		try {
		    if(child != null)
			co.addChild(child);
		} catch(ChatException e) {
		    ErrorLog.error(e, 213, "Could not add " + child);
		}
	    }

	String[] files = fDir.list();
	for(int i = 0; i < files.length; i++) {
	    if (v != null && v.contains(files[i]))
		continue;
	    child = load(sDir + files[i] + File.separator, co);
	    try {
		if(child != null)
		    co.addChild(child);
	    } catch(ChatException e) {
		ErrorLog.error(e, 213, "Could not add " + child);
	    }
	}
	
	// load the bad language filter for this chat object ...
	try {
	co.blf.load(co) ;
	} catch (IOException ioe) {
	}

	return co;
    }

    void log(Message m) {
	if (logOut == null) {
	    try {
		logOut = new PrintWriter(new FileWriter(getPath() + FILE_TRANSCRIPT, true));
	    } catch (IOException e) {
		ErrorLog.error(e, 500, "Could not log Transcript");
		return;
	    }
	}
	logOut.print((new Date()).toString());
	logOut.print("<BR>");
	logOut.println(m.getHTML(null));
	logOut.flush();
    }

    protected abstract String modifyPage() throws ChatException;  

    public Page modifyPage(Login l) throws ChatException {
	if(!hasPrivilege(l, CAN_EDIT))
	    throw new UnauthorizedException("You do not have edit privileges for "
					    + sName + "." );

	StringBuffer sb = new StringBuffer(1000);
	String s = this.modifyPage();

	s = replace(s, "#sKeyWord#", getKeyWord());
	s = replace(s, "#Command#", String.valueOf(MODIFY));
	s = replace(s, "#ParentName#", parent.sName);
	s = replace(s, "#ParentKey#", parent.getKeyWord());
	//s = replace(s, "#URL_IMG#", URL_IMG);
	s = replace(s, "#GalleryPullDown#",
		    PageFactory.makeDirectoryPulldown(new File(DIR_RESOURCE + "images/backgrounds")).toString());


	// The Tagenizer breaks down Strings into HTML tags.
	Tagenizer tz = new Tagenizer(new String(s));
	Tag t;
	int iSoFar = 0;
	String sName, sOldValue;
	String sValue = "";
	String sSelectValue = "";

	// Loop through HTML tags until there are no more.
	while((t = tz.NextTag()) != null) {
	    // Add the latest segment to the output buffer up to the beginning of the next tag.
	    sb.append(s.substring(iSoFar, t.iBegin));
	    iSoFar = t.iBegin;  // Set the current postion in the input buffer.

	    // HTML input types to check for.
	    boolean bTextarea = false;
	    boolean bSelect = false;
	    boolean bOption = false;

	    //Only look at input tags and set the input type.
	    if(t.sName.equals("INPUT") || (bTextarea = t.sName.equals("TEXTAREA"))
	       || (bSelect = t.sName.equals("SELECT")) || (bOption = t.sName.equals("OPTION"))) {
		sName = null;
		sOldValue = null;
		// Loop through the arguments of the tag (t)
		for(int i=0; i<t.args.length; i++) {
		    //System.out.println(t.args[i]);
		    //System.out.println(t.vals[i]);
		    if(t.args[i].equals("NAME")){
			// Set sName to be the name of the CGI variable
			sName = t.vals[i];
			continue;
		    }
		    if(t.args[i].equals("VALUE")) {
			// Set sOldValue to be the current (default) value of the variable, removing the
			// quotes.
			sOldValue = new String(t.vals[i]);
			sOldValue = sOldValue.replace('"', ' ').trim();
		    }
		}

		boolean bBool = false;
		sValue = "";

		if(bOption) {
		    // If the value of this option tag matches the value of the Room variale
		    // we are looking for (sSelectValue), set sValue to " SELECTED ", so this
		    // option will be the default.
		    // System.out.println(sName + " " + sOldValue + " " + sSelectValue);
		    if((sOldValue != null) && sOldValue.equals(sSelectValue)) {
			//System.out.println("match");
			sValue = " SELECTED ";
		    }
		} else {
		    if(sName == null)
			// Skip variables without names.
			continue;
		    //	  System.out.println(sName + " :INPUT");

		    if(sName.equals("sBgType")) {
			// Special handling of background types !!!
			// System.out.println("bgtype: " + sOldValue + " " + sBgType);
			bBool = true;
			if((sOldValue.equals("URL") && !sBgType.equals("Color")) ||
			   (sOldValue.equals("Color") && sBgType.equals("Color")))
			    sValue = " CHECKED ";
			//continue;//hack because no handling of radio buttons.
		    } else {
			// Otherwise just set sValue based on the value of the Room object
			try {
			    Class c = this.getClass();
			    //Debug.println(c.toString());
			    Field fd = c.getField(sName);
			    //Debug.println(fd.toString());
			    String sType = fd.getType().getName();

			    if(bBool = sType.equals("boolean")) {
				// Either checked or nothing.
				sValue = fd.getBoolean(this) ? " CHECKED " : "";
			    } else {
				// Strings, Integers, etc ... are stored as strings.
				sValue = (fd.get(this) != null) ? String.valueOf(fd.get(this)) : "";
			    }
			} catch(NoSuchFieldException e) {
			    Debug.println("Error number 8 : " + e + " : " + sName);
			    continue;
			} catch(IllegalAccessException e) {
			    ErrorLog.error(e, 9, "IllegalAccessException");
			    return null;
			}
		    }
		}
		if(bTextarea) {
		    // Text areas have no value argument.  The value is stored btween the
		    // <TEXTAREA> and </TEXTAREA> tags
		    sb.append(s.substring(iSoFar, iSoFar = t.iEnd + 1));
		    sb.append(sValue);
		    continue;
		}

		if(bSelect) {
		    // for select tags, merely record the value wanted.
		    sSelectValue = sValue;
		    continue;
		}
		
		// Set sAdd to the current text of the tag, removing value-setting arguments
		// as needed.
		String sAdd = s.substring(iSoFar, t.iEnd);
		sAdd = replace(sAdd, (bOption ? " SELECTED" : (bBool ? "CHECKED" : "VALUE")), "");
		/*	int iEnd = s.indexOf("CHECKED", iSoFar);
			if((iEnd < 0) || (iEnd > t.iEnd))
			iEnd = t.iEnd;
			int iValue = s.indexOf("VALUE", iSoFar);
			if((iValue > 0) && (iValue < iEnd))
			iEnd = iValue;
			sb.append(s.substring(iSoFar, iEnd));*/

		sb.append(sAdd);  // Append the beginning of the tag to the output buffer.
		// Append the value from the Room object.
		sb.append((bOption || bBool) ? sValue : " VALUE=\"" + sValue + "\" ");
		// Set the position to be exactly on the '>', so it gets output in the next iteration.
		iSoFar = t.iEnd;
	    }
	}
	sb.append(s.substring(iSoFar)); // Append the remainder of the string
	return new Page(sb.toString());
    }  

    protected abstract ChatObject newChild(Login lOwner, LookupTable lt)
	throws ChatException;

    public long numChatters() {
	return activeCount;
    }  

    protected Object open(String filename) {
	return open(filename, null);
    }  

    protected Object open(String filename, Object deflt) {
	Object oReturn = null;

	try {
	    ObjectInputStream ois = new ObjectInputStream(
				  new FileInputStream(
				    new File(getPath(), filename)));
	    try {
		oReturn = ois.readObject();
	    } catch(ClassNotFoundException e) {
		ErrorLog.error(e, 214 , "Could not load the class in ChatObject.open");
	    }
	} catch (IOException e) {
	    ErrorLog.error(e, 211, "Could not open file in ChatObject open");
	} finally {
	    if (oReturn == null)
		oReturn = deflt;
	}

	return oReturn;
    }  

    protected Page popPage(int iCommand, LookupTable lt) {
	Page p = new Page();
	p.addArgument("ONLOAD", "document.Pop.submit()");

	Form f = form("Pop", true, iCommand);
	p.addHTML(f);
	f.addArgument("TARGET", CHAT_FRAME);
	String[] names = lt.getNames();
	String[][] values = lt.getFullValues();

	for(int i = 0; i < names.length; i++)
	    for(int j = 0; j < values[i].length; j++)
			f.addHTML(new Input(Input.HIDDEN, names[i], values[i][j]));

		return p;
    }  

    static String printCheckBox(String sVarName, String sLabel, boolean bValue) {
	return "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + sVarName + "\"" +
	    (bValue ? " CHECKED" : "") + ">\n <INPUT TYPE=HIDDEN NAME=\"" + SET_ONE_VAR +
	    sVarName + "\">" + sLabel + "\n";
    }

    public Page privilegesPage(Login lUser) throws UnauthorizedException {
	if(!isOwner(lUser))
	    throw new UnauthorizedException("Only owners can designate admins");

	Form f = new Form("AdminForm", URL_PREFIX + getKeyWord(), "Post");

	f.addHTML("<h1><FONT FACE=\"Arial,Helvetica,Geneva\">" + this.sName + " Admin Manager</font></h1>");

	//Add Hidden Variables
	f.addHTML(new Input(Input.HIDDEN, COM_VAR, String.valueOf(UPDATE_ADMINS)));

	// make the table and add it to the form
	Container table = new Container("TABLE");
	table.addArgument("BORDER", "0");
	table.addArgument("WIDTH", "650");
	f.addHTML(table);

	// add the table header
	Container th = new Container("TR");
	table.addHTML(th);
	th.addHTML("<TD WIDTH=40 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>Delete</B></font></TD>");
	th.addHTML("<TD WIDTH=100 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>Name</B></font></TD>");
	th.addHTML("<TD WIDTH=190 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>Start</B></font></TD>");
	th.addHTML("<TD WIDTH=200 BGCOLOR=\"#A4C6F4\">"
		   + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>End</B></font></TD>");

	// add privileges to the table header
	for(int i = 0; i < sPrivilegeNames.length ; i++)
	    th.addHTML("<TD width=40 BGCOLOR=\"#A4C6F4\">"
		       + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2><B>" + sPrivilegeNames[i]
		       + "</B></FONT></TD>");
	// Make the rows of admins
	Enumeration eAdmins = htAdmins.elements();

	Admin a;
	Container tr;
	while(eAdmins.hasMoreElements()) {
	    a = (Admin)eAdmins.nextElement();
	    tr = new Container("TR");
	    table.addHTML(tr);
	    tr.addHTML(new Input(Input.HIDDEN, sAdminNameVar, a.getLogin()));
	    tr.addHTML(new Container("TD", new Input(Input.CHECKBOX, a.getLogin() + "_DELETE", "")));
	    tr.addHTML("<TD><FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=1>"
		       + a.getLogin() + "</FONT></TD>");
	    tr.addHTML(new Container("TD", new HTMLDate(a.getLogin() + "_start", a.starts())));
	    tr.addHTML(new Container("TD", new HTMLDate(a.getLogin() + "_end", a.expires())));
	    for(int i = 0; i < sPrivilegeNames.length; i++) {
		tr.addHTML(new Container("TD",
					    new CheckBox(a.getLogin() + "_" + String.valueOf(i), a.privilegeSet(i))
						));
	    }
	}
	f.addHTML(new Input(Input.SUBMIT, "Submit Button", "Update Admins"));
	f.addHTML(new Input(Input.RESET, "Undo Changes", "Undo Changes"));

	Page page = PageFactory.makeUtilPage(new Container("Center", f));
	page.addHeadHTML(new Title(sName + " Admin Privilege Page"));

	return page;
    }    

    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {
	in.defaultReadObject();
	activeCount = 0;
	try {
	    registry.add(sKeyWord, this);
	} catch (ChatException e) {
	    throw new IOException(e.toString());
	}
    }  

    protected void remove(ChatObject child) {
	// Think if we need to synchronize children or not !!!
	synchronized (children) {
	    children.removeElement(child);
	}
    }  

    public void removeChatter(Chatter c)
	throws ChatException {
	if(parent != null)
	    parent.removeChatter(c);
	activeCount--;
    }  

    /**
     * Replace all occurences of <i>what</i> with <i>that</i> in <i>str<i>.
     *
     * @param str the String to be modified
     * @param what the substring to be replaced
     * @param that the substring to replace with
     * @return the modified string.
     */
    public static final String replace(String str, final String what, final String that) {
	int j;
	while( (j=str.indexOf(what)) != -1 )
	    str = str.substring(0,j)+that+str.substring(j+what.length());

	return str;
    }  

    protected void save(Object o, String filename)
	throws ChatException {
	try {
	    File f = new File(getPath(), filename) ;
	    if (o != null) {
		ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
		oos.writeObject(o);
	    } else {
		f.delete() ;
	    }
	} catch (IOException e) {
	    throw new ChatException(
				    "The following exception occured while trying to save your file:\n<br>"
				    + e.toString() + "\n<br>"
				    + "Your file may not have been saved.");
	} catch (SecurityException se)
	    {} 
    }  

    void saveImageList() {
	try {
	    save(vImages, FILE_POST_IMAGES);
	} catch (ChatException ignored) { }
    }  

    private void saveOrder()
	throws ChatException {
	synchronized (children) {
	    if (children != null && children.size() > 0) {
		Vector v = new Vector(children.size());
		for (int i = 0; i < children.size(); i++)
		    v.addElement(((ChatObject) children.elementAt(i)).sKeyWord);
		save(v, FILE_ORDER);
	    }
	}
    }  

    void saveSoundList() {
	try {
	    save(vSounds, FILE_POST_SOUNDS);
	} catch (ChatException ignored) { }
    }  

    protected static final synchronized void setFields(Object o, final LookupTable lt) {
	if(lt == null) return; // can't update from a null lookupTable
	Class c = o.getClass();
	Field[] fArray=c.getFields();     // Make an array of the fields.
	// Loop through variables
	for (int i = 0; i < fArray.length; i++) {
	    int iModifiers = fArray[i].getModifiers();
	    String fieldName = fArray[i].getName(); // The name of the field.
	    String sValue = lt.getValue(fieldName); // The value of the CGI variable of that name.

	    //System.out.println(fieldName + ": " + sValue);
	    try {
		Class typeClass = fArray[i].getType();
		String fieldType = typeClass.getName(); // The type of the variable in Room
		if(fieldType.equals("boolean") &&
		   (lt.getValue(SET_ONE_VAR + fieldName) != null || lt.getValue(SET_VAR) != null))
		    {
		    fArray[i].setBoolean(o, false); // if nothing is sent to a boolean it is false
		    }

		if(sValue != null) {
		    int iValue;
		    if(fieldType.equals("int")) {
			// For integers convert string to integer
			iValue = Integer.parseInt(sValue);
			fArray[i].setInt(o, iValue);
		    } else if(fieldType.equals("boolean")) {
			// For booleans, existence indicates 
			fArray[i].setBoolean(o, true);
		    } else if(!sValue.equals("")
			      || lt.getValue(SET_ONE_VAR + fieldName) != null
			      || lt.getValue(SET_VAR) != null) {
			//  Otherwise it is a string; replace double quotes to
			//  avoid later HTML problems ???
			//	fArray[i].set(o, sValue.replace('"', '\''));
			fArray[i].set(o, sValue);
		    }
		}
	    } catch(IllegalAccessException e) {
				// Problem with permissions.
		ErrorLog.error(e, 10, fieldName + " access: " + sValue);
	    } catch(IllegalArgumentException e) {
				// If a certain field is not found.
		ErrorLog.error(e, 11, fieldName + " arg: " + sValue);
	    }
	}
    }

    protected void update(LookupTable lt)
	throws ChatException {
	String oldKey = sKeyWord;
	File oldPath = new File(getPath());
	setFields(this, lt);

	String newKey = lt.getValue("sKeyWord");
	if(newKey != null && !"".equals(newKey)) {
	    newKey = newKey.trim().toLowerCase();
	    if(!newKey.equals(oldKey)) {
		registry.remove(oldKey);
		registry.add(newKey, this);
		sKeyWord = newKey;
		if(oldPath.exists())
		    oldPath.renameTo(new File(getPath()));
	    }
	} else if(oldKey == null) { // there is no key
	    throw new ChatException("You must specify a keyword.");
	}


	Body b = new Body();
	Container head = null;
	Page pageTemplateTemp = new Page(head, b);

	pageTemplateTemp.addHeadHTML(new Title(sName));

	/** Modified by ritchie
	 ** take into account the bad word filter setting
	 */

	String bwf = (String)lt.getValue("iWordSet") ;
	if (bwf != null) {
	    iWordSet = Integer.parseInt(bwf) ;
	}

	

	if(sBgType != null)
	    if(sBgType.equals("Color"))
		sBgURL = null;
	    else
		sBgURL = lt.getValue("sBg" + sBgType);  // Read the value of backgound

	if(sBgURL != null)
	    b.addArgument("BACKGROUND", sBgURL);

	if(sBg != null)
	    b.addArgument("BGCOLOR", sBg);

	if(sTextColor != null)
	    b.addArgument("TEXT", sTextColor);

	if(sLinkColor != null)
	    b.addArgument("LINK", sLinkColor);

	if(sVLinkColor != null)
	    b.addArgument("VLINK", sVLinkColor);


	if (bHtmlDis) {
	    for(int i = 0; i < children.size(); i++) {
		((ChatObject)children.elementAt(i)).bHtmlDis = true ;
	    }
	}
	       
	for(int i = 0; i < children.size(); i++) {
	    ((ChatObject)children.elementAt(i)).bPMDis = bPMDis ;
	}

	pageTemplate = pageTemplateTemp;


	try {
	    File f = new File(getPath());
	    if(!f.exists())
		f.mkdirs();
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getPath() + FILE_DATA));
	    oos.writeObject(this.getClass().getName());
	    oos.writeObject(lt);
	    oos.writeObject(lOwner);
	    oos.close();
	} catch (IOException e) {
	    throw new ChatException(
				    "The following exception occured while trying to save your file:\n<br>"
				    + e.toString() + "\n<br>"
				    + "Your file may not have been saved.");
	}
	saveOrder();
    }        

    public Page updateAdmins(Login l, LookupTable lt) throws UnauthorizedException, ChatException {
	if(!isOwner(l)) throw new UnauthorizedException ("Only owners can update Admins");
	if(lt == null) throw new ChatException ("Missing Form Data in Update Admins");

	String[] adminNames = lt.getFullValue(sAdminNameVar);

	for(int i = 0 ; i < adminNames.length ; i++) {
	    Admin a = (Admin)htAdmins.get(adminNames[i]);
	    if(a == null) continue;
	    if(lt.getValue(a.getLogin() + "_DELETE") != null) {
		htAdmins.remove(adminNames[i]);
		continue;
	    }
	    synchronized(a) {
		try {
		    // get start and end time
		    Calendar start = HTMLDate.getCalendar(a.getLogin() + "_start", lt);
		    Calendar stop  = HTMLDate.getCalendar(a.getLogin() + "_end", lt);
		    a.starts(start);
		    a.expires(stop);

		    // get privileges
		    for(int j = 0; j < sPrivilegeNames.length; j++)
			a.setPrivilege(j, lt.getValue(a.getLogin() + "_" + String.valueOf(j)) != null);
		} catch(NumberFormatException e) {
		    ErrorLog.error(e, 203, "Could not parse date in Update Admins");
		    //  What shoud we do here !!!
		}
	    }
	}
	save(htAdmins, FILE_ADMINS);
	return privilegesPage(l);
    }  

    public Page viewLog()
	throws ChatException {
	return new Page(fileToString(getPath() + FILE_TRANSCRIPT));
    }  


    public String privateRoomPage=
	"<CENTER>" +
	"<b><font face=\"Arial,Helvetica,Geneva\" size=\"2\">"+
	"To create a private room, enter an Admin User Name and Admin Password."+
	"</font></b><br>"+
	"<TABLE WIDTH=\"620\" BORDER=\"1\" CELLSPACING=\"1\" CELLPADDING=\"5\">"+
	"<FORM onSubmit=\"return checkPass(document.forms.Password.p2)\" METHOD=\"GET\""+
	"NAME=\"Password\">"+
	"<input type=hidden name=Action value=311>"+
	"<tr>"+
	"<td align=\"RIGHT\" valign=\"TOP\"><FONT FACE=\"Arial,Helvetica,Geneva\""+
	"size=\"-2\">(The fields require 4 or more"+
	" characters)</font><br>"+
	"<font face=\"Arial, Helvetica, Geneva\" size=\"1\" color=\"FF0000\">Admin </font>"+
	"<font face=\"Arial, Helvetica, Geneva\" size=\"1\">User Name: </font>"+
	"<input type=\"text\" name=\"user\" size=\"15\"></td>"+
	"<td align=\"RIGHT\" valign=\"TOP\"><FONT FACE=\"Arial,Helvetica,Geneva\""+
	"size=\"-2\">(Room Name and Key Name required)</font><br><FONT"+
	" FACE=\"Arial,Helvetica,Geneva\" size=\"1\">Name your chat room: </font><input"+
	" type=text name=sName size=\"15\"></td>"+
	"</tr>"+
	"<tr>"+
	"<td align=\"RIGHT\" valign=\"TOP\">"+
	"<font face=\"Arial, Helvetica, Geneva\" size=\"1\" color=\"FF0000\">Admin </font>"+
	"<font face=\"Arial, Helvetica, Geneva\" size=\"1\">Password: </font>"+
	"<input type=\"password\" name=\"p1\" size=\"15\"></td>"+
	"<td align=\"RIGHT\" valign=\"TOP\"><FONT FACE=\"Arial,Helvetica,Geneva\""+
	" size=\"1\">Key Name (one word): </font><input type=text name=sName"+
	" size=\"15\"></td>"+
	"</tr>"+
	"<tr>"+
	"<td align=\"RIGHT\" valign=\"TOP\"><FONT FACE=\"Arial,Helvetica,Geneva\""+
	"size=\"1\">Confirm Password: </FONT><input type=\"password\" name=\"p2\""+
	"size=\"15\" onBlur=\"checkPass(this)\"></td>"+
	"<td align=\"RIGHT\" valign=\"TOP\"><input type=\"Image\" src=\""+commandButton(SUBMIT)+"\""+
	" border=\"0\"></td>"+
	"</tr>"+
	"</TABLE></FORM>"+
	"<font face=\"Arial\" size=\"2\" color=\"FF0000\"><b>IMPORTANT:</b></font> <font "+
	"face=\"Arial\" size=\"1\">Enter the same"+
	" Admin User Name and Admin Password anytime you Login."+
	"<br>"+
	"After your private room is created, you may Edit the room if you are logged"+
	" in with the Admin User Name and Admin Password.</font>" +
	"</CENTER>" ;

}
