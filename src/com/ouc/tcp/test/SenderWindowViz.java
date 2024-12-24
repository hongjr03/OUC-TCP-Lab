package com.ouc.tcp.test;

import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

// 能导出可视化所需要的数据
public class SenderWindowViz {
    private final LinkedBlockingDeque<SenderElem> window;

    private int cwnd = 1;
    private double dCwnd = 1.0;
    private int ssthresh = 16;

    private UDT_Timer timer;
    private final int delay = 3000;
    private final int period = 3000;
    private final TCP_Sender sender;

    private int lastAck = -1;
    private int lastAckCount = 0;
    private final int lastAckCountLimit = 3;

    // Added fields for visualization
    private List<Integer> cwndHistory = new ArrayList<>();
    private List<Long> timeHistory = new ArrayList<>();
    private long startTime;

    public class GBN_RetransTask extends TimerTask {
        private final SenderWindowViz window;

        public GBN_RetransTask(SenderWindowViz window) {
            this.window = window;
        }

        public void run() {
            window.sendWindow();
        }
    }

    private static final String CSV_FILE_PATH = "tcp_data.csv";
    private final BufferedWriter csvWriter;

    public SenderWindowViz(TCP_Sender sender) {
        this.sender = sender;
        this.window = new LinkedBlockingDeque<>();
        this.timer = new UDT_Timer();
        this.startTime = System.currentTimeMillis();

        // Initialize CSV writer
        try {
            // Create new file and write header
            this.csvWriter = new BufferedWriter(new FileWriter(CSV_FILE_PATH, false));
            csvWriter.write("Time(ms),Cwnd,Ssthresh\n");
            csvWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create CSV file: " + e.getMessage());
        }
    }

    public boolean isCwndFull() {
        return window.size() >= cwnd;
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public void pushPacket(TCP_PACKET packet) {
        recordCwnd(); // Record initial cwnd
        if (isEmpty()) {
            timer = new UDT_Timer();
            timer.schedule(new GBN_RetransTask(this), delay, period);
        }
        window.push(new SenderElem(packet, SenderFlag.NOT_ACKED.ordinal()));
    }

    public void sendWindow() {
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

    public void resetTimer() {
        timer.cancel();
        timer = new UDT_Timer();
        if (!window.isEmpty()) {
            timer.schedule(new GBN_RetransTask(this), delay, period);
        }
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
                if (cwnd < ssthresh) {
                    cwnd++;
                    dCwnd = cwnd;
                    recordCwnd();
                }
                resetTimer();
            }
        }

        // Update congestion window
        if (cwnd >= ssthresh) {
            double oldDCwnd = dCwnd;
            dCwnd += (double) 1 / cwnd;
            if ((int)dCwnd > (int)oldDCwnd) {
                cwnd = (int) dCwnd;
            }
        }
        recordCwnd();
        if (ack == lastAck) {
            lastAckCount++;
        } else {
            lastAck = ack;
            lastAckCount = 1;
        }

        if (lastAckCount >= lastAckCountLimit) {
            ssthresh = cwnd / 2;
            cwnd = ssthresh + 3;
            dCwnd = (double) cwnd;

            resendPacket(ack);
        }
        recordCwnd(); // Record cwnd change
    }

    // Modified method to record cwnd
    private void recordCwnd() {
        long currentTime = System.currentTimeMillis() - startTime;

        // Write to CSV file
        try {
            csvWriter.write(String.format("%d,%d,%d\n", currentTime, cwnd, ssthresh));
            csvWriter.flush(); // Ensure data is written immediately
        } catch (IOException e) {
            System.err.println("Failed to write to CSV file: " + e.getMessage());
        }
    }

    // Add cleanup method
    public void cleanup() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close CSV file: " + e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }


    // New methods to retrieve visualization data
    public List<Integer> getCwndHistory() {
        return new ArrayList<>(cwndHistory);
    }

    public List<Long> getTimeHistory() {
        return new ArrayList<>(timeHistory);
    }
}
