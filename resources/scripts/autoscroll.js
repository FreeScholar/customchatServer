/*
 * @author JamesVorder
 * 11-MAR-2019 (Moved here from AutoScroll.java)
 * @description This stuff gets used to initialize the live chat page.
 */
//var autoScrollOn = true;
//var scrollOnFunction;
//var scrollOffFunction;
//
//setInterval(function () {
//    if (autoScrollOn === true) {
//        window.scroll(0, 65000);
//    }
//}, 200);
//
//var scrollOn = function () {
//    autoScrollOn = true;
//}  // end scrollOn
//
//var scrollOff = function () {
//    autoScrollOn = false;
//}  // end scrollOff
//
////var source = new EventSource(this.chatRoom);
//
//var StartUp = function () {
//    this.onblur = scrollOn;
//    this.onfocus = scrollOff;
//}  // end StartUp
//
//var onLoad = function () {
//    alert("You have timed out of the room. " +
//            "Hit Reload or Refresh on your browser to re-enter the chat.")
//}

//this.StartUp();
var url = "http://" + window.location.host + this.chatRoom;
//console.log(url);
$(document).ready(function(event){
    setInterval(function(){
        $.ajax({
            type: 'GET',
            url: url,
            async: true,
            contentType: 'application/json',
            done: function(err){
                console.log(err);
            },
            success: function(res){
                //console.log(res);
                if(res.data.priv == undefined || res.data.pub == undefined){
                    $("#room-messages-frame").prepend(res.data);
                } else{
                    var privatePreamble = "<i>(private)</i>\t";
                    var private = (res.data.priv != "") ? JSON.parse(res.data.priv) : [];
                    //console.log("PRIVATE: " + private + " (Count: " + private.length + ")");
                    var publicPreamble = "<i>(public)</i>\t";
                    var public = (res.data.pub != "") ? JSON.parse(res.data.pub) : [];
                    //console.log("PUBLIC: " + public + " (Count: " + private.length + ")")
                    if(private.length > 0){
                        for(var i = 0; i < private.length; i++){
                            //console.log(private[i]);
                            $("#room-messages-frame").append(privatePreamble);
                            $("#room-messages-frame").append(
                                private[i].from + " : " + private[i].message + "<br>");
                        }
                    }
                    if(public.length > 0){
                        for(var i = 0; i < public.length; i++){
                            //console.log(public[i]);
                            $("#room-messages-frame").append(publicPreamble);
                            $("#room-messages-frame").append(
                                public[i].from + " : " + public[i].message + "<br>");
                        }
                    }
                    $("#room-messages-frame").scrollTop($("#room-messages-frame")[0].scrollHeight);
                }
            }
        });
    }, 1000);
});