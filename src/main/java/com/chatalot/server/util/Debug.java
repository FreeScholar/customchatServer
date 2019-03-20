package com.chatalot.server.util;

public class Debug {
  public static void print(int ch) {
	if(System.getProperty("debug") != null)
	  System.out.print((char)ch);
  }  
  public static void print(String s) {
	if(System.getProperty("debug") != null)
	  System.out.print(s);
  }  
  public static void println(String s) {
	if(System.getProperty("debug") != null)
	  System.out.println(s);
  }  
}
