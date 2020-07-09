package com.android.oxymeter.work_manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.oxymeter.R;
import com.android.oxymeter.activities.MainTabActivity;
import com.android.oxymeter.room_db.History.ReadingsTable;
import com.android.oxymeter.room_db.History.SessionAndReadings;
import com.android.oxymeter.room_db.History.SessionTable;
import com.android.oxymeter.room_db.OxymeterDatabase;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.SyncSession.SyncSessionAPI;
import com.android.oxymeter.web_services.SyncSession.SyncSessionCallback;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SyncSessionWorker extends Worker {

    private SyncSessionAPI syncSessionAPI;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private int mNotificationId = 1;


    public SyncSessionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Context context = getApplicationContext();

        long localSessionId = getInputData().getLong(Constants.EXTRAS_LOCAL_SESSION_ID, 0);
        boolean showNotification = getInputData().getBoolean(Constants.EXTRAS_SHOW_PROGRESS_DIALOG, false);

        SessionAndReadings sessionAndReadings = getSessionDetails(localSessionId);

        if (sessionAndReadings == null) {
            CommonUtils.myLog("SyncSessionWorker", "Session is null");
            CommonUtils.dismissProgress();
            return Result.failure();
        }

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (showNotification) {
            displayNotification();
        }


        try {

            boolean sessionIsSynced = callSyncSessionAPI(context, sessionAndReadings);

            if (sessionIsSynced) {

                CommonUtils.myLog("SyncSessionWorker", "Success");

                CommonUtils.dismissProgress();

                if (mBuilder != null) {
                    // When done, update the notification one more time to remove the progress bar
                    mBuilder.setContentText(context.getResources().getString(R.string.sync_success))
                            .setProgress(0, 0, false);
                    mNotificationManager.notify(mNotificationId, mBuilder.build());
                }

                return Result.success();
            } else {

                CommonUtils.myLog("SyncSessionWorker", "Failed");

                CommonUtils.dismissProgress();

                if (mBuilder != null) {
                    // When done, update the notification one more time to remove the progress bar
                    mBuilder.setContentText(context.getResources().getString(R.string.sync_failed))
                            .setProgress(0, 0, false);
                    mNotificationManager.notify(mNotificationId, mBuilder.build());
                }

                return Result.failure();
            }


        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.myLog("SyncSessionWorker", "Exception");
            CommonUtils.dismissProgress();

            if (mBuilder != null) {
                // When done, update the notification one more time to remove the progress bar
                mBuilder.setContentText(context.getResources().getString(R.string.sync_failed))
                        .setProgress(0, 0, false);
                mNotificationManager.notify(mNotificationId, mBuilder.build());
            }

            return Result.failure();
        }

    }

    private SessionAndReadings getSessionDetails(long localSessionId) {

        SessionAndReadings sessionAndReadings = new SessionAndReadings();

        SessionTable session = OxymeterDatabase.getDatabase(getApplicationContext()).sessionDao().getSessionDetailsByLocalID(localSessionId);

        if (session != null) {

            sessionAndReadings.setId(session.getId());
            sessionAndReadings.setUser_id(CommonUtils.checkData(session.getmUserId()));
            sessionAndReadings.setIs_self(session.getmSelf());
            sessionAndReadings.setStart_time(session.getmStartTime());
            sessionAndReadings.setEnd_time(session.getmEndTime());
            sessionAndReadings.setDuration(session.getmDuration());
            sessionAndReadings.setAverage_pulse(session.getmAveragePulse());
            sessionAndReadings.setAverage_spo2(session.getmAverageSpO2());
            sessionAndReadings.setAverage_pi(session.getmAveragePI());
            sessionAndReadings.setIs_sync(session.getmSync());
            sessionAndReadings.setSession_id(session.getmSessionID());

            List<ReadingsTable> readingsTables = OxymeterDatabase.getDatabase(getApplicationContext()).readingsDao().getReadingsByStartTimeStamp(session.getmStartTime());

            List<SessionAndReadings.Readings> readingsList = new ArrayList<>();

            for (int i = 0; i < readingsTables.size(); i++) {
                SessionAndReadings.Readings readings = new SessionAndReadings.Readings();

                readings.setPulse(readingsTables.get(i).getmPulse());
                readings.setSpo2(readingsTables.get(i).getmSpO2());
                readings.setPi_data(readingsTables.get(i).getmPI());

                readingsList.add(readings);
            }

            if (readingsList.size() > 0) {
                sessionAndReadings.setReadingsTables(readingsList);
            }

            Gson gson = new Gson();
            String json = gson.toJson(sessionAndReadings);

            CommonUtils.myLog("SyncSessionWorker", "getSessionDetails sessionAndReadings: " + json);
        } else {
            sessionAndReadings = null;
        }

        return sessionAndReadings;
    }

    private void displayNotification() {

        String channelId = getApplicationContext().getResources().getString(R.string.channel_id);
        CharSequence channelName = getApplicationContext().getResources().getString(R.string.channel_name);
        String channelDescription = getApplicationContext().getResources().getString(R.string.channel_description);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription(channelDescription);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getApplicationContext(), MainTabActivity.class);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(intent);
// Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(channelName)
                .setContentText(getApplicationContext().getResources().getString(R.string.progress_msg_syncing))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, true)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        mNotificationManager.notify(mNotificationId, mBuilder.build());

    }

    private boolean callSyncSessionAPI(Context context, SessionAndReadings sessionAndReadings) {

        final boolean[] isSessionSynced = {true};

        syncSessionAPI = new SyncSessionAPI(context, false, sessionAndReadings, new SyncSessionCallback() {

            @Override
            public void onSuccess(long localSessionId, String serverSessionId) {
                syncSessionAPI = null;
                isSessionSynced[0] = true;

                CommonUtils.myLog("SyncSessionWorker", "callSyncSessionAPI");

                OxymeterDatabase.getDatabase(context).sessionDao().updateSyncStatus(1, serverSessionId, localSessionId);

                Intent intent = new Intent(Constants.INTENT_ACTION_UPDATE_SESSION_STATUS);
                intent.putExtra(Constants.KEY_FLAG, 1);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onFailure(String errorMessage) {
                syncSessionAPI = null;
                isSessionSynced[0] = false;
            }
        });
        return isSessionSynced[0];
    }
}
