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

    /**
     * HTTP endpoint for referral API
     */
    private static final String REFERRAL_API_ENDPOINT = "https://referral-gateway-hj2cd4bxba-de.a.run.app";

    /**
     * HTTP connection timeout to account for backend startup delay
     */
    private static final int CONNECTION_TIMEOUT_SECONDS = 15;

    private static ReferralClient mInstance;
    private OnReferralResultListener mReferralResultListener;
    private String mApiKey;
    private OkHttpClient mHttpClient;
    private Retrofit mRetrofit;
    private ReferralApi mApi;

    /**
     * Do not expose class constructor, access a singleton instance from {@link #getInstance()}
     */
    private ReferralClient() {
        mReferralResultListener = null;
        mApiKey = "";

        // OkHttp Client interceptor to log request and response data
        // TODO - may eventually remove logging entirely
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        mHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        // Converter factory to enable serialization of AutoValue objects to Gson for
        // use by Retrofit to generate http request body
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(
                new GsonBuilder()
                        .registerTypeAdapterFactory(new AutoValueGsonTypeAdapterFactory())
                        .setLenient()
                        .create());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REFERRAL_API_ENDPOINT)
                .addConverterFactory(gsonConverterFactory)
                .client(mHttpClient)
                .build();

        mApi = retrofit.create(ReferralApi.class);
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
     * Register a listener to handle referral results from the recommendation backend
     *
     * @param listener
     */
    public void registerReferralResultListener(OnReferralResultListener listener) {
        mReferralResultListener = listener;
    }

    /**
     * Make asynchronous request to the recommendation engine backend
     * <p>
     * TODO - context is currently used to pick up the android app package name, this could be
     * a simple String so that the Java class is more portable
     *
     * @param context
     * @param locale
     */
    public void referralRequest(Context context, String locale) {
        ReferralRequest body = ReferralRequest.builder()
                .setLocale(locale)
                .setPackageName(context.getPackageName())
                .build();

        Call<ReferralResponse> call = mApi.requestReferral(body, mApiKey);
        call.enqueue(new Callback<ReferralResponse>() {
            @Override
            public void onResponse(Call<ReferralResponse> call, Response<ReferralResponse> response) {
                if (response.isSuccessful()) {
                    if (mReferralResultListener != null) {
                        mReferralResultListener.onReferralResult(response.body().referrals);
                    } else {
                        Log.w(TAG, "Received response from referral API but no result listener is registered");
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

    /**
     * Set API key for Referral API
     * @param key api key string
     */
    public void setApiKey(String key) {
        mApiKey = key;
    }
}
