package customchat.util;

import java.io.*;
import java.util.*;
import java.util.Calendar;

public abstract class ErrorLog {
  private static Long errors = new Long(0);
  private static PrintWriter log = new PrintWriter(new BufferedWriter (new OutputStreamWriter(System.err)));
  private static char delim = '\t';
  public static boolean LOG = false ;
/**
   * Prints a an error to the logfile specified by setOutput (System.out is the default)
   *
   */
public synchronized static void error(Throwable err, int errNo, String userMessage) {
	if (LOG) {
		// log the time and date
		log.print(new Date().toString() + delim);

		// print error number
		log.print(String.valueOf(errNo) + delim);

		// print the users error discription
		log.print(userMessage + delim);

		if (err != null) {
		// print the error message
		log.print(err.toString());

		// put a newline on at the end
		log.println();
		if (System.getProperty("print_stacktrace") != null)
			err.printStackTrace(log);
		}
		log.flush();
	}
}
  /**
   * Set the deliminator used to separate fields in the logfile
   */
  public synchronized static void setDeliminator(char deliminator ) {
	delim = deliminator;
  }  
  /**
   *  Sets the file to write the log to.  If the file alread exists the log is
   *  appended to it.
   */
  public synchronized static void setOutput(String logFile) throws FileNotFoundException{
	PrintWriter temp = log;
	try {
	log = new PrintWriter(new BufferedWriter (new OutputStreamWriter(new FileOutputStream(logFile, true))));
	} catch (IOException e) {
	  log = new PrintWriter(new BufferedWriter (new OutputStreamWriter(System.out)));
	  error(e,0,"Could not open error log.  Defaulting to err.");
	}
  }  
}
