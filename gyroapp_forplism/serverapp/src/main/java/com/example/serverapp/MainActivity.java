package com.example.serverapp;//package your.package.name;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private AngleDataMessageQueue angleDataMessageQueue;
    private SensorManager sensorManager;
    private TextView textInit, textCurrent, textDelta, textStatus;
    SocketThread sThread;
    BluetoothThread bThread;
    Thread drawThread;
    AngleData currentAngleData;
    AngleData initialAngleData;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.angleDataMessageQueue = new AngleDataMessageQueue();
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sThread = new SocketThread(this.angleDataMessageQueue, this.initialAngleData, this.currentAngleData);
        sThread.start();
        bThread = new BluetoothThread(this.angleDataMessageQueue);
        bThread.start();

        textCurrent = findViewById(R.id.text_current);

        Button button = this.findViewById(R.id.button);
        // Get an instance of the TextView
        textInit = findViewById(R.id.text_init);
        textDelta = findViewById(R.id.text_delta);
        textStatus = findViewById(R.id.connectionStatusView);
        this.drawThread = new Thread(new Runnable(){
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //textView.setText(currentAngleData.pitchX + ", " + currentAngleData.rollY + ", " + currentAngleData.azimuthZ);
                                initialAngleData = sThread.getInitAngleData();
                                if(initialAngleData != null){
                                    textInit.setText("INIT: " + initialAngleData.pitchX + ", " + initialAngleData.rollY + ", " + initialAngleData.azimuthZ);
                                }
                                currentAngleData = sThread.getCurrentAngleData();
                                if(currentAngleData != null){
                                    textCurrent.setText("CURRENT: " + currentAngleData.pitchX + ", " + currentAngleData.rollY + ", " + currentAngleData.azimuthZ);
                                }
                                if(sThread.isConnected())
                                {
                                    textStatus.setText("CONNECTED");
                                }else{
                                    if(textStatus != null) {
                                        textStatus.setText("NOT CONNECTED");
                                    }
                                }
                            }
                        });
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        drawThread.start();
    }

    //　バックグラウンドから復帰時に呼び出される
    @Override
    protected void onResume() {
        super.onResume();
    }

    // バックグラウンド時に呼び出される
    @Override
    protected void onPause() {
        super.onPause();
    }


}