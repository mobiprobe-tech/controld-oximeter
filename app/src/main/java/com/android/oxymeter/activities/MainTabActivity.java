package com.android.oxymeter.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.android.oxymeter.R;
import com.android.oxymeter.ble.BluetoothLeService;
import com.android.oxymeter.ble.DataParser;
import com.android.oxymeter.ble.DeviceScanActivity;
import com.android.oxymeter.fragments.ui.data_tab.DataFragment;
import com.android.oxymeter.fragments.ui.data_tab.DataViewModel;
import com.android.oxymeter.fragments.ui.history_tab.HistoryFragment;
import com.android.oxymeter.fragments.ui.users_tab.UsersFragment;
import com.android.oxymeter.interfaces.OnLocationFoundListener;
import com.android.oxymeter.room_db.History.ReadingsTable;
import com.android.oxymeter.room_db.History.ReadingsViewModel;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.room_db.History.SessionViewModel;
import com.android.oxymeter.room_db.OxymeterDatabase;
import com.android.oxymeter.utilities.BottomBarAdapter;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.utilities.CustomViewPager;
import com.android.oxymeter.utilities.LocationUtils;
import com.android.oxymeter.web_services.GetSubUsers.GetSubUsersAPI;
import com.android.oxymeter.web_services.GetSubUsers.GetSubUsers_ViewModelCallback;
import com.android.oxymeter.work_manager.SyncSessionWorker;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.android.oxymeter.ble.SampleGattAttributes.OXYGEN_RATE_MEASUREMENT;
import static com.android.oxymeter.ble.SampleGattAttributes.UUID_CHARACTER_RECEIVE_OXYGEN;
import static com.android.oxymeter.utilities.Constants.EXTRAS_DEVICE_ADDRESS;
import static com.android.oxymeter.utilities.Constants.EXTRAS_SELECTED_RECORDING_TYPE;
import static com.android.oxymeter.utilities.Constants.REQUEST_SESSION_CONSTRAINTS;
import static com.android.oxymeter.utilities.LocationUtils.REQUEST_CHECK_SETTINGS;
import static com.android.oxymeter.utilities.LocationUtils.REQUEST_FIND_LOCATION;

public class MainTabActivity extends BaseActivity implements DataFragment.OnDataFragmentInteractionListener {

    private final static String TAG = MainTabActivity.class.getSimpleName();

    private boolean isDoubleBackToExitPressedOnce;

    private CustomViewPager viewPager;
    BottomNavigationView bottomNavigationView;

    DataFragment dataFragment;

    private static final int REQUEST_SCAN_DEVICE = 997;

    private LocationUtils locationPermissionUtils;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private DataParser mDataParser;
    private String mDeviceAddress;
    private BluetoothGattCharacteristic chReceiveData;

    private DataViewModel dataViewModel;

    ///USERS
    private GetSubUsersAPI mGetSubUsersAPI;


    //SESSION
    private String mSelectedUserID;
    private long mStartTime;
    private long mCurrentSessionLocalID = 0;
    private long maxSessionRecordTimeMillis;
    private boolean mTimeBoundRecordingIsSelected = false, mTestRecordingSuccessDialogVisible = false;

    private SessionViewModel sessionViewModel;
    private SessionTable sessionTable;
    private ReadingsViewModel readingsViewModel;
    private ArrayList<ReadingsTable> readingsTableArrayList;

    private boolean isRecording = false;
    double avgPulse = 0.0, avgSpO2 = 0.0, avgPI = 0.0;

    int sPo2 = 0, pulse = 0;
    double pi = 0.0;

    //WorkManager
    private Constraints constraints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        dataViewModel = new ViewModelProvider(MainTabActivity.this).get(DataViewModel.class);
        sessionViewModel = new ViewModelProvider(MainTabActivity.this).get(SessionViewModel.class);
        readingsViewModel = new ViewModelProvider(MainTabActivity.this).get(ReadingsViewModel.class);

        setUpToolbar();
        initializeView();

        getUsersList(true);

        LocalBroadcastManager.getInstance(Objects.requireNonNull(MainTabActivity.this))
                .registerReceiver(updateUsersListBroadcast, new IntentFilter(Constants.INTENT_ACTION_UPDATE_USERS_LIST));

        // Create a Constraints object that defines when the task should run in WorkManager
        constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

    }

    @Override
    public void onBackPressed() {

        if (isDoubleBackToExitPressedOnce) {
            MainTabActivity.this.finish();
            return;
        }

        this.isDoubleBackToExitPressedOnce = true;
        CommonUtils.showSmallToast(getApplicationContext(), getResources().getString(R.string.press_again_to_exit));

        new Handler().postDelayed(() -> isDoubleBackToExitPressedOnce = false, 2000);

    }

    @Override
    public void setUpToolbar() {

        try {
            Toolbar mToolBar = findViewById(R.id.toolbar);
            setSupportActionBar(mToolBar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.app_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initializeView() {

        dataFragment = new DataFragment();

        bottomNavigationView = findViewById(R.id.nav_view);
        viewPager = findViewById(R.id.viewPager);

        //optimisation
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);

        BottomBarAdapter pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        pagerAdapter.addFragments(dataFragment);
        pagerAdapter.addFragments(new HistoryFragment());
        pagerAdapter.addFragments(new UsersFragment());

        viewPager.setAdapter(pagerAdapter);

        //Handling the tab clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    viewPager.setCurrentItem(2);
                    return true;

            }
            return false;
        });

        maxSessionRecordTimeMillis = getResources().getInteger(R.integer.max_record_session_time_minutes) * 60000;

        mTestRecordingSuccessDialogVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (CommonUtils.checkData(mDeviceAddress).isEmpty() || chReceiveData == null) {

            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);


        } else {

            if (mConnected) {
                menu.findItem(R.id.action_disconnect).setVisible(true);
                menu.findItem(R.id.action_search).setVisible(false);

            } else {
                menu.findItem(R.id.action_search).setVisible(true);
                menu.findItem(R.id.action_disconnect).setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:

                disconnectBLE();
                checkBluetoothSupport();

                return true;

            case R.id.action_disconnect:

                CommonUtils.showAlertWithTwoCustomButton(MainTabActivity.this, "", getResources().getString(R.string.sure_to_disconnect), getResources().getString(R.string.yes), getResources().getString(R.string.no), this::disconnectBLE, () -> {

                });

                return true;

            case R.id.action_logout:

                CommonUtils.showAlertWithTwoCustomButton(MainTabActivity.this, "", getResources().getString(R.string.sure_to_logout), getResources().getString(R.string.yes), getResources().getString(R.string.no), () -> {

                    disconnectBLE();

                    AuthUI.getInstance()
                            .signOut(getApplicationContext())
                            .addOnCompleteListener(task -> {

                                CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.logout_success));

                                try {
                                    OxymeterDatabase.getDatabase(getApplicationContext()).clearAllTables();//Clear the local database
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //Do nothing
                                }

                                SharedPreferences sharedPref = getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                                sharedPref.edit().clear().apply();



                            });


                }, () -> {
                    //do nothing
                });


                return true;

            case R.id.action_profile:

                if (CommonUtils.isNetworkAvailable(MainTabActivity.this)) {

                    Intent intent = new Intent(MainTabActivity.this, ProfileDetailsActivity.class);
                    intent.putExtra(Constants.EXTRAS_TITLE, getResources().getString(R.string.menu_profile));
                    startActivity(intent);
                } else {
                    CommonUtils.showLongToast(MainTabActivity.this, getResources().getString(R.string.no_internet));
                }

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();


        // registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

       /* unregisterReceiver(mGattUpdateReceiver);

        if (locationPermissionUtils != null) {
            locationPermissionUtils = null;
        }

        disconnectBLE();*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDataParser != null) {
            mDataParser.stop();
        }

        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (locationPermissionUtils != null) {
            locationPermissionUtils = null;
        }

        disconnectBLE();

        try {
            unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
            //Do nothing
        }

        mBluetoothLeService = null;
        mDeviceAddress = null;

        if (mGetSubUsersAPI != null) {
            mGetSubUsersAPI.cancelRequestAPI();
        }
    }


    private void checkBluetoothSupport() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.ble_not_supported));
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter mBluetoothAdapter = null;

        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (bluetoothManager == null || mBluetoothAdapter == null) {
            CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.error_bluetooth_not_supported));
        } else {
            getCurrentLocation();
        }

    }


    /**
     * Method to get current location of user
     */
    private void getCurrentLocation() {
        OnLocationFoundListener locationFoundListener = new OnLocationFoundListener() {
            @Override
            public void onLocationFound(double latitude, double longitude) {

                mDeviceAddress = null;
                invalidateOptionsMenu();
                clearUI();

                Intent intent = new Intent(MainTabActivity.this, DeviceScanActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_DEVICE);
            }

            @Override
            public void onLocationNotFound() {
                CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.error_gps_required));
            }
        };
        locationPermissionUtils = new LocationUtils(MainTabActivity.this, locationFoundListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FIND_LOCATION) {
            locationPermissionUtils.onRequestPermissionsResult(requestCode, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {

            locationPermissionUtils.onActivityResult(requestCode, resultCode);

        } else if (requestCode == REQUEST_SCAN_DEVICE && resultCode == RESULT_OK) {

            //String mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
            mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);

            Intent gattServiceIntent = new Intent(MainTabActivity.this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


            mDataParser = new DataParser(new DataParser.onPackageReceivedListener() {
                @Override
                public void onOxiParamsChanged(final DataParser.OxiParams params) {
                    runOnUiThread(() -> {

                        sPo2 = params.getSpo2();
                        pulse = params.getPulseRate();
                        pi = params.getPi();

                        dataViewModel.setSpo2(sPo2);
                        dataViewModel.setPulseRate(pulse);
                        dataViewModel.setPi(pi);

                    });
                }

                @Override
                public void onPlethWaveReceived(final int amp) {
                    runOnUiThread(() -> {

                        dataViewModel.setAmp(amp);

                        long totalDuration = Calendar.getInstance().getTimeInMillis() - mStartTime;
                        dataViewModel.setTimeElapsed(CommonUtils.millisToMinutes(totalDuration));

                        if (mTimeBoundRecordingIsSelected && totalDuration >= maxSessionRecordTimeMillis) {
                            stopSession(true);
                            return;
                        }

                        if (isRecording && sPo2 != 0 && sPo2 < 127 && pulse != 0 && pulse < 255) {

                            ReadingsTable readings = new ReadingsTable();

                            readings.setmStartTime(mStartTime);
                            readings.setmSpO2(sPo2);
                            readings.setmPulse(pulse);
                            readings.setmPI(pi);

                            readingsViewModel.addReading(readings);
                        }

                    });
                }
            });

            mDataParser.start();

        } else if (requestCode == REQUEST_SESSION_CONSTRAINTS) {
            if (resultCode == RESULT_OK) {

                mSelectedUserID = CommonUtils.checkData(data.getStringExtra(Constants.EXTRAS_SELECTED_USER_ID));
                mTimeBoundRecordingIsSelected = data.getBooleanExtra(EXTRAS_SELECTED_RECORDING_TYPE, false);

                startSession();

            } else {
                isRecording = false;
                dataFragment.setButtonVisibility(isRecording);
            }
        }

    }

    /**
     * Disconnect Ble Device
     */
    private void disconnectBLE() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }

        try {
            stopSession(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDeviceAddress = null;
        invalidateOptionsMenu();
        clearUI();
    }


    /**
     * Set values in viewModel to null in order to clear the UI when device is not connected
     */
    private void clearUI() {

        dataViewModel.setSpo2(null);
        dataViewModel.setPulseRate(null);
        dataViewModel.setAmp(null);
        dataViewModel.setPi(null);

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                CommonUtils.myLog(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                stopSession(false);
                invalidateOptionsMenu();
                clearUI();
                chReceiveData = null;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                initCharacteristic();
                mBluetoothLeService.setCharacteristicNotification(chReceiveData, true);

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //Do nothing
                // CommonUtils.myLog(TAG, "ACTION_DATA_AVAILABLE: " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_SPO2_DATA_AVAILABLE.equals(action)) {

                mDataParser.add((Objects.requireNonNull(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA))));
            }
        }
    };

    public void initCharacteristic() {
        List<BluetoothGattService> services =
                mBluetoothLeService.getSupportedGattServices();
        BluetoothGattService mDataService = null;
        chReceiveData = null;

        if (services == null) {
            return;
        }

        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(UUID.fromString(OXYGEN_RATE_MEASUREMENT))) {
                mDataService = service;
            }
        }

        if (mDataService != null) {
            List<BluetoothGattCharacteristic> characteristics =
                    mDataService.getCharacteristics();
            for (BluetoothGattCharacteristic ch : characteristics) {
                if (ch.getUuid().equals(UUID_CHARACTER_RECEIVE_OXYGEN)) {
                    chReceiveData = ch;

                    invalidateOptionsMenu();
                }

            }
        }

    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_SPO2_DATA_AVAILABLE);
        return intentFilter;
    }


    // ****************************************** USERS *******************************************/

    /**
     * call web API to get list of sub-users
     *
     * @param showProgress TRUE shows progressbar otherwise FALSE
     */
    public void getUsersList(boolean showProgress) {

        if (CommonUtils.isNetworkAvailable(Objects.requireNonNull(MainTabActivity.this)) && mGetSubUsersAPI == null) {

            mGetSubUsersAPI = new GetSubUsersAPI(MainTabActivity.this, showProgress, new GetSubUsers_ViewModelCallback() {

                @Override
                public void onSuccess() {
                    mGetSubUsersAPI = null;

                }

                @Override
                public void onNoRecordFound(String errorMessage) {
                    mGetSubUsersAPI = null;
                    //Do nothing
                }

                @Override
                public void onFailure(String errorMessage) {
                    mGetSubUsersAPI = null;
                    //Do nothing
                }
            });

        }

    }


    private final BroadcastReceiver updateUsersListBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            try {
                Objects.requireNonNull(MainTabActivity.this).runOnUiThread(() -> {
                    try {
                        int flag = intent.getIntExtra(Constants.KEY_FLAG, -1);
                        if (flag == 1) {
                            getUsersList(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                //do nothing
            }
        }
    };


    /*********************  Session recording ******************************/


    @Override
    public void onStartSession() {
        if (mConnected) {

            Intent intent = new Intent(MainTabActivity.this, SessionConstraintsDialogActivity.class);
            startActivityForResult(intent, REQUEST_SESSION_CONSTRAINTS);

        } else {
            CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.device_disconnected));
        }
    }

    @Override
    public void onStopSession() {
        stopSessionAlertDialog();
    }


    private void startSession() {

        mCurrentSessionLocalID = 0;

        mStartTime = Calendar.getInstance().getTimeInMillis();

        isRecording = true;

        dataFragment.setButtonVisibility(isRecording);
        sessionTable = new SessionTable();

        sessionTable.setmUserId(mSelectedUserID);
        sessionTable.setmSelf(mSelectedUserID.isEmpty() ? 1 : 0);
        sessionTable.setmStartTime(mStartTime);
        sessionTable.setmSync(0);
        sessionTable.setmSessionID("");
        sessionTable.setmIsTimeBoundRecording(mTimeBoundRecordingIsSelected ? 1 : 0);

        DecimalFormat df2 = new DecimalFormat("#.##");

        readingsViewModel.getAllReadings(mStartTime).observe(MainTabActivity.this, readingsTables -> {

            if (readingsTableArrayList == null) {
                readingsTableArrayList = new ArrayList<>();
            }

            readingsTableArrayList.clear();

            readingsTableArrayList.addAll(readingsTables);

            if (readingsTableArrayList.size() > 0) {

                int pulse = 0, sPo2 = 0;
                double pi = 0.0;

                for (int i = 0; i < readingsTableArrayList.size(); i++) {
                    pulse = pulse + readingsTableArrayList.get(i).getmPulse();
                    sPo2 = sPo2 + readingsTableArrayList.get(i).getmSpO2();
                    pi = pi + readingsTableArrayList.get(i).getmPI();
                }

                avgPulse = (double) (pulse / readingsTableArrayList.size());
                avgSpO2 = (double) (sPo2 / readingsTableArrayList.size());
                avgPI = pi / readingsTableArrayList.size();

                BigDecimal bd = new BigDecimal(avgPI).setScale(2, RoundingMode.HALF_UP);
                avgPI = bd.doubleValue();


            }
        });
    }

    private void stopSessionAlertDialog() {

        CommonUtils.showAlertWithTwoCustomButton(MainTabActivity.this, "", getResources().getString(R.string.sure_to_stop_session), getResources().getString(R.string.yes), getResources().getString(R.string.no), () -> stopSession(false), () -> {
            //Do nothing
        });
    }

    //Show success dialog after completion of test recording
    private void testSessionRecordedDialog(long localSessionId) {

        mTestRecordingSuccessDialogVisible = true;

        CommonUtils.showAlertWithSingleCustomButton(MainTabActivity.this, "", getResources().getString(R.string.test_recording_success, getResources().getInteger(R.integer.max_record_session_time_minutes)), getResources().getString(R.string.ok), new CommonUtils.OnAlertOkClickListener() {
            @Override
            public void onOkButtonClicked() {
                MainTabActivity.this.syncSession(localSessionId);
            }
        });

    }

    private void stopSession(boolean showSuccessDialog) {

        CommonUtils.myLog("stopSession", "mTestRecordingSuccessDialogVisible: " + mTestRecordingSuccessDialogVisible);

        /*if (mTestRecordingSuccessDialogVisible) {
            syncSession(mCurrentSessionLocalID);

            return;
        }*/

        if (sessionTable != null) {

            if (readingsViewModel.getAllReadings(mStartTime).hasActiveObservers()) {
                readingsViewModel.getAllReadings(mStartTime).removeObservers(MainTabActivity.this);
            }

            long mEndTime = Calendar.getInstance().getTimeInMillis();

            long duration = mEndTime - mStartTime;

            if (mTimeBoundRecordingIsSelected && duration > maxSessionRecordTimeMillis) {
                duration = maxSessionRecordTimeMillis;
            }

            sessionTable.setmEndTime(mEndTime);
            sessionTable.setmDuration(duration);
            sessionTable.setmAveragePulse(avgPulse);
            sessionTable.setmAverageSpO2(avgSpO2);
            sessionTable.setmAveragePI(avgPI);

            //  sessionViewModel.insertSession(sessionTable);

            mCurrentSessionLocalID = OxymeterDatabase.getDatabase(getApplicationContext()).sessionDao().insertSession(sessionTable);

            CommonUtils.myLog("MainTabActivity", " Stop Direct: " + mCurrentSessionLocalID);

            if (mCurrentSessionLocalID == 0) {

                final Handler handler = new Handler();
                handler.postDelayed(() -> {

                    mCurrentSessionLocalID = sessionViewModel.getSessionLocalIdByTimeStamp(mStartTime);
                    CommonUtils.myLog("MainTabActivity", "stopSession mCurrentSessionLocalID: " + mCurrentSessionLocalID + showSuccessDialog);

                   /* if (showSuccessDialog) {
                        testSessionRecordedDialog(mCurrentSessionLocalID);
                    } else {*/
                    syncSession(mCurrentSessionLocalID);
                    //}

                    //    mCurrentSessionLocalID = 0;
                    sessionTable = null;

                }, 1000);
            } else {

                CommonUtils.myLog("stopSession", "showSuccessDialog: " + showSuccessDialog);

               /* if (showSuccessDialog) {
                    testSessionRecordedDialog(mCurrentSessionLocalID);
                } else {*/
                syncSession(mCurrentSessionLocalID);
                // }

                //  mCurrentSessionLocalID = 0;
                sessionTable = null;
            }

            isRecording = false;
            dataFragment.setButtonVisibility(isRecording);
        }


    }

    private void syncSession(long localSessionId) {

        CommonUtils.myLog("syncSession", "mTestRecordingSuccessDialogVisible: " + mTestRecordingSuccessDialogVisible);

        mTestRecordingSuccessDialogVisible = false;

        boolean showNotification = false; // display notification

        if (!CommonUtils.isNetworkAvailable(MainTabActivity.this)) {

            showNotification = true;
            CommonUtils.showAlertWithSingleCustomButton(MainTabActivity.this, "", getResources().getString(R.string.no_internet_sync_later), getResources().getString(R.string.ok), () -> {
                //do nothing
            });
        } else {
            CommonUtils.showProgress(MainTabActivity.this, getResources().getString(R.string.progress_msg_syncing));
        }

        /* Set input for WorkManager */
        Data.Builder builder = new Data.Builder();
        builder.putLong(Constants.EXTRAS_LOCAL_SESSION_ID, localSessionId)
                .putBoolean(Constants.EXTRAS_SHOW_PROGRESS_DIALOG, showNotification)
                .build();

             /*
          Setup WorkManager for syncing current session on server
         */
        OneTimeWorkRequest syncSessionWorkRequest = new OneTimeWorkRequest.Builder(SyncSessionWorker.class)
                .setInitialDelay(Constants.WORK_MANAGER_INITIAL_DELAY, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .addTag(Constants.SYNC_WORKER_TAG)
                .setInputData(builder.build())
                .build();

        WorkManager.getInstance().enqueueUniqueWork(String.valueOf(localSessionId), ExistingWorkPolicy.REPLACE, syncSessionWorkRequest);


        if (!showNotification) {

            WorkManager.getInstance(MainTabActivity.this).getWorkInfoByIdLiveData(syncSessionWorkRequest.getId())
                    .observe(MainTabActivity.this, workInfo -> {
                        if (workInfo != null) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                CommonUtils.dismissProgress();
                                CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.sync_success));

                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                                CommonUtils.dismissProgress();
                                CommonUtils.showSmallToast(MainTabActivity.this, getResources().getString(R.string.sync_failed));
                            }

                        }
                    });

        }
    }

}
