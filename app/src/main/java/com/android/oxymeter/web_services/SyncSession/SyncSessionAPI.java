package com.android.oxymeter.web_services.SyncSession;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.oxymeter.R;
import com.android.oxymeter.room_db.History.SessionAndReadings;
import com.android.oxymeter.room_db.History.SessionViewModel;
import com.android.oxymeter.room_db.OxymeterDatabase;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.ApiWebInterface;
import com.android.oxymeter.web_services.GenericCallback;
import com.android.oxymeter.web_services.RetrofitRestClient;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncSessionAPI {
    private Call<SyncSessionResponse> call;
    private SessionViewModel sessionViewModel;

    public SyncSessionAPI(Context activity, boolean showProgress, SessionAndReadings sessionAndReadings, SyncSessionCallback callback) {

        if (showProgress) {

            CommonUtils.showProgress(activity, activity.getResources().getString(R.string.progress_msg_syncing));
        }

        // sessionViewModel = new ViewModelProvider(activity).get(SessionViewModel.class);

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String userToken = sharedPref.getString(Constants.TOKEN, "");

        String avgPI = CommonUtils.checkData(String.valueOf(sessionAndReadings.getAverage_pi()));
        String avgPulse = CommonUtils.checkData(String.valueOf(sessionAndReadings.getAverage_pulse()));
        String avgSpO2 = CommonUtils.checkData(String.valueOf(sessionAndReadings.getAverage_spo2()));
        String duration = CommonUtils.checkData(String.valueOf(sessionAndReadings.getDuration()));
        String endTime = CommonUtils.checkData(String.valueOf(sessionAndReadings.getEnd_time()));
        String localSessionId = CommonUtils.checkData(String.valueOf(sessionAndReadings.getId()));
        String userType = CommonUtils.checkData(String.valueOf(sessionAndReadings.getIs_self()));
        String syncStatus = CommonUtils.checkData(String.valueOf(sessionAndReadings.getIs_sync()));
        String serverSessionId = CommonUtils.checkData(sessionAndReadings.getSession_id());
        String startTime = CommonUtils.checkData(String.valueOf(sessionAndReadings.getStart_time()));
        String userId = CommonUtils.checkData(sessionAndReadings.getUser_id());

        ArrayList<SessionAndReadings.Readings> readingsList = new ArrayList<>();

        for (int i = 0; i < sessionAndReadings.getReadingsTables().size(); i++) {
            SessionAndReadings.Readings readings = new SessionAndReadings.Readings();

            readings.setPulse(sessionAndReadings.getReadingsTables().get(i).getPulse());
            readings.setSpo2(sessionAndReadings.getReadingsTables().get(i).getSpo2());
            readings.setPi_data(sessionAndReadings.getReadingsTables().get(i).getPi_data());

            readingsList.add(readings);
        }

        String readingsListJsonString = new Gson().toJson(readingsList);

        ApiWebInterface service = RetrofitRestClient.createService(ApiWebInterface.class, activity);

        call = service.syncSession(userToken, avgPI, avgPulse, avgSpO2, duration, endTime, localSessionId, userType, syncStatus, serverSessionId, startTime, userId, readingsListJsonString);

        call.enqueue(new Callback<SyncSessionResponse>() {
            @Override
            public void onResponse(Call<SyncSessionResponse> call, Response<SyncSessionResponse> response) {

                CommonUtils.dismissProgress();

                CommonUtils.myLog("SyncSessionAPI", "onResponse");

                String errorMessage = activity.getResources().getString(R.string.sync_failed);

                if (response.isSuccessful()) {

                    try {

                        if ((response.body() != null ? response.body().getResponseCode() : 0) == 1) {

                            long localSessionId = Integer.valueOf(response.body().getLocalSessionId());
                            String serverSessionId = CommonUtils.checkData(response.body().getServerSessionId());

                            CommonUtils.myLog("SyncSessionAPI", "serverSessionId: " + serverSessionId);

//                            OxymeterDatabase.getDatabase(activity).sessionDao().updateSyncStatus(1, serverSessionId, localSessionId);

                            callback.onSuccess(localSessionId, serverSessionId);

                        } else {

                            if (!CommonUtils.checkData(Objects.requireNonNull(response.body()).getResponseMessage()).isEmpty()) {
                                errorMessage = CommonUtils.checkData(response.body().getResponseMessage());
                            }

                            CommonUtils.myLog("SyncSessionAPI", "Fail");

                            callback.onFailure(errorMessage);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(errorMessage);
                    }

                } else {
                    try {
                        String json = Objects.requireNonNull(response.errorBody()).string();

                        JSONObject jsonObject = new JSONObject(json);

                        if (jsonObject.has("response_message")) {
                            errorMessage = jsonObject.optString("response_message", errorMessage);
                        }
                        CommonUtils.myLog("SyncSessionAPI", "Fail try");
                        callback.onFailure(errorMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncSessionResponse> call, Throwable t) {
                CommonUtils.dismissProgress();
                CommonUtils.myLog("SyncSessionAPI", "onFailure");
                t.printStackTrace();
                if (!call.isCanceled()) {
                    callback.onFailure(activity.getResources().getString(R.string.sync_failed));
                }
            }
        });
    }

    /**
     * Cancel ongoing web service/API request
     */
    public void cancelRequestAPI() {
        if (call != null) {
            call.cancel();
        }

    }
}
