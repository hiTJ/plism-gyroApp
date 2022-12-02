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
    private MessageQueueListenerInterface messageQueuelistener = null;
    public SocketThread(){
        messageQueue = new MessageQueue();
    }
    public void run(){
        try (ServerSocket server = new ServerSocket()) {
            InetSocketAddress ipep = new InetSocketAddress(10000);
            // ソケット接続
            server.bind(ipep);
            while(true) {
                try (Socket socket = server.accept()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = reader.readLine();
                    messageQueue.add(data);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public String pollMessage(){
        if(messageQueue.isEmpty()){
            return "";
        }else{
            return messageQueue.poll();
        }
    }

    public void setMessageQueueListener(MessageQueueListenerInterface listener){
        this.messageQueuelistener = listener;
    }
    public void removeMessageQueueListener(){
        this.messageQueuelistener = null;
    }
}

