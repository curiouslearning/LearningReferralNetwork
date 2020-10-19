package com.curiouslearning.referralnetwork;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;

import com.curiouslearning.referralnetwork.api.ReferralApi;
import com.curiouslearning.referralnetwork.api.model.ReferralRequest;
import com.curiouslearning.referralnetwork.api.model.ReferralResponse;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
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

    /**
     * Maximum value for learning progress
     */
    public static final int MAX_PROGRESS = 100;

    private static ReferralClient mInstance;

    private OnReferralResultListener mReferralResultListener;
    private String mApiKey;
    private OkHttpClient mHttpClient;
    private Retrofit mRetrofit;
    private ReferralApi mApi;

    /**
     * Application package name is passed in the referral request. It is required by the
     * recommendation engine to come up with app referrals that are related to the current
     * application (in addition to other usage metrics)
     */
    private String mPackageName;
    private Locale mSystemLocale;

    /**
     * The user's progress in the learning application as expressed on a scale from
     * zero to {@link #MAX_PROGRESS}
     */
    private int mProgress;

    // Number of sessions, a session is defined as the application in the Started/Resumed state
    private int mTotalSessions;
    private int mAverageSessionLength;
    private int mDaysSinceLastSession;
    private long lastActivityResumedTime;
    private int mSessionStartProgress;
    private int mSessionEndProgress;


    /**
     * Do not expose class constructor, access a singleton instance from {@link #getInstance(Context)}
     */
    private ReferralClient(Context context) {
        mTotalSessions = 0;
        mProgress = 0;
        mApiKey = "";
        mSystemLocale = Locale.getDefault();

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

        mRetrofit = new Retrofit.Builder()
                .baseUrl(REFERRAL_API_ENDPOINT)
                .addConverterFactory(gsonConverterFactory)
                .client(mHttpClient)
                .build();

        mApi = mRetrofit.create(ReferralApi.class);

        Application app = (Application) context.getApplicationContext();
        if (app == null) {
            Log.w(TAG, "Unable to register lifecycle notifications - null application");
            mPackageName = "unknown";
        } else {
            mPackageName = app.getPackageName();

            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                    Log.d(TAG, "onActivityCreated");
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    Log.d(TAG, "onActivityStarted");
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    // Locale settings may have changed while the app was paused. Reinitialize the
                    // TODO - Android API 24+ allows user to specify multiple locales in their
                    //   system  preference, which can be accessed via LocaleList.getDefault()
                    mSystemLocale = Locale.getDefault();

                    mTotalSessions++;
                    lastActivityResumedTime = System.currentTimeMillis();
                    mSessionStartProgress = mProgress;
                    Log.d(TAG, "onActivityResumed - total sessions: " + mTotalSessions);
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                    long duration = System.currentTimeMillis() - lastActivityResumedTime;
                    mSessionEndProgress = mProgress;
                    Log.d(TAG, "onActivityPaused - duration: " + duration / 1000 + " sec");
                    // TODO - Report to server
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    Log.d(TAG, "onActivityStopped");
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                        @NonNull Bundle bundle) {
                    Log.d(TAG, "onActivitySaveInstanceState");
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    Log.d(TAG, "onActivityDestroyed");
                }
            });
        }
    }

    /**
     * Get an instance of the referral client
     *
     * @return ReferralClient instance
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET"})
    @NonNull
    public static ReferralClient getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new ReferralClient(context);
        }
        Log.d(TAG, "Get Instance");
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
     * @param parameters parameters of the referral request
     */
    public void referralRequest(ReferralParameters parameters) {
        ReferralRequest body = ReferralRequest.builder()
                .setLocale(mSystemLocale.toLanguageTag())
                .setPackageName(mPackageName)
                .build();

        Call<ReferralResponse> call = mApi.requestReferral(body, mApiKey);
        call.enqueue(new Callback<ReferralResponse>() {
            @Override
            public void onResponse(Call<ReferralResponse> call,
                                   Response<ReferralResponse> response) {
                if (response.isSuccessful()) {
                    if (mReferralResultListener != null) {
                        mReferralResultListener.onReferralResult(response.body().referrals);
                    } else {
                        Log.w(TAG,
                                "Received response from referral API but no result listener is registered");
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

    public void setProgress(int progress) {
        if (progress < 0 || progress > MAX_PROGRESS) {
            Log.e(TAG, "Invalid progress value, expect 0 - " + MAX_PROGRESS);
            return;
        }

        if (progress < mProgress) {
            Log.w(TAG, "Setting progress to less than the previous value");
        }
        mProgress = progress;
    }

    /**
     * Set API key for Referral API
     *
     * @param key api key string
     */
    public void setApiKey(String key) {
        mApiKey = key;
    }
}
