package customchat.util;

public class AuthInfo {
  public String[] sHandles;
  public long[] lIDs;
  public long lAccountID;

  public AuthInfo(String[] saHandle, long[] laHandleID, long lAID) {
	sHandles = saHandle;
	lIDs = laHandleID;
	lAccountID = lAID;
  }  
  public boolean verifyHandle(String handle) {
	for(int i = 0; i< sHandles.length; i++)
	  if(sHandles[i].equals(handle))
		return true;
	return false;
  }  
}
