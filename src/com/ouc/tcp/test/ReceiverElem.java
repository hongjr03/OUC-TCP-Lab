package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

enum ReceiverFlag {
    WAIT, BUFFERED
    // WAIT: 等待接收或已经确认
    // BUFFERED: 已经接收但还未确认
}

public class ReceiverElem {
    private TCP_PACKET packet;
    private int flag;

    public ReceiverElem() {
        this.packet = null;
        this.flag = ReceiverFlag.WAIT.ordinal();
    }

    public boolean isBuffered() {
        return flag == ReceiverFlag.BUFFERED.ordinal();
    }

    public void reset() {
        this.packet = null;
        this.flag = ReceiverFlag.WAIT.ordinal();
    }

    public void recvPacket(TCP_PACKET packet) {
        this.packet = packet;
        this.flag = ReceiverFlag.BUFFERED.ordinal();
    }

    public TCP_PACKET getPacket() {
        try {
            return packet.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
