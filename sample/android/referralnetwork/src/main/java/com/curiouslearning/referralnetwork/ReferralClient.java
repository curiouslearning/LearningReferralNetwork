package com.curiouslearning.referralnetwork;

import android.content.Context;
import android.util.Log;

import com.curiouslearning.referralnetwork.api.ReferralApi;
import com.curiouslearning.referralnetwork.api.model.ReferralRequest;
import com.curiouslearning.referralnetwork.api.model.ReferralResponse;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReferralClient {
    private static final String TAG = "ReferralClient";

    /** HTTP endpoint for referral API */
    private static final String REFERRAL_API_ENDPOINT = "https://referral-gateway-hj2cd4bxba-de.a.run.app";

    /** Singleton instance of the referral client */
    private static ReferralClient mInstance;

    /** Listener to handle referral results from the server */
    private OnReferralResultListener mListener;

    private String mApiKey;

    private ReferralClient() {
        mListener = null;
        mApiKey = "";
    }

    /**
     * Get an instance of the referral client
     *
     * @return ReferralClient instance
     */
    public static ReferralClient getInstance() {
        if (mInstance == null) {
            mInstance = new ReferralClient();
        }
        return mInstance;
    }

    /**
     * Register a listener to handle referral results from the server
     * @param listener
     */
    public void registerReferralResultListener(OnReferralResultListener listener) {
        mListener = listener;
    }

    public void referralRequest(Context context, String locale) {
        // Register type adapter so we can use AutoValue with Gson
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(
                new GsonBuilder()
                        .registerTypeAdapterFactory(new AutoValueGsonTypeAdapterFactory())
                        .setLenient()
                        .create());

        // OkHttp Client interceptor to log request and response data
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REFERRAL_API_ENDPOINT)
                .addConverterFactory(gsonConverterFactory)
                .client(httpClient)
                .build();

        ReferralApi api = retrofit.create(ReferralApi.class);
        ReferralRequest body = ReferralRequest.builder()
                .setLocale(locale)
                .setPackageName(context.getPackageName())
                .build();

        Call<ReferralResponse> call = api.requestReferral(body, mApiKey);
        call.enqueue(new Callback<ReferralResponse>() {
            @Override
            public void onResponse(Call<ReferralResponse> call, Response<ReferralResponse> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "post submitted to API." + response.body().toString());

                    if (mListener != null) {
                        mListener.onReferralResult(response.body().items);
                    }
                } else {
                    Log.e(TAG, response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ReferralResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void setApiKey(String key) {
        mApiKey = key;
    }
}
