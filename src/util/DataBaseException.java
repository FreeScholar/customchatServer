package customchat.util;

public class DataBaseException extends Exception {
 /* public String toString() {
	if(sReturn != null)
	  return new String(sReturn);
	else
	  return super.toString();
  }

  public String toString(boolean bDebugging) {
	if(bDebugging && sReturn != null)
	  return super.toString() + " : " + sReturn;
	else
	  return toString();
  }
 */

  public DataBaseException(String s) {
	super(s);
  }  
}
