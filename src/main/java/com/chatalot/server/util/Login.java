package com.chatalot.server.util;

import java.io.*;

public class Login implements Serializable {
    static final long serialVersionUID = 1953156289206525120L;
    private String sLogin = null;
    private String sPass = null;
	 public String IP = null; 
	
    public Login(Login l) {
	this(l.sLogin, l.sPass);
    }  

    public Login(String sUserName, String sPassPhrase) {
	if(sUserName != null) {
	    sLogin = new String(sUserName);
	}
	if(sPassPhrase != null) {
	    sPass = new String(sPassPhrase);
	}
    }  

    public boolean equals(Login l) {
	if(l == null) return false;
	return(l.equals(sLogin, sPass));
    }

    public boolean equals(Object anObject) {
	if(this == anObject) {
	    return true;
	}
	if((anObject != null) && (anObject instanceof Login))
	    return equals((Login)anObject);
	return false;
    }

    public boolean equals(String sUserName, String sPassPhrase) {
	return(mySEquals(sUserName, sLogin) && mySEquals(sPassPhrase, sPass)); 
    }

    public String getLogin() {
	return new String(sLogin);
    }  

    public String getPassPhrase() {
	return new String(sPass);
    }  
    public int hashCode() {
	return (sLogin + ":" + sPass).hashCode();
    }  

    private boolean mySEquals(String s1, String s2) { 
	if(s1 == null || s2 == null)
	    return s1 == s2;
	else
	    return s1.equals(s2);
    }  

    public String toString() {
	return new String(sLogin); }
}
