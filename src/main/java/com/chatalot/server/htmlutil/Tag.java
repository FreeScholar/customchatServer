package com.chatalot.server.htmlutil;

import java.util.*;
import java.io.*;

public class Tag extends HTML {
    protected String name;
    protected Hashtable arguments;


    //public static final Tag P = new Tag("P");
    //public static final Tag BR = new Tag("BR");

    public Tag(final String name) {
	this.name = name;
	arguments = new Hashtable(3);
    }

    public Tag addArgument(Argument a) {
	arguments.put(a.name , a);
	return this;
    }  

    public Tag addArgument(final String name) {
	return addArgument(new Flag(name));
    }  

    public Tag addArgument(final String name, final Object value) {
	return addArgument(new Argument(name, value));
    }

    public void removeArgument(Argument a) {
	removeArgument(a.name);
    }  

    public void removeArgument(String s) {
	arguments.remove(s);
    }  

    /**
     *
     * @return
     */
    @Override
    protected String toText() {
	StringBuilder sb = new StringBuilder(100);
	sb.append("<").append(name.toUpperCase());
	Enumeration e = arguments.elements();
	while(e.hasMoreElements())
	    sb.append(((Argument)e.nextElement()).toString());
	sb.append(">");
	return sb.toString();
    }
}
