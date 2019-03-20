package com.chatalot.server.htmlutil;

public class Link extends Container {

  public Link(String URL, HTML h) {
	super("A");
	if(URL == null) throw new NullPointerException("Cannot create Link with null URL");
	this.addArgument("href",URL);
	if(h == null) throw new NullPointerException("Cannot create Link with null HTML");
	this.addHTML(h);
  }  
  public Link(String URL, HTML h, String target) {
	this(URL, h);
	addArgument("TARGET", target);
  }  
  public Link(String URL, String LinkText) {
	this(URL, new Text(LinkText));
  }  
  public Link(String URL, String LinkText, String target) {
	this(URL, new Text(LinkText), target);
  }  
}
