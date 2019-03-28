package com.chatalot.server.chat;

public class AutoScrollException extends ChatException {
    Chatter c;
    boolean printHeader;

    AutoScrollException(Chatter c, boolean printHeader) {
        this.c = c;
        this.printHeader = printHeader;
    }
    public Chatter getChatter() {
        return c;
    }
}
