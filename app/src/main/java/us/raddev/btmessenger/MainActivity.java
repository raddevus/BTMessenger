package us.raddev.btmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.UUID;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    final static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter btAdapter;
    BtHandler btHandler;
    Set<BluetoothDevice> pairedDevices;
    public static String btCurrentDeviceName;
    private Spinner btDeviceSpinner;
    private static ArrayAdapter<String> adapter;
    ArrayList<String> listViewItems = new ArrayList<String>();
    private EditText textToSend;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewItems);
        btDeviceSpinner = (Spinner) findViewById(R.id.btDeviceSpinner);
        btDeviceSpinner.setAdapter(adapter);

        textToSend = (EditText)findViewById(R.id.textToSend);
        sendButton = (Button)findViewById(R.id.sendButton);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            pairedDevices = GetPairedDevices(btAdapter);
            //DiscoverAvailableDevices();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (MainActivity.btCurrentDeviceName == ""){
                    return;
                }
                sendTextViaBT();
                writeData();
            }
        });

        btDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                btCurrentDeviceName = String.valueOf(btDeviceSpinner.getSelectedItem());
                //saveDeviceNamePref();
                Log.d("MainActivity", "DeviceInfo : " + btCurrentDeviceName);
                //logViewAdapter.add("DeviceInfo : " + btCurrentDeviceName);
                //logViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }



    private void sendTextViaBT(){
        if (btAdapter == null) {
            btAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else
        {
            Log.d("MainActivity", "no bt adapter available");
            return; // cannot get btadapter
        }

        if (pairedDevices == null) {
            pairedDevices = btAdapter.getBondedDevices();
        }
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice btItem : pairedDevices) {
                if (btItem != null) {
                    String name = btItem.getName();
                    if (name.equals(MainActivity.btCurrentDeviceName)) {
                        UUID uuid = btItem.getUuids()[0].getUuid();
                        Log.d("MainActivity", uuid.toString());
                        if (btHandler == null) {
                            btHandler = new BtHandler(btItem, uuid, null);
                        }
                        btHandler.run(btAdapter);

                        return;
                    }
                }
            }
        }
    }

    private void writeData(){

        String outText = textToSend.getText().toString();
        Log.d("MainActivity", "sending text : " + outText);
        if (!outText.equals("")){
            btHandler.writeMessage(outText);
            try {
                Thread.sleep(200);
                btHandler.cancel();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                btHandler = null;
            }
        }
    }

    private Set<BluetoothDevice> GetPairedDevices(BluetoothAdapter btAdapter) {

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                adapter.add(device.getName());// + "\n" + device.getAddress());
            }
            adapter.notifyDataSetChanged();
        }
        return pairedDevices;
    }

    public  void DiscoverAvailableDevices(final ArrayAdapter<String>adapter, final ArrayAdapter<BluetoothDevice> otherDevices) {
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    //btDevice = device;
                    adapter.add(device.getName());// + "\n" + device.getAddress());
                    otherDevices.add(device);
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }
}
