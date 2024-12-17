package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.io.IOException;
import java.util.logging.*;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

public class SenderWindow {
    private final LinkedBlockingDeque<SenderElem> window;

    private int cwnd = 1;
    private int ssthresh = 16;
    private int nextAck = 0; // 拥塞避免，下一次增大窗口时收到的 ack

    private UDT_Timer timer;
    private final int delay = 1000;
    private final int period = 1000;
    private final TCP_Sender sender;
    private int lastAck = -1;
    private int lastAckCount = 0;
    private final int lastAckCountLimit = 3;

    public static class GBN_RetransTask extends TimerTask {
        private final SenderWindow window;

        public GBN_RetransTask(SenderWindow window) {
            this.window = window;
        }

        public void run() {
            window.sendWindow();
        }
    }

    public void resetTimer() {
        timer.cancel();
        timer = new UDT_Timer();
        if (!isEmpty()) {
            timer.schedule(new GBN_RetransTask(this), delay, period);
        }
    }

    public SenderWindow(TCP_Sender sender) {
        this.sender = sender;
        this.window = new LinkedBlockingDeque<>();
        this.timer = new UDT_Timer();
    }

    public boolean isCwndFull() {
        return window.size() >= cwnd;
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public void pushPacket(TCP_PACKET packet) {
        // 如果窗口为空，启动定时器
        if (isEmpty()) {
            timer = new UDT_Timer();
            timer.schedule(new GBN_RetransTask(this), delay, period);
        }
        window.push(new SenderElem(packet, SenderFlag.NOT_ACKED.ordinal()));
    }

    public void sendWindow() {
        // 发送窗口中的数据
        for (SenderElem elem : window) {
            if (!elem.isAcked()) {
                sender.udt_send(elem.getPacket());
            }
        }
    }

    private void resendPacket(int ack) {
        for (SenderElem elem : window) {
            if (elem.getPacket().getTcpH().getTh_seq() > ack) {
                sender.udt_send(elem.getPacket());
                break;
            }
        }
    }

    public void setPacketAcked(int ack, int length) {
        for (SenderElem elem : window) {
            if (elem.getPacket().getTcpH().getTh_seq() <= ack) {
                elem.setAcked();
                window.remove(elem);
                continue;
            }
            break;
        }

        resetTimer();

        if (cwnd < ssthresh) {
            cwnd++;
        } else if (ack >= nextAck) {
            nextAck += cwnd * length;
            cwnd++;
        }

        if (ack == lastAck) {
            lastAckCount++;
        } else {
            lastAck = ack;
            lastAckCount = 1;
        }

        if (lastAckCount >= lastAckCountLimit) {
            ssthresh = Math.max(cwnd / 2, 1);
            cwnd = 1;
            resendPacket(ack);
        }


    }

}
