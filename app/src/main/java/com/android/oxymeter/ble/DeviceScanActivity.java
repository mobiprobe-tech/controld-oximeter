/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.oxymeter.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.android.oxymeter.R;
import com.android.oxymeter.activities.BaseActivity;
import com.android.oxymeter.interfaces.OnLocationFoundListener;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.utilities.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.android.oxymeter.utilities.LocationUtils.REQUEST_CHECK_SETTINGS;
import static com.android.oxymeter.utilities.LocationUtils.REQUEST_FIND_LOCATION;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends BaseActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private ListView mListView;
    private ArrayList<BluetoothDevice> mLeDevicesList;

    private LocationUtils locationPermissionUtils;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        CommonUtils.myLog("DeviceScanActivity", "onCreate");

        setUpToolbar();
        initializeView();

        mHandler = new Handler();

        checkPermissionsAndStartScanning();

    }

    @Override
    public void setUpToolbar() {

        try {
            Toolbar mToolBar = findViewById(R.id.toolbar);
            setSupportActionBar(mToolBar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.title_devices);
            mToolBar.setNavigationOnClickListener(v -> onBackPressed());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeView() {
        mListView = findViewById(R.id.device_list_view);

        mListView.setOnItemClickListener((adapterView, view, position, l) -> {

            final BluetoothDevice device = mLeDevicesList.get(position);
            if (device == null) return;

            final Intent intent = new Intent();
            intent.putExtra(Constants.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, device.getAddress());

            if (mScanning) {
                mBluetoothLeScanner.stopScan(mLeScanCallback);
                mScanning = false;
            }

            setResult(RESULT_OK, intent);
            DeviceScanActivity.this.finish();

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.action_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.action_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                checkPermissionsAndStartScanning();
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonUtils.myLog("DeviceScanActivity", "onPause");
        scanLeDevice(false);

       /* if (mLeDevicesList == null) {
            mLeDevicesList = new ArrayList<>();
        }

        mLeDevicesList.clear();

        notifyBleDeviceListAdapter(mLeDevicesList);*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationPermissionUtils != null) {
            locationPermissionUtils = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CommonUtils.myLog("DeviceScanActivity", "onActivityResult: " + requestCode + "  " + resultCode);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            try {
                locationPermissionUtils.onActivityResult(requestCode, resultCode);
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.something_went_wrong));
            }
        } else if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode == RESULT_OK) {
                checkPermissionsAndStartScanning();
            } else {
                // UserTable chose not to enable Bluetooth.
                CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.error_bluetooth_required));
            }
        }

    }

    private void checkPermissionsAndStartScanning() {

        OnLocationFoundListener locationFoundListener = new OnLocationFoundListener() {
            @Override
            public void onLocationFound(double latitude, double longitude) {

                CommonUtils.myLog("DeviceScanActivity.this", "onLocationFound ");

                // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
                // BluetoothAdapter through BluetoothManager.
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

                if (bluetoothManager != null) {
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                }

                // Checks if Bluetooth is supported on the device.
                if (bluetoothManager == null || mBluetoothAdapter == null) {
                    CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.error_bluetooth_not_supported));
                    DeviceScanActivity.this.finish();
                    return;
                }

                CommonUtils.myLog("DeviceScanActivity.this", "onLocationFound mBluetoothAdapter: " + mBluetoothAdapter + "  mBluetoothLeScanner: " + mBluetoothLeScanner);

                if (mBluetoothAdapter != null) {

                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

                    // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
                    // fire an intent to display a dialog asking the user to grant permission to enable it.
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else if (mBluetoothLeScanner == null) {
                        CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        scanLeDevice(true);
                    }
                } else {
                    CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onLocationNotFound() {
                CommonUtils.showSmallToast(DeviceScanActivity.this, getResources().getString(R.string.error_gps_required));
            }
        };

        locationPermissionUtils = new LocationUtils(DeviceScanActivity.this, locationFoundListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FIND_LOCATION) {
            locationPermissionUtils.onRequestPermissionsResult(requestCode, grantResults);
        }
    }

    private void scanLeDevice(final boolean enable) {

        if (enable) {

            if (mBluetoothLeScanner == null) {
                return;
            }
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                mScanning = false;
                try {
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                    //do nothing
                }
                invalidateOptionsMenu();
            }, SCAN_PERIOD);

            mScanning = true;

            if (mLeDevicesList == null) {
                mLeDevicesList = new ArrayList<>();
            }

            mLeDevicesList.clear();

            //TODO

////
//            List<ScanFilter> scanFilterList = new ArrayList<>();
//
//          scanFilterList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(SampleGattAttributes.OXYGEN_RATE_MEASUREMENT))).build());

            /*ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString(SampleGattAttributes.OXYGEN_RATE_MEASUREMENT))
                    .build();
            scanFilterList.add(scanFilter);*/


            UUID BLP_SERVICE_UUID = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG_OXYGEN);
            UUID[] serviceUUIDs = new UUID[]{BLP_SERVICE_UUID};
            List<ScanFilter> scanFilterList = null;
            if (serviceUUIDs != null) {
                scanFilterList = new ArrayList<>();
                for (UUID serviceUUID : serviceUUIDs) {
                    ScanFilter filter = new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(serviceUUID))
                            .build();
                    scanFilterList.add(filter);
                }
            }


            ScanSettings builderScanSettings;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                builderScanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                        .setReportDelay(0L)
                        .build();
            } else {
                builderScanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(0L)
                        .build();
            }

            //  mBluetoothLeScanner.startScan(scanFilterList, builderScanSettings, mLeScanCallback);

            mBluetoothLeScanner.startScan(mLeScanCallback);
////

        } else {

            if (mScanning) {
                mScanning = false;

                if (mBluetoothLeScanner != null) {
                    mBluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }

        }

        invalidateOptionsMenu();
    }

    // Device scan callback.
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            CommonUtils.myLog("DeviceScanActivity", "onScanResult: " + result.getDevice().getName());

            if (!mLeDevicesList.contains(result.getDevice())) {

                mLeDevicesList.add(result.getDevice());
            }

            notifyBleDeviceListAdapter(mLeDevicesList);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            CommonUtils.myLog("DeviceScanActivity", "onBatchScanResults: " + results.size());

            for (ScanResult result : results) {
                if (!mLeDevicesList.contains(result.getDevice())) {
                    mLeDevicesList.add(result.getDevice());
                }
            }

            notifyBleDeviceListAdapter(mLeDevicesList);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            CommonUtils.showLongToast(DeviceScanActivity.this, "ScanFailed: " + errorCode);

            notifyBleDeviceListAdapter(mLeDevicesList);

        }

    };

    private void notifyBleDeviceListAdapter(ArrayList<BluetoothDevice> mDevicesList) {

        LeDeviceListAdapter mLeDeviceListAdapter = new LeDeviceListAdapter(DeviceScanActivity.this, mDevicesList);
        mListView.setAdapter(mLeDeviceListAdapter);
        mLeDeviceListAdapter.notifyDataSetChanged();

    }

}
