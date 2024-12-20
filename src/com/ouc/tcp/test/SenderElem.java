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
    private UDT_Timer timer;

    public SenderElem() {
        this.packet = null;
        this.flag = SenderFlag.NOT_ACKED.ordinal();
        this.timer = null;
    }

    public boolean isAcked() {
        return flag == SenderFlag.ACKED.ordinal();
    }

    public void resetElem() {
        this.packet = null;
        this.flag = SenderFlag.NOT_ACKED.ordinal();
    }

    public TCP_PACKET getPacket() {
        return packet;
    }

    public void setElem(TCP_PACKET packet) {
        try {
            this.packet = packet.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        this.flag = SenderFlag.NOT_ACKED.ordinal();
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
