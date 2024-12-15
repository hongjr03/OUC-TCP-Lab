package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

enum Flag {
    EMPTY, WAIT, ACKED
    // EMPTY: 窗口中的元素为空
    // WAIT: 窗口中的元素还未被确认
    // ACKED: 窗口中的元素已经被发送且已经被确认
}

public class SenderElem {
    private TCP_PACKET packet;
    private int flag;
    private UDT_Timer timer;

    public SenderElem() {
        this.packet = null;
        this.flag = Flag.EMPTY.ordinal();
        this.timer = null;
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

    public int getFlag() {
        return flag;
    }

    public void newTimer() {
        this.timer = new UDT_Timer();
    }

    public void scheduleTimer(UDT_RetransTask retransTask, int delay, int period) {
        this.timer.schedule(retransTask, delay, period);
    }

    public void cancelTimer() {
        this.timer.cancel();
    }
}