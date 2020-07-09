package com.android.oxymeter.web_services.UpdateUser;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.android.oxymeter.R;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.ApiWebInterface;
import com.android.oxymeter.web_services.GenericCallback;
import com.android.oxymeter.web_services.GenericResponse;
import com.android.oxymeter.web_services.RetrofitRestClient;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserAPI {

    private Call<GenericResponse> call;

    public UpdateUserAPI(FragmentActivity activity, boolean showProgress, String userID, String name, String email, String mobile, String gender, String dob, String height, String weight, String bloodGroup, GenericCallback callback) {

        if (showProgress) {
            CommonUtils.showProgress(activity, activity.getResources().getString(R.string.progress_msg_update));
        }

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String userToken = sharedPref.getString(Constants.TOKEN, "");

        ApiWebInterface service = RetrofitRestClient.createService(ApiWebInterface.class, activity);

        if (CommonUtils.checkData(userID).isEmpty()) {
            call = service.updateUser(userToken, name, email, mobile, gender, dob, height, weight, bloodGroup);
        } else {
            call = service.updateSubUser(userToken, userID, name, email, mobile, gender, dob, height, weight, bloodGroup);
        }

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {

                CommonUtils.dismissProgress();

                String errorMessage = activity.getResources().getString(R.string.something_went_wrong);

                if (response.isSuccessful()) {
                    try {

                        if ((response.body() != null ? response.body().getResponseCode() : 0) == 1) {

                            String successMessage = activity.getResources().getString(R.string.user_update_success);

                            if (!CommonUtils.checkData(Objects.requireNonNull(response.body()).getResponseMessage()).isEmpty()) {
                                successMessage = CommonUtils.checkData(response.body().getResponseMessage());
                            }

                            callback.onSuccess(successMessage);

                        } else {

                            if (!CommonUtils.checkData(Objects.requireNonNull(response.body()).getResponseMessage()).isEmpty()) {
                                errorMessage = CommonUtils.checkData(response.body().getResponseMessage());
                            }

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

                        callback.onFailure(errorMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {

                CommonUtils.dismissProgress();

                if (!call.isCanceled()) {
                    callback.onFailure(t.getLocalizedMessage());
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
