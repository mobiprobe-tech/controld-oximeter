package com.android.oxymeter.web_services;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitRestClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 60 * 1000; // 60s
    private static final int READ_TIMEOUT_MILLIS = 60 * 1000; // 60s
    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    private static HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.BODY;
    public static AppMode appMode = AppMode.DEV;

    static public String getURL() {
        return getBaseUrl(appMode);
    }

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(getURL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create()
                    );


    public static <S> S createService(Class<S> serviceClass, Context context) {

        loggingInterceptor.setLevel(loggingLevel);

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.SECONDS);
        okHttpClient.connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.SECONDS);
        okHttpClient.retryOnConnectionFailure(true);
        okHttpClient.addInterceptor(chain -> {

            Request request = chain.request();

            /*String token = Prefs.with(context).getString(DataNames.ACCESS_TOKEN, "");

            Request.Builder builder1 = request.newBuilder().header("Authorization",
                    "bearer " + token);

            Request newRequest = builder1.build();*/
            return chain.proceed(request);

        });
        okHttpClient.addInterceptor(loggingInterceptor);

        Retrofit retrofit = builder.client(okHttpClient.build()).build();

        return retrofit.create(serviceClass);
    }


    /**
     * Set mode of the current application
     *
     * @param appMode DEV, TEST, LIVE
     */
    private static String getBaseUrl(AppMode appMode) {

        String BASE_URL = "";

        switch (appMode) {

            case DEV:
                BASE_URL = "YOUR API URL HERE";
                loggingLevel = HttpLoggingInterceptor.Level.BODY;
                break;

            case TEST:
                BASE_URL = "YOUR API URL HERE";
                loggingLevel = HttpLoggingInterceptor.Level.BODY;
                break;

            case LIVE:
                BASE_URL = "YOUR API URL HERE";
                loggingLevel = HttpLoggingInterceptor.Level.NONE;
                break;

        }

        return BASE_URL;
    }

    public enum AppMode {
        DEV, TEST, LIVE
    }

}
