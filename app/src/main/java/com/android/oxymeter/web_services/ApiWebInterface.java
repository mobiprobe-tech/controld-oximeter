package com.android.oxymeter.web_services;

import com.android.oxymeter.room_db.History.SessionAndReadings;
import com.android.oxymeter.web_services.GetSubUsers.GetSubUsersResponse;
import com.android.oxymeter.web_services.Login.LoginResponse;
import com.android.oxymeter.web_services.SyncSession.SyncSessionResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiWebInterface {

    @Multipart
    @POST("signup.php")
    Call<LoginResponse> signIn(@Part("firebase_id") String fireBaseId,
                               @Part("firebase_token") String fireBaseToken,
                               @Part("type") int loginType,
                               @Part("name") String name,
                               @Part("email") String email,
                               @Part("mobile") String mobile,
                               @Part("gender") String gender,
                               @Part("dob") String dob,
                               @Part("height") String height,
                               @Part("weight") String weight,
                               @Part("bloodgroup") String bloodGroup);

    @Multipart
    @POST("get_profile.php")
    Call<UserDetailsResponse> getProfile(@Part("user_token") String token);

    @Multipart
    @POST("update_user.php")
    Call<GenericResponse> updateUser(@Part("user_token") String token,
                                     @Part("name") String name,
                                     @Part("email") String email,
                                     @Part("mobile") String mobile,
                                     @Part("gender") String gender,
                                     @Part("dob") String dob,
                                     @Part("height") String height,
                                     @Part("weight") String weight,
                                     @Part("bloodgroup") String bloodGroup);

    @Multipart
    @POST("add_sub_user.php")
    Call<GenericResponse> addSubUser(@Part("user_token") String token,
                                     @Part("name") String name,
                                     @Part("email") String email,
                                     @Part("mobile") String mobile,
                                     @Part("gender") String gender,
                                     @Part("dob") String dob,
                                     @Part("height") String height,
                                     @Part("weight") String weight,
                                     @Part("bloodgroup") String bloodGroup);

    @Multipart
    @POST("fetch_sub_users.php")
    Call<GetSubUsersResponse> getSubUsersList(@Part("user_token") String token);

    @Multipart
    @POST("fetch_sub_user.php")
    Call<UserDetailsResponse> getSubUserDetails(@Part("user_token") String token,
                                                @Part("user_id") String userId);

    @Multipart
    @POST("update_sub_user.php")
    Call<GenericResponse> updateSubUser(@Part("user_token") String token,
                                        @Part("sub_user_id") String userId,
                                        @Part("name") String name,
                                        @Part("email") String email,
                                        @Part("mobile") String mobile,
                                        @Part("gender") String gender,
                                        @Part("dob") String dob,
                                        @Part("height") String height,
                                        @Part("weight") String weight,
                                        @Part("bloodgroup") String bloodGroup);

    @Multipart
    @POST("delete_sub_user.php")
    Call<GenericResponse> deleteSubUser(@Part("user_token") String token,
                                        @Part("user_id") String userId);

    @Multipart
    @POST("log_session.php")
    Call<SyncSessionResponse> syncSession(@Part("user_token") String token,
                                          @Part("average_pi") String avgPI,
                                          @Part("average_pulse") String avgPulse,
                                          @Part("average_spo2") String avgSpO2,
                                          @Part("duration") String duration,
                                          @Part("end_time") String endTime,
                                          @Part("id") String localSessionId,
                                          @Part("is_self") String userType,
                                          @Part("is_sync") String syncStatus,
                                          @Part("session_id") String serverSessionId,
                                          @Part("start_time") String startTime,
                                          @Part("user_id") String userId,
                                          @Part("readingsTables") String readings);


}
