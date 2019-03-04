package customchat.chat;

import customchat.util.ErrorLog;
import java.util.*;

public class Registry {
  Hashtable reg = new Hashtable(100);
  public Registry() {}  
  public synchronized void add(String keyWord, ChatObject co)
	  throws ChatException {
	if(reg.containsKey(keyWord))
	  throw new ChatException("Key Name \"" + keyWord + "\" is already in use.");
	reg.put(keyWord, co);
  }  
  public ChatObject get(String keyWord) throws ChatException{
	ChatObject co = (ChatObject)reg.get(keyWord);
	if(co == null)
	  throw new ChatException("Key \"" + keyWord + "\" is not a valid key");
	return co;
  }  
  public synchronized void remove(String keyWord) {
	if(!reg.containsKey(keyWord))
	  ErrorLog.error(new ChatException(), 300,
	  "Tried to remove keyWord \"" + keyWord + "\" from the registry that was not there.");
	reg.remove(keyWord);
  }  
}
