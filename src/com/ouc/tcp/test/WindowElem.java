package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

public class WindowElem {
    protected TCP_PACKET packet;
    protected int flag;

    public WindowElem() {
        packet = null;
        flag = 0;
    }

    public TCP_PACKET getPacket() {
        return packet;
    }

    public void setElem(TCP_PACKET packet, int flag) {
        this.packet = packet;
        this.flag = flag;
    }

    public void resetElem() {
        packet = null;
        flag = 0;
    }
}

