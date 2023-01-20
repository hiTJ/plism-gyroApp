package com.example.serverapp;

import android.app.Activity;

public class AngleDataQueueListener implements MessageQueueListenerInterface {
    SocketThread socketThread;
    AngleData currentAngleData;
    public AngleDataQueueListener(SocketThread socketThread, AngleData currentAngleData){
        this.socketThread = socketThread;
        this.currentAngleData = currentAngleData;
    }
    /**
     * MessageQueueにQueuingされたことを通知する
     */
    @Override
    public void onQueuedMessage(){
    }
}
