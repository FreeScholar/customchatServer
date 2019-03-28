package com.chatalot.server.chat;

import com.chatalot.server.htmlutil.Page;
import java.io.*;


public class AutoScroll extends Object {
    PrintWriter out;

    public AutoScroll(Chatter c, final PrintWriter pw, boolean bPrintHeader) {
		out = pw;

		if(bPrintHeader){
			String content = "<FONT SIZE='1' FACE='Verdana, Arial, Helvetica'><B>MESSAGES WILL APPEAR ON THE SCREEN IN REAL TIME, AS OTHERS POST THEM.</FONT>" +
					"</B></FONT><BR><FONT SIZE='1' FACE='Verdana, Arial, Helvetica'>" +
					"<BR> If you are idle for a while you may see the message 'Transfer interrupted!', or if you want to clear the screen" +
					" of all messages...<BR> just hit the Reload button in your browser to reconnect to the CustomChat Server.<BR>" +
					"</FONT><BR><HR>";
			content = String.format("{ \"data\": \"%s\" }", content);
			this.print(content);
		} else{
			this.print(String.format("{ \"data\": %s }", c.JSONGetNewMessages()));
		}
	}

    public static String generateJSONHTTPResponse(String content){
		int contentLength = content.getBytes().length;
		String header = "HTTP/1.1 200 OK\n" +
				"Content-type: application/json; charset=utf-8\n"
				+ "Content-Length: " + contentLength + "\n"
				+ "Connection: close\n\n";
		return header + content;
	}
    public synchronized void Exit() {
	if(out != null) out.close();
    }
    public void finalize() {
	Exit();
    }  
    public void print( final String responseBody) {
	    out.println(generateJSONHTTPResponse(responseBody));
		out.flush();
    }
}
