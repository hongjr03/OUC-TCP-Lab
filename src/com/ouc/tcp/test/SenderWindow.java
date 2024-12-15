package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.message.TCP_PACKET;

public class SenderWindow {
    private int size;
    private SenderElem[] window;
    private int base;
    private int nextToSend; // 下一个要发送的元素的下标
    private int rear; // 窗口的最后一个元素的下标
    private Client client;

    public SenderWindow(Client client, int size) {
        this.client = client;
        this.size = size;
        this.window = new SenderElem[size];
        for (int i = 0; i < size; i++) {
            this.window[i] = new SenderElem();
        }
        this.base = 0;
        this.nextToSend = 0;
        this.rear = 0;
    }

    public boolean isFull() {
        return (rear + 1) % size == base;
    }

    public boolean isEmpty() {
        return base == rear;
    }

    public void pushPacket(TCP_PACKET packet) {
        window[rear].setPacket(packet);
        window[rear].setFlag(SenderFlag.READY.ordinal());
        rear = (rear + 1) % size;
    }

    public TCP_PACKET getPacketToSend(int delay, int period) {
        if (isEmpty() || nextToSend == rear) {
            return null;
        }
        TCP_PACKET pack = window[nextToSend].getPacket();
        window[nextToSend].newTimer();
        window[nextToSend].scheduleTimer(new UDT_RetransTask(client, pack), delay, period);
        nextToSend = (nextToSend + 1) % size;
        return pack;
    }

    public void setPacketConfirmed(int seq) {
        for (int i = base; i != rear; i = (i + 1) % size) {
            if (window[i].getPacket().getTcpH().getTh_seq() == seq) {
                window[i].cancelTimer();
                window[i].setFlag(SenderFlag.CONFIRMED.ordinal());
                break;
            }
        }
        while (base != rear && window[base].getFlag() == SenderFlag.CONFIRMED.ordinal()) {
            base = (base + 1) % size;
        }
    }

}
