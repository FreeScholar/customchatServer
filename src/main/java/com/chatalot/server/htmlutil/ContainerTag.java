package com.chatalot.server.htmlutil;

import java.util.*;

public class ContainerTag extends Container{

  public ContainerTag() {
	super("SPAN"); // could be SPAN instead
  }        
	public ContainerTag(String name) {
		super(name);
	}
	public ContainerTag(String name, HTML contents) {
		this(name);
		addHTML(contents);
	}
  public ContainerTag(String name, String s) {
		this(name);
		addHTML(new Text(s));
	}
  public Container addHTML(HTML h) {
	return super.addHTML(h);
	}
	public Container addHTML(String s) {
  	return addHTML(new Text(s));
	}
}
