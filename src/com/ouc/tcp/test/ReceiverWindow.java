package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

enum AckFlag {
    ORDERED, DUPLICATE, UNORDERED, IS_BASE
    // ORDERED: 接收到的包是按序的
    // DUPLICATE: 接收到的包是重复的
    // UNORDERED: 接收到的包提前送达，乱序
    // IS_BASE: 接收到的包是基序号的包，开始交付数据
}

public class ReceiverWindow {
    private final int size;
    private final ReceiverElem[] window;
    private int base; // 窗口的基序号，即下一个期望接收的包的序号


    public ReceiverWindow(int size) {
        this.size = size;
        this.window = new ReceiverElem[size];
        for (int i = 0; i < size; i++) {
            this.window[i] = new ReceiverElem();
        }
        this.base = 0;
    }

    private int getIdx(int seq) {
        return seq % size;
    }

    public int bufferPacket(TCP_PACKET packet) {
        int seq = (packet.getTcpH().getTh_seq() - 1) / packet.getTcpS().getData().length;
        if (seq >= base + size) {
            return AckFlag.UNORDERED.ordinal();
        }
        if (seq < base) {
            return AckFlag.DUPLICATE.ordinal();
        }
        window[getIdx(seq)].setElem(packet, ReceiverFlag.BUFFERED.ordinal());
        if (seq == base) {
            return AckFlag.IS_BASE.ordinal();
        }
        return AckFlag.ORDERED.ordinal();
    }

    public TCP_PACKET getPacketToDeliver() {
        if (!window[getIdx(base)].isBuffered()) {
            return null;
        }

        TCP_PACKET packet = window[getIdx(base)].getPacket();
        window[getIdx(base)].resetElem();
        base++;
        return packet;
    }
}
