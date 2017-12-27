package customchat.chat;

import customchat.htmlutil.Page;

//import chat.*;
import java.io.*;
import java.net.*;


public class AutoScroll extends Object {
    public boolean bMisery = false;
    PrintWriter out;
    private String sBoundary = "ThisRandomString\nContent-type: text/html\r\n\n";
    private static final String HTTPResponse = "HTTP/1.1 200 OK\r\nContent-type: multipart/mixed;boundary=ThisRandomString\r\n\n";

    public AutoScroll(Page p, final PrintWriter pw, final boolean b, boolean bPrintHeader) {
	out = pw;
	bMisery = b;
	if (bPrintHeader)
	    out.print(HTTPResponse);

	if(!bMisery && !Connection.anotherIEHack)
	    out.println(sBoundary);

	p.addHeadHTML(
		      "<SCRIPT LANGUAGE=\"JavaScript1.1\">\n" +
		      "\n" +
		      "  <!--\n" +
		      "    var autoScrollOn = 1;\n" +
		      "    var scrollOnFunction;\n" +
		      "    var scrollOffFunction;\n" +
		      "\n" +
		      "    function scrollWindow( )\n" +
		      "    {\n" +
		      "        if ( autoScrollOn == 1 )\n" +
		      "        {\n" +
		      "            this.scroll(0, 65000);\n" +
		      "            setTimeout('scrollWindow()', 200);\n" +
		      "        }  // end if\n" +
		      "    }  // end scrollWindow\n" +
		      "\n" +
		      "    function scrollOn( )\n" +
		      "    {\n" +
		      "        autoScrollOn = 1;\n" +
		      "        scrollWindow( );\n" +
		      "    }  // end scrollOn\n" +
		      "\n" +
		      "    function scrollOff( )\n" +
		      "    {\n" +
		      "        autoScrollOn = 0;\n" +
		      "    }  // end scrollOff\n" +
		      "\n" +
		      "    function StartUp( )\n" +
		      "    {\n" +
		      "        this.onblur  = scrollOnFunction;\n" +
		      "        this.onfocus = scrollOffFunction;\n" +
		      "        scrollWindow( );\n" +
		      "    }  // end StartUp\n" +
		      "\n" +
		      "  scrollOnFunction = new Function('scrollOn( )')\n" +
		      "  scrollOffFunction = new Function('scrollOff( )')\n" +
		      "  StartUp();\n" +
		      "  self.onload = new Function('alert(\"You have timed out of the room.  " +
		      "Hit Reload or Refresh on your browser to re-enter the chat.\")');\n" +
		      "  \n" +
		      "  // -->\n" +
		      "\n" +
		      "</SCRIPT>");

	p.addHTML("<FONT SIZE='1' FACE='Verdana, Arial, Helvetica'><B>MESSAGES WILL APPEAR ON THE SCREEN IN REAL TIME, AS OTHERS POST THEM.</FONT>" +
		  "</B></FONT><BR><FONT SIZE='1' FACE='Verdana, Arial, Helvetica'>" +
		  "<BR> If you are idle for a while you may see the message \"Transfer interrupted!\", or if you want to clear the screen" +
		  " of all messages...<BR> just hit the Reload button in your browser to reconnect to the CustomChat Server.<BR>" +
		  "</FONT><BR><HR>\n\n");

	out.println(p.openPage());
	out.flush();
    }                  
    public synchronized void Exit() {
	if(out != null) out.close();
    }
    public void finalize() {
	Exit();
    }  
    public void print( final String Messages) {

	if (!Messages.equals(""))
	    out.println(Messages);
				
	if (bMisery && !Messages.equals("")) {
				
	}
	out.flush();
    }
}
