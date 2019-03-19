package customchat.htmlutil;

public class Body extends Container {

  public Body() {
	super("BODY");
  }  
  public Body(String BgColor, String TextColor, String LinkColor, String VLinkColor, String BgURL) {
	this();
	if(BgColor != null) this.addArgument("BGCOLOR", BgColor);
	if(TextColor != null) this.addArgument("TEXT", TextColor);
	if(LinkColor != null) this.addArgument("LINK", LinkColor);
	if(VLinkColor != null) this.addArgument("VLINK", VLinkColor);
	if(BgURL != null) this.addArgument("BACKGROUND", BgURL);
  }  


}
