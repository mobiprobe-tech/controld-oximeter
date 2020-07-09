package com.android.oxymeter.web_services.GetUserDetails;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.android.oxymeter.R;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.ApiWebInterface;
import com.android.oxymeter.web_services.RetrofitRestClient;
import com.android.oxymeter.web_services.UserDetailsCallback;
import com.android.oxymeter.web_services.UserDetailsResponse;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserDetailsAPI {

    private Call<UserDetailsResponse> call;

    public GetUserDetailsAPI(FragmentActivity activity, boolean showProgress, String userID, UserDetailsCallback callback) {

        if (showProgress) {
            CommonUtils.showProgress(activity, activity.getResources().getString(R.string.progress_msg_loading));
        }

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String userToken = sharedPref.getString(Constants.TOKEN, "");

        ApiWebInterface service = RetrofitRestClient.createService(ApiWebInterface.class, activity);

        if (CommonUtils.checkData(userID).isEmpty()) {
            call = service.getProfile(userToken);
        } else {
            call = service.getSubUserDetails(userToken, userID);
        }

        call.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(Call<UserDetailsResponse> call, Response<UserDetailsResponse> response) {

                CommonUtils.dismissProgress();

                String errorMessage = activity.getResources().getString(R.string.something_went_wrong);

                if (response.isSuccessful()) {
                    try {

                        if ((response.body() != null ? response.body().getResponseCode() : 0) == 1) {

                            if (response.body().getData() != null) {

                                UserDetailsResponse.Datum userDetailsResponse = new UserDetailsResponse.Datum();

                                String name = CommonUtils.checkData(response.body().getData().get(0).getName());

                                userDetailsResponse.setName(name);
                                userDetailsResponse.setEmail(CommonUtils.checkData(response.body().getData().get(0).getEmail()));
                                userDetailsResponse.setPhone(CommonUtils.checkData(response.body().getData().get(0).getPhone()));
                                userDetailsResponse.setGender(CommonUtils.checkData(response.body().getData().get(0).getGender()));
                                userDetailsResponse.setDob(CommonUtils.checkData(response.body().getData().get(0).getDob()));
                                userDetailsResponse.setHeight(CommonUtils.checkData(response.body().getData().get(0).getHeight()));
                                userDetailsResponse.setWeight(CommonUtils.checkData(response.body().getData().get(0).getWeight()));
                                userDetailsResponse.setBloodgroup(CommonUtils.checkData(response.body().getData().get(0).getBloodgroup()));

                                callback.onSuccess(userDetailsResponse);
                            } else {
                                callback.onFailure(errorMessage);
                            }

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
            public void onFailure(Call<UserDetailsResponse> call, Throwable t) {

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
