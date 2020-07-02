package com.example.bluetoothconnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG="MainActivity";
    BluetoothAdapter mbluetoothadapter;
    Connection mBluetoothConnection;

    Button btnStartConnection;
    Button btnSend;
    TextView incomingMessages;
    StringBuilder messages;
    EditText edSend;
    private static final UUID MY_UUID_INSECURE=
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothDevice mBTDevice;
    Button btnEnableDisable_Discoverable;
    public ArrayList<BluetoothDevice>mBTDevices=new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mbluetoothadapter.ACTION_STATE_CHANGED)){
                final int state= intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mbluetoothadapter.ERROR);
                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onReceive:STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"mBroadcastReceiver1:STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"mBroadcastReceiver1:STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"mBroadcastReceiver1:STATE TURNING ON");
                        break;

                }
            }
        }
    };
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() 
    {
        @Override
        public void onReceive(Context context, Intent intent) {
           final String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
              int mode=intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR); 
            switch (mode)
            {
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                    Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                    break;
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                    Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.Able to receive connection. ");
                    break;
                case BluetoothAdapter.SCAN_MODE_NONE:
                    Log.d(TAG, "mBroadcastReceiver2:Discoveabilty Disabled.Not able to receive connection. ");
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    Log.d(TAG, "mBroadcastReceiver2:Connecting..... ");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.d(TAG, "mBroadcastReceiver2:Connected. ");
                    break;
            }
            }
        }
    };
    private  BroadcastReceiver mBroadcastReceiver3= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          final String action=intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
               BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: "+device.getName()+":"+device.getAddress());
                mDeviceListAdapter=new DeviceListAdapter(context, R.layout.device_adapter_view,mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver4=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice mDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDED)
                {
                    Log.d(TAG, "BroadCastReceiver: BOND_BONDED");
                    mBTDevice=mDevice;
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "BroadCastReceiver: BOND_BONDING");
                }
                if(mDevice.getBondState()==BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "BroadCastReceiver: BOND_NONE");
                }
            }

        }
    };


    @Override
    protected void onDestroy()
    {
    Log.d(TAG, "onDestroy: called");
    super.onDestroy();
    unregisterReceiver(mBroadcastReceiver1);
    unregisterReceiver(mBroadcastReceiver2);
    unregisterReceiver(mBroadcastReceiver3);
    unregisterReceiver(mBroadcastReceiver4);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF=(Button)findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable=(Button)findViewById(R.id.btnDiscoverable_ON_OFF);
        lvNewDevices=(ListView)findViewById(R.id.lvnewDevices);
        mBTDevices=new ArrayList<>();

        btnStartConnection=(Button)findViewById(R.id.btnStartConnection);
        btnSend=(Button)findViewById(R.id.btnSend);
        edSend=(EditText)findViewById(R.id.editText);

        incomingMessages=(TextView)findViewById(R.id.incomingMessage);
        messages=new StringBuilder();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incoming messages"));

        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
         registerReceiver(mBroadcastReceiver4,filter);

        mbluetoothadapter=BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();

            }
        });
        btnStartConnection.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                byte[] bytes=edSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                edSend.setText("");
            }
        });
    }
     BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Message Received!");
            String text=intent.getStringExtra("theMessage");
            messages.append(text + "\n" );
            incomingMessages.setText(messages);

        }
    };

    public void startConnection()
    {

        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid)
    {

        Log.d(TAG, "setBtnStartConnection: Initialing RFCOM BLuetooth connection");
        mBluetoothConnection.startClient(device,uuid);
    }
    public void enableDisableBT()
    {
        if(mbluetoothadapter==null)
        {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities ");
        }
        if(!mbluetoothadapter.isEnabled())
        {
            Log.d(TAG, "enableDisableBT: Enabling BT");
            Intent enableBTIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);

        }
        if(mbluetoothadapter.isEnabled())
        {
            Log.d(TAG, "enableDisableBT: Disabling BT");
            mbluetoothadapter.disable();
            IntentFilter BTIntent=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1,BTIntent);

        }
    }

    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making Device discoverable for 300 seconds");

        Intent discoverableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);

        IntentFilter intentfilter=new IntentFilter(mbluetoothadapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentfilter);
    }


    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices");
        if(mbluetoothadapter.isDiscovering())
        {
            mbluetoothadapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling Discovery");
            checkBTPermissions();
            mbluetoothadapter.startDiscovery();
            IntentFilter discoverableDevicesIntent=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverableDevicesIntent);

        }
        if(!mbluetoothadapter.isDiscovering())
        {
           checkBTPermissions();
           mbluetoothadapter.startDiscovery();
           IntentFilter discoverableDevicesIntent=new IntentFilter(BluetoothDevice.ACTION_FOUND);
           registerReceiver(mBroadcastReceiver3,discoverableDevicesIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions()
    {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck=this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck+=this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck!=0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }}
        else{
            Log.d(TAG, "checkBTPermissions: No Need to check permissions.SDK version< LOLLIPOP");
        }

    }


    @Override
    public void onItemClick(AdapterView<?>adapterView,View view,int i,long l)
    {
        mbluetoothadapter.cancelDiscovery();
        Log.d(TAG, "onItemClick: You Clicked on a Device");
        String deviceName=mBTDevices.get(i).getName();
        String deviceAddress=mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName"+deviceName);
        Log.d(TAG, "onItemClick: deviceAddress"+deviceAddress);

        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG, "Trying to pair with "+deviceName);
            mBTDevices.get(i).createBond();
            mBTDevice=mBTDevices.get(i);
            mBluetoothConnection=new Connection(MainActivity.this);
        }
    }


}

