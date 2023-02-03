package com.example.serverapp;

import android.util.Log;

public class BluetoothThread extends Thread{
    private AngleDataMessageQueue angleDataMessageQueue;
    private AngleData initializedAngleData;
    private boolean isRunning = false;
    private boolean isConnected = false;
    public boolean isConnected(){return isConnected;}
    public boolean isRunning(){return isRunning;}
    public BluetoothThread(AngleDataMessageQueue angleDataMessageQueue){
        this.angleDataMessageQueue = angleDataMessageQueue;
        this.initializedAngleData = new AngleData(0,0,0,0,0);
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
                if(angleData == null){
                    continue;
                }
                if(angleData.initialization == 1){
                    this.initializedAngleData = angleData;
                    Log.d("debug", "Do initialization!!");
                }
                int currentX = this.initializedAngleData.pitchX - angleData.pitchX;
                int currentZ = this.initializedAngleData.azimuthZ - angleData.azimuthZ;
                int initialization = angleData.initialization;
                Log.d("debug", "X: " + String.valueOf(currentX));
                Log.d("debug", "Z: " + String.valueOf(currentZ));
                Log.d("debug", "Init: " + String.valueOf(initialization));
            }
        }
    }
}
