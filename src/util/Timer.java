package customchat.util;

import java.util.Hashtable;
import java.util.Date;

public abstract class Timer {
  private static Hashtable timers = new Hashtable();
  private static boolean on = false;
  static {
	on = System.getProperty("chat.timer") != null;
  }


  public static void start() {
	if(on) {
	  timers.put(Thread.currentThread(), new Date());
	}
  }  
  public static Object start(Object key) {
	if (on) {
	  Date now = new Date();
	  if(key == null)
	  key = new Double( Math.random());
	  timers.put(key, now);
	  return key;
	}
	  return null;
  }  
  public static long stop() {
	if(on) {
	  return stop(Thread.currentThread());
	}
	return -100L;
  }  
  public static long stop(Object key) {
	if(on) {
	  Date now = new Date();
	  Date start;
	  long time;
	  synchronized (timers) {
		start = (Date) timers.remove(key);
	  }
	  if(start == null) {
		System.err.println("Did not Start Timer.");
		return -1L;
	  }
	  time = now.getTime() - start.getTime();
	  return time;
	}
	  return -1L;
  }  
}
