package com.example.serverapp;

import android.content.Context;
import android.util.Log;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothThread extends Thread{
    private AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initializedAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
    private BluetoothAdapter bluetoothAdapter;
    public BluetoothThread(AngleDataMessageQueue angleDataMessageQueue, BluetoothAdapter bluetoothAdapter){
        this.angleDataMessageQueue = angleDataMessageQueue;
        this.initializedAngleData = null;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    private void connect2Stand(){

        this.isConnected = true;
    }
    //private AngleData pollAngleData(){
    //    return angleDataMessageQueue.poll();
    //}
    public void run(){
        //Bluetoothのメインループ
        while(true){
            connect2Stand();
            if(!isConnected){
                isRunning = false;
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
                Log.d("debug", "X: " + String.valueOf(currentX));
                Log.d("debug", "Z: " + String.valueOf(currentZ));
                Log.d("debug", "Init: " + String.valueOf(initialization));
            }
        }
    }
}
