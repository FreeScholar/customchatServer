package customchat.chat;

import java.util.*;
import java.io.*;
import java.net.*;
import customchat.util.*;
import customchat.htmlutil.*;

/**
 * This class impliments one area of the house.  It keeps a list of all the
 * rooms and all of the chatters in this area.  It also has HTML methods to
 * get infromation from the rooms.
 *
 * @author CustomChat Server
 * @version 1.5
 */
public class Floor extends ChatObject {
    // static final long serialVersionUID = -5087148074027832925L;
    //added by ritchie
    static final long serialVersionUID = 6309882955745548636L;
    // Variables that are set by the Floor Creator
    public String sAreaImg = null;
    public boolean bCtrImg = true;
    public String sHeaderHTML = null;
    public boolean bCtrHead = true;


    protected static final String sNewHTML = htmlFile("NewArea.html");

    /**
     * creates a Floor in house <I>h</I> named <I>name</I>, and initializes the TimeOut thread.
     *
     * @param name The human readable name of the floor.
     * @param h the house containing this floor.
     */
    public Floor(Login l, House h, String keyWord, String name) throws ChatException {
	super(l, h, keyWord, name);
	pageTemplate = new Page();
    }
    public Floor(Login l, LookupTable lt, House h) throws ChatException {
	this(l, h, lt.getValue("sKeyWord"), lt.getValue("sName"));
	update(lt);
    }
    protected String createChildPage() throws ChatException {
	String s = fileToString(Room.sNewHTML);
	s = replace(s, "#ParentName#", sName);
	s = replace(s, "#ParentKey#", sKeyWord);
	return s;
    }  
    protected String getChildName() {
	return "Room";
    }  
    /**
     * Prints out the HTML code for the live list, and switching rooms page.
     * If a chatter is provided, the function provides links on the room name
     * to add the chatter to the room.
     *
     * @param ct usually null except in switching rooms where it is the chatter switching rooms.
     * @param out the output stream to print to.
     * @param bNoPic true means escape images on page.
     * @param bAdmin true means display admin options.
     */

    public Page list(Login l, String handle, boolean showPics)
	throws ChatException {

       	Page p = (Page)pageTemplate.clone();
	
	StringBuffer sb = new StringBuffer();

	sb.append("<SCRIPT language=\"JavaScript\">\n");
	//	sb.append("<!--\n");
	sb.append("function openPM(link) {\n");
	sb.append("		PMwin = window.open(link, 'pmwin','width=650,height=450,scrollbars=no,menubar=no,resize=no')\n");
	sb.append("}\n");
	//	sb.append("// --!>\n");
	sb.append("</SCRIPT>");
	
       	p.addHeadHTML(sb.toString());

	if(sAreaImg != null && sAreaImg.length() > 0) {
	    Image im = new Image(sAreaImg);
	    if(bCtrImg)
		p.addHTML(new Container("CENTER", im));
	    else
		p.addHTML(im);
	}

	if(bCtrHead)
	    p.addHTML("<CENTER>");
	if(sHeaderHTML != null)
	    p.addHTML(sHeaderHTML);
	if(bCtrHead)
	    p.addHTML("</CENTER>");

	// search form
	
	Form f = form("Search", true, FIND);
	
	// added by ritchie	
	f.addHTML("<FONT FACE=\"Arial,Helvetica,Geneva\" SIZE=2>Search for Someone</FONT><BR>") ;
	f.addHTML(" ");
	f.addHTML(new Input(Input.TEXT, SEARCH_VAR, ""));
	p.addHTML(f) ;
	p.addHTML(liveList(l, handle, showPics, iDefaultDepth));

	return p;
    }

    protected HTML liveList0(int level, String handle,boolean showPics,Container ct)
	throws ChatException {
	return super.liveList0(6, handle, showPics, ct);
    }  

    protected String modifyPage() throws ChatException {
	return fileToString(sNewHTML);
    }  

    protected ChatObject newChild(Login lOwner, LookupTable lt)
	throws ChatException {
	return new Room(lOwner, lt, this);
    }  
}
