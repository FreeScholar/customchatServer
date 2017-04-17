package customchat.htmlutil;

public class Input extends Tag {
    public static final short HIDDEN = 0;
    public static final short CHECKBOX = 1;
    public static final short RADIO = 2;
    public static final short TEXT = 3;
    public static final short SUBMIT = 4;
    public static final short RESET = 5;
    public static final short IMAGE = 6;
    public static final short PASSWORD = 7;
    public static final short BUTTON = 8;
    public static final String[] TYPES = {"HIDDEN",
					  "CHECKBOX",
					  "RADIO",
					  "TEXT",
					  "SUBMIT",
					  "RESET",
					  "IMAGE",
					  "PASSWORD",
					  "BUTTON"};


    public Input(short type, String name, String value) {
	super("INPUT");
	addArgument("TYPE",TYPES[type]);
	addArgument("NAME",name);
	addArgument("VALUE",value);
	if(type == IMAGE)
	    addArgument("BORDER", "0");
    }  
    protected String toText() {
	return "<FONT SIZE=2>" + super.toText() + "</FONT>";
    }  
}
