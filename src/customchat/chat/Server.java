package customchat.chat;

import customchat.util.*;
import customchat.licensekey.*;

import java.net.*;	// Needed so we can access sockets, etc
import java.io.*;	 // So we can use streams
import java.util.*;     // Needed for the tokenized


//============================================================================
// This is the main part of the CustomChat Server.  The main method starts the server
// running in an endless loop, waiting for connections.  The method
// setToAll( Connection, String ) sends the String it has received from
// the Connection to _all_ current connections
//
public class Server extends Thread {
    private static final Date dStarted = new Date();

    // Class Variables/values.
    public final static int DEFAULT_PORT = 6743;
    protected static int port;
    protected ServerSocket listen_socket;
    protected ThreadGroup threadgroup;
    protected Vector connections;
    private int iMaxThreads;
	private static final int soTimeout =  1000 * 60 * 10;
    //public Integer NumConnections = 0;
    //protected Vulture vulture;

    public House h;

    protected static final String sHTTP = "HTTP/1.1 ";
    public static final String sHTTPOK = sHTTP + "200 OK\nContent-type: text/html\n\n";

    private static URL addr ;



    //--------------------------------------------------------------------------
    // The constructor for this class, the server created listens endlessly on
    // the port it is assigned.  When a connection to that port is made the
    // server starts a new connection thread to communicate with it.
    //
    public Server(int iPort, int iMaxThreads) {
	// Create our server thread with a name.
	super("Server");
	if (System.getProperty("chat.loadtest") != null) {
	    try {
		h = new House();
	    } catch (Exception e) {
		fail(e, "Something bad happened during load test startup.");
	    }
	} else {

	    h = (House) ChatObject.load(ChatObject.DIR_DATA, null);
	    h.sKeyWord = "/" ;

	    if (h != null) {
		System.out.println("House loaded from : " + ChatObject.DIR_DATA);
	    }
	    else {
		try {
		    h = new House();
		} catch (Exception e2) {
		    fail(e2, "Could not create new House");
		}
	    }
	}

	this.port = iPort;
	this.iMaxThreads = iMaxThreads;
	System.out.println("Running on port :\t" + iPort);
	System.out.println("Running with maxThreads :\t" + String.valueOf(iMaxThreads));

	try {
	    listen_socket = new ServerSocket(port, 100);
	} catch (IOException e) {
	    fail(e, "Exception creating server socket (" + port + ")");
	}

	// Create a threadgroup for our connections
	threadgroup = new ThreadGroup("Server Connections");
	setPriority(NORM_PRIORITY - 2);
	this.start();
    }
    //--------------------------------------------------------------------------
    // Exit with an error message, when an exception occurs.
    //
    public static void fail(Throwable e, String msg) {
	ErrorLog.error(e, 14, msg);
	System.exit(-1);
    }  
   
   public static URL getAddr() {
	return addr;
    }
	
    public static  int getPort() {
	return port;
    }
   
   //--------------------------------------------------------------------------
    // Start the server up, listening on an optionally specified port
    //
    public static void main(String[] args) {
	int iPort = DEFAULT_PORT, iMaxThreads = 40;
	String sPropFile = null;
	if (args.length > 0) {
	    try {
		iPort = Integer.parseInt(args[0]);
	    } catch (NumberFormatException e) {
		iPort = DEFAULT_PORT;
	    }
	}
	if (args.length > 1) {
	    sPropFile = args[1];
	} else
	    sPropFile = "." + File.separator + "chat.properties";

	// read in properties file
	Properties p = new Properties(System.getProperties());
	
	try {
	    p.load(new FileInputStream(sPropFile));
	    System.setProperties(p);
	    // set loggin on/off
	    String l = p.getProperty("chat.log") ;
	    if (l != null) { // otherwise the default is log off as initialised as a static in ErrorLog
		boolean log = Boolean.valueOf(l).booleanValue() ;
		ErrorLog.LOG= log ;
	    }
	} catch (IOException e) {
	    System.out.println("Error reading properties file");
	}
	License l = null;
	try {
	    l = License.getInstance(new File("ccLicense"), iPort);
	    ChatObject.maxChatters = l.maxChatters();
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    fail(e, "Could not start server");
	}
	addr = l.getAddr() ;
	System.out.println("\n\nCustomChat Chat Server version 1.5 by CustomChat Inc.");
	System.out.println("Written by CustomChat Inc.");
	System.out.println("Graphics and Design by Angelo Aversa");
	System.out.println("Copyright 1998, 1999, 2000");
	System.out.println("All Rights Reserved.");
	System.out.println();
	System.out.println("This license will expire on : " + l.date());
	System.out.println("To obtain a licensed copy of the program see our web site at:");
	System.out.println("http://www.customchat.com");
	String logFile = System.getProperty("chat.errorlog");

	if (ErrorLog.LOG) {
	    if (logFile == null)
		logFile = "." + File.separator + "logs" + File.separator + "err.log";
	    try {
		ErrorLog.setOutput(logFile);
		System.out.println("Logging Errors to : " + logFile);
	    } catch (FileNotFoundException e) {
		ErrorLog.error(e, 17, "Could not open error log.");
	    }
	}
	
	new Server(iPort, iMaxThreads);
    }
    public static int numConnections() {
	return Connection.connection_number;
    }
    //--------------------------------------------------------------------------
    // The body of the server thread.  Loop forever, listening for and
    // accepting connections from clients.  For each connection,
    // create a Connection object to handle communication through the
    // new Socket.  When we create a new connection, add it to the
    // Vector of connections, and display it in the List.  Note that we
    // use synchronized to lock the Vector of connections.  The Vulture
    // class does the same, so the vulture won't be removing dead
    // connections while we're adding fresh ones.

    public void run() {
	Runtime rt = Runtime.getRuntime();
	Connection c;
	try {
	    while(true) {
		if(threadgroup.activeCount() < iMaxThreads) {
		    try {
			Socket client_socket = listen_socket.accept();
			client_socket.setSoTimeout(soTimeout);
			c = new Connection(client_socket, threadgroup, this.getPriority()+1, this);
			if(System.getProperty("chat.reThread") == null ) {
			    Thread t = new Thread (c);
			    t.setPriority(this.getPriority() +1 );
			    t.start();
			} else
			    (new ReThread(c)).start();
			yield();
		    } catch (SocketException e) {
			ErrorLog.error(e, 16, "Error connecting to client.");
			// Wait for some sockets to be freed up.
			try {
			    System.gc();
			    sleep(soTimeout);
			} catch(InterruptedException ignored) {}
		    }

		} else
		    try{ sleep(1000L); } catch(InterruptedException e) {	}
	    }
	}
	catch (IOException e) {
	    fail(e, "Exception while listening for connections");
	} catch (VirtualMachineError e) {
	    fail(e, "JVM Error Caught in Server Run!");
	}
    }
    public static long runTime() {
	return ((new Date()).getTime() - dStarted.getTime())/1000L;
    }
    public synchronized void wakeup() {
	notify();
    }  
}
