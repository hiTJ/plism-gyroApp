package com.example.serverapp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketThread extends Thread{
    private MessageQueue messageQueue;
    private boolean running = true;
    public SocketThread(){
        messageQueue = new MessageQueue();
    }
    public void run(){
        try (ServerSocket server = new ServerSocket()) {
            InetSocketAddress ipep = new InetSocketAddress("", 10000);
            // ソケット接続
            server.bind(ipep);
            // ソケット接続が完了すればinputstreamとoutputstreamを受け取る。
            try (Socket socket = server.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 入力を受け取ったら、大文字に変換の上で応答
                String data;
                while((data = reader.readLine()) != null) {
                    messageQueue.add(data);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

