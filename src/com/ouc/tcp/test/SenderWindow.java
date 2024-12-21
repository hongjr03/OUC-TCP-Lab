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
    private double dCwnd = 1.0;
    private int ssthresh = 16;

    private UDT_Timer timer;
    private final int delay = 1000;
    private final int period = 1000;
    private final TCP_Sender sender;

    private int lastAck = -1;
    private int lastAckCount = 0;
    private final int lastAckCountLimit = 3;

    private static final Logger logger = Logger.getLogger(SenderWindow.class.getName());

    static {
        try {
            // Create a FileHandler that writes log to a file called "sender.log"
            FileHandler fileHandler = new FileHandler("sender.log", false);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize logger handler.", e);
        }
    }

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
        logger.log(Level.INFO, "[cwnd] " + cwnd + ", [ssthresh] " + ssthresh);
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

    public void setPacketAcked(int ack) {
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
            logger.log(Level.INFO, "[Slow Start] cwnd " + cwnd + " -> " + (cwnd + 1));
            cwnd++;
            dCwnd = cwnd;
        } else {
            dCwnd += (double) 1 /cwnd;
            logger.log(Level.INFO, "[Congestion Avoidance (+ Increase)] cwnd " + cwnd + " -> " + dCwnd);
            cwnd = (int) dCwnd;
        }

        if (ack == lastAck) {
            lastAckCount++;
        } else {
            lastAck = ack;
            lastAckCount = 1;
        }

        if (lastAckCount >= lastAckCountLimit) {
            logger.log(Level.INFO, "[Congestion Avoidance (x Decrease)] ssthresh " + ssthresh + " -> " + cwnd / 2);
            ssthresh = cwnd / 2;
            logger.log(Level.INFO, "[Slow Start] cwnd " + cwnd + " -> " + ssthresh);
            cwnd = ssthresh;
            dCwnd = (double) cwnd;
            resendPacket(ack);
        }


    }

}
