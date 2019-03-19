
helpstat = false;
stprompt = true;
basic = false;
fname = "AddText" ;

var head="display:''"


function doit(header){
var head=header.style
if (helpstat) {
head.display=""
}
else if (basic) {
head.display=""
}
else if (stprompt) {
head.display=""
}
else {
head.display="none"
}
}

function xspand(header){
var head=header.style
if (head.display == "none") {
head.display=""
}
else {
head.display="none"
}
}

function thelp(swtch){
        if (swtch == 1){
                basic = false;
                stprompt = false;
                helpstat = true;
        } else if (swtch == 0) {
                helpstat = false;
                stprompt = false;
                basic = true;
        } else if (swtch == 2) {
                helpstat = false;
                basic = false;
                stprompt = true;
        } else {
                helpstat = false;
                basic = false;
                stprompt = false;
        }
}

function showsize(size,ibox) {
		fname = ibox ;
        if (helpstat) {
                alert("Size Tag\nSets the text size.\nPossible values are 1 to 6.\n 1 being the smallest and 3 the largest.\nUSE: <font size="+size+">This is size "+size+" text</font>");
        } else if (basic) {
                AddTxt="<font size="+size+"></font>";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be size "+size,"Text");
                if (txt!=null) {
                        AddTxt="<font size="+size+">"+txt+"</font>";
                        AddText(AddTxt);
                }
        }
}

function bold(ibox) {
		fname = ibox ;
        if (helpstat) {
                alert("Bold Tag\nMakes the enlosed text bold.\nUSE: <b>This is some bold text</b>");
        } else if (basic) {
                AddTxt="<B></B>";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be made BOLD.","Text");
                if (txt!=null) {
                        AddTxt="<B>"+txt+"</B>";
                        AddText(AddTxt);
                }
        }
}

function AddText(NewCode) {
	// document.REPLIER.EditedMessage.value+=NewCode
	if (fname == "RealTime") {
		document.Bottom.Public.value += NewCode ;
		document.Bottom.Public.focus() ;
	}
	if (fname == "Whisper") {
		document.Bottom.Private.value += NewCode ;
	}
	if (fname == "Speak") {
		document.Bottom.Public.value += NewCode ;

	}
}


function italicize(ibox) {
		fname = ibox
        if (helpstat) {
                alert("Italicize Tag\nMakes the enlosed text italicized.\nUSE: <i>This is some italicized text</i>");
        } else if (basic) {
                AddTxt="[i][/i]";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be italicized","Text");
                if (txt!=null) {
                        AddTxt="<i>"+txt+"</i>";
                        AddText(AddTxt);
                }
        }
}


function quote(ibox) {
		fname = ibox ;
        if (helpstat){
                alert("Quote tag\nQuotes the enclosed text to reference something specific that someone has posted. ***This function needs to be coded to pop a small window with a textarea for user to paste paragraphs of text.\nUSE: <quote>Coming Soon - paste a paragraph of text</quote> ***This function needs to be coded to pop a small window with a textarea for user to paste paragraphs of text - rows=10 wrap=VIRTUAL cols=45.");
        } else if (basic) {
                AddTxt="<quote></quote>";
                AddText(AddTxt);
        } else {
                txt=prompt("Coming Soon - paste a paragraph of text","Text");
                if(txt!=null) {
                        AddTxt="<quote>"+txt+"</quote>";
                        AddText(AddTxt);
                }
        }
}

function showcolor(color,ibox) {
		fname = ibox ;
        if (helpstat) {
                alert("Color Tag\nSets the text color.  Any named color can be used.\nUSE: <font color="+color+">This is some "+color+" text</font>");
        } else if (basic) {
                AddTxt="<font color="+color+"></font>";
                AddText(AddTxt);
        } else {
        txt=prompt("Text to be "+color,"Text");
                if(txt!=null) {
                        AddTxt="<font color="+color+">"+txt+"</font>";
                        AddText(AddTxt);
                }
        }
}

function center(ibox) {
		fname = ibox ;
        if (helpstat) {
                alert("Centered tag\nCenters the enclosed text.\nUSE: <center>This text is centered</center>");
        } else if (basic) {
                AddTxt="<center></center>";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be centered","Text");
                if (txt!=null) {
                        AddTxt="<center>"+txt+"</center>";
                        AddText(AddTxt);
                }
        }
}

function hyperlink(ibox) {
   	fname = ibox ;
     if (helpstat) {
                alert("Hyperlink Tag\nTurns an url into a hyperlink.\nUSE: <a href=http://www.anywhere.com</a>\nUSE: <a href=http://www.anywhere.com>link text</a>");
        } else if (basic) {
                AddTxt="<a TARGET=new href=></a>";
                AddText(AddTxt);
        } else {
                txt2=prompt("Text to be shown for the link.\nLeave blank if you want the url to be shown for the link.","");
                if (txt2!=null) {
                        txt=prompt("URL for the link.","http://");
                        if (txt!=null) {
                                if (txt2=="") {
                                        AddTxt="<a TARGET=new href=>"+txt+"</a>";
                                        AddText(AddTxt);
                                } else {
                                        AddTxt="<a TARGET=new href="+txt+">"+txt2+"</a>";
                                        AddText(AddTxt);
                                }
                        }
                }
        }
}

function image(ibox) {
	    fname = ibox ;
		if (helpstat){
                alert("Image Tag\nInserts an image into the post.\nUSE: [<IMG SRC=]http:\www.anywhere.comimage.gif[>]");
        } else if (basic) {
                AddTxt="[<IMG SRC=> border=0>]";
                AddText(AddTxt);
        } else {
                txt=prompt("URL for graphic","http://");
                if(txt!=null) {
                        AddTxt="<IMG SRC="+txt+">";
                        AddText(AddTxt);
                }
        }
}



function underline(ibox) {
		fname = ibox ;
        if (helpstat) {
                alert("Underline Tag\nUnderlines the enclosed text.\nUSE: <u>This text is underlined</u>");
        } else if (basic) {
                AddTxt="<u></u>";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be Underlined.","Text");
                if (txt!=null) {
                        AddTxt="<u>"+txt+"</u>";
                        AddText(AddTxt);
                }
        }
}

function showfont(font,ibox) {
		fname = ibox ;
        if (helpstat){
                alert("Font Tag\nSets the font face for the enclosed text.\nUSE: <font="+font+">The font of this text is "+font+"</font>");
        } else if (basic) {
                AddTxt="<font face="+font+"></font>";
                AddText(AddTxt);
        } else {
                txt=prompt("Text to be in "+font,"Text");
                if (txt!=null) {
                        AddTxt="<font face="+font+">"+txt+"</font>";
                        AddText(AddTxt);
                }
        }
}
