package customchat.chat;

import java.sql.*;
import customchat.util.*;

public class Auth {
  private String sURL,
				 sUser,
				 sPassword;
  java.sql.Connection connection = null;
  Statement statement;
  ResultSet result;

  public Auth(String sURL, String sUser, String sPassword) {
	try {
	  this.sURL = sURL;
	  this.sUser = sUser;
	  this.sPassword = sPassword;

	  Class.forName("com.imaginary.sql.msql.MsqlDriver");
	  //Class.forName("symantec.itools.db.jdbc.Driver");
	  connection = DriverManager.getConnection(sURL, sUser, sPassword);

	} catch(java.sql.SQLException e) {
	  ErrorLog.error(e, 1, "sql problem 1: ");
	} catch(ClassNotFoundException e) {
	  ErrorLog.error(e, 2, "class not found: ");
	}
  }  
  public String BannedMsg(long iEmailID) {
	try {
	  statement = connection.createStatement();
	  result = statement.executeQuery("SELECT Message, Until FROM Banned WHERE EmailID = " +
				      new Long(iEmailID) + "AND Until > _sysdate");
	  if(!result.next()) {
		statement.close();
		return null;
	  }
	  String s = result.getString(1) + "<BR>You will be able to re enter Chatalot after " +
	result.getDate(2); 
	  statement.close();
	  return s;
	} catch(java.sql.SQLException e) {
	  ErrorLog.error(e, 4, "sqlProblem:");
	}
	return null;
  }  
  public AuthInfo Verify(Login lUser) throws DataBaseException {
	try {
	  //look up user
	  statement = connection.createStatement();
	  result = statement.executeQuery("SELECT ID FROM Email " +
				      "WHERE  Login = '" + msqlEncode(lUser.getLogin()) +
				      "' AND Password = '" + msqlEncode(lUser.getPassPhrase()) + "'");
	  if( !result.next() ) {
	      //not found--unauthorized
	      statement.close();
	      throw new RecordNotFoundException("Either the Username or the Passphrase you provided was incorrect.");
	  }
	  long lID = result.getLong(1);
	
	  statement.close();
	  statement = connection.createStatement();
	  result = statement.executeQuery("SELECT Handle, ID FROM Handle " +
				      "WHERE EmailID = " + new Long(lID) );

	  String s[] = new String[4];
	  long l[] = new long[4];
	  for(int i=0; i<4; i++) {
	      if(!result.next()) {
	        s[i] = null;
	        l[i] = -1;
	      } else {
	        s[i] = result.getString(1);
	        l[i] = result.getLong(2);
	      }
	  }
	  statement.close();
	  if(s[0]==null)
	      throw new NoHandlesException("You have no registered Handles.  Please add one before chatting.");
	  
	  return new AuthInfo(s, l, lID);
	} catch (java.sql.SQLException e) {
	  ErrorLog.error(e, 5, "sql problem 2: ");
	  throw new DataBaseDownException("SQL Error : " + e);
	} catch (NullPointerException e) {
	  throw new DataBaseDownException("Connection Failed...");
	}
  }  
  //returns users level or negative one if not found
  public  String Verify(String sPrimary, String sPassword, int iLevel, int iID) {
	try {
	  statement = connection.createStatement();
	  result = statement.executeQuery("SELECT Handle.Handle FROM Handle, Email " +
				      "WHERE Handle.EmailID = Email.ID AND " +
				      "Email.Password = '" + msqlEncode(sPassword) +
				      "' AND Email.Login = '" + msqlEncode(sPrimary) +
				      "' AND Email.MLevel = " + new Integer(iLevel) +
				      " AND Handle.ID = " + new Integer(iID));
	  if(!result.next()) {
	statement.close();
	return null;
	  }
	  String s = result.getString(1);
	  statement.close();
	  return s;
	} catch(java.sql.SQLException e) {
	  ErrorLog.error(e, 3, "sqlProblem: ");
	}
	return null;
  }  
  String msqlEncode(String s) {
	int i = 0;

	while((i=s.indexOf('\\', i)) > -1) {
	  s = s.substring(0, i) + '\\' + s.substring(i);
	  i += 2;
	}

	while((i=s.indexOf('\'', i)) > -1) {
	  s = s.substring(0, i) + '\\' + s.substring(i);
	  i += 2;
	}
	  
	return s;
  }  
}
