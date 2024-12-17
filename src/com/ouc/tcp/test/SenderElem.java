package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

enum SenderFlag {
    NOT_ACKED, ACKED
}

public class SenderElem {
    private TCP_PACKET packet;
    private int flag;

    public SenderElem() {
        this.packet = null;
        this.flag = SenderFlag.NOT_ACKED.ordinal();
    }

    public SenderElem(TCP_PACKET packet, int flag) {
        this.packet = packet;
        this.flag = flag;
    }

    public void reset() {
        this.packet = null;
        this.flag = SenderFlag.NOT_ACKED.ordinal();
    }

    public TCP_PACKET getPacket() {
        return packet;
    }

    public void setPacket(TCP_PACKET packet) {
        try {
            this.packet = packet.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public boolean isAcked() {
        return flag == SenderFlag.ACKED.ordinal();
    }

    public void setAcked() {
        flag = SenderFlag.ACKED.ordinal();
    }
}
