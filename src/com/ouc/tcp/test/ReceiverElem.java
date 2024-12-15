package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

enum ReceiverFlag {
    WAIT, BUFFERED, ACKED
    // WAIT: 等待接收
    // BUFFERED: 已经接收但还未确认
    // ACKED: 已经接收并确认
}

public class ReceiverElem {
    private TCP_PACKET packet;
    private int flag;

    public ReceiverElem() {
        this.packet = null;
        this.flag = ReceiverFlag.WAIT.ordinal();
    }

    public void reset() {
        this.packet = null;
        this.flag = ReceiverFlag.WAIT.ordinal();
    }

    public void setPacket(TCP_PACKET packet) {
        try {
            this.packet = packet.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public TCP_PACKET getPacket() {
        try {
            return packet.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFlag(int ordinal) {
        this.flag = ordinal;
    }

    public int getFlag() {
        return flag;
    }
}
