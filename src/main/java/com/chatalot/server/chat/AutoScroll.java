package com.chatalot.server.chat;

import com.chatalot.server.htmlutil.Page;
import java.io.*;


public class AutoScroll extends Object {
    public boolean bMisery = false;
    PrintWriter out;
    private static final String S_BOUNDARY = "===";//"ThisRandomString\nContent-type: text/html\r\n\n";
    private static final String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\nContent-type: application/json;";

    /*
    * We can format our json like this...
    * {
    * 	data: ... HTML goes here ...
    * }
    */
			//"HTTP/1.1 200 OK\r\nContent-type: text/event-stream\r\nAccess-Control-Allow-Origin: '*'\r\nCache-Control: no-cache\r\nConnection: keep-alive\r\n";//;boundary=ThisRandomString\r\n\n";

    public AutoScroll(Page p, final PrintWriter pw, final boolean b, boolean bPrintHeader) {
	out = pw;
	bMisery = b;
	/*if (bPrintHeader)
	    out.print(HTTP_RESPONSE);*/

	/*if(!bMisery && !Connection.anotherIEHack)
	    out.println(S_BOUNDARY);*/

	//p.addHeadHTML("<script type='text/javascript' src='/resources/scripts/autoscroll.js'></script>");

	p.addHTML("<FONT SIZE='1' FACE='Verdana, Arial, Helvetica'><B>MESSAGES WILL APPEAR ON THE SCREEN IN REAL TIME, AS OTHERS POST THEM.</FONT>" +
		  "</B></FONT><BR><FONT SIZE='1' FACE='Verdana, Arial, Helvetica'>" +
		  "<BR> If you are idle for a while you may see the message \"Transfer interrupted!\", or if you want to clear the screen" +
		  " of all messages...<BR> just hit the Reload button in your browser to reconnect to the CustomChat Server.<BR>" +
		  "</FONT><BR><HR>\n\n");
	String responseBody = "{ \"data\": \"hello, world!\" }";
	int contentLength = responseBody.getBytes().length;
	String header = "HTTP/1.1 200 OK\n" +
			"Content-type: application/json\n"
			+ "Content-Length: " + contentLength + "\n"
			+ "Connection: close\n";
	out.println(header);//p.openPage());
		out.println(responseBody);
		//out.println("--" + S_BOUNDARY + "--");
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
	    out.println("data: " + Messages);
				
	if (bMisery && !Messages.equals("")) {
				
	}
	out.flush();
    }
}
