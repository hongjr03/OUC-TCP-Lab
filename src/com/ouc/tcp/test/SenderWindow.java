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

    private int getIdx(int seq) {
        return seq % size;
    }

    public boolean isFull() {
//        return (rear + 1) % size == base;
        return rear - base == size;
    }

    public boolean isEmpty() {
        return base == rear;
    }

    public void pushPacket(TCP_PACKET packet) {
        int idx = getIdx(rear);
        window[idx].setPacket(packet);
        window[idx].setFlag(SenderFlag.READY.ordinal());
//        do {
        rear++;

//        } while (window[rear].getFlag() != SenderFlag.EMPTY.ordinal());
    }

    public TCP_PACKET getPacketToSend(int delay, int period) {
        if (isEmpty() || nextToSend == rear) {
            // 窗口为空或者窗口中的所有元素都已经发送
            return null;
        }
//        while (window[nextToSend].getFlag() != SenderFlag.READY.ordinal()) {
//            nextToSend = (nextToSend + 1) % size;
//            if (nextToSend == rear) {
//                return null;
//            }
//        }
        int idx = getIdx(nextToSend);
        TCP_PACKET pack = window[idx].getPacket();
        window[idx].newTimer();
        window[idx].scheduleTimer(new UDT_RetransTask(client, pack), delay, period);
        nextToSend++;
        return pack;
    }

    public void setPacketConfirmed(int seq) {
        for (int i = base; i != rear; i++) {
            int idx = getIdx(i);
            if (window[idx].getPacket().getTcpH().getTh_seq() == seq && window[idx].getFlag() == SenderFlag.READY.ordinal()) {
                window[idx].cancelTimer();
                window[idx].setFlag(SenderFlag.CONFIRMED.ordinal());
                break;
            }
        }
        while (base != rear && window[getIdx(base)].getFlag() == SenderFlag.CONFIRMED.ordinal()) {
            int idx = getIdx(base);
            window[idx].reset();
            base++;
        }
    }

}
