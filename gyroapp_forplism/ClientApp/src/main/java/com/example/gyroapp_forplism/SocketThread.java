package com.example.gyroapp_forplism;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private AngleDataMessageQueue angleDataMessageQueue;
    private String socketIp = "";
    private int port;
    private boolean isRunning = false;
    private boolean isConnected = false;
    private boolean needInitialize = false;
    public SocketThread(AngleDataMessageQueue gQueue){
        this.angleDataMessageQueue = gQueue;
    }
    @NonNull
    @Contract(pure = true)
    private byte[] castBytesToWORD(byte[] bytes){
        byte[] word = new byte[2];
        for (int i = 0; i < word.length; i++)
        {
            word[word.length-i-1] = bytes[bytes.length-i-1];
        }
        return word;
    }
    public void setHost(String host, int port){
        this.socketIp = host;
        this.port = port;
    }
    private void connectSocket(Socket client, String host, int port) throws IOException {
        InetSocketAddress ipep = new InetSocketAddress(socketIp, 10000);
        // ソケット接続
        client.connect(ipep);
        isConnected = true;
    }
    private AngleData pollLatestAngleData(){
        AngleData latestAngleData = null;
        int initialize = 0;
        int deviceReset = 0;
        while (angleDataMessageQueue.size() != 0) {
            AngleData angleData = angleDataMessageQueue.poll();
            if(angleData.initialize == 1){
                initialize = 1;
            }
            if(angleData.deviceReset == 1){
                deviceReset = 1;
            }
            latestAngleData = angleData;
        }
        if(latestAngleData != null){
            latestAngleData.initialize = initialize;
            latestAngleData.deviceReset = deviceReset;
        }
        return latestAngleData;
    }
    public void run(){
        try (Socket client = new Socket()) {
            // ソケットに接続するため、接続情報を設定する。
            //InetSocketAddress ipep = new InetSocketAddress("10.0.2.2", 9999);
            //InetSocketAddress ipep = new InetSocketAddress("192.168.3.50", 10000);
            connectSocket(client, socketIp, port);
            // ソケット接続が完了すればinputstreamとoutputstreamを受け取る。
            try (OutputStream sender = client.getOutputStream(); InputStream receiver = client.getInputStream()) {
                // メッセージはfor文を通って10回にメッセージを送信する。
                while (true) {
                    while(true) {
                        if (!isConnected){
                            angleDataMessageQueue.clear();
                            Log.d("DEBUG", "Dispose this thread by yourself.");
                            return;
                        }
                        if (angleDataMessageQueue.isEmpty()) {
                            SocketThread.sleep(10);
                        }else{
                            if(isRunning) {
                                break;
                            }
                        }
                    }
                    // 送信するメッセージを作成する。
                    AngleData angleData = pollLatestAngleData();
                    // stringをbyte配列に変換する。
                    //byte[] dataX = floatToByteArray(deltaAngleData.deltaPitchX);
                    //byte[] dataY = floatToByteArray(deltaAngleData.deltaRollY);
                    //byte[] dataZ = floatToByteArray(deltaAngleData.deltaAzimuthZ);
                    byte direction = (byte)(angleData.direction);
                    byte[] dataX = ByteBuffer.allocate(4).putInt(angleData.pitchX).array();
                    byte[] dataY = ByteBuffer.allocate(4).putInt(angleData.rollY).array();
                    byte[] dataZ = ByteBuffer.allocate(4).putInt(angleData.azimuthZ).array();
                    byte initialize = (byte)(angleData.initialize);
                    byte deviceReset = (byte)(angleData.deviceReset);
                    //byte dataX = (byte)((int)angleData.pitchX);
                    //byte dataY = (byte)((int)angleData.rollY);
                    //byte dataZ = (byte)((int)angleData.azimuthZ);
                    byte[] x = castBytesToWORD(dataX);
                    byte[] y = castBytesToWORD(dataY);
                    byte[] z = castBytesToWORD(dataZ);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(direction);
                    outputStream.write(x);
                    outputStream.write(y);
                    outputStream.write(z);
                    outputStream.write(initialize);
                    outputStream.write(deviceReset);
                    byte[] data = outputStream.toByteArray();
                    // ByteBufferを通ってデータサイズをbyteタイプに変換する。
                    ByteBuffer b = ByteBuffer.allocate(4);
                    // byteフォマートはlittleエンディアンだ。
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    b.putInt(data.length);
                    // データ長さを送信
                    sender.write(b.array(), 0, 4);
                    // データ送信
                    sender.write(data);

                    data = new byte[4];
                    // データを長さを受信
                    receiver.read(data, 0, 4);
                    // ByteBufferを通ってlittleエンディアンで変換してデータサイズを受け取る。
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    int length = bb.getInt();
                    // データサイズほど、バッファーを設定する。
                    data = new byte[length];
                    // データを受け取る。
                    receiver.read(data, 0, length);
                }
            }
        } catch (Throwable e) {
            // エラーが発生する時コンソールに出力する。
            e.printStackTrace();
        } finally {
            isConnected = false;
        }
    }

    public boolean isConnected(){
        return isConnected;
    }
    public void setConnected(boolean isConnected){ this.isConnected = isConnected; }
    public void setRunning(boolean isRunning){
        this.isRunning = isRunning;
    }

    //Utils
    public static byte[] floatToByteArray(float value) {
        int intBits =  Float.floatToIntBits(value);
        return new byte[] {
                (byte) (intBits >> 24),
                (byte) (intBits >> 16),
                (byte) (intBits >> 8),
                (byte) (intBits) };
    }
}

