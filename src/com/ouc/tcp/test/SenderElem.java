package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

enum SenderFlag {
    NOT_ACKED, ACKED
}

public class SenderElem extends WindowElem{
    private UDT_Timer timer;

    public SenderElem() {
        super();
        this.timer = null;
    }

    public SenderElem(TCP_PACKET packet, int flag) {
        super();
        this.packet = packet;
        this.flag = flag;
    }

    public boolean isAcked() {
        return flag == SenderFlag.ACKED.ordinal();
    }

    public void scheduleTask(UDT_RetransTask retransTask, int delay, int period) {
        this.timer = new UDT_Timer();
        this.timer.schedule(retransTask, delay, period);
    }

    public void ackPacket() {
        this.flag = SenderFlag.ACKED.ordinal();
        this.timer.cancel();
    }
}
