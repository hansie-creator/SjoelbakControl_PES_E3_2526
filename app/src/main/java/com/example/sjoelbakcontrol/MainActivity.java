package com.example.sjoelbakcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String MAC_ADDRESS = "2C:BC:BB:A7:A5:CA";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private OutputStream outStream;
    private InputStream inStream;
    private TextView statusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusLabel = findViewById(R.id.statusLabel);
        Button btnLinks = findViewById(R.id.btnLinks);
        Button btnRechts = findViewById(R.id.btnRechts);
        Button btnLoad = findViewById(R.id.btnLoad);

        // --- CONTINUE BEWEGING LOGICA ---

        btnLinks.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("START_L\n");
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                sendData("STOP\n");
            }
            return true;
        });

        btnRechts.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendData("START_R\n");
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                sendData("STOP\n");
            }
            return true;
        });

        // --- OVERIGE KNOPPEN ---

        btnLoad.setOnClickListener(v -> sendData("LOAD\n"));

        findViewById(R.id.btnVak1).setOnClickListener(v -> sendData("VAK:1\n"));
        findViewById(R.id.btnVak2).setOnClickListener(v -> sendData("VAK:2\n"));
        findViewById(R.id.btnVak3).setOnClickListener(v -> sendData("VAK:3\n"));
        findViewById(R.id.btnVak4).setOnClickListener(v -> sendData("VAK:4\n"));
        findViewById(R.id.btnVak5).setOnClickListener(v -> sendData("VAK:5\n"));

        connectBluetooth();
    }

    private void connectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return;
        BluetoothDevice device = adapter.getRemoteDevice(MAC_ADDRESS);

        new Thread(() -> {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                outStream = socket.getOutputStream();
                inStream = socket.getInputStream();
                runOnUiThread(() -> statusLabel.setText("Status: Verbonden!"));
                startListening();
            } catch (IOException e) {
                runOnUiThread(() -> statusLabel.setText("Status: Verbinding mislukt"));
            }
        }).start();
    }

    private void sendData(String msg) {
        if (outStream == null) return;
        try {
            outStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inStream.read(buffer);
                    String message = new String(buffer, 0, bytes).trim();
                    runOnUiThread(() -> {
                        if (message.startsWith("POSITIE:")) {
                            statusLabel.setText("Positie: " + message.substring(8));
                        }
                    });
                } catch (IOException e) { break; }
            }
        }).start();
    }
}