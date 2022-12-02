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

public class MainActivity extends Activity implements MessageQueueListenerInterface {

    private SensorManager sensorManager;
    private TextView textView, textInfo;
    SocketThread sThread;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sThread = new SocketThread();
        sThread.start();


        textInfo = findViewById(R.id.text_info);

        Button button = this.findViewById(R.id.button);
        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        Thread drawThread = new Thread(new Runnable(){
            public void run() {
                while (true) {
                    try {
                        String message = sThread.pollMessage();
                        if (message != "") {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.setText(message);
                                }
                            });
                        }
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        drawThread.start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        String ns = "OnResume";
        textView.setText(ns);
    }

    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onQueuedMessage(){
        String message = sThread.pollMessage();
        textInfo.setText((message));
    }

}