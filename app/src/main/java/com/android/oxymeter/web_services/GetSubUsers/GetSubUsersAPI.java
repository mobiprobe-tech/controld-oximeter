package com.android.oxymeter.web_services.GetSubUsers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

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

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetSubUsersAPI {

    private Call<GetSubUsersResponse> call;
    private UsersViewModel usersViewModel;

    public GetSubUsersAPI(FragmentActivity activity, boolean showProgress, GetSubUsers_ViewModelCallback callback) {

        if (showProgress) {
            CommonUtils.showProgress(activity, activity.getResources().getString(R.string.progress_msg_loading));
        }

        usersViewModel = new ViewModelProvider(activity).get(UsersViewModel.class);

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String userToken = sharedPref.getString(Constants.TOKEN, "");

        ApiWebInterface service = RetrofitRestClient.createService(ApiWebInterface.class, activity);

        call = service.getSubUsersList(userToken);

        call.enqueue(new Callback<GetSubUsersResponse>() {
            @Override
            public void onResponse(Call<GetSubUsersResponse> call, Response<GetSubUsersResponse> response) {
                CommonUtils.dismissProgress();

                String errorMessage = activity.getResources().getString(R.string.something_went_wrong);

                if (response.isSuccessful()) {
                    try {

                        if ((response.body() != null ? response.body().getResponseCode() : 0) == 1) {


                            ArrayList<UserTable> usersList = new ArrayList<>();

                            for (int i = 0; i < response.body().getData().size(); i++) {

                                UserTable userTable = new UserTable();

                                userTable.setmUserID(CommonUtils.checkData(response.body().getData().get(i).getSubUserId()));
                                userTable.setmName(CommonUtils.checkData(response.body().getData().get(i).getName()));
                                userTable.setmEmail(CommonUtils.checkData(response.body().getData().get(i).getEmail()));
                                userTable.setmPhone(CommonUtils.checkData(response.body().getData().get(i).getPhone()));
                                userTable.setmGender(CommonUtils.checkData(response.body().getData().get(i).getGender()));
                                userTable.setmDob(CommonUtils.checkData(response.body().getData().get(i).getDob()));
                                userTable.setmHeight(CommonUtils.checkData(response.body().getData().get(i).getHeight()));
                                userTable.setmWeight(CommonUtils.checkData(response.body().getData().get(i).getWeight()));
                                userTable.setmBloodGroup(CommonUtils.checkData(response.body().getData().get(i).getBloodgroup()));
                                userTable.setmUserType(Constants.USER_TYPE_SUB);

                                boolean isUniqueUser = true;

                                if (usersList.size() > 0) {
                                    for (int j = 0; j < usersList.size(); j++) {
                                        if (usersList.get(j).getmUserID().equalsIgnoreCase(CommonUtils.checkData(response.body().getData().get(i).getSubUserId()))) {
                                            isUniqueUser = false;

                                            break;
                                        }
                                    }
                                }

                                if (isUniqueUser) {
                                    usersList.add(userTable);
                                }
                            }

                            if (usersList.size() > 0) {

                                usersViewModel.deleteAllSubUsers();

                                final Handler handler = new Handler();
                                handler.postDelayed(() -> {

                                    usersViewModel.addAllUsers(usersList);

                                    callback.onSuccess();
                                }, 500);

                            } else {
                                callback.onNoRecordFound(activity.getResources().getString(R.string.no_record_found));
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
            public void onFailure(Call<GetSubUsersResponse> call, Throwable t) {
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
