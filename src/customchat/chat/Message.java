package customchat.chat;

import java.util.*;
import java.io.*;

public class Message extends Object implements Serializable {
//  public static final long serialVersionUID=-967182475622858258L;
// added by ritchie

 static final long serialVersionUID = -4687374969378381578L;

  //Variables for message
  private String sText;//message body
  private String sFrom; //whom from?
  private String sDisplayFrom;
  private String[] aTo; //whom to? null = public
  private Date dTime; //when sent?
  public short type = PUBLIC;

  public static final short PUBLIC    = 0;
  public static final short PRIVATE   = 1;
  public static final short BROADCAST = 2;
  public static final short INSTANT   = 3;
  public static final short SYSTEM   = 4;
  public static final short REMOVE   = 5;
  public static final short ADDCHATTER   = 6;

  /* Creates a Broadcast message */
  //public Message(String text, String from, short type) {
	//	this(text, from, (String[])null, BROADCAST);
	//}

  ///* Creates a private message */
	//public Message(String text, String from, String to) {
	//	this(text, from, new String[] {to}, PUBLIC);
  //}

  public Message(String text, String from, String to, short type) {
		this(text, from, new String[] {to}, type);
  }  
  ///* Creates a public message */
  //public Message(String text, String from, String[] to) {
  //  this(text, from, to, PUBLIC);
  //}


  public Message(String text, String from, String[] to, short type) {
	sText = text;
	sFrom = from;
	aTo = to;
	dTime = new Date();
	this.type = type;
	if(from == null)
	  sDisplayFrom = "<b><font FACE=\"Arial,Helvetica,Geneva\" SIZE=\"2\"><a target=\"_new\" href=\"http://customchat.com\">CustomChat Server</a></font></b>";
	else {
	  Chatter c = UserRegistry.getChatter(from);
	  if(c == null)
		sDisplayFrom = from;
	  else {
		sDisplayFrom = c.getFullHTML();
		}
	}
  }  

  public synchronized void Filter(Filter fl) {
	Filter newFl = new Filter();
	sText = newFl.FilterHTML(sText);
  }  

  public String getFrom() {
	return sFrom;
  }  

   //html methods
    public String getHTML(Chatter ct) {
	String sTextTemp = sText, sFromTemp = sDisplayFrom;

	try {	   
		if (type != REMOVE && type != ADDCHATTER) {
			if(ct == null || ct.NoSound() || sFrom == null){
			sTextTemp = Filter.SndFilter(sTextTemp);
			}
		}
	    
	    if(ct == null || ct.bNoPic || sFrom == null)
		sTextTemp = Filter.ImgFilter(sTextTemp);
	    
	    if(ct == null || ct.NoPicHandle())
		sFromTemp = Filter.ImgFilter(sFromTemp);
	    
	    if (ct != null) {
		    // don't filter the message for html if we have a system message
			// cos we may have <script> </script> code in it ...
			if (ct.loc.bHtmlDis &&  ! ct.isAdmin && 
				type != REMOVE && type != ADDCHATTER) 	{
				return "<div class='user-and-message'><div class='u font-weight-bold'>" + Filter.stripHTML(sFromTemp) + "</div><div class='m'>" + Filter.stripHTML(sTextTemp) + "</div></div>";
				}
			else {
				return  "<div class='user-and-message'><div class='u font-weight-bold'>" + Filter.stripHTML(sFromTemp) + "</div><div class='m'>" + sTextTemp + "</div></div>";
			}
	    } else {
		return "<div class='user-and-message'><div class='u font-weight-bold'>" + Filter.stripHTML(sFromTemp) + "</div><div class='m'>" + sTextTemp + "</div></div>";
	    }
	} catch (Exception e) {
	    System.out.println("Messages:getHTML:"+e) ;
	    System.out.println("\tChatter is "+ct) ;	    
	    if (ct != null)
		System.out.println("\tloc is "+ct.loc) ;
	}
	return "<div class='user-and-message'><div class='u'>" + Filter.stripHTML(sFromTemp) + "</div><div class='m'>" + sTextTemp + "</div></div>";

  }  

    public String getPlain() {
	return Filter.stripHTML(sText) ;
    }

  public short getType() {
	return type;
  }
  
  public boolean is(short type) {
	return this.type == type;
  }
  
  public void languageFilter(int wordSet,ChatObject co)
  throws BadLanguageException {
      sText = co.blf.filter(wordSet, sText);      
  }  

  public String[] recips() {
	return aTo;
  }  

 @Override
  public String toString() {
	return sText;
  }  

  public void append(String s) {
	StringBuilder sb  = new StringBuilder() ;
	sb.append(sText) ;
	sb.append(s) ;
	sText = sb.toString() ;
  }
}
