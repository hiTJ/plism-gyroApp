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
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
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

    private void connect2Stand(){
        this.bluetoothSerial.connectToDevice("MAC ADDRESS");
        while(!this.bluetoothSerial.isConnected()){
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
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
                AngleData angleData = this.angleDataMessageQueue.poll();
                if(angleData == null || initializedAngleData == null){
                    continue;
                }
                if(angleData.initialization == 1){
                    this.initializedAngleData = angleData;
                    Log.d("debug", "Do initialization!!");
                }
                //下方向が正にで通知されているので、反転させる
                int currentX = this.initializedAngleData.pitchX - angleData.pitchX;
                //右方向が正にで通知されているので、init時の値をそのままマイナスする
                int currentZ =  angleData.azimuthZ - this.initializedAngleData.azimuthZ;
                if(currentX > 45){
                    currentX = 45;
                }
                if(currentX < 0){
                    currentX = 0;
                }
                currentZ = currentZ < 0 ? currentZ + 360 : currentZ;
                int initialization = angleData.initialization;
                byte[] dataX = ByteBuffer.allocate(4).putInt(currentX).array();
                byte[] dataZ = ByteBuffer.allocate(4).putInt(currentZ).array();
                byte[] x = castBytesToWORD(dataX);
                byte[] z = castBytesToWORD(dataZ);
                byte[] serial = concat(x, z);
                this.bluetoothSerial.write(serial);
                Log.d("debug", "X: " + String.valueOf(x));
                Log.d("debug", "Z: " + String.valueOf(z));
                Log.d("debug", "Init: " + String.valueOf(initialization));
            }
        }
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
