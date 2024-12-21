package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;

enum SenderFlag {
    NOT_ACKED, ACKED
}

public class SenderElem extends WindowElem{

    public SenderElem() {
        super();
    }

    public boolean isAcked() {
        return flag == SenderFlag.ACKED.ordinal();
    }

    public void ackPacket() {
        this.flag = SenderFlag.ACKED.ordinal();
    }
}