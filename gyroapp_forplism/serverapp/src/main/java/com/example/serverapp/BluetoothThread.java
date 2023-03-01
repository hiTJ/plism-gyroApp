package com.example.serverapp;

import android.content.Context;
import android.util.Log;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class BluetoothThread extends Thread{
    private AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initializedAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    private boolean needDeviceReset = false;
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
    public void setNeedDeviceReset(boolean needDeviceReset){
        this.needDeviceReset = needDeviceReset;
    }
    public boolean getNeedDeviceReset(){
        return this.needDeviceReset;
    }
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private String deviceName = "";
    private BluetoothSerial bluetoothSerial;
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
            if(result.initialize == 1){
                init = 1;
            }
        }
        if(result != null){
            result.initialize = init;
        }

        return result;
    }

    private void connect2Stand(){
        try {
            this.bluetoothSerial.connectToDevice("94:E6:86:12:14:D2");
            while (!this.bluetoothSerial.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        this.isConnected = true;
    }
    public void run(){
        //Bluetoothのメインループ
        while(true){
            if(!isConnected){
                isRunning = false;
                connect2Stand();
            }else{
                AngleData angleData = pollAngleData();
                if(angleData == null){
                    continue;
                }
                if (angleData.deviceReset == 1){
                    Log.d("debug", "Reset Device!!");
                    try {
                        resetDevice();
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
                int currentX = this.initializedAngleData.pitchX - angleData.pitchX;
                //右方向が正にで通知されているので、init時の値をそのままマイナスする
                int currentZ =  angleData.azimuthZ - this.initializedAngleData.azimuthZ;
                currentX = currentX + 60;
                if(currentX > 119){
                    currentX = 119;
                }
                if(currentX < 0){
                    currentX = 0;
                }
                currentZ = currentZ + 180;
                currentZ = currentZ < 0 ? currentZ + 360 : currentZ;
                int initialization = angleData.initialize;
                byte[] dataX = ByteBuffer.allocate(4).putInt(currentX).array();
                byte[] dataZ = ByteBuffer.allocate(4).putInt(currentZ).array();
                byte[] x = castBytesToWORD(dataX);
                byte[] z = castBytesToWORD(dataZ);
                String strx = "y" + String.valueOf(currentX) + " ";
                String strz = "x" + String.valueOf(currentZ) + "\n";
                byte[] sbytex = strx.getBytes();
                byte[] sbytez = strz.getBytes();
                this.bluetoothSerial.write(concat(sbytex, sbytez));
                Log.d("debug", "X: " + String.valueOf(x));
                Log.d("debug", "Z: " + String.valueOf(z));
                Log.d("debug", "Init: " + String.valueOf(initialization));
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void resetDevice() throws InterruptedException {
        String strc = "c\n";
        byte[] sbytec = strc.getBytes();
        this.bluetoothSerial.write(sbytec);
        Thread.sleep(2000);
    }

    //Utils
    @NonNull
    @Contract(pure = true)
    private byte[] castBytesToWORD(byte[] bytes){
        byte[] word = new byte[2];
        for (int i = 0; i < word.length; i++)
        {
            word[word.length-i-1] = bytes[bytes.length-i-1];
        }
        return word;
    }
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
