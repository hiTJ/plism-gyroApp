package com.example.serverapp;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initAngleData;
    private AngleData currentAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    private MessageQueueListenerInterface messageQueuelistener = null;
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
            InetSocketAddress ipep = new InetSocketAddress(10000);
            // ソケット接続
            server.bind(ipep);
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

                        int direction = data[0];
                        int rollX = data[1] * 0x100 + (data[2] & 0xFF);
                        int pitchY = data[3] * 0x100 + (data[4] & 0xFF);
                        int azimuthZ = data[5] * 0x100 + (data[6] & 0xFF);
                        int initialization = data[7];
                        AngleData angleData = new AngleData(direction, rollX, pitchY, azimuthZ, initialization);
                        if(initialization == 1){
                            setInitAngleData(angleData);
                        }
                        setCurrentAngleData(angleData);
                        angleDataMessageQueue.add(angleData);
                    }
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
    //public AngleData pollAngleData(){
    //    return angleDataMessageQueue.poll();
    //}
    //public AngleData peekAngleData(){
    //    return angleDataMessageQueue.peek();
    //}

    public void setMessageQueueListener(MessageQueueListenerInterface listener){
        this.messageQueuelistener = listener;
    }
    public void removeMessageQueueListener(){
        this.messageQueuelistener = null;
    }
}

