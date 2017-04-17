package customchat.htmlutil;

import customchat.util.*;
import java.util.*;

public class HTMLDate extends HTML {
  private PullDown hour;
  private PullDown day;
  private PullDown month;
  private PullDown year;
  private static final int MAXYEAR;
  private static final int MINYEAR;

  private static final String  january   = "Jan.";
  private static final String  february  = "Feb.";
  private static final String  march     = "Mar.";
  private static final String  april     = "Apr.";
  private static final String  may       = "May";
  private static final String  june      = "June";
  private static final String  july      = "July";
  private static final String  august    = "Aug.";
  private static final String  september = "Sept.";
  private static final String  october   = "Oct.";
  private static final String  november  = "Nov.";
  private static final String  december  = "Dec.";

  // Set the MAXYEAR and MINYEAR Variable
  static {
	int temp2 = Calendar.getInstance().get(Calendar.YEAR);  // default MINYEAR
	int temp1 = temp2 + 10;  // default MAXYEAR

	if(System.getProperty("chat.maxyear") != null)
	try {
	  temp1 = Integer.parseInt(System.getProperty("chat.maxyear"));
	} catch (NumberFormatException e) {
	  ErrorLog.error(e, 200, "Could not set MAXYEAR");
	} catch (NullPointerException ignore) {/* no argument set do nothing*/}

	if(System.getProperty("chat.minyear") != null)
	try {
	  temp2 = Integer.parseInt(System.getProperty("chat.minyear"));
	} catch (NumberFormatException e) {
	  ErrorLog.error(e, 201, "Could not set MINYEAR");
	} catch (NullPointerException ignore) {/* no argument set do nothing*/}

	MAXYEAR = Math.max(temp1, temp2);
	MINYEAR = Math.min(temp1, temp2);
  }

  public HTMLDate(String nameBase) {
	this( nameBase, Calendar.getInstance());
  }  
  public HTMLDate(String nameBase, Calendar c) {

	// Make the day pulldown
	day = new PullDown(nameBase + "_DAY");
	for(int i = 1; i <= 31; i++) {
	  day.addOption(String.valueOf(i), String.valueOf(i),   c.get(Calendar.DAY_OF_MONTH) == i);
	}

	// Make the month pulldown
	month = new PullDown(nameBase + "_MONTH");
	month.addOption(january,    String.valueOf(Calendar.JANUARY),   c.get(Calendar.MONTH) == Calendar.JANUARY);
	month.addOption(february,   String.valueOf(Calendar.FEBRUARY),  c.get(Calendar.MONTH) == Calendar.FEBRUARY);
	month.addOption(march,      String.valueOf(Calendar.MARCH),     c.get(Calendar.MONTH) == Calendar.MARCH);
	month.addOption(april,      String.valueOf(Calendar.APRIL),     c.get(Calendar.MONTH) == Calendar.APRIL);
	month.addOption(may,        String.valueOf(Calendar.MAY),       c.get(Calendar.MONTH) == Calendar.MAY);
	month.addOption(june,       String.valueOf(Calendar.JUNE),      c.get(Calendar.MONTH) == Calendar.JUNE);
	month.addOption(july,       String.valueOf(Calendar.JULY),      c.get(Calendar.MONTH) == Calendar.JULY);
	month.addOption(august,     String.valueOf(Calendar.AUGUST),    c.get(Calendar.MONTH) == Calendar.AUGUST);
	month.addOption(september,  String.valueOf(Calendar.SEPTEMBER), c.get(Calendar.MONTH) == Calendar.SEPTEMBER);
	month.addOption(october,    String.valueOf(Calendar.OCTOBER),   c.get(Calendar.MONTH) == Calendar.OCTOBER);
	month.addOption(november,   String.valueOf(Calendar.NOVEMBER),  c.get(Calendar.MONTH) == Calendar.NOVEMBER);
	month.addOption(december,   String.valueOf(Calendar.DECEMBER),  c.get(Calendar.MONTH) == Calendar.DECEMBER);

	// Make the year pulldown
	year = new PullDown(nameBase + "_YEAR");
	for(int i = MINYEAR ; i < MAXYEAR ; i ++) {
	  year.addOption(String.valueOf(i), String.valueOf(i),   c.get(Calendar.YEAR) == i);
	}
  }  
  public static Calendar getCalendar(String nameBase, LookupTable l)
  throws NumberFormatException {
	Calendar ret = Calendar.getInstance();
	try {
	  int iDay   = Integer.parseInt(l.getValue(nameBase + "_DAY"));
	  int iMonth = Integer.parseInt(l.getValue(nameBase + "_MONTH"));
	  int iYear  = Integer.parseInt(l.getValue(nameBase + "_YEAR"));
	  ret.set(iYear, iMonth, iDay, 0, 0);
	} catch(NumberFormatException e) {
	  ErrorLog.error(e, 202, "Could not parse date information in HTMLDate.getCalendar()");
	  throw e;
	}
	return ret;
  }  

  public static Calendar getCalendarDate(int d,int m,int y)
  throws NumberFormatException {
	Calendar ret = Calendar.getInstance();
	try {
	  ret.set(y, m, d, 0, 0);
	} catch(NumberFormatException e) {
	  ErrorLog.error(e, 202, "Could not parse date information in HTMLDate.getCalendar()");
	  throw e;
	}
	return ret;
  }  


  public String toText() {
	StringBuffer sb = new StringBuffer(200);
	sb.append(day.toString());
	sb.append(month.toString());
	sb.append(year.toString());
	return sb.toString();
  }  
}
