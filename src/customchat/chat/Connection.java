package customchat.chat;

import java.net.*;
import java.io.*;
import java.util.*;
import customchat.util.*;


//=============================================================================
// This class is the thread that handles all communication with a client
// It also notifies the Vulture when the connection is dropped.

class Connection implements Runnable {

    // Class variables/values
    static int connection_number = 0;
    //private   final ThreadGroup tg;
    private static final Runtime rt = Runtime.getRuntime();

    // Socket stuff
    protected Server server;
    protected Socket client;
    protected BufferedReader in;//changed to buffered.. from datainputst
    protected PrintWriter out;//changed to writer
    protected OutputStream os;
    private boolean resetTimeOut = false ;
    private   String sHTTPVersion;      // The version of HTTP from Request
    private   String sIP = null;
    private   Login lUser = null;
    private   String sVariables = null; //cgi variables  query string
    private	  String sKey = null;
    private   boolean bMisery = false;  // Using MSIE
    private   boolean bReg = false; // True if connected to a Database
	public static boolean anotherIEHack = false;
    private static Hashtable htReset    = new Hashtable();

    private class CGIParseException extends ChatException {
	CGIParseException(String s) {
	    super(s);
	}
	CGIParseException(){
	    super();
	}
    };

    private class NotLoggedInException extends ChatException {
	NotLoggedInException(String s) { super(s);}
	NotLoggedInException() { super();}
    };

    // Initialize the streams and start the thread
    public Connection(Socket client_socket, ThreadGroup threadgroup,
		      int priority, Server s)
    {
	// Give the thread a group, a name, and a priority.
	//super(threadgroup, "Connection " + connection_number++);
	//tg = threadgroup;
	// Now that we are initialised, we can give ourselves a meaningful name
	//super.setName( "Connection number ("+ connection_number + ") " +
	//client_socket.getInetAddress().getHostName() +
	//		 " port:" + client_socket.getPort() );

	//this.setPriority(priority);

	// Save our other arguments away
	client = client_socket;
	this.server = s;

	// Create the streams
	try {
	    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    os = client.getOutputStream();
	    out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(os)));
	} catch (IOException e) {
	    try {
		client.close();
	    } catch (IOException e2) { ; }
	    ErrorLog.error(e, 17, "Connection");
	}
    }
    void MessagePage(String s) {
	MessagePage(s, true);
    }  
    void MessagePage(String s, boolean bPrintHeader) {

	if(bPrintHeader)
	    out.println(Server.sHTTPOK);
	try {
	    if (s.startsWith("You have been removed from the conversation.")) { // hack
		out.println(ChatObject.fileToString(ChatObject.htmlFile("messageheaderframebust.html")));
	    }
	    else {
		out.println(ChatObject.fileToString(ChatObject.htmlFile("messageheader.html")));
	    }
	} catch(ChatException e){
	    ErrorLog.error(e, 20, "On message " + s);
	}


	out.println(s);

	try {
	    out.println(ChatObject.fileToString(ChatObject.htmlFile("messagefooter.html")));
	} catch(ChatException e){
	    ErrorLog.error(e, 20, "On message " + s);
	}
	out.close();
    }                                                
    /* public void Log(InetAddress ia, String sObject, int iCode) {
       Date d = new Date();
       String s = d.toString();
       StringTokenizer st = new StringTokenizer(s);
       String sMonth, sDay, sTime, sYear;
       try {
       Object dummy = st.nextElement();
       sMonth = (String)st.nextElement();
       sDay = (String)st.nextElement();
       sTime = (String)st.nextElement();
       dummy = st.nextElement();
       sYear = (String)st.nextElement();
       } catch (NoSuchElementException e) {
       return;
       }
       synchronized(h.sbLog) {
       try {
       h.sbLog.append(ia.getHostName() + " - - [" + sDay + "/" + sMonth +
       "/" + sYear + ":" + sTime + " -0500] \"GET " +
       URLEncoder.encode(sObject) +
       " HTTP/1.1\" " + new Integer(iCode) + " -\n");
       } catch (NullPointerException e) {
       //	System.out.println(e);
       }
       }
       }
    */

    private void challenge() {
	out.println(Server.sHTTP + "401 Unauthorized");
	out.println("WWW-Authenticate: Basic realm=\"chat\"\n");
    }

    private LookupTable parseRequest()
	throws CGIParseException, IOException, NotLoggedInException {

	String line;  //Buffer for reading HTTP requests
	String sToken = "";  // Stores each token of the request line
	StringTokenizer st;   // Breaks each line of the request into tokens.
	LookupTable lt = null;
	// read in a line
	//if(!in.ready()) throw new CGIParseException("Input stream not ready.");
	line = in.readLine();

	if(line == null || line.equals("")) throw new CGIParseException("Empty line.");

	//parse browser requests directly
	boolean bGet=false;
	if((bGet = line.startsWith("GET ")) || line.startsWith("POST")) {

	    //parse the first line
	    st = new StringTokenizer(line);
	    st.nextToken(); //method
	    sToken = st.nextToken();
	    sHTTPVersion = st.nextToken();
		int start ;
	    int iLength=-1;
	    //look for the authorization line
		anotherIEHack = false ;
	    String sPrimary = null, sPassword = null, sAuth = null;
	    while( (line != null) && !(line.equals(""))) {
		//System.out.println(line + "#");
		//check for length > 21
		if(line.startsWith("Authorization:") && (line.length() > 21)) {
		    sAuth = line.substring(21); //just the code
		    sAuth = sAuth.trim();
		    //Parse the authorization info
		    //reminder--do not allow :'s in the passwords
		    sAuth = customchat.util.Base64.decode(sAuth);
		    int iLastColon = sAuth.lastIndexOf(':');
		    if(iLastColon < 0)
			continue;
		    sPrimary = sAuth.substring(0, iLastColon);//primary handle-used for authentication
		    sPassword = sAuth.substring(iLastColon+1);
		    if(sPrimary.length() < 4) {
			throw new NotLoggedInException("Please enter a Username with more than three characters.");
		    }
		    if(sPassword.length() < 4) {
			throw new NotLoggedInException("Please enter a Password with more than three characters.");
		    }
		    lUser = new Login(sPrimary, sPassword);
			lUser.IP = client.getInetAddress().getHostAddress().toString() ;
		} else if (line.toUpperCase().startsWith("CONTENT-LENGTH:")) {
		    iLength = Integer.parseInt(line.substring(16));
		} else if((start = line.indexOf("MSIE")) >= 0) {
			 if (line.indexOf("WebTV") >= 0) {
			 	bMisery = true ;
			 } else {
				 //System.out.println(line);
				 String y = line.substring(start,start+10) ;
				 //System.out.println(y) ;
				 if(y.indexOf("4") >= 0) { 
					bMisery = true;
					//System.out.println("I'm an IE 4.0") ;
				 }
				 else {
					//System.out.println("I'm not an IE 4.0") ;
					bMisery = false ;
					anotherIEHack = true ;
				 }
			 }
		}
		line = in.readLine();
		// System.out.println(line);
	    }

	    //the GET method stores variables in the URL

	    int qmark;
	    if((qmark = sToken.indexOf("?")) >= 0) {
		sVariables = sToken.substring(qmark+1);
		sToken = sToken.substring(0, qmark);
	    }
	    if(!bGet) {
		//the POST method stores it at the end
		char []c = new char[iLength];
		sVariables += "&";
		int iCount = 0, iGot;
		while((iGot = in.read(c, 0, iLength)) > 0) {
		    sVariables += new String(c, 0, iGot);
		    if((iCount += iGot) >= iLength)
			break;
		}
	    }
	    if(sToken != null && sToken.length() > 0) {

		try {
		    String sUrl = customchat.util.URLDecoder.decode(sToken).trim();
		    int offset = 1;
		    if(sUrl.startsWith("/"))
			offset = 1;
		    else if(sUrl.startsWith("http://"))
			offset = sUrl.indexOf('/', "http://".length()) + 1;
		    else if(sUrl.startsWith("file:///"))
			offset = sUrl.indexOf('/', "file:///".length()) + 1;
		    else
			offset = 0;
		    //sKey = customchat.util.URLDecoder.decode(sToken.substring(1)).trim().toLowerCase();
		    sKey = sUrl.substring(offset);
		} catch (Exception e) { throw new CGIParseException(); }

	    } else
		sKey = "";

	    //Parse the CGI variables
		//System.out.println("QUERY STRING:"+sVariables) ;
	    QueryStringParser qsp = new QueryStringParser(sVariables +
							  (bMisery ? "&" + Chatter.USING_IE_VAR : ""));
		lt = qsp.parse() ;
		//System.out.println("LT :  "+lt) ;
	    return lt;
	}
	throw new CGIParseException();
    }
    /**
     * Resets the login information for the user.
     */
    private void resetUser()  throws ChatException {
	if(!htReset.containsKey(client.getInetAddress())) {
	    htReset.put(client.getInetAddress(), new Integer(1));
	    throw new NotLoggedInException("<p><center>please use the back button, and then RELOAD or REFRESH your browser -- </center></p>");
	}

	htReset.remove(client.getInetAddress());
	throw new RedirectException(ChatObject.URL_PREFIX + sKey);
    }
		
		
	// ----------------------------------------------------------------------------------
	
	public void run() {
	
	LookupTable lt = null;
	long parseTime = -1L;
	Object key = customchat.util.Timer.start(null);
	try {
	
	    // Reads CGI Variables into the lookup table.
	    customchat.util.Timer.start();
	    lt = parseRequest();
	    parseTime = customchat.util.Timer.stop();

	    // If load testing allow authentication using cgi var LoadUser
	    if (System.getProperty("chat.loadtest") != null) {
		String sUser;
		if ((sUser = lt.getValue("LoadUser")) != null)
		    lUser = new Login(sUser, "Test");
	    }

		if (lt.getValue("Reset") != null)
				resetUser();

	    // handle "normal" http requests

	    if (sKey.startsWith(ChatObject.URL_RESOURCE.substring(1))) {
			String sFile = ChatObject.DIR_RESOURCE + sKey.substring(ChatObject.URL_RESOURCE.length() - 1);
			if (sFile.contains(".."))
				throw new ChatException(".. is not allowed in URL's.");
			File f = new File(sFile);
			if (!f.exists() || !f.isFile() || !f.canRead())
				throw new ChatException("Error reading file: " + sFile + ".");

			os.write(("HTTP/1.0 200 Okay\r\nContent-type: " + customchat.htmlutil.HTTP.guessMimeType(sFile) + "\r\n" + "Content-length: " + String.valueOf(f.length()) + "\r\n\n").getBytes("latin1"));
			BufferedInputStream fs = new BufferedInputStream(new FileInputStream(f));
			byte ba[] = new byte[256];
			int iread = 0;
			while ((iread = fs.read(ba)) >= 0) {
                            os.write(ba, 0, iread);
                        }
                        fs.close();
				
			
	    } else {
			ChatObject co = ChatObject.registry.get(sKey.toLowerCase()) ;
			co.doCommand(lUser, lt, out, true);
	}

	} catch (IOException e) {
	    MessagePage(e.getMessage());
	} catch (UnauthorizedException e) {
	    challenge();
	    MessagePage(e.getMessage(), false);
		
	} catch (AutoScrollException e) {
	    Chatter c = e.getChatter();
	    c.setScroll(new AutoScroll(c.location().getTemplate(), out, bMisery, true));
		
	} catch (ShutDownException e) {
	    ServerSocket ss = server.listen_socket;

	    if (ss != null) {
				try {
					ss.close();
				} catch (IOException ioe) {
					ErrorLog.error(ioe, -1, "Could not close server socket");
				}
		}
	
		MessagePage("The Server has shut down.");
	    
		try {
				client.close();
	    } catch (IOException e2) {
			
	    }
		
	    System.runFinalization();
	    System.exit(e.restart() ? 1 : 0);
		
	} catch (NotLoggedInException e) {
	    challenge();
	    MessagePage(e.getMessage(), false);
		
	} catch (CGIParseException e) {
	    MessagePage("Invalid HTTP Request : " + e);
	} catch (RedirectException e) {
	    out.print("HTTP/1.0 302 Found\r\n");
	    out.print(e.toString());
	    Debug.println(e.toString());
	    out.print("\r\n\r\n");
	    out.close();
	} catch (ChatException e) {
	    MessagePage(e.getMessage());
	} catch (OutOfMemoryError e) {
	    MessagePage("The system is experiencing heavy volume; try again later.<p>" + e);
	    System.gc();
	    throw e;
	}
	// When we're done, for whatever reason, be sure to close
	// the socket, and to notify the Vulture object.  Note that
	// we have to use synchronized first to lock the vulture
	// object before we can call notify() for it.
	finally {
            out.close();
	    server.wakeup();
	}
    }
    //---------------------------------------------------------------------------
    // This method returns the string representation of the Connection.
    // This is string is unused, but handy for debugging purposes
    public String toString() {
	return /*this.getName() +*/ " connected to: "
	    + client.getInetAddress().getHostName()
	    + ":" + client.getPort() + " username: ";
    }
}  // end of Connection
