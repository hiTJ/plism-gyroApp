package com.example.serverapp;//package your.package.name;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.serverapp.R;

import java.util.Locale;

public class MainActivity extends Activity {

    private AngleDataMessageQueue angleDataMessageQueue;
    private SensorManager sensorManager;
    private TextView textView, textInfo, textStatus;
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
        sThread = new SocketThread(this.angleDataMessageQueue);
        sThread.start();
        bThread = new BluetoothThread(this.angleDataMessageQueue);
        bThread.start();

        textInfo = findViewById(R.id.text_info);

        Button button = this.findViewById(R.id.button);
        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        textStatus = findViewById(R.id.connectionStatusView);
        this.drawThread = new Thread(new Runnable(){
            public void run() {
                while (true) {
                    try {
                        //currentAngleData = sThread.peekAngleData();
                        currentAngleData = angleDataMessageQueue.peek();
                        if (currentAngleData != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.setText(currentAngleData.pitchX + ", " + currentAngleData.rollY + ", " + currentAngleData.azimuthZ);
                                    if(sThread.isConnected())
                                    {
                                        textStatus.setText("CONNECTED");
                                    }else{
                                        textStatus.setText("NOT CONNECTED");
                                    }
                                }
                            });
                        }
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