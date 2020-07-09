package com.android.oxymeter.web_services.Login;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.android.oxymeter.R;
import com.android.oxymeter.fragments.ui.users_tab.UsersViewModel;
import com.android.oxymeter.room_db.Users.UserTable;
import com.android.oxymeter.utilities.CommonUtils;
import com.android.oxymeter.utilities.Constants;
import com.android.oxymeter.web_services.ApiWebInterface;
import com.android.oxymeter.web_services.RetrofitRestClient;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginAPI {

    private Call<LoginResponse> call;
    private UsersViewModel usersViewModel;

    public LoginAPI(FragmentActivity activity, boolean showProgress, String firebaseID, String firebaseToken, int loginType, String name, String email, String mobile, String gender, String dob, String height, String weight, String bloodGroup, LoginCallback callback) {

        if (showProgress) {
            CommonUtils.showProgress(activity, activity.getResources().getString(R.string.progress_msg_authentication));
        }

        usersViewModel = new ViewModelProvider(activity).get(UsersViewModel.class);

        ApiWebInterface service = RetrofitRestClient.createService(ApiWebInterface.class, activity);

        call = service.signIn(firebaseID, firebaseToken, loginType, name, email, mobile, gender, dob, height, weight, bloodGroup);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                CommonUtils.dismissProgress();

                String errorMessage = activity.getResources().getString(R.string.something_went_wrong);

                if (response.isSuccessful()) {

                    try {

                        if ((response.body() != null ? response.body().getResponseCode() : 0) == 1) {

                            String userToken = CommonUtils.checkData(response.body().getData().get(0).getUserToken());
                            String userName = CommonUtils.checkData(response.body().getData().get(0).getName());
                            String userEmail = CommonUtils.checkData(response.body().getData().get(0).getEmail());
                            String userPhone = CommonUtils.checkData(response.body().getData().get(0).getPhone());
                            String userDob = CommonUtils.checkData(response.body().getData().get(0).getDob());

                            SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                            sharedPref.edit()
                                    .putString(Constants.TOKEN, userToken)
                                    .putInt(Constants.LOGIN_TYPE, loginType)
                                    .apply();

                            userName = userName.isEmpty() ? Constants.MYSELF : userName;

                            UserTable userTable = new UserTable();

                            userTable.setmUserID("");
                            userTable.setmName(userName);
                            userTable.setmEmail(userEmail);
                            userTable.setmPhone(userPhone);
                            userTable.setmGender(CommonUtils.checkData(response.body().getData().get(0).getGender()));
                            userTable.setmDob(userDob);
                            userTable.setmHeight(CommonUtils.checkData(response.body().getData().get(0).getHeight()));
                            userTable.setmWeight(CommonUtils.checkData(response.body().getData().get(0).getWeight()));
                            userTable.setmBloodGroup(CommonUtils.checkData(response.body().getData().get(0).getBloodGroup()));
                            userTable.setmUserType(Constants.USER_TYPE_LOGGED_IN);

                            usersViewModel.addUser(userTable);


                            callback.onSuccess(activity.getResources().getString(R.string.sign_in_success));

                        } else {

                            if (!CommonUtils.checkData(Objects.requireNonNull(response.body()).getResponseMessage()).isEmpty()) {
                                errorMessage = CommonUtils.checkData(response.body().getResponseMessage());
                            }

                            callback.onFailure(errorMessage);

                        }


                    } catch (Exception e) {
                        Writer writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        String s = writer.toString();
                        e.printStackTrace();
                        callback.onFailure(s);
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
                        Writer writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        String s = writer.toString();
                        e.printStackTrace();
                        callback.onFailure(errorMessage);
                    }
                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
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
