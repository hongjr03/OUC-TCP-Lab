package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

enum SenderFlag {
    NOT_ACKED, ACKED
}

public class SenderElem extends WindowElem{

    public SenderElem() {
        super();
    }

    public SenderElem(TCP_PACKET packet, int flag) {
        super();
        this.packet = packet;
        this.flag = flag;
    }

    public boolean isAcked() {
        return flag == SenderFlag.ACKED.ordinal();
    }

    public void ackPacket() {
        this.flag = SenderFlag.ACKED.ordinal();
    }
}
