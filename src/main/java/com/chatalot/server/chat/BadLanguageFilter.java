package com.chatalot.server.chat;

import com.chatalot.server.htmlutil.*;
import com.chatalot.server.util.* ;
import java.util.*;
import java.io.*;


public class BadLanguageFilter {
    public static final int NONE  = -1;
    public static final int G     = 0;
    public static final int PG    = 1;
    public static final int PG13  = 2;
    public static final int R     = 3;

    public BadLanguageFilter() {
    }

    public static final String[] saveDics = {"G.dic",
					     "PG.dic",
					     "PG13.dic",
					     "R.dic"};

    private   Hashtable WORDS_G = new Hashtable();
    private   Hashtable WORDS_PG = new Hashtable();
    private   Hashtable WORDS_PG13 = new Hashtable();
    private   Hashtable WORDS_R = new Hashtable();

    private static boolean throwExceptions = false;
    public boolean defined = false ;

    public  Hashtable[] WORDS = null         ; //= { WORDS_G,
    //     WORDS_PG,
    //				       WORDS_PG13,
    //				       WORDS_R};


    private static String WHITESPACE = "" ;
  
    static {
	if(System.getProperty("chat.whitespace") == null) {
	    StringBuffer sb = new StringBuffer();
	    for(char i = 0; i < 255; i++) {
		if(!Character.isLetter(i))
		    sb.append(i);
	    }
	    WHITESPACE = sb.toString();
	} else
	    WHITESPACE = System.getProperty("chat.whitespace");
    }


    private void makeWORDS() {
	WORDS = new Hashtable[4] ;
	WORDS[0] = WORDS_G = new Hashtable() ;
	WORDS[1] = WORDS_PG = new Hashtable();
	WORDS[2] = WORDS_PG13 = new Hashtable();
	WORDS[3] = WORDS_R = new Hashtable() ;
    }


    public void load(ChatObject co) throws IOException{
	makeWORDS() ;
	try {
	for(int i = 0; i < saveDics.length; i++) {

	    BufferedReader in = new BufferedReader(
			        new InputStreamReader(
				new FileInputStream(
    			        new File(co.getPath() , saveDics[i]))));

	    String word;
	    while((word = in.readLine()) != null) {
		WORDS[i].put(word.trim().toLowerCase(), "1");
	    }
	    defined = true ;
	}
	} catch (IOException ioe) {
	    defined = false ;
	    throw ioe ;
	}
    }

    public  Container editPage(ChatObject co) {
	Container p = new Container();

	Form f = new Form("BadWords",
			  co.sKeyWord+"?Action=" + String.valueOf(House.BAD_WORDS) ,
			  "POST");
	p.addHTML(f);	

	f.addHTML("<CENTER><H1><font face=\"Helvetica, Arial, Geneva\">Bad Word Manager</font></H1></CENTER>") ;
	
	f.addHTML("<TABLE align=\"center\" width=\"600\"><TR><TD>") ;
	f.addHTML("<FONT FACE=\"Helvetica, Arial, Geneva\" SIZE=2>By using the Bad Word Manager, you can customize the lists of forbidden words for each filter.  To add a bad word, just type it at the bottom of the list.  Changes can be made on the fly, while users are chatting. To remove a word, just delete it from the list.  You may cut and paste words as though it were a regular text document.  When you are done making your changes, click submit.  The page will reload and you will see the updated list.  If you wish to undo your changes, hit refresh and the list will revert back to its last saved state.<P>When editing a room you must select a Bad Word Filter from the dropdown list within the Room Creator for the filter to work - the default setting is 'None'. </FONT>") ;
	f.addHTML("</TD></TR></TABLE><BR>") ;

	
	// set up the table with each of the four dictionaries in it

	f.addHTML(  "<TABLE align=\"center\"><tr>"
		    + "<td align=\"center\"><font face=\"Helvetica, Arial, Geneva\">G </font></td>"
		    + "<td align=\"center\"><font face=\"Helvetica, Arial, Geneva\">PG </font></td>"
		    + "<td align=\"center\"><font face=\"Helvetica, Arial, Geneva\">PG13 </td></font>"
		    + "<td align=\"center\"><font face=\"Helvetica, Arial, Geneva\">R </font> </td>"
		    + "</tr>");

	StringBuffer sb;

	try {
	    load(co) ;
	}
	catch (IOException ioe) {
	    WORDS = null ;
	}

	for(int i = G; i <= R; i++) {
	    sb = new StringBuffer(6); // Ave Bad Word Length

	    Enumeration words = null ;

	    if (WORDS != null)
		words = WORDS[i].keys();

	    sb.append("<TEXTAREA ROWS=\"20\" COLS=\"15\" "
		      + "NAME=\"BAD_WORD_" + String.valueOf(i) + "\">");

	    // Loop through the words
	    if (WORDS != null) {
		while(words.hasMoreElements()) {
		    sb.append(words.nextElement().toString());
		    // No newline after the last line
		    sb.append(words.hasMoreElements() ? "\n" : "");
		}
	    }

	    sb.append("</textarea>");

	    f.addHTML(new Container("td",sb.toString()));
	}

	f.addHTML("</TR></TABLE>") ;
	f.addHTML("<CENTER>") ;
	f.addHTML(new Input(Input.SUBMIT, "", "Submit Changes"));
	f.addHTML(new Input(Input.RESET,  "", "Undo All Changes"));
	f.addHTML("<BR>");
	f.addHTML(new Link(ChatObject.URL_PREFIX, "<h1><font size=2 face=\"Helvetica, Arial, Geneva\"><b>Back To Live List</b></font></h1>"));
	f.addHTML("</CENTER>") ;


	return p;
    }                                      

    public String filter(final int wordset, String s)
	throws BadLanguageException {

	if (WORDS == null)
	    return s;

	if(wordset < 0 || wordset >= WORDS.length)
	    return s;

	Hashtable badWords = WORDS[wordset];
	StringTokenizer st = new StringTokenizer(s, WHITESPACE);
	StringBuffer sb = new StringBuffer(s);

	String token;
	int last = 0;
	while(st.hasMoreTokens()) {
	    token = st.nextToken();
	    if(badWords.containsKey(token.toLowerCase()))
		if(throwExceptions)
		    throw new BadLanguageException(token);
		else {
		    int start = s.indexOf(token, last);
		    last = start + token.length();
		    for(int i = start; i < last; i ++)
			sb.setCharAt(i, '*');
		}
	}
	return (sb.toString());
    }      


     public static void throwsExceptions(boolean b) {
	throwExceptions = b;
    }  

    public Container update(LookupTable lt,ChatObject co) {
	if(lt == null)
	    return editPage(co);

	for(int i = G; i <= R; i++) {
	    // Get the list of words
	    String words = lt.getValue("BAD_WORD_" + String.valueOf(i));
	  
	    // words should never be null
	    if(words == null)
		continue;

	    if (WORDS == null)
		makeWORDS() ;

	    // Clear out the old dictionary
	    WORDS[i].clear();

	    // Words are seperated by newlines
	    StringTokenizer st = new StringTokenizer(words, "\n");

	    // Add in all of the words
	    while(st.hasMoreTokens())
		WORDS[i].put(st.nextToken().trim().toLowerCase(), "1");

	    try {
		PrintWriter save = new PrintWriter(
				   new FileOutputStream(
			       	new File(co.getPath(), saveDics[i])));

		Enumeration it = WORDS[i].keys();
		while(it.hasMoreElements())
		    save.println(it.nextElement().toString());

		save.close();
	    } catch (IOException e) {
		ErrorLog.error(e, 4001, "Could not save dictionary " + saveDics[i]);
	    }
	}

	return editPage(co);
    }        
}
