package com.ouc.tcp.test;

enum ReceiverFlag {
    WAIT, BUFFERED
    // WAIT: 等待接收或已经确认
    // BUFFERED: 已经接收但还未确认
}

public class ReceiverElem extends WindowElem {

    public ReceiverElem() {
        super();
    }

    public boolean isBuffered() {
        return flag == ReceiverFlag.BUFFERED.ordinal();
    }

}