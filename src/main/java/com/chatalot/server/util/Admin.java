package customchat.util;

import java.util.BitSet;
import java.util.Calendar;

public class Admin extends Login {

 static final long serialVersionUID = 6098287278162095052L;
  private BitSet privileges;
  private Calendar cExpires;
  private Calendar cStarts;

  public Admin(String sUserName, String sPassPhrase) {
	this(sUserName, sPassPhrase, new BitSet(8), Calendar.getInstance(), Calendar.getInstance());
  }  
  public Admin(String sUserName, String sPassPhrase, BitSet bsPrivileges, Calendar starts, Calendar expires){
	super(sUserName, sPassPhrase);
	this.cExpires = expires;
	this.cStarts = starts;
	privileges = bsPrivileges;
	if (privileges == null)
	  privileges =  new BitSet(8);
  }  
  public Admin(String sUserName, String sPassPhrase, Calendar expires) {
	this(sUserName, sPassPhrase, new BitSet(8), Calendar.getInstance(), expires);
  }  
  public Admin(String sUserName, String sPassPhrase, Calendar starts, Calendar expires) {
	this(sUserName, sPassPhrase, new BitSet(8), starts, expires);
  }  
  public void addPrivilege(int privilege) {
	setPrivilege(privilege, true);
  }  
  public Calendar expires() {
	return expires(null);
  }  
  public Calendar expires(Calendar c) {
	if(c != null)
	  cExpires = c;
	return cExpires;
  }  
  public boolean hasExpired() {
	return cExpires.before(Calendar.getInstance());
  }  
  public boolean hasPrivilege(int privilege) {
	return privilegeSet(privilege) && !hasExpired();
  }  
  public boolean privilegeSet(int privilege) {
	try {
	  return privileges.get(privilege);
	} catch(IndexOutOfBoundsException e) {
	  ErrorLog.error(e, 101, "Used a negative index in privelage");
	}
	return false;
  }  
  public void removePrivilege(int privilege) {
	setPrivilege(privilege, false);
  }  
  public void setPrivilege(int privilege, boolean hasPrivilege) {
	try {
	  if(hasPrivilege)
		privileges.set(privilege);
	  else
		privileges.clear(privilege);
	} catch(IndexOutOfBoundsException e) {
	  ErrorLog.error(e, 100, "Used a negative index in privelage");
	}
  }  
  public Calendar starts(){
	return starts(null);
  }  
  public Calendar starts(Calendar c) {
	if(c != null)
	  cStarts = c;
	return cStarts;
  }  
}
