package com.chatalot.server.chat;

import java.util.*;
import java.io.*;

class Tagenizer {
  private String s;
  int iBegin;
  static public char sOpen = '<';
  static public char sClose = '>';
  static public char sCloseMarker = '/';
  static public String sLT = "lessthan";

  Tagenizer(String ss) {
	s = ss;
	iBegin = s.indexOf(sOpen);
  }  
  public Tag NextTag() {
	int iLast, iEnd, iNext;
	if(iBegin < 0)
	  return null;
	if((iEnd = s.indexOf(sClose, iBegin+1)) < 0) {
	  iLast = iBegin;
	  iBegin = s.indexOf(sOpen, iBegin+1);
	  return new Tag(sLT, new String[0], new String[0], iLast, iLast, false); //less than sign
	}
	if(((iNext = s.indexOf(sOpen, iBegin+1)) > 0) && (iNext < iEnd)) {
	  iLast = iBegin;
	  iBegin = iNext;
	  return new Tag(sLT, new String[0], new String[0], iLast, iLast, false); //less than sign
	}

	String sBody = s.substring(iBegin+1, iEnd);
	iLast = iBegin;
	iBegin = iNext;
	if((sBody == null) || (sBody.equals(""))) {
	  //System.out.println(s);
	  return NextTag();
	}

	boolean bClose=false;
	//    System.out.println(sBody + "#");
	//System.out.println(s.substring(iNext) + "#");
	//System.out.println(s.substring(iLast) + "#");
	if(sBody.charAt(0) == sCloseMarker) {
	  bClose= true;
	  sBody = sBody.substring(1);
	}
	
	int iNumDouble=0, iNumSingle=0;
	for(int i=0; i<sBody.length(); i++) {
	  switch(sBody.charAt(i)) {
	  case '"':
	iNumDouble++;
	break;
	  case '\'':
	iNumSingle++;
	break;
	  default:
	break;
	  }
	}

	if(((iNumDouble % 2) != 0) || ((iNumSingle % 2) != 0))
	 return new Tag(sLT, new String[0], new String[0], iLast, iEnd, false);

	StringTokenizer stBody = new StringTokenizer(sBody);
	String sName;
	if(!stBody.hasMoreElements())
	  return NextTag();
	sName = (String)stBody.nextElement();
	
	String s, sElement, sArg, sValue="";
	StringTokenizer stArg;
	Vector vArg = new Vector();
	Vector vValue = new Vector();
	while(stBody.hasMoreElements()) {
	  sElement = (String)stBody.nextElement();
	  /*      if(sElement.indexOf('"') >= 0)
	while((stBody.hasMoreElements()) && (s = (String)stBody.nextElement()).indexOf('"') < 0)
	  sElement += s;
	  */
	  stArg = new StringTokenizer(sElement, "=");
	  if(stArg.countTokens() < 1)
	//this should never happen
	continue;
	  sArg = stArg.nextToken();
	  if(stArg.countTokens() == 2) {
	sValue = stArg.nextToken();
	  } else while(stArg.hasMoreTokens()) {
	sValue = stArg.nextToken();
	  }
	  vArg.addElement(sArg.toUpperCase());
	  vValue.addElement(sValue.replace('"', ' ').trim());
	}

	String[] a=new String[vArg.size()], v=new String[vValue.size()];
	vArg.copyInto(a);
	vValue.copyInto(v);

	return new Tag(sName, a, v, iLast, iEnd, bClose);
  }  
}
