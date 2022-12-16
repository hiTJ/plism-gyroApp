package com.example.gyroapp_forplism;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private DeltaAngleMessageQueue deltaAngleMessageQueue;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public SocketThread(DeltaAngleMessageQueue gQueue){
        deltaAngleMessageQueue = gQueue;
    }
    public void run(){
        try (Socket client = new Socket()) {
            // ソケットに接続するため、接続情報を設定する。
            //InetSocketAddress ipep = new InetSocketAddress("10.0.2.2", 9999);
            InetSocketAddress ipep = new InetSocketAddress("192.168.3.50", 10000);
            // ソケット接続
            client.connect(ipep);
            isConnected = true;
            // ソケット接続が完了すればinputstreamとoutputstreamを受け取る。
            try (OutputStream sender = client.getOutputStream(); InputStream receiver = client.getInputStream()) {
                // メッセージはfor文を通って10回にメッセージを送信する。
                while (true) {
                    while(true) {
                        if (!isConnected){
                            deltaAngleMessageQueue.clear();
                            Log.d("DEBUG", "Dispose this thread by yourself.");
                            return;
                        }
                        if (deltaAngleMessageQueue.isEmpty()) {
                            SocketThread.sleep(10);
                        }else{
                            if(isRunning) {
                                break;
                            }
                        }
                    }
                    // 送信するメッセージを作成する。
                    DeltaAngleData deltaAngleData = deltaAngleMessageQueue.poll();
                    // stringをbyte配列に変換する。
                    //byte[] dataX = floatToByteArray(deltaAngleData.deltaPitchX);
                    //byte[] dataY = floatToByteArray(deltaAngleData.deltaRollY);
                    //byte[] dataZ = floatToByteArray(deltaAngleData.deltaAzimuthZ);
                    byte dataX = (byte)((int)deltaAngleData.deltaPitchX);
                    byte dataY = (byte)((int)deltaAngleData.deltaRollY);
                    byte dataZ = (byte)((int)deltaAngleData.deltaAzimuthZ);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(dataX);
                    outputStream.write(dataY);
                    outputStream.write(dataZ);
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

                    // byteタイプの???をstringタイプに変換する。
                    //msg = new String(data, "UTF-8");
                    // コンソールに出力する。
                    //System.out.println(msg);
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

