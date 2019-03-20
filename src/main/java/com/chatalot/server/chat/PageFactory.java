package com.chatalot.server.chat;

import com.chatalot.server.util.*;
import com.chatalot.server.htmlutil.*;
import java.io.*;

public abstract class PageFactory {
  private static final short GALLERYPAGE_COLS = 4;
  public static final String VAR_GALLERY_DIR = "GalleryDir";

  // admin utility page header and footer
  private static Page UTIL_PAGE = null;
  private static String UTIL_FOOTER = null;

  static {
	try {
	  UTIL_PAGE = new Page(ChatObject.fileToString(ChatObject.htmlFile("messageheader.html")));
	  UTIL_FOOTER = ChatObject.fileToString(ChatObject.htmlFile("messagefooter.html"));
	} catch (ChatException e) {
	  ErrorLog.error(e, 501, "error reading utility page header or footer");
	  if (UTIL_PAGE == null)
		UTIL_PAGE = new Page();
	  if (UTIL_FOOTER == null)
		UTIL_FOOTER = "";
	}
  }


  public static HTML makeDirectoryPulldown(File dir) {
	if (!dir.isDirectory())
	  return new Container("CUSTOMCHATERROR",
							  "<!--File is not a directory: " + dir.getPath() + ".-->");

	String[] names = dir.list();
	if (names.length <= 0)
	  return new Container("CUSTOMCHATERROR",
							  "<!--Directory contains no files of the correct types-->");

	Container script = new Container("SCRIPT");
	script.addHTML("\n<!--\nvar winRef = null;\n"
		+ "function openGallery(dir) {\n"
		+ "  if(winRef && !winRef.closed) \n"
		+ "     winRef.close();\n"
		+ "  winRef = open(\"" + ChatObject.getRoot().commandURL(ChatObject.GALLERY_PAGE)
		+                         "&" + VAR_GALLERY_DIR + "="
		+                         "images/backgrounds/\" + dir + \"/&\" + (new Date()).getTime(),\n"
		    + "                       \"other_win\",\"menubar=no,scrollbars=yes,"
		+                         "resizable=yes,width=680,height=450\");\n"
		+ "  winRef.focus(); \n"
		+ "  return false;\n"
		+ "}\n//-->");

	PullDown pd = new PullDown("Gallery");
	pd.addArgument("onCHANGE", "openGallery(this.options[this.selectedIndex].value);");
	pd.addHTML(script);
	pd.addOption("Choose One") ;
	for (int i = 0; i < names.length; i++) {
	  pd.addOption(names[i]);
	}

	return pd;
  }      
  public static Page makeGalleryPage(File dir, String baseURL, String sFieldName)
  throws IOException {
	if(!dir.isDirectory())
	  throw new IOException("File is not a directory:" + dir.getPath());

	String[] names = dir.list();
	if(names.length <= 0)
	  throw new IOException("Directory contains no files of the correct types");

	Container c = new Container("CENTER");
	Container t = new Container("TABLE");
	c.addHTML(t);

	for(int row = 0; (row * GALLERYPAGE_COLS) < names.length; row ++) {
	  t.addHTML("<TR>");
	  for(int col = 0; (col < GALLERYPAGE_COLS) && (row * GALLERYPAGE_COLS + col) < names.length; col ++) {
		t.addHTML("<TD>");
		t.addHTML("<A HREF=\"\" onClick=\"" + sFieldName + "='"
				+ baseURL + names[col + row * GALLERYPAGE_COLS] + "';"
				+ "self.close();\" >");
		t.addHTML(new Image(baseURL + names[col + row * GALLERYPAGE_COLS],
							names[col + row * GALLERYPAGE_COLS]));
		t.addHTML("</A></TD>\n");
	  }
	  t.addHTML("</TR>\n\n");
	}
	return makeUtilPage(c);
  }  
  public static Page makeUtilPage(Container ct) {
	Page p = (Page)UTIL_PAGE.clone();
	p.addHTML(ct);
	p.addHTML(UTIL_FOOTER);

	return p;
  }  
}
