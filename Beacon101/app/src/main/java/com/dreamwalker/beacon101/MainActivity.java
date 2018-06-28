package com.dreamwalker.beacon101;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.dreamwalker.beacon101.BeaconConst.ALTBEACON_LAYOUT;
import static com.dreamwalker.beacon101.BeaconConst.EDDYSTONE_TLM_LAYOUT;
import static com.dreamwalker.beacon101.BeaconConst.EDDYSTONE_UID_LAYOUT;
import static com.dreamwalker.beacon101.BeaconConst.EDDYSTONE_URL_LAYOUT;
import static com.dreamwalker.beacon101.BeaconConst.EDDYSTON_BEACON_PARSER;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private BackgroundPowerSaver backgroundPowerSaver;

    BluetoothAdapter bluetoothAdapter;
    BeaconManager beaconManager;
    RegionBootstrap regionBootstrap;
    Region region;

    FloatingActionButton floatingActionButton;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MainActivity mainActivity = null;


    RecyclerView recyclerView;
    ArrayList<KNUBeacon>  beaconArrayList;
    BeaconDeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView  = (RecyclerView)findViewById(R.id.recycler_view);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        beaconArrayList = new ArrayList<>();

        checkPermission();
        getBluetoothAdapter();
        checkBluetoothAdapter(bluetoothAdapter);

        setBeaconManager("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
//        region = new Region("com.dreamwalker.beacon101.boostrapRegion", null, null, null);
//        regionBootstrap = new RegionBootstrap(this, region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);


        bindBeaconService(this);
        //beaconManager.bind(this);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (beaconManager.isBound(MainActivity.this)) {
                    floatingActionButton.setImageResource(R.drawable.ic_speaker_notes_black_24dp);
                    Log.i(TAG, "Stop BLE Scanning...");
                    beaconManager.unbind(MainActivity.this);
                } else {
                    floatingActionButton.setImageResource(R.drawable.ic_speaker_notes_off_black_24dp);
                    Log.i(TAG, "Start BLE Scanning...");
                    beaconManager.bind(MainActivity.this);
                }
            }
        });

    }

    private void bindBeaconService(BeaconConsumer beaconConsumer) {
        beaconManager.bind(beaconConsumer);
    }

    private void getBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void checkBluetoothAdapter(BluetoothAdapter adapter) {
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.e(TAG, "onCreate: getInstanceForApplication");
            //setBeaconManager("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
            // BEACON_PARSER 는 문자열인데요. iBeacon 이냐 EddyStone 이냐에 따라 다른 문자열을 필요로합니다.
            //BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        }
    }

    private void setBeaconManager(String beaconFilter) {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(beaconFilter));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTON_BEACON_PARSER));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_TLM_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT));
    }

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton("OK", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("OK", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }

                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JAICHANGPARK"));
                intent.setPackage("com.android.chrome");
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beaconArrayList.size() != 0){
                    beaconArrayList.clear();
                }
                if (beacons.size() > 0) {
                    Iterator<Beacon> iterator = beacons.iterator();

                    while (iterator.hasNext()) {
                       // beaconArrayList = new ArrayList<>();
                        Beacon beacon = iterator.next();
                        String address = beacon.getBluetoothAddress();
                        Log.e(TAG, "getBluetoothAddress: " + address );
                        double rssi = beacon.getRssi();
                        int txPower = beacon.getTxPower();
                        double distance = Double.parseDouble(decimalFormat.format(beacon.getDistance()));
//                        int major = beacon.getId2().toInt();
//                        int minor = beacon.getId3().toInt();
                        String major = beacon.getId2().toString();
                        String minor = beacon.getId3().toString();
                        String uuid = String.valueOf(beacon.getId1()).toUpperCase();

                        beaconArrayList.add(new KNUBeacon(beacon.getBluetoothName(), address,uuid, major, minor, String.format("%s m",String.valueOf(distance))));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            adapter = new BeaconDeviceAdapter(getApplicationContext(), beaconArrayList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

//                if (collection.size() > 0) {
//                    for (Beacon beacon : collection) {
//                        Log.e(TAG, "getServiceUuid: " + beacon.getServiceUuid());
//                        Log.e(TAG, "getBluetoothName: " + beacon.getBluetoothName());
//                        Log.e(TAG, "getBluetoothAddress: " + beacon.getBluetoothAddress());
//                        Log.e(TAG, "getParserIdentifier: " + beacon.getParserIdentifier());
//                        Log.e(TAG, "getBeaconTypeCode: " + beacon.getBeaconTypeCode());
//                        Log.e(TAG, "getDataFields: " + beacon.getDataFields());
//                        Log.e(TAG, "getDistance: " + beacon.getDistance());
//                        Log.e(TAG, "getExtraDataFields: " + beacon.getExtraDataFields());
//                        Log.e(TAG, "getId1: " + beacon.getId1().toString());
//                        Log.e(TAG, "getId2: " + beacon.getId2().toString());
//                        Log.e(TAG, "getId3: " + beacon.getId3().toString());
//
//                        double rssi = beacon.getRssi();
//                        int txPower = beacon.getTxPower();
//                        double distance = Double.parseDouble(decimalFormat.format(beacon.getDistance()));
//                        int major = beacon.getId2().toInt();
//                        int minor = beacon.getId3().toInt();
//
//                        Log.e(TAG, "getId1: " + rssi);
//                        Log.e(TAG, "txPower: " + txPower);
//                        Log.e(TAG, "distance: " + distance);
//                        Log.e(TAG, "major: " + major);
//                        Log.e(TAG, "minor: " + minor);
//
//                    }
//                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myMoniter", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                //Log.e(TAG, "didEnterRegion: " + region.getBluetoothAddress());
                Log.e(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                //Log.e(TAG, "didEnterRegion: " + region.getBluetoothAddress());
                Log.e(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.e(TAG, "I have just switched from seeing/not seeing beacons: " + i);
                //Log.e(TAG, "didEnterRegion: " + region.getBluetoothAddress());
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

//    @Override
//    public void didEnterRegion(Region region) {
//
//
////        Log.e(TAG, "didEnterRegion: ");
////
////        if (!haveDetectedBeaconsSinceBoot) {
////            Log.e(TAG, "auto launching MainActivity");
////            Intent intent = new Intent(this, MainActivity.class);
////            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
////            // to keep multiple copies of this activity from getting created if the user has
////            // already manually launched the app.
////            this.startActivity(intent);
////            haveDetectedBeaconsSinceBoot = true;
////        } else {
////            if (mainActivity != null) {
////                // If the Monitoring Activity is visible, we log info about the beacons we have
////                // seen on its display
////                mainActivity.logToDisplay("I see a beacon again" );
////            } else {
////                // If we have already seen beacons before, but the monitoring activity is not in
////                // the foreground, we send a notification to the user on subsequent detections.
////                Log.d(TAG, "Sending notification.");
////                sendNotification();
////            }
////        }
//
////        Log.d(TAG, "Got a didEnterRegion call");
////        regionBootstrap.disable();
////
////        Intent intent = new Intent(this, MainActivity.class);
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
////
////        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "1")
////                .setSmallIcon(R.drawable.ic_launcher_foreground)
////                .setContentTitle("My notification")
////                .setContentText("Hello World!")
////                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
////                // Set the intent that will fire when the user taps the notification
////                .setContentIntent(pendingIntent)
////                .setAutoCancel(true);
////
////        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
////
////// notificationId is a unique int for each notification that you must define
////        notificationManager.notify(1, mBuilder.build());
//
//
//        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
//        // if you want the Activity to launch every single time beacons come into view, remove this call.
////        regionBootstrap.disable();
////        Intent intent = new Intent(this, MainActivity.class);
////        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
////        // created when a user launches the activity manually and it gets launched from here.
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        this.startActivity(intent);
//
//
//    }
//
//    @Override
//    public void didExitRegion(Region region) {
//
//    }
//
//    @Override
//    public void didDetermineStateForRegion(int i, Region region) {
//
//    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "sex")
                        .setContentTitle("Beacon Reference Application")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.drawable.ic_launcher_foreground);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
