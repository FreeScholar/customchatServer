package com.chatalot.server.chat;

import java.util.*;
import java.io.*;

class Tag {
  int iBegin, iEnd;
  public String sName;
  public boolean bClose = false;
  public String[] args, vals;

  Tag(String s, String[] a, String[] v, int i, int j) {
	Setup(s, a, v, i, j);
  }  
  Tag(String s, String[] a, String[] v, int i, int j, boolean b) {
	bClose = b;
	Setup(s, a, v, i, j);
  }  
  void Setup(String s, String[] a, String[] v, int i, int j) {
	sName = s.toUpperCase();
	args = a;
	vals = v;
	iBegin = i;
	iEnd = j;
  }  
}
