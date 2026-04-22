package com.example.sjoelbakcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Bluetooth variabelen
    private final String MAC_ADDRESS = "2C:BC:BB:A7:A9:72"; //MAC ADRES ESP
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private OutputStream outStream;
    private TextView statusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editS = findViewById(R.id.editS);
        EditText editM = findViewById(R.id.editM);
        EditText editW = findViewById(R.id.editW);
        Button btnSend = findViewById(R.id.btnSend);
        statusLabel = findViewById(R.id.statusLabel);

        connectBluetooth();

        btnSend.setOnClickListener(v -> {
            String payload = editS.getText().toString() + "," +
                    editM.getText().toString() + "," +
                    editW.getText().toString() + "\n";
            sendData(payload);
        });
    }

    private void connectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(MAC_ADDRESS);
        new Thread(() -> {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                outStream = socket.getOutputStream();
                runOnUiThread(() -> statusLabel.setText("Status: Verbonden!"));
            } catch (IOException e) {
                runOnUiThread(() -> statusLabel.setText("Status: Verbinding mislukt/Niet verbonden"));
            }
        }).start();
    }

    private void sendData(String msg) {
        if (outStream != null) {
            try {
                outStream.write(msg.getBytes());
                Toast.makeText(this, "Data verzonden", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                statusLabel.setText("Fout bij verzenden");
            }
        }
    }
}