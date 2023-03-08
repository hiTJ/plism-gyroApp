package com.example.serverapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AngleDataMessageQueueUnitTest {
    AngleDataMessageQueue angleDataMessageQueue;
    @Test
    public void angleDataMessageQueue_constructed() {
        angleDataMessageQueue = new AngleDataMessageQueue();
    }
}