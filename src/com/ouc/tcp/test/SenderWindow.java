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
    private int delay = 1000;
    private int period = 1000;

    public class GBN_RetransTask extends TimerTask {
        private TCP_Sender sender;
        private SenderWindow window;

        public GBN_RetransTask(TCP_Sender sender, SenderWindow window) {
            this.sender = sender;
            this.window = window;
        }

        public void run() {
            window.sendWindow(sender);
        }
    }

    public void resetTimer(TCP_Sender sender) {
        timer.cancel();
        timer = new UDT_Timer();
        timer.schedule(new GBN_RetransTask(sender, this), delay, period);
    }

    public SenderWindow(TCP_Sender sender, int size) {
        this.size = size;
        this.window = new SenderElem[size];
        for (int i = 0; i < size; i++) {
            this.window[i] = new SenderElem();
        }
        this.base = 0;
        this.nextToSend = 0;
        this.rear = 0;
        this.timer = new UDT_Timer();
    }

    private int getIdx(int seq) {
        return seq % size;
    }

    public boolean isFull() {
        return rear - base == size;
    }

    public boolean isEmpty() {
        return base == rear;
    }

    public boolean isAllSent() {
        return nextToSend == rear;
    }

    public boolean atBase() {
        return nextToSend == base;
    }

    public void pushPacket(TCP_PACKET packet) {
        int idx = getIdx(rear);
        window[idx].setPacket(packet);
        window[idx].setFlag(SenderFlag.NOT_ACKED.ordinal());
        rear++;
    }

    public void sendWindow(TCP_Sender sender) {
        nextToSend = base;
        while (nextToSend < rear) {
            sendPacket(sender);
        }
    }

    public void sendPacket(TCP_Sender sender) {
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

    public void setPacketAcked(TCP_Sender sender, int ack) {
        int idx = getIdx(base);
        while (
                window[idx].getPacket().getTcpH().getTh_seq() <= ack && !window[idx].isAcked() && base < rear
        ) {
            window[idx].setAcked();
            base++;
            idx = getIdx(base);
        }

        resetTimer(sender);
    }

}
