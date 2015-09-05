package physicalweb;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.UUID;

import saviour.Details;
import saviour.GetAddressTask;
import saviour.My_Database;
import tronbox.heineken.BluetoothHelper;
import tronbox.heineken.HexAsciiHelper;


public class PhysicalWebBrain extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private BluetoothDevice connectedDevice;
    private String bluetoothAddress;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private BluetoothGattDescriptor bluetoothGattDescriptor;

    public final static UUID UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x2220);
    public final static UUID UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x2221);
    public final static UUID UUID_SEND = BluetoothHelper.sixteenBitUuid(0x2222);
    public final static UUID UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);

    private String TAG = "PhysicalWebBrain";

    public  GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    IntentFilter intentFilter = new IntentFilter("SAVIOUR_BUSY");

    boolean state = false;

    private My_Database database;

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int i, byte[] bytes) {

            Log.w("DeviceNameIs", device.getName());

            if(device.getName().equals("Nikhil"))
            {

                Log.w("Dataaaaaaa", parseScanRecord(bytes));

                bluetoothAdapter.stopLeScan(leScanCallback); // Stop the scan as no longer needed
                bluetoothAddress = device.getAddress();

                connectedDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

                if(connectedDevice != null){

                    connectedDevice.connectGatt(getApplicationContext(), true, bluetoothGattCallback);

                }


                Log.w(TAG, "Device Found "+device.getName()+"_"+device.getAddress());


            } else {

                Log.w(TAG, "Device Not Found");

            }

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       Log.w("Operation", "Service has started");

        registerReceiver(broadcastReceiver, intentFilter);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){

            boolean value =  bluetoothAdapter.startLeScan(leScanCallback);
            Log.w(TAG, "Scan Result "+value);

        } else {


        }


        IntentFilter buttonFilter = new IntentFilter("Button_Pressed");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                send(new String("1").getBytes());

            }

        }, buttonFilter);


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {



        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if(newState == BluetoothProfile.STATE_CONNECTED){ // If connection to the remote device is active

                bluetoothGatt = gatt; // Store the BluetoothGatt for the future use.
                gatt.discoverServices(); // Retrieve all the Services being offered by the BLE.

            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){ // If connection to the remote device is active

                Log.w(TAG, "Saviour Disconnected");
                bluetoothAdapter.startLeScan(leScanCallback);

            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if(status == BluetoothGatt.GATT_SUCCESS){

                bluetoothGattService = gatt.getService(UUID_SERVICE); // UUID_SERVICE is the desired service.

                if(bluetoothGattService != null){ // Found your service.

                    Log.w(TAG, "My Service Found");

                    bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID_RECEIVE);

                    if(bluetoothGattCharacteristic != null){

                         bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(UUID_CLIENT_CONFIGURATION);

                        Log.w(TAG+"_Data", "My Characterstics Found");

                          if (bluetoothGattDescriptor != null)
                        {

                            gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);

                            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            gatt.writeDescriptor(bluetoothGattDescriptor);

                            gatt.readCharacteristic(bluetoothGattCharacteristic);

                        }



                    }
                }


            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if(status == BluetoothGatt.GATT_SUCCESS){

                String readData = HexAsciiHelper.bytesToString(characteristic.getValue());

                Log.w("Reading_Data", readData);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            String readData = HexAsciiHelper.bytesToString(characteristic.getValue());

            if(readData.equals("ON")){

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);

                state = true;
                googleApiClient.connect();

            }

            Log.w(TAG, readData);
        }
    };

    public boolean send(byte[] data) {
        if (bluetoothGatt == null || bluetoothGattService == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }

        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID_SEND);

        if (characteristic == null) {
            Log.w(TAG, "Send characteristic not found");
            return false;
        }

        Log.w(TAG, "Sending Data");

        characteristic.setValue(data);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return bluetoothGatt.writeCharacteristic(characteristic);
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

    }


    @Override
    public void onConnected(Bundle bundle) {


        startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(state == true){

            state = false;
            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
            new GetAddressTask(getApplicationContext()).execute(location);
        }
    }





    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            database = new My_Database(context, "Phone_DB", null, 7);

            Cursor familyCursor = database.getFriendsInfo();

            ArrayList<Details> list = new ArrayList<Details>();

            String sms = null;

            if(familyCursor.moveToFirst())
            {
                do {

                    list.add(new Details(familyCursor.getString(1),familyCursor.getString(2),familyCursor.getString(3)));

                }while (familyCursor.moveToNext());

            }

            if(list.size() > 0 && intent.getExtras().containsKey("Address")){

                String address = intent.getExtras().getString("Address");

                for(Details d : list){

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage("+91"+d.getPhone_Number(), null, d.getName()+" "+d.getMessage()+" at "+address, null, null);

                }
            }

        }
    };

    private static String parseScanRecord(byte[] scanRecord) {
        StringBuilder output = new StringBuilder();
        String data = "";

        int i = 0;
        while (i < scanRecord.length) {
            int len = scanRecord[i++] & 0xFF;
            if (len == 0) break;
            switch (scanRecord[i] & 0xFF) {
                // https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
                case 0x0A: // Tx Power
                    output.append("\n  Tx Power: ").append(scanRecord[i+1]);
                    break;
                case 0xFF: // Manufacturer Specific data (RFduinoBLE.advertisementData)
                    output.append("\n  Advertisement Data: ")
                            .append(HexAsciiHelper.bytesToHex(scanRecord, i + 3, len));

                    String ascii = HexAsciiHelper.bytesToAsciiMaybe(scanRecord, i + 3, len);
                    if (ascii != null) {
                        data = ascii;
                        output.append(" (\"").append(ascii).append("\")");
                    }
                    break;
            }
            i += len;
        }
        return data;
    }
}
