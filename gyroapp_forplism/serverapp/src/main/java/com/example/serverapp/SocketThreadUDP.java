package com.example.serverapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SocketThreadUDP extends Thread{
    private final AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initAngleData;
    private AngleData currentAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public SocketThreadUDP(AngleDataMessageQueue angleDataMessageQueue, AngleData initAngleData, AngleData currentAngleData){
        this.angleDataMessageQueue = angleDataMessageQueue;
        this.initAngleData = initAngleData;
        this.currentAngleData = currentAngleData;
    }
    public AngleData getInitAngleData(){return initAngleData;}
    public AngleData getCurrentAngleData(){return currentAngleData;}
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
    public void run(){
        isConnected = false;
        try (DatagramSocket server = new DatagramSocket(10000)) {
            isConnected = true;
            while(true) {
                byte[] rdata = new byte[1024];
                DatagramPacket packet = new DatagramPacket(rdata, rdata.length);
                server.receive(packet);
                byte[] data = Arrays.copyOf(packet.getData(),packet.getLength());
                int direction = data[0];
                int rollX = data[1] * 0x100 + (data[2] & 0xFF);
                int pitchY = data[3] * 0x100 + (data[4] & 0xFF);
                int azimuthZ = data[5] * 0x100 + (data[6] & 0xFF);
                int initialization = data[7];
                int resetDevice = data[8];
                AngleData angleData = new AngleData(direction, rollX, pitchY, azimuthZ, initialization, resetDevice);
                if (initialization == 1) {
                    setInitAngleData(angleData);
                }
                setCurrentAngleData(angleData);
                angleDataMessageQueue.add(angleData);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            isRunning = false;
            isConnected = false;
        }

    }
    private void setInitAngleData(AngleData initAngleData){
        this.initAngleData = new AngleData(initAngleData);
    }
    private void setCurrentAngleData(AngleData currentAngleData){
        this.currentAngleData = new AngleData(currentAngleData);
    }
}

