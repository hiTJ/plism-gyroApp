package com.example.gyroapp_forplism;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private DeltaAngleMessageQueue gyroQueue;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public SocketThread(DeltaAngleMessageQueue gQueue){
        gyroQueue = gQueue;
    }
    public void run(){
        try (Socket client = new Socket()) {
            // ソケットに接続するため、接続情報を設定する。
            //InetSocketAddress ipep = new InetSocketAddress("10.0.2.2", 9999);
            //InetSocketAddress ipep = new InetSocketAddress("126,0,29,26", 10000);
            InetSocketAddress ipep = new InetSocketAddress("192.168.3.50", 10000);
            // ソケット接続
            client.connect(ipep);
            isConnected = true;
            // ソケット接続が完了すればinputstreamとoutputstreamを受け取る。
            try (OutputStream sender = client.getOutputStream(); InputStream receiver = client.getInputStream()) {
                // メッセージはfor文を通って10回にメッセージを送信する。
                while (true) {
                    while(true) {
                        if (gyroQueue.isEmpty()) {
                            SocketThread.sleep(10);
                        }else{
                            if (!isConnected){
                                Log.d("DEBUG", "Dispose this thread by yourself.");
                                return;
                            }
                            break;
                        }
                    }
                    // 送信するメッセージを作成する。
                    String msg = gyroQueue.poll().message;
                    // stringをbyte配列に変換する。
                    byte[] data = msg.getBytes();
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
    public void toggleRunning(){
        isRunning = !isRunning;
    }
}

