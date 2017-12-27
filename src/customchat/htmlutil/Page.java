package customchat.htmlutil;

import customchat.util.ErrorLog;

public class Page extends Container {
    StringBuilder sBuilder = new StringBuilder();
    String docType = "<!DOCTYPE html>";
    Container head;
    public Container body;
    Container frameSet;


    public Page() {
	this( new Container("HEAD"), new Body());
    }  

    public Page(Body bdy) {
	this( new Container("HEAD"),bdy);
    }      

    public Page(Container head, Body body) {
	super("HTML");

	if (head == null) {
            head = new Container("HEAD");
        }
        
	this.head = head;
	super.addHTML(this.head);

        head.addHTML("<meta charset='utf-8'>");
        head.addHTML("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css\" integrity=\"sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb\" crossorigin=\"anonymous\">");
	head.addHTML("<STYLE TYPE=\"text/css\"><!-- A:link, A:visited, A:active { text-decoration: none; } --></STYLE>");

	if (body == null) {
             body = new Body();
        }
	   

	this.body = body;
	super.addHTML(this.body);
    }    
    /**
     * Create a page from contents.  Assumes that contents is a properly formatted
     * HTML page.  Calling addHTML or addHeadHTML will cause a null pointer error.
     *
     */

    /**
     * Create a page from contents.Assumes that contents is a properly formatted
 HTML page.  Calling addHTML or addHeadHTML will cause a null pointer error.
     * @param contents
     */
    public Page(String contents) {
	this();
	addHTML(contents);
    }  

    public Tag addArgument(Argument a) {
	return body.addArgument(a);
    }  

    public HTML addFrame(HTML h) {
	if(frameSet == null) {
	    frameSet = new Container("FRAMESET", h);
	    contents.setElementAt(frameSet, 1);
	    super.addHTML(new Container("NOFRAMES", body));
	} else
	    frameSet.addHTML(h);
	return h;
    }  

    public HTML addFrame(String s) {
	return addFrame(new Text(s));
    }  

    public void addFrameSetArg(String argName, String argValue) {
	if(frameSet == null) {
	    frameSet = new Container("FRAMESET");
	    contents.setElementAt(frameSet, 1);
	    super.addHTML(new Container("NOFRAMES", body));
	}
	frameSet.addArgument(argName, argValue);
    }  

    public Container addHTML(HTML h) {
	return body.addHTML(h);
    }  

    public Container addHTML(String s) {
	return body.addHTML(s);
    }  

    public HTML addHeadHTML(HTML h) {
	return head.addHTML(h);
    }  

    public HTML addHeadHTML(String s) {
	if (head == null) {
            head = new Container("HEAD");
        }
	     	    
	return head.addHTML(s);
    } 

    public Object clone() {

	Page p = new Page() ;

	try {
	    if (contents.elementAt(0) != null) {
		Object o = ((Container)contents.elementAt(0)).clone();
		p.head = (Container)o ;
	    }

	    if(frameSet == null) {
		Container ct = (Container)contents.elementAt(1) ;
		if (ct != null) {
		    Object newct = ct.clone() ;
		    p.body = (Container)newct ;
		}
	    } else {
		if (contents.elementAt(1) != null) {
		    p.frameSet =  (Container)((Container)contents.elementAt(1)).clone();
		    Container newct = (Container)((Container)(contents.elementAt(2))).contents.elementAt(0);
		    p.body = (Container)newct.clone();
		}
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    p = new Page();
	    ErrorLog.error(e, 208, "Could not clone page.");
	}

	return p;
    }  

    public String openPage() {
	return toString();
    }  

    @Override
    public String toString() 
    {	
	StringBuilder sb = new StringBuilder();
		
	if (frameSet == null) {
            sb.append(docType);
	    sb.append("<HTML>\n");
	    sb.append(head.toString());
	    sb.append(body.toString(true));
	    sb.append("</HTML>\n");
	}
        else {
            sb.append(docType);
	    sb.append(head.toString());
	    sb.append(frameSet);
        }
	    

	return sb.toString();
    }
}
