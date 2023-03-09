package com.example.serverapp;

import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;


import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class BluetoothThread extends Thread{
    private final AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initializedAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    private boolean isAllowedConnect = false;
    private int dx = 0;
    private int dy = 0;
    public int getDx(){return this.dx;}
    public int getDy(){return this.dy;}
    public boolean isConnected(){return isConnected;}
    public boolean isAllowedConnect(){return isAllowedConnect;}
    public void setAllowedConnectStatus(boolean isAllowed){
        this.isAllowedConnect = isAllowed;
    }
    public boolean isRunning(){return isRunning;}
    private final BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private final BluetoothSerial bluetoothSerial;
    public BluetoothThread(AngleDataMessageQueue angleDataMessageQueue, BluetoothAdapter bluetoothAdapter){
        this.angleDataMessageQueue = angleDataMessageQueue;
        this.initializedAngleData = null;
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothSerial = new BluetoothSerial(this.bluetoothAdapter);
    }
    private AngleData pollAngleData(){
        AngleData result = null;
        int init = 0;
        for(int i = 0; i < angleDataMessageQueue.size(); i++){
            result = angleDataMessageQueue.poll();
            if(result != null && result.initialize == 1){
                init = 1;
            }
        }
        if(result != null){
            result.initialize = init;
        }

        return result;
    }

    private void connect2Stand(){
        if(isAllowedConnect) {
            try {
                this.bluetoothSerial.connectToDevice("94:E6:86:12:14:D2");
                while (!this.bluetoothSerial.isConnected()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.isConnected = true;
        }
    }
    public void run(){
        //Bluetoothのメインループ
        while(true){
            if(!isConnected){
                connect2Stand();
            }else{
                if(!isAllowedConnect){
                    this.bluetoothSerial.disconnect();
                    this.isConnected = false;
                    continue;
                }
                AngleData angleData = pollAngleData();
                if(angleData == null){
                    continue;
                }
                if (angleData.deviceReset == 1){
                    Log.d("debug", "Reset Device!!");
                    try {
                        resetDevice();
                        this.initializedAngleData = angleData;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if(angleData.initialize == 1){
                    this.initializedAngleData = angleData;
                    Log.d("debug", "Do initialization!!");
                }
                if(initializedAngleData == null){
                    continue;
                }
                //下方向が正にで通知されているので、反転させる
                int currentX =  angleData.pitchX - this.initializedAngleData.pitchX;
                //右方向が正にで通知されているので、init時の値をそのままマイナスする
                int currentZ =  angleData.azimuthZ - this.initializedAngleData.azimuthZ;
                currentX = currentX + 60;
                if(currentX > 119){
                    currentX = 119;
                }
                if(currentX < 0){
                    currentX = 0;
                }
                this.dy = currentX;
                currentZ = currentZ < 0 ? currentZ + 360 : currentZ;
                this.dx = currentZ;
                int initialization = angleData.initialize;
                String strY = "y" + currentX + " ";
                String strX = "x" + currentZ + "\n";
                byte[] sByteX = strY.getBytes();
                byte[] sByteZ = strX.getBytes();
                this.bluetoothSerial.write(concat(sByteX, sByteZ));
                Log.d("debug", strY);
                Log.d("debug", strX);
                Log.d("debug", "Init: " + initialization);
            }
            //try {
            //    Thread.sleep(100);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
        }
    }
    private void resetDevice() throws InterruptedException {
        String strX = "x0\n";
        byte[] sByteZ = strX.getBytes();
        this.bluetoothSerial.write(sByteZ);
        Thread.sleep(2000);
        String strC = "c\n";
        byte[] sByteC = strC.getBytes();
        this.bluetoothSerial.write(sByteC);
        Thread.sleep(4000);
    }

    //Utils
    @NonNull
    public static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (arrays != null) {
            Arrays.stream(arrays).filter(Objects::nonNull)
                    .forEach(array -> out.write(array, 0, array.length));
        }
        return out.toByteArray();
    }
}
