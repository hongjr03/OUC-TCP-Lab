/***************************2.1: ACK/NACK*****************
 ***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;

public class TCP_Receiver extends TCP_Receiver_ADT {

    private TCP_PACKET ackPack;    //回复的ACK报文段
    private final ReceiverWindow window = new ReceiverWindow(16);
//    private UDT_Timer timer = new UDT_Timer();

    /*构造函数*/
    public TCP_Receiver() {
        super();    //调用超类构造函数
        super.initTCP_Receiver(this);    //初始化TCP接收端
    }

    @Override
    //接收到数据报：检查校验和，设置回复的ACK报文段
    public void rdt_recv(TCP_PACKET recvPack) {

        //检查校验码，生成ACK
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
            int bufferResult = window.bufferPacket(recvPack);
            if (bufferResult == AckFlag.IS_BASE.ordinal()) {
                TCP_PACKET packet = window.getPacketToDeliver();
                while (packet != null) {
                    dataQueue.add(packet.getTcpS().getData());
                    tcpH.setTh_ack(packet.getTcpH().getTh_seq());
                    ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
                    tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
                    packet = window.getPacketToDeliver();
                }
            }

        }
        reply(ackPack);    //回复ACK报文段
        // 错误包不回复 ACK

        System.out.println();

        //交付数据
        deliver_data();
    }

    @Override
    //交付数据（将数据写入文件）；不需要修改
    public void deliver_data() {
        //检查dataQueue，将数据写入文件
        File fw = new File("recvData.txt");
        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(fw, true));

            //循环检查data队列中是否有新交付数据
            while (!dataQueue.isEmpty()) {
                int[] data = dataQueue.poll();

                //将数据写入文件
                for (int i = 0; i < data.length; i++) {
                    writer.write(data[i] + "\n");
                }

                writer.flush();        //清空输出缓存
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    //回复ACK报文段
    public void reply(TCP_PACKET replyPack) {
        //设置错误控制标志
        tcpH.setTh_eflag((byte) 7);    //eFlag=0，信道无错误

        //发送数据报
        client.send(replyPack);
    }

}
