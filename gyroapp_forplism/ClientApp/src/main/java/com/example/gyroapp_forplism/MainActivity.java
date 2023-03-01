package com.example.gyroapp_forplism;//package your.package.name;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textView, textInfo, textIp;
    private AngleDataMessageQueue angleDataMessageQueue;
    SocketThread sThread;
    private AngleCalculator angleCalculator;
    private boolean needInitialize = false, needResetDevice = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        angleDataMessageQueue = new AngleDataMessageQueue();
        angleCalculator = new AngleCalculator();

        textInfo = findViewById(R.id.text_info);

        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        textIp = findViewById(R.id.ipaddress_text);

        Button connectButton = this.findViewById(R.id.ConnectButton);
        Button disconnectButton = this.findViewById(R.id.DisconnectButton);
        Button startButton = this.findViewById(R.id.StartButton);
        Button stopButton = this.findViewById(R.id.StopButton);
        Button initializeButton = this.findViewById(R.id.InitializeButton);
        Button deviceResetButton = this.findViewById(R.id.DeviceResetButton);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        initializeButton.setEnabled(false);
        deviceResetButton.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ip = textIp.getText().toString();
                if(ip == ""){
                    return;
                }
                if(sThread == null){
                    sThread = new SocketThread(angleDataMessageQueue);
                    sThread.setHost(ip, 10000);
                    sThread.start();
                }
                startButton.setEnabled(true);
                disconnectButton.setEnabled(true);
                connectButton.setEnabled(false);
                initializeButton.setEnabled(false);
                deviceResetButton.setEnabled(false);
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sThread.setConnected(false);
                startButton.setEnabled(false);
                disconnectButton.setEnabled(false);
                initializeButton.setEnabled(false);
                deviceResetButton.setEnabled(false);
                while(true){
                    if(sThread == null || !sThread.isAlive()){
                        break;
                    }
                }
                connectButton.setEnabled(true);
                sThread = null;
            }
        });
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(sThread.isAlive() && sThread.isConnected()) {
                    sThread.setRunning(true);
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    disconnectButton.setEnabled(false);
                    initializeButton.setEnabled(true);
                    deviceResetButton.setEnabled(true);
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(sThread.isAlive() && sThread.isConnected()) {
                    sThread.setRunning(false);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                    initializeButton.setEnabled(false);
                    deviceResetButton.setEnabled(false);
                }
            }
        });
        initializeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(sThread.isAlive() && sThread.isConnected()) {
                    //TODO Initialize処理
                    needInitialize = true;
                }
            }
        });
        deviceResetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(sThread.isAlive() && sThread.isConnected()) {
                    //TODO Device reset 処理
                    needResetDevice = true;
                }
            }
        });

    }
    private Sensor gyro = null, mag = null, accel = null;

    // バックグラウンドからの復帰でも呼ばれる
    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        //if(this.gyro == null) {
        //    this.gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //    sensorManager.registerListener(this, this.gyro, SensorManager.SENSOR_DELAY_UI);
        //}
        if(this.mag == null) {
            this.mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, this.mag, SensorManager.SENSOR_DELAY_UI);
        }
        if(this.accel == null){
            this.accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, this.accel, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // バックグラウンド時にも呼ばれる
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        // バックグラウンドでも動作するようにコメントアウト
        //sensorManager.unregisterListener(this);
    }


    private void doGyroAction(@NonNull SensorEvent event){
        float sensorX = event.values[0];
        float sensorY = event.values[1];
        float sensorZ = event.values[2];

        String strTmp = String.format(Locale.US, "Gyroscope\n " +
                " X: %f\n Y: %f\n Z: %f",sensorX, sensorY, sensorZ);
        textView.setText(strTmp);

        //showInfo(event);
        GyroData gyroData = new GyroData(sensorX, sensorY, sensorZ);
        //TODO Gyro値を使用するならば改めてqueue追加の処理を入れる
    }
    private void doAngleAction(@NonNull SensorEvent event){
        this.angleCalculator.setAccelerometer(event.values.clone());
        this.angleCalculator.calcAngle(needInitialize, needResetDevice);
        AngleData angleData = this.angleCalculator.getCurrentAngleData();
        angleData.initialize = needInitialize ? 1 : 0;
        angleData.deviceReset = needResetDevice ? 1 : 0;
        showAngleInfo((int)angleData.pitchX, (int)angleData.rollY, (int)angleData.azimuthZ);
        if(sThread != null && sThread.isAlive()){
            this.angleDataMessageQueue.add(angleData);
            this.needInitialize = false;
            this.needResetDevice = false;
        }
    }
    private void doAccelAction(@NonNull SensorEvent event){
        doAngleAction(event);
    }
    private void doMagneticAction(@NonNull SensorEvent event){
        doAngleAction(event);
    }

    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        Log.d("debug","onSensorChanged");

        switch (event.sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                //doGyroAction(event);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                doAccelAction(event);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                doMagneticAction(event);
                break;
            default:
                return;
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
        }
    }


    private void showAngleInfo(float pitchX, float rollY, float azimuthZ){
        String strTmp = String.format(Locale.US, "Angle\n " +
                " Pitch: %f\n Roll: %f\n Azimuth: %f",pitchX, rollY, azimuthZ);
        textView.setText(strTmp);
    }
    // センサーの各種情報を表示する
    private void showInfo(@NonNull SensorEvent event){
        // センサー名
        StringBuffer info = new StringBuffer("Name: ");
        info.append(event.sensor.getName());
        info.append("\n");

        // ベンダー名
        info.append("Vendor: ");
        info.append(event.sensor.getVendor());
        info.append("\n");

        // 型番
        info.append("Type: ");
        info.append(event.sensor.getType());
        info.append("\n");

        // 最小遅れ
        int data = event.sensor.getMinDelay();
        info.append("Mindelay: ");
        info.append(data);
        info.append(" usec\n");

        // 最大遅れ
        data = event.sensor.getMaxDelay();
        info.append("Maxdelay: ");
        info.append(data);
        info.append(" usec\n");

        // レポートモード
        data = event.sensor.getReportingMode();
        String stinfo = "unknown";
        if(data == 0){
            stinfo = "REPORTING_MODE_CONTINUOUS";
        }else if(data == 1){
            stinfo = "REPORTING_MODE_ON_CHANGE";
        }else if(data == 2){
            stinfo = "REPORTING_MODE_ONE_SHOT";
        }
        info.append("ReportingMode: ");
        info.append(stinfo);
        info.append("\n");

        // 最大レンジ
        info.append("MaxRange: ");
        float fData = event.sensor.getMaximumRange();
        info.append(fData);
        info.append("\n");

        // 分解能
        info.append("Resolution: ");
        fData = event.sensor.getResolution();
        info.append(fData);
        info.append(" m/s^2\n");

        // 消費電流
        info.append("Power: ");
        fData = event.sensor.getPower();
        info.append(fData);
        info.append(" mA\n");

        textInfo.setText(info);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
