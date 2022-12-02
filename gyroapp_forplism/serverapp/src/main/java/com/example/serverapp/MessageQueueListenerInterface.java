package com.example.serverapp;

import java.util.EventListener;

public interface MessageQueueListenerInterface extends EventListener {
    /**
     * MessageQueueにQueuingされたことを通知する
     */
    public void onQueuedMessage();
}
