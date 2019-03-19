package customchat.chat;

import java.util.*;
import java.io.*;

public class Filter implements Serializable {

	// added by ritchie
	static final long serialVersionUID = 6131908114008458807L;
  //static String sEscape = "&lt;";
  static String sReplace = ChatObject.URL_IMG + "misc/blue.gif";
  static String AllowedTags[] = { "A", "CENTER", "B", "I", "H1", "H2", "H3", "H4",
			   "H5", "H6", "DIV", "U", "NOBR", "FONT",
			   "BIG", "SMALL", "BLINK", "HR", "SUB", "SUP", "P",
			   "CITE", "TT", "EM", "STRONG", "MENU", "DIR", "DFN",
			   "VAR", "SAMP", "KBD", "BLOCKQUOTE", "ADDRESS", 
			   "TEXTAREA", "SELECT", "TABLE", "CAPTION", "TR", "TD",
			   "EMBED", "BR", "IMG" };

  static String NoClose[] = { "BR", "HR", "IMG", "LI", "P" , "EMBED", "TR", "TD" };

  Hashtable htOpenTags = new Hashtable();
  Hashtable htClosedTags = new Hashtable();
  Hashtable htArgs = new Hashtable();
  Hashtable htForbid = new Hashtable();
  Hashtable htAllow = new Hashtable();
  Hashtable htLimit = new Hashtable();
  Hashtable htNest = new Hashtable();
  Hashtable htNoClose = new Hashtable();

  int iEscaped = 0;

  public Filter() {
	//arguments allowed -- default none
	htArgs.put("A", "HREF,TARGET,");
	htArgs.put("IMG", "WIDTH,HEIGHT,ALIGN,SRC,BORDER,ALT,");
	htArgs.put("EMBED", "WIDTH,HEIGHT,ALIGN,SRC,BORDER,ALT,LOOP,HIDDEN,AUTOSTART,");
	htArgs.put("FONT", "SIZE,COLOR,FACE,HELVETICA,ARIAL,");
	htArgs.put("HR", "SIZE,WIDTH,ALIGN,");
	htArgs.put("BR", "CLEAR,");

	//values allowed for certain arguments
	htAllow.put("ALIGN", "LEFT,RIGHT,CENTER,BOTTOM,");
	htAllow.put("CLEAR", "ALL,LEFT,RIGHT,");

	//Limits to # of instances of a certain tag in one message -- default no
	//limit
	htLimit.put("BLINK", new Integer(4));
	htLimit.put("IMG", new Integer(10));
	htLimit.put("H1", new Integer(4));
	htLimit.put("H2", new Integer(4));
	htLimit.put("P", new Integer(15));
	htLimit.put("BR", new Integer(15));
	htLimit.put("HR", new Integer(10));
	htLimit.put("EMBED", new Integer(1));

	//Limit to number of nested instances of certain tags -- default no limit
	htNest.put("B", new Integer(3));
	htNest.put("I", new Integer(2));
	htNest.put("OL", new Integer(3));
	htNest.put("UL", new Integer(3));
	//    htNest.put("FONT", new Integer(12));

	for(int i=0; i<AllowedTags.length; i++) {
	  htOpenTags.put(AllowedTags[i], new Integer(0));
	  //htClosedTags.put(AllowedTags[i], 0);
	}

	Integer hack = new Integer(137);
	for(int i=0; i< NoClose.length ; i++) {
	  htNoClose.put(NoClose[i], hack);
	}
  }  

  void Close(StringBuffer sb, String sTag) {
	sb.append(Tagenizer.sOpen);
	sb.append(Tagenizer.sCloseMarker);
	sb.append(sTag);
	sb.append(Tagenizer.sClose);
  }  

  void Escape(StringBuffer sb, int ibegin, int iend) {
	//i += (iEscaped++)*sEscape.length();

	//sb.setCharAt(i, ' ');
	//sb.insert(i, sEscape);

	for(int i = ibegin; i <= iend && i < sb.length(); i++)
	  sb.setCharAt(i, ' ');
  }  

  public String FilterHTML(String s) {
	if (s == null || s.length() < 1)
	  return s;
	String sArg;
	Tag sTag;
	StringBuffer sb = new StringBuffer(s);
	Tagenizer tz = new Tagenizer(s);
	int iSoFar, iClosed = 0;
	Integer I;

	iEscaped = 0;

	while((sTag = tz.NextTag()) != null) {
	  //sb.append("Tag Name is " + sTag.sName + "#");
	  //sb.append(sTag.bClose ? "Close" : "Open");

	  if((I = (Integer)htClosedTags.get(sTag.sName)) != null)
	      iClosed = I.intValue();//how many closed
	  else
	      iClosed = 0;

	  if(sTag.bClose) {
	      //just record a closing tag
	      htClosedTags.put(sTag.sName, new Integer(++iClosed));
	      continue;
	  }

	  if((I = (Integer)htOpenTags.get(sTag.sName)) == null) {
	      //not allowed
	      Escape(sb, sTag.iBegin, sTag.iEnd);
	      continue;
	  }

	  iSoFar = I.intValue();//how many open
	  if(((I = (Integer)htLimit.get(sTag.sName)) != null) && (iSoFar >= I.intValue())) {
	      //past limit
	      Escape(sb, sTag.iBegin, sTag.iEnd);
	      continue;
	  }

	  if(((I = (Integer)htNest.get(sTag.sName)) != null) && (iSoFar - iClosed >= I.intValue())) {
	      //past nesting limit
	      Escape(sb, sTag.iBegin, sTag.iEnd);
	      continue;
	  }

	  //check args
	  String sArgs;
	  if(((sArgs = (String)htArgs.get(sTag.sName)) == null) && (sTag.args.length > 0)) {
	      // too many args
	      Escape(sb, sTag.iBegin, sTag.iEnd);
	      continue;
	  }

	  //check each arg
	  for(int i=0; i<sTag.args.length; i++) {
	      if(!sArgs.contains(sTag.args[i] + ",")) {
	        //argument not found
	        Escape(sb, sTag.iBegin, sTag.iEnd);
	        continue;
	      }
		String sAllowed;
	      if(((sAllowed = (String)htAllow.get(sTag.sName)) != null)
		  &&(!sAllowed.contains(sTag.args[i] + ","))) {

	        //value not allowed for arg
	        Escape(sb, sTag.iBegin, sTag.iEnd);
	        continue;
	      }
	  }

	  //ok!
	  htOpenTags.put(sTag.sName, new Integer(++iSoFar));
	}

	//close open guys
	Enumeration e = htOpenTags.keys();
	String sCurr;
	while(e.hasMoreElements()){
	  if( ((sCurr = (String)e.nextElement()) != null ) &&
		  ((I = (Integer)htOpenTags.get(sCurr)) != null) &&
	        ((iSoFar = I.intValue()) > 0)) {
	    iClosed = 0;
	      if((I = (Integer)htClosedTags.get(sCurr)) != null ) {
	        iClosed = I.intValue();
	      }
	      //close the tags
		if( !htNoClose.containsKey(sCurr) )
	        for(int i=0; i<iSoFar-iClosed; i++) {
	          Close(sb, sCurr);
	        }
	  }
	}

	//add new line break with enter key
	for(int i=0; i<sb.length(); i++ ) {
	  if(sb.charAt(i) == '\n') {
	      sb.insert(++i, "<BR>");
	      i += 3;
	  }
	}

	return sb.toString();
  }  

  static String ImgFilter(String s) {
	Tagenizer t = new Tagenizer(s);

	Tag tag;
	int i, iSoFar=0;
	String sSrc = null, sSoFar = "";
	while((tag = t.NextTag()) != null) {
	  if(!tag.sName.equals("IMG") || tag.bClose )
	      continue;

	  for(i=0; i<tag.args.length; i++) {
	      if(tag.args[i].toUpperCase().equals("SRC")) {
	        sSrc = tag.vals[i];
	        break;
	      }
	  }

	  if(sSrc == null)
	      continue;

	  sSoFar +=  s.substring(iSoFar, tag.iBegin) +
	    Tagenizer.sOpen + "A HREF=" + sSrc + " Target=\"_blank\"" +
	    Tagenizer.sClose + Tagenizer.sOpen + "IMG SRC=" +
	    sReplace + " BORDER=0" + " ALT=\"Click to see IMAGE\"" + Tagenizer.sClose + Tagenizer.sOpen +
	    Tagenizer.sCloseMarker + "A" + Tagenizer.sClose;
	    iSoFar = tag.iEnd + 1;
	}

	return sSoFar + s.substring(iSoFar);
  }  


  static String SndFilter(String s) {
	Tagenizer t = new Tagenizer(s);

	Tag tag;
	int iSoFar=0;
	String sSoFar = "";
	while((tag = t.NextTag()) != null) {
	  if(!tag.sName.equals("EMBED") || tag.bClose )
	      continue;

	  sSoFar += s.substring(iSoFar, tag.iBegin)  ;
	  // + "<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\">Select Options to Hear Sounds</font>";
	  iSoFar = tag.iEnd + 1;

	}

	return sSoFar + s.substring(iSoFar);
  }  

  public static String ctrHTML(String s, boolean bCtr) {
	if(bCtr == false)
	  return s;

	return "<CENTER>" + s + "</CENTER>";
  }  

  public static String stripHTML(final String s) {
	StringBuilder sb;
            sb = new StringBuilder(s.length());
	int i, j;
	char ch;

	for(i = 0; i < s.length() ; i++) {
	  if((ch = s.charAt(i)) != '<')
		sb.append(ch);
	  else {
		j = s.indexOf('>', i + 1);
		if(j < 0) {
		  sb.append(s.substring(i));
		  break;
		} else
		  i = j;
	  }
	}

	String filtered = sb.toString() ;
	if (filtered.equals("")) {
	    filtered = "<B><FONT FACE=Arial,Helvetica,Verdana,Geneva SIZE=2>This Feature has been" +
		" DISABLED by the Administrator</FONT></B>" ;
	}
	
	return filtered ;
  }  
}
