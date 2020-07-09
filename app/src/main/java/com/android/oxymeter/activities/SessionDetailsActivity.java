package com.android.oxymeter.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.android.oxymeter.room_db.History.ReadingsTable;
import com.android.oxymeter.room_db.History.ReadingsViewModel;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.room_db.History.SessionViewModel;
import com.android.oxymeter.room_db.OxymeterDatabase;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.work_manager.SyncSessionWorker;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SessionDetailsActivity extends BaseActivity {

    private String mUserID = "", mRemoteSessionID = "", mUserName;
    private long mStartTimeStamp = 0, mLocalSessionID = 0;
    private boolean isSynced = false;

    private AppCompatTextView mUserNameTextView, mSyncStatusTextView, mDateTextView, mDurationTextView, mPulseTextView, mSpO2TextView, mPiTextView, mRecordingTypeTextView;

    private GraphView mSessionGraphView;
    private LineGraphSeries<DataPoint> series;
    private double MAX_PULSE = 0;

    private SessionViewModel sessionViewModel;
    private ReadingsViewModel readingsViewModel;

    private List<ReadingsTable> readingsList;

    //WorkManager
    private Constraints constraints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        sessionViewModel = new ViewModelProvider(SessionDetailsActivity.this).get(SessionViewModel.class);
        readingsViewModel = new ViewModelProvider(SessionDetailsActivity.this).get(ReadingsViewModel.class);

        if (getIntent().hasExtra(Constants.EXTRAS_SELECTED_USER_NAME)) {
            mUserName = getIntent().getStringExtra(Constants.EXTRAS_SELECTED_USER_NAME);
        }

        if (getIntent().hasExtra(Constants.EXTRAS_START_TIMESTAMP)) {
            mStartTimeStamp = getIntent().getLongExtra(Constants.EXTRAS_START_TIMESTAMP, 0);
        }

        if (getIntent().hasExtra(Constants.EXTRAS_SELECTED_USER_ID)) {
            mUserID = getIntent().getStringExtra(Constants.EXTRAS_SELECTED_USER_ID);
        }

        if (getIntent().hasExtra(Constants.EXTRAS_REMOTE_SESSION_ID)) {
            mRemoteSessionID = getIntent().getStringExtra(Constants.EXTRAS_REMOTE_SESSION_ID);
        }

        if (getIntent().hasExtra(Constants.EXTRAS_LOCAL_SESSION_ID)) {
            mLocalSessionID = getIntent().getLongExtra(Constants.EXTRAS_LOCAL_SESSION_ID, 0);
        }

        setUpToolbar();
        initializeView();

        getSessionDetails();

        // Create a Constraints object that defines when the task should run in WorkManager
        constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        LocalBroadcastManager.getInstance(Objects.requireNonNull(SessionDetailsActivity.this))
                .registerReceiver(updateSessionStatusBroadcast, new IntentFilter(Constants.INTENT_ACTION_UPDATE_SESSION_STATUS));
    }

    @Override
    public void setUpToolbar() {
        try {
            Toolbar mToolBar = findViewById(R.id.toolbar);
            setSupportActionBar(mToolBar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.session));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mToolBar.setNavigationOnClickListener(v -> onBackPressed());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeView() {

        mUserNameTextView = findViewById(R.id.userTextView);
        mSyncStatusTextView = findViewById(R.id.syncStatusTextView);
        mDateTextView = findViewById(R.id.dateTextView);
        mDurationTextView = findViewById(R.id.durationTextView);
        mPulseTextView = findViewById(R.id.pulseTextView);
        mSpO2TextView = findViewById(R.id.sPo2TextView);
        mPiTextView = findViewById(R.id.piTextView);
        mRecordingTypeTextView = findViewById(R.id.recordingTypeTextView);

        mSessionGraphView = findViewById(R.id.graph);

        mSessionGraphView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sync_menu, menu);

        menu.findItem(R.id.action_sync).setVisible(!isSynced);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_sync) {
            syncSession();
        }

        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver updateSessionStatusBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            try {
                Objects.requireNonNull(SessionDetailsActivity.this).runOnUiThread(() -> {
                    try {
                        int flag = intent.getIntExtra(Constants.KEY_FLAG, -1);
                        if (flag == 1) {
                            getSessionDetails();
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

    private void getSessionDetails() {

        CommonUtils.showProgress(SessionDetailsActivity.this, getResources().getString(R.string.progress_msg_loading));

        try {

            SessionTable session = sessionViewModel.getSessionDetailsByLocalID(mLocalSessionID);

            if (session != null) {

                mUserNameTextView.setText(CommonUtils.checkData(mUserName));

                isSynced = session.getmSync() == 1;

                invalidateOptionsMenu();

                if (isSynced) {
                    mSyncStatusTextView.setTextColor(ContextCompat.getColor(SessionDetailsActivity.this, R.color.colorGreen));
                } else {
                    mSyncStatusTextView.setTextColor(ContextCompat.getColor(SessionDetailsActivity.this, R.color.bloodColor));
                }

                mSyncStatusTextView.setText(isSynced ? getResources().getString(R.string.synced) : getResources().getString(R.string.unsynced));

                mDateTextView.setText(CommonUtils.millisToDate(session.getmStartTime()));
                mDurationTextView.setText(CommonUtils.millisToMinutes(session.getmDuration()));
                mPulseTextView.setText(String.valueOf(session.getmAveragePulse()));
                mSpO2TextView.setText(String.valueOf(session.getmAverageSpO2()));
                mPiTextView.setText(String.valueOf(session.getmAveragePI()));

                String recordingType = session.getmIsTimeBoundRecording() == 1 ? getResources().getString(R.string.test_recording, getResources().getInteger(R.integer.max_record_session_time_minutes)) : getResources().getString(R.string.open_reading);
                mRecordingTypeTextView.setText(CommonUtils.checkData(recordingType));

                getReadings(session.getmStartTime());

                CommonUtils.dismissProgress();

            } else {
                CommonUtils.dismissProgress();
                CommonUtils.showSmallToast(SessionDetailsActivity.this, getResources().getString(R.string.something_went_wrong));
            }


        } catch (Exception e) {
            CommonUtils.dismissProgress();
            CommonUtils.showSmallToast(SessionDetailsActivity.this, getResources().getString(R.string.something_went_wrong));
            e.printStackTrace();
        }

    }

    private void getReadings(long timeStamp) {

        List<ReadingsTable> readingsTables = OxymeterDatabase.getDatabase(getApplicationContext()).readingsDao().getReadingsByStartTimeStamp(timeStamp);

        if (readingsList == null) {
            readingsList = new ArrayList<>();
        }

        readingsList.clear();

        if (readingsTables.size() > 0) {
            readingsList.addAll(readingsTables);

            // now we populate the samples on the graph...
//        series.appendData(new DataPoint(0,0 ), false, 5000);
            ArrayList<DataPoint> datapoints = new ArrayList<>();
            int i = 0;
            for (ReadingsTable currentSample : readingsList) {
                if (currentSample.getmPulse() > MAX_PULSE)
                    MAX_PULSE = currentSample.getmPulse();

                datapoints.add(new DataPoint(i, currentSample.getmPulse()));
                i++;
            }
            DataPoint[] dataPointArray = new DataPoint[datapoints.size()];
            datapoints.toArray(dataPointArray);
            series = new LineGraphSeries<>(dataPointArray);
            initGraph(i);
            mSessionGraphView.addSeries(series);

            mSessionGraphView.setVisibility(View.VISIBLE);

        } else {
            CommonUtils.showSmallToast(SessionDetailsActivity.this, getResources().getString(R.string.something_went_wrong));
        }

    }


    private void initGraph(int sampleCount) {

        //clear existing samples..
        mSessionGraphView.getViewport().setXAxisBoundsManual(true);
        mSessionGraphView.getViewport().setYAxisBoundsManual(true);
        mSessionGraphView.getViewport().setMinY((double) 40);
        mSessionGraphView.getViewport().setMaxY(MAX_PULSE + 30);
        mSessionGraphView.getViewport().setMaxX((double) sampleCount);
        mSessionGraphView.getViewport().setScrollable(true);

        mSessionGraphView.getViewport().setDrawBorder(true);
        //mSessionGraphView.getGridLabelRenderer().setNumHorizontalLabels(10);
        mSessionGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        mSessionGraphView.getGridLabelRenderer().setNumVerticalLabels(5);
        mSessionGraphView.getGridLabelRenderer().setVerticalLabelsVAlign(GridLabelRenderer.VerticalLabelsVAlign.ABOVE);
        mSessionGraphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        mSessionGraphView.getGridLabelRenderer().reloadStyles();
        series.setBackgroundColor(Color.argb(20, 165, 199, 255));
        series.setColor(Color.argb(255, 165, 199, 255));
        series.setDrawBackground(true);
        series.setThickness(4);

    }

    private void syncSession() {
        boolean showNotification = false; // display notification

        if (!CommonUtils.isNetworkAvailable(SessionDetailsActivity.this)) {

            showNotification = true;

            CommonUtils.showAlertWithSingleCustomButton(SessionDetailsActivity.this, "", getResources().getString(R.string.no_internet_sync_later), getResources().getString(R.string.ok), () -> {
                //do nothing
            });
        } else {
            CommonUtils.showProgress(SessionDetailsActivity.this, getResources().getString(R.string.progress_msg_syncing));
        }

        /* Set input for WorkManager */
        Data.Builder builder = new Data.Builder();
        builder.putLong(Constants.EXTRAS_LOCAL_SESSION_ID, mLocalSessionID)
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

        WorkManager.getInstance().enqueueUniqueWork(String.valueOf(mLocalSessionID), ExistingWorkPolicy.REPLACE, syncSessionWorkRequest);

        if (!showNotification) {

            WorkManager.getInstance(SessionDetailsActivity.this).getWorkInfoByIdLiveData(syncSessionWorkRequest.getId())
                    .observe(SessionDetailsActivity.this, workInfo -> {
                        if (workInfo != null) {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                CommonUtils.dismissProgress();
                                CommonUtils.showSmallToast(SessionDetailsActivity.this, getResources().getString(R.string.sync_success));

                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                                CommonUtils.dismissProgress();
                                CommonUtils.showSmallToast(SessionDetailsActivity.this, getResources().getString(R.string.sync_failed));
                            }

                        }
                    });

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (readingsViewModel.getAllReadings(mStartTimeStamp).hasActiveObservers()) {
            readingsViewModel.getAllReadings(mStartTimeStamp).removeObservers(SessionDetailsActivity.this);
        }
    }
}
