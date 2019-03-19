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

var StartUp = function () {
    this.onblur = scrollOn;
    this.onfocus = scrollOff;
}  // end StartUp

var onLoad = function () {
    alert("You have timed out of the room. " +
            "Hit Reload or Refresh on your browser to re-enter the chat.")
}

this.StartUp();
//window.onload = this.onLoad();