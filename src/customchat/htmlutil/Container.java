package customchat.htmlutil;

import java.util.*;
import customchat.util.ErrorLog;


public class Container extends Tag implements Cloneable {
    public Vector contents; // list of HTML elements contained in this tag

    public Container() {
	super("div");
	contents = new Vector(3);
    }

    public Container(String name) {
	super(name);
	contents = new Vector(3);
    }  
    
    public Container(String name, HTML contents) {
	this(name);
	addHTML(contents);
    }
    
    public Container(String name, String s) {
	this(name);
	addHTML(new Text(s));
    }

    public Container addHTML(HTML h) {
	contents.addElement(h);
	return this;
    }  
    public Container addHTML(String s) {
	return addHTML(new Text(s));
    }  
    @Override
    public synchronized Object clone() {
	Container c = new Container(name) ;
	Enumeration e = contents.elements() ;
	c.arguments = (Hashtable)arguments.clone()   ;

	while (e.hasMoreElements()) {
	    Object o = e.nextElement() ;
	    HTML h = (HTML)o ;
	    c.contents.addElement(h.clone()) ;
	}
	
	return c;
    }  

    /**
     *
     * @param noClose
     * @return
     */
    protected String toString(boolean noClose) {
	StringBuilder sb;
        sb = new StringBuilder();
	if(noClose) {
	    sb.append(super.toText());
	    sb.append("\n");
	    for(int i = 0; i < contents.size(); i++)
		sb.append(((HTML)contents.elementAt(i)).toString());
	    sb.append("\n");
	    return sb.toString();
	} else
	    return toString();
    }  

    /**
     *
     * @return
     */
    @Override
    protected String toText() {
	StringBuilder sb;
        sb = new StringBuilder(100);
	sb.append(super.toText());
	sb.append("\n");
	for(int i = 0; i < contents.size(); i++)
	    sb.append(((HTML)contents.elementAt(i)).toString());
	sb.append("</").append(name).append(">");
	sb.append("\n");
	return sb.toString();
    }  
}
