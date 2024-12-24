package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

public class SenderWindow {
    private final LinkedBlockingDeque<SenderElem> window;

    private int size = 16;
    private UDT_Timer timer;
    private final int delay = 3000;
    private final int period = 3000;
    private final TCP_Sender sender;

    public class GBN_RetransTask extends TimerTask {
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
        return window.size() >= size;
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

    public void sendPacket() {
        SenderElem elem = window.poll();
        if (elem == null) {
            return;
        }
        if (!elem.isAcked()) {
            sender.udt_send(elem.getPacket());
        }
        window.push(elem);
    }

    private void resendPacket(int ack) {
        int expectedAck = ack + 100;
        for (SenderElem elem : window) {
            if (elem.getPacket().getTcpH().getTh_seq() == expectedAck) {
                sender.udt_send(elem.getPacket());
                break;
            }
        }
    }

    public void ackPacket(int ack) {
        for (SenderElem elem : window) {
            if (elem.getPacket().getTcpH().getTh_seq() <= ack) {
                elem.ackPacket();
                window.remove(elem);
                resetTimer();
            }
        }
    }
}

