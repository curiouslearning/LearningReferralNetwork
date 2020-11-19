package com.curiouslearning.referralnetwork;

import android.content.Context;
import android.util.Log;

import com.curiouslearning.referralnetwork.api.ReferralApi;
import com.curiouslearning.referralnetwork.api.model.ReferralItem;
import com.curiouslearning.referralnetwork.api.model.ReferralRequest;
import com.curiouslearning.referralnetwork.api.model.ReferralResponse;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Referral API client used by LRN apps
 */
public class ReferralClient {
    private static final String TAG = "LRN.Client";

    /**
     * HTTP endpoint for referral API
     */
    private static final String REFERRAL_API_ENDPOINT = "https://referral-gateway-hj2cd4bxba-de.a.run.app";

    /**
     * HTTP connection timeout to account for backend startup delay
     */
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;

    // TODO - may move this to a separate class/enum
    public static final int SUCCESS = 0;
    public static final int ERROR_CONNECTION_TIMEOUT = -1;
    public static final int ERROR_UNKNOWN = -2;

    private static ReferralClient mInstance;

    private OnReferralResultListener mReferralResultListener;
    private final ReferralApi mApi;

    /**
     * Application package name is passed in the referral request. It is required by the
     * recommendation engine to come up with app referrals that are related to the current
     * application (in addition to other usage metrics)
     */
    private final String mPackageName;

    /**
     * Container class for all reportable metrics with regard to the current user
     */
    private final SessionManager mSessionManager;

    /**
     * Do not expose class constructor, access a singleton instance from {@link #getInstance(Context)}
     */
    private ReferralClient(Context context) {
        mPackageName = context.getPackageName();
        mSessionManager = new SessionManager(context);

        // OkHttp Client interceptor to log request and response data
        // TODO - may eventually remove logging entirely
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.newBuilder()
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

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(REFERRAL_API_ENDPOINT)
                .addConverterFactory(gsonConverterFactory)
                .client(httpClient)
                .build();

        mApi = mRetrofit.create(ReferralApi.class);
    }

    /**
     * Get an instance of the referral client
     *
     * @return ReferralClient instance
     */
    @NonNull
    public static ReferralClient getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new ReferralClient(context);
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
     *
     * @param maxResults maximum number of referrals to fetch
     */
    public void referralRequest(String language, int maxResults) {
        // Locale settings may have changed while the app was paused. Reinitialize the
        // TODO - Android API 24+ allows user to specify multiple locales in their
        //   system  preference, which can be accessed via LocaleList.getDefault()
        Locale locale = Locale.getDefault();

        // TODO - revisit how we represent the progress parameter in referral API, it may
        //   not be mapped to a set of skills down the road and just be a lot simpler
        Map<String, Integer> progressMap = new HashMap<>();
        progressMap.put("default", mSessionManager.getProgress());

        // Construct the request body
        ReferralRequest body = ReferralRequest.builder()
                .setLocale(locale.toLanguageTag())
                .setPackageName(mPackageName)
                .setProgressBySkill(progressMap)
                .setTotalSessions(mSessionManager.getTotalSessions())
                .setMaxResults(maxResults)
                .build();

        Call<ReferralResponse> call = mApi.requestReferral(body, BuildConfig.REFERRAL_API_KEY);
        call.enqueue(new Callback<ReferralResponse>() {
            @Override
            public void onResponse(Call<ReferralResponse> call,
                                   Response<ReferralResponse> response) {
                if (mReferralResultListener == null) {
                    Log.w(TAG, "No referral result listener is registered");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    // TODO - check response status for error
                    // if (response.body().status ...)

                    // TODO - instead of passing the content of the response body directly,
                    //   post-process the response into client object so that we can add UTM
                    //   parameters to the PlayStore path (create a Referral class)
                    mReferralResultListener.onReferralResult(response.body().referrals, SUCCESS);
                } else {
                    Log.w(TAG, response.errorBody().toString());

                    // TODO - we can use response errorBody message to determine the exact
                    //  cause of failure, and return proper error code. Use HTTP status code for now
                    mReferralResultListener.onReferralResult(new ArrayList<ReferralItem>(), response.code());
                }
            }

            @Override
            public void onFailure(Call<ReferralResponse> call, Throwable t) {
                if (mReferralResultListener == null) {
                    Log.w(TAG, "No referral result listener is registered");
                    return;
                }

                if (t instanceof SocketTimeoutException) {
                    Log.w(TAG, "Connection timeout");
                    mReferralResultListener.onReferralResult(new ArrayList<ReferralItem>(),
                        ERROR_CONNECTION_TIMEOUT);
                } else {
                    t.printStackTrace();
                    mReferralResultListener.onReferralResult(new ArrayList<ReferralItem>(),
                        ERROR_UNKNOWN);
                }
            }
        });
    }

    /**
     *
     * @param type skill type
     * @param progress progress in percentage
     */
    public void setProgress(String type, int progress) {
        mSessionManager.setProgress(progress);
    }
}
