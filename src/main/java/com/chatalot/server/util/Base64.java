package com.chatalot.server.util;

public class Base64 {

  private static char base64Table(char base64char)
  {
	if (( base64char >= 'A' ) && ( base64char <= 'Z' ))
	  return (char)(base64char - 'A');
	if (( base64char >= 'a' ) && ( base64char <= 'z' ))
	  return (char)(base64char - 'a' + 26);
	if (( base64char >= '0' ) && ( base64char <= '9' ))
	  return (char)(base64char - '0' + 52);
	if ( base64char == '+' )
	  return 62;
	if ( base64char == '/' )
	  return 63;
	return 0;
  }  
  private static char base64Table(int base64int)
  {
	if (( base64int >= 0 ) && ( base64int <= 25 ))
	  return (char)('A' + base64int);
	if (( base64int >= 26 ) && ( base64int <= 51 ))
	  return (char)(base64int - 26 + 'a');
	if (( base64int >= 52 ) && ( base64int <= 61 ))
	  return (char)(base64int - 52 + '0');
	if ( base64int == 62 )
	  return '+';
	if ( base64int == 63 )
	  return '/';
	return 0;
  }  
  public static String decode(String encodedString)
  {
	char[] newCharArray = new char[256];
	int newCharIndex = 0;
	char[] charArray = encodedString.toCharArray();
	for ( int i = 0; i < charArray.length; )
	  {
	char ithChar = charArray[i];
	char iPlusOneChar = charArray[i+1];
	if ( ithChar == '=' || iPlusOneChar == '=' )
	  break;
	char curChar = base64Table(ithChar);
	curChar <<= 2;
	curChar &= 127;
	char nextChar = base64Table(iPlusOneChar);
	nextChar >>>= 4;
	newCharArray[newCharIndex++] = (char)(curChar | nextChar);

	ithChar = iPlusOneChar;
	iPlusOneChar = charArray[i+2];
	if ( iPlusOneChar == '=' )
	  break;
	curChar = base64Table(ithChar);
	curChar <<= 4;
	curChar &= 127;
	nextChar = base64Table(iPlusOneChar);
	nextChar >>>= 2;
	newCharArray[newCharIndex++] = (char)(curChar | nextChar);

	ithChar = iPlusOneChar;
	iPlusOneChar = charArray[i+3];
	if ( iPlusOneChar == '=' )
	  break;
	curChar = base64Table(ithChar);
	curChar <<= 6;
	curChar &= 127;
	nextChar = base64Table(iPlusOneChar);
	newCharArray[newCharIndex++] = (char)(curChar | nextChar);

	i += 4;
	  }
	return new String(newCharArray, 0, newCharIndex);
  }  
  public static String encode(String userName, String password)
  {
	char[] newCharArray = new char[256];
	int newCharIndex = 0;
	String target = userName + ":" + password;
	char[] charArray = target.toCharArray();
	for ( int i = 0; i < charArray.length; )
	  {
	char ithChar = charArray[i];
	newCharArray[newCharIndex++] = base64Table((int)(ithChar >> 2));

	char iPlusOneChar = '\0';
	if ( (i + 1) < charArray.length )
	  iPlusOneChar = charArray[i+1];
	ithChar &= 3;
	ithChar <<= 4;
	ithChar |= (iPlusOneChar >>> 4);
	newCharArray[newCharIndex++] = base64Table((int)ithChar);

	if ( iPlusOneChar == '\0' )
	  {
	    newCharArray[newCharIndex++] = '=';
	    newCharArray[newCharIndex++] = '=';
	    break;
	  }

	ithChar = iPlusOneChar;
	iPlusOneChar = '\0';
	if ( (i + 2) < charArray.length )
	  iPlusOneChar = charArray[i+2];
	ithChar &= 15;
	ithChar <<= 2;
	ithChar |= (iPlusOneChar >>> 6);
	newCharArray[newCharIndex++] = base64Table((int)ithChar);

	if ( iPlusOneChar == '\0' )
	  {
	    newCharArray[newCharIndex++] = '=';
	    break;
	  }

	newCharArray[newCharIndex++] = base64Table((int)(iPlusOneChar & 63));

	i += 3;
	  }
	// while ( newCharIndex <
	return new String(newCharArray, 0, newCharIndex);
  }  
}
