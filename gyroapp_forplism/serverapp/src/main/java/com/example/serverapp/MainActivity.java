package com.example.serverapp;//package your.package.name;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private AngleDataMessageQueue angleDataMessageQueue;
    private BluetoothAdapter bluetoothAdapter;
    private TextView textInit, textCurrent, textDelta, sTextStatus, bTextStatus;
    //SocketThread sThread;
    SocketThreadUDP sThread;
    BluetoothThread bThread;
    Thread drawThread;
    AngleData currentAngleData;
    AngleData initialAngleData;
    Button bConnectButton, sDisConnectButton, bDisConnectButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) )
        {
            Toast.makeText( this, "BLE is not supported", Toast.LENGTH_SHORT ).show();
            finish();    // アプリ終了宣言
            return;
        }
        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if( null == this.bluetoothAdapter )
        {    // Android端末がBluetoothをサポートしていない
            Toast.makeText( this, "BLE is not supported", Toast.LENGTH_SHORT ).show();
            finish();    // アプリ終了宣言
            return;
        }

        this.angleDataMessageQueue = new AngleDataMessageQueue();

        //sThread = new SocketThread(this.angleDataMessageQueue, this.initialAngleData, this.currentAngleData);
        //sThread.start();
        //bThread = new BluetoothThread(this.angleDataMessageQueue, this.bluetoothAdapter);
        //bThread.start();
        bConnectButton = this.findViewById(R.id.bConnectButton);
        bDisConnectButton = this.findViewById(R.id.bDisConnectButton);
        sDisConnectButton = this.findViewById(R.id.sDisConnectButton);

        textCurrent = findViewById(R.id.text_current);
        textDelta = findViewById(R.id.text_delta);

        // Get an instance of the TextView
        textInit = findViewById(R.id.text_init);
        textDelta = findViewById(R.id.text_delta);
        sTextStatus = findViewById(R.id.sConnectionStatusView);
        bTextStatus = findViewById(R.id.bConnectionStatusView);

        sDisConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bThread.setAllowedConnectStatus(true);
            }
        });

        bDisConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bThread.setAllowedConnectStatus(false);
            }
        });
    }

    //　バックグラウンドから復帰時に呼び出される
    @Override
    protected void onResume() {
        super.onResume();
        sThread = new SocketThreadUDP(this.angleDataMessageQueue, this.initialAngleData, this.currentAngleData);
        sThread.start();
        bThread = new BluetoothThread(angleDataMessageQueue, bluetoothAdapter);
        bThread.start();
        this.drawThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //textView.setText(currentAngleData.pitchX + ", " + currentAngleData.rollY + ", " + currentAngleData.azimuthZ);
                                if(sThread == null){
                                    return;
                                }
                                initialAngleData = sThread.getInitAngleData();
                                currentAngleData = sThread.getCurrentAngleData();
                                if(initialAngleData != null){
                                    textInit.setText("INIT: " + initialAngleData.pitchX + ", " + initialAngleData.azimuthZ);
                                    if(currentAngleData != null){
                                        int pitchX = currentAngleData.pitchX - initialAngleData.pitchX + 60;
                                        if(pitchX > 119){
                                            pitchX = 119;
                                        }
                                        if(pitchX < 0){
                                            pitchX = 0;
                                        }
                                        int azimuthZ = currentAngleData.azimuthZ - initialAngleData.azimuthZ;
                                        if(azimuthZ < 0){
                                            azimuthZ = azimuthZ + 360;
                                        }
                                        textCurrent.setText("CURRENT: " + currentAngleData.pitchX + ", " + currentAngleData.azimuthZ);
                                        if(bThread != null && bThread.isConnected()){
                                            textDelta.setText("DELTA_B: " + bThread.getDy() + ", " + bThread.getDx());
                                        }else{
                                            textDelta.setText("DELTA: " + pitchX + ", " + azimuthZ);
                                        }
                                    }
                                }
                                if(sThread.isConnected())
                                {
                                    sTextStatus.setText("CONNECTED");
                                    sDisConnectButton.setEnabled(true);
                                }else {
                                    sTextStatus.setText("NOT CONNECTED");
                                    sDisConnectButton.setEnabled(false);
                                }
                                //if(bThread == null){
                                //    return;
                                //}
                                if(bThread.isConnected()){
                                    bTextStatus.setText("CONNECTED");
                                    bConnectButton.setEnabled(false);
                                    bDisConnectButton.setEnabled(true);
                                }else {
                                    bTextStatus.setText("NOT CONNECTED");
                                    bConnectButton.setEnabled(true);
                                    bDisConnectButton.setEnabled(false);
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

    // バックグラウンド時に呼び出される
    @Override
    protected void onPause() {
        super.onPause();
    }


}