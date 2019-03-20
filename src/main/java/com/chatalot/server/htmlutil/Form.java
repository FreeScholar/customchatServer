package com.chatalot.server.htmlutil;

public class Form extends Container {
	public Form(String name, String action, String method) {
		super("FORM");
		if(name != null)
			addArgument("NAME",name);
		if(action != null)
			addArgument("ACTION", action);
		if(method != null)
			addArgument("METHOD", method);
		else
			addArgument("METHOD", "GET");
	}
}
