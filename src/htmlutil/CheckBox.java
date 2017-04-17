package customchat.htmlutil;

public class CheckBox extends Input {
  public static final String TRUE = "TRUE";
  public static final String FALSE = "FALSE";

  public CheckBox(String name, boolean checked) {
	super(Input.CHECKBOX, name, checked? TRUE : FALSE);
	if(checked)
	  addArgument(Flag.CHECKED);
  }  
}
