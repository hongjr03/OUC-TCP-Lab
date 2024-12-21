package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.TimerTask;

public class SenderWindow {
    private int size;
    private SenderElem[] window;
    private int base;
    private int nextToSend; // 下一个要发送的元素的下标
    private int rear; // 窗口的最后一个元素的下标
    private UDT_Timer timer;
    private int delay;
    private int period;
    private TCP_Sender sender;

    public class GBN_RetransTask extends TimerTask {
        private TCP_Sender sender;
        private SenderWindow window;

        public GBN_RetransTask(TCP_Sender sender, SenderWindow window) {
            this.sender = sender;
            this.window = window;
        }

        public void run() {
            window.sendWindow();
        }
    }

    public void resetTimer() {
        timer.cancel();
        timer = new UDT_Timer();
        timer.schedule(new GBN_RetransTask(sender, this), delay, period);
    }

    public SenderWindow(TCP_Sender sender, int size, int delay, int period) {
        this.sender = sender;
        this.size = size;
        this.window = new SenderElem[size];
        for (int i = 0; i < size; i++) {
            this.window[i] = new SenderElem();
        }
        this.base = 0;
        this.nextToSend = 0;
        this.rear = 0;
        this.timer = new UDT_Timer();
        this.delay = delay;
        this.period = period;
    }

    private int getIdx(int seq) {
        return seq % size;
    }

    public boolean isFull() {
        return rear - base == size;
    }

    private boolean isEmpty() {
        return base == rear;
    }

    private boolean isAllSent() {
        return nextToSend == rear;
    }

    private boolean atBase() {
        return nextToSend == base;
    }

    public void pushPacket(TCP_PACKET packet) {
        int idx = getIdx(rear);
        window[idx].setElem(packet, SenderFlag.NOT_ACKED.ordinal());
        rear++;
    }

    public void sendWindow() {
        nextToSend = base;
        while (nextToSend < rear) {
            sendPacket();
        }
    }

    public void sendPacket() {
        if (isEmpty() || isAllSent()) {
            // 窗口为空或者窗口中的所有元素都已经发送
            return;
        }

        int idx = getIdx(nextToSend);
        TCP_PACKET pack = window[idx].getPacket();

        // 如果是第一个元素，启动定时器
        if (atBase()) {
            timer.schedule(new GBN_RetransTask(sender, this), delay, period);
        }

        nextToSend++;
        sender.udt_send(pack);
    }

    public void ackPacket(int seq) {
        for (int i = base; i != rear; i++) {
            int idx = getIdx(i);
            if (window[idx].getPacket().getTcpH().getTh_seq() <= seq && !window[idx].isAcked()) {
                window[idx].ackPacket();
                window[idx].resetElem();
                base++;
                resetTimer();
            }
        }
    }

}
