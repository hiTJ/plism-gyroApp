package com.example.serverapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AngleDataUnitTest {
    @Test
    public void angleData_constructed() {
        AngleData angleData = new AngleData(1, 1, 2, -2, 1, 0);
        assertNotEquals(angleData, null);
        AngleData angleData1 = new AngleData(angleData);
        assertNotEquals(angleData1, null);
        assertNotEquals(angleData, angleData1);

        assertEquals(angleData.pitchX, angleData1.pitchX);
        assertEquals(angleData.azimuthZ, angleData1.azimuthZ);
        assertEquals(angleData.rollY, angleData1.rollY);
        assertEquals(angleData.deviceReset, angleData1.deviceReset);
        assertEquals(angleData.initialize, angleData1.initialize);
        assertEquals(angleData.direction, angleData1.direction);
    }
}