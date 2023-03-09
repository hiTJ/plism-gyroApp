package com.example.serverapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private final AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initAngleData;
    private AngleData currentAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public SocketThread(AngleDataMessageQueue angleDataMessageQueue, AngleData initAngleData, AngleData currentAngleData){
        this.angleDataMessageQueue = angleDataMessageQueue;
        this.initAngleData = initAngleData;
        this.currentAngleData = currentAngleData;
    }
    public AngleData getInitAngleData(){return initAngleData;}
    public AngleData getCurrentAngleData(){return currentAngleData;}
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
    public void run(){
        try (ServerSocket server = new ServerSocket()) {
            InetSocketAddress iPep = new InetSocketAddress(10000);
            // ソケット接続
            server.bind(iPep);
            while(true) {
                isConnected = false;
                try (Socket socket = server.accept()) {
                    isConnected = true;
                    try (OutputStream sender = socket.getOutputStream(); InputStream receiver = socket.getInputStream()) {
                        while (true) {
                            isRunning = true;
                            byte[] lengthData = new byte[4];
                             receiver.read(lengthData, 0, 4);
                            // ByteBufferを通ってlittleエンディアンで変換してデータサイズを受け取る。
                            ByteBuffer bb = ByteBuffer.wrap(lengthData);
                            bb.order(ByteOrder.LITTLE_ENDIAN);
                            int length = bb.getInt();
                            // データサイズほど、バッファーを設定する。
                            byte[] data = new byte[length];
                            // データを受け取る。
                            receiver.read(data, 0, length);

                            //// ByteBufferを通ってデータサイズをbyteタイプに変換する。
                            //ByteBuffer b = ByteBuffer.allocate(4);
                            //// byteフォマートはlittleエンディアンだ。
                            //b.order(ByteOrder.LITTLE_ENDIAN);
                            //b.putInt(data.length);
                            //// データ長さを送信
                            //sender.write(b.array(), 0, 4);
                            //// データ送信
                            //sender.write(data);

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
                    }
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
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

