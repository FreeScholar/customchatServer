package com.chatalot.server.htmlutil;


public class Header extends Container {

	public Header(int i, HTML contents) {
		super("H" + String.valueOf(i), contents);
	}

	public Header(int i, String contents) {
	    this(i, new Text(contents));
	}
}
