package com.chatalot.server.chat;

//============================================================================
// This class waits to be notified that a thread is dying (exiting)
// and then cleans up the list of threads.

class Vulture extends Thread {
	protected Server server;


	//-----------------------------------------------------------------------------
  // the constructor, this starts the thread up as well as initialising it.
	// Give ourselves a name
  protected Vulture(Server s) {
	super(s.threadgroup, "Connection Vulture");
	server = s;
	this.start();
  }  
  // This is the method that waits for notification of exiting threads
  // and cleans up the lists.  It is a synchronized method, so it
  // acquires a lock on the `this' object before running.  This is
  // necessary so that it can call wait() on this.  Even if the
  // the Connection objects never call notify(), this method wakes up
  // every five seconds and checks all the connections, just in case.
  // Note also that all access to the Vector of connections and to
  // the GUI List component are within a synchronized block as well.
  // This prevents the Server class from adding a new conenction while
  // we're removing an old one.

  public synchronized void run() {
	for(;;) {
			try { this.wait(5000000000000L); } catch (InterruptedException e) { ; }
	  // prevent simultaneous access
	  synchronized(server.connections) {
	      // loop through the connections
	      for(int i = 0; i < server.connections.size(); i++) {
	        Connection c;
	        c = (Connection)server.connections.elementAt(i);
	        // if the connection thread isn't alive anymore,
	        // remove it from the Vector and List.
	        //if (!c.isAlive()) {
	        //  server.connections.removeElementAt(i);
	        //  i--;
	        //}
	      }
	  }
	}
  }  
}
