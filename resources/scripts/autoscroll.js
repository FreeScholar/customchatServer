/*
 * @author JamesVorder
 * 11-MAR-2019 (Moved here from AutoScroll.java)
 * @description This stuff gets used to initialize the live chat page.
 */
var autoScrollOn = true;
var scrollOnFunction;
var scrollOffFunction;

setInterval(function () {
    if (autoScrollOn === true) {
        window.scroll(0, 65000);
    }
}, 200);

var scrollOn = function () {
    autoScrollOn = true;
}  // end scrollOn

var scrollOff = function () {
    autoScrollOn = false;
}  // end scrollOff

//var source = new EventSource(this.chatRoom);

var StartUp = function () {
    this.onblur = scrollOn;
    this.onfocus = scrollOff;
}  // end StartUp

var onLoad = function () {
    alert("You have timed out of the room. " +
            "Hit Reload or Refresh on your browser to re-enter the chat.")
}

this.StartUp();
var url = "http://" + window.location.host + this.chatRoom;
console.log(url);
$(document).ready(function(event){
    $.ajax({
        type: 'GET',
        url: url,
        async: true,
        contentType: 'application/json',
        done: function(err){
            console.log(err);
        },
        success: function(res){
            console.log(res);
              $("#room-messages-frame").html(res.data);
            }
    });
});


window.onload = function(){
//   console.log("window loaded");
//    this.source.onmessage = function(event){
//        document.getElementById("room-messages-frame").innerHTML += event.data + "<br>";
//    }
//    this.source.onopen = function(event){
//        console.log("connection established.");
//        document.getElementById("room-messages-frame").innerHTML += "Connection established!" + "<br>";
//    }
//    setInterval(function () {
//
//    }, 1000);

}

//window.onload = this.onLoad();