package com.chatalot.server.chat;

/*
 * Java Network Programming, Second Edition
 * Merlin Hughes, Michael Shoffner, Derek Hamner
 * Manning Publications Company; ISBN 188477749X
 *
 * http://nitric.com/jnp/
 *
 * Copyright (c) 1997-1999 Merlin Hughes, Michael Shoffner, Derek Hamner;
 * all rights reserved; see license.txt for details.
 */
import java.util.*;


public class ReThread implements Runnable {
  protected Runnable target;

  private static int id = 0;
  //private static final int START_THREADS = 10;
  private static Vector threads = new Vector ();

  // Spawn START_THEADS numer of threads to begin with
  //static {
  //  for(int i = 0; i < START_THREADS; i++)
  //    threads.addElement(new ());
  //}


  private ReThread reThread;
  private Thread thread;

  private ThreadGroup tg = new ThreadGroup("ReThread ThreadGroup");

  public ReThread (Runnable target) {
	this.target = target;
  }  
  private static synchronized int getID () { return id ++; }  
  public synchronized void interrupt () {
	if ((target != null) && ((thread != null) ^ (reThread != null))) {
	  if (thread != null) {
		thread.interrupt ();
	  } else {
		reThread.interrupt0 (this);
	  }
	}
  }  
  protected synchronized void interrupt0 (ReThread reThread) {
	if ((target != null) && (reThread == this.reThread)) {
	  thread.interrupt ();
	}
  }  
  public void run () {
	while (true) {
	  try {
		target.run ();
	  } catch (RuntimeException ex) {
		ex.printStackTrace ();
	  }
	  waitForTarget ();
	}
  }  
  public synchronized void start () {
	//System.out.println("ReThread threads vector contains : " + String.valueOf(threads.size()) +" reThreads.");
	//System.out.println("Active threads : " + String.valueOf(tg.activeCount()));
	if ((thread == null) && (reThread == null)) {
	  synchronized (threads) {
		if (threads.isEmpty ()) {
		  thread = new Thread (tg, this, "ReThread-" + getID ());
		  thread.setPriority(Thread.NORM_PRIORITY - 1);
		  thread.start ();
		} else {
		  reThread = (ReThread) threads.lastElement ();
		  threads.setSize (threads.size () - 1);
		  reThread.start0 (this);
		}
	  }
	}
  }  
  protected synchronized void start0 (ReThread reThread) {
	this.reThread = reThread;
	target = reThread.target;
	notify ();
  }  
  protected synchronized void waitForTarget () {
	target = null;
	threads.addElement (this);
	while (target == null) {
	  try {
		wait ();
	  } catch (InterruptedException ignored) {
	  }
	}
  }  
}
