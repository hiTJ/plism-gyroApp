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
    private AngleDataMessageQueue angleDataMessageQueue;
    private boolean running = true;
    private MessageQueueListenerInterface messageQueuelistener = null;
    public SocketThread(){
        angleDataMessageQueue = new AngleDataMessageQueue();
    }
    public void run(){
        try (ServerSocket server = new ServerSocket()) {
            InetSocketAddress ipep = new InetSocketAddress(10000);
            // ソケット接続
            server.bind(ipep);
            try (Socket socket = server.accept()) {
                //InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
                //BufferedReader reader = new BufferedReader(inputStream);
                try (OutputStream sender = socket.getOutputStream(); InputStream receiver = socket.getInputStream()) {
                    while (true) {
                        byte[] data = new byte[4];
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
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public AngleData pollAngleData(){
        if(angleDataMessageQueue.isEmpty()){
            return null;
        }else{
            return angleDataMessageQueue.poll();
        }
    }

    public void setMessageQueueListener(MessageQueueListenerInterface listener){
        this.messageQueuelistener = listener;
    }
    public void removeMessageQueueListener(){
        this.messageQueuelistener = null;
    }
}

