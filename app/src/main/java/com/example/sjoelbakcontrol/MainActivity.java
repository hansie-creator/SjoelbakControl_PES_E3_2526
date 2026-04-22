package com.example.sjoelbakcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Bluetooth variabelen
    private final String MAC_ADDRESS = "2C:BC:BB:A7:A9:72"; // MAC ADRES ESP
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;
    private OutputStream outStream;
    private TextView statusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bestaande UI elementen
        EditText editS = findViewById(R.id.editS);
        EditText editM = findViewById(R.id.editM);
        EditText editW = findViewById(R.id.editW);
        EditText editSD = findViewById(R.id.editSD);
        Button btnSend = findViewById(R.id.btnSend);
        statusLabel = findViewById(R.id.statusLabel);

        // De 5 nieuwe vak-knoppen koppelen
        Button btnVak1 = findViewById(R.id.btnVak1);
        Button btnVak2 = findViewById(R.id.btnVak2);
        Button btnVak3 = findViewById(R.id.btnVak3);
        Button btnVak4 = findViewById(R.id.btnVak4);
        Button btnVak5 = findViewById(R.id.btnVak5);

        connectBluetooth();

        // Bestaande verzend-knop voor tekstvelden
        btnSend.setOnClickListener(v -> {
            String payload = editS.getText().toString() + "," +
                    editM.getText().toString() + "," +
                    editW.getText().toString() + "," +
                    editSD.getText().toString() + "\n";
            sendData(payload);
        });

        // ClickListener voor de 5 vakjes
        View.OnClickListener vakListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vakNummer = "";
                int id = v.getId();

                if (id == R.id.btnVak1) vakNummer = "1";
                else if (id == R.id.btnVak2) vakNummer = "2";
                else if (id == R.id.btnVak3) vakNummer = "3";
                else if (id == R.id.btnVak4) vakNummer = "4";
                else if (id == R.id.btnVak5) vakNummer = "5";

                // We sturen "VAK:X" zodat de ESP het makkelijk kan filteren
                sendData("VAK:" + vakNummer + "\n");
            }
        };

        // De listener koppelen aan de knoppen
        btnVak1.setOnClickListener(vakListener);
        btnVak2.setOnClickListener(vakListener);
        btnVak3.setOnClickListener(vakListener);
        btnVak4.setOnClickListener(vakListener);
        btnVak5.setOnClickListener(vakListener);
    }

    private void connectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            statusLabel.setText("Status: Bluetooth niet ondersteund");
            return;
        }

        BluetoothDevice device = adapter.getRemoteDevice(MAC_ADDRESS);
        new Thread(() -> {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                outStream = socket.getOutputStream();
                runOnUiThread(() -> statusLabel.setText("Status: Verbonden!"));
            } catch (IOException e) {
                runOnUiThread(() -> statusLabel.setText("Status: Verbinding mislukt"));
            }
        }).start();
    }

    private void sendData(String msg) {
        if (outStream != null) {
            try {
                outStream.write(msg.getBytes());
                Toast.makeText(this, "Verzonden: " + msg.trim(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                runOnUiThread(() -> statusLabel.setText("Fout bij verzenden"));
            }
        } else {
            Toast.makeText(this, "Niet verbonden met ESP!", Toast.LENGTH_SHORT).show();
        }
    }
}