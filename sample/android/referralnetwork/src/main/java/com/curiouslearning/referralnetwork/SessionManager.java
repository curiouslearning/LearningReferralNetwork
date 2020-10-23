package com.curiouslearning.referralnetwork;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Manages the collection, aggregation and reporting of LRN analytics
 * <p>
 * Event (reporting) triggers
 * - session starts
 * - session ends
 * - progress level change
 * - playstore redirect - not sure how to track this one
 * <p>
 * Metrics ideas
 * - active time between progress levels
 * - progress level granularity depends on app
 *
 * <p>
 * TODO - implement reporting API
 * TODO - cache metrics into shared preference and perform batched reporting
 */
public class SessionManager implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "LRN.Session";

    private static final String EVENT_PREF_FILE = "com.curiouslearning.referralnetwork.events";

    /**
     * Maximum value for learning progress (100%)
     */
    public static final int MAX_PROGRESS = 100;

    private List<Event> mEvents;

    /**
     * The user's progress in the learning application as expressed on a scale from
     * zero to {@link #MAX_PROGRESS}
     */
    private int mProgress;

    /**
     * App instance ID obtained from {@link FirebaseInstanceId} for event tagging
     */
    private String mInstanceId;

    /**
     * Keep track of app usage statistics for referral requests
     */
    private int mTotalSessions;
    private long mAverageSessionLength;
    private long mDaysSinceLastSession;
    private int mSessionStartProgress;
    private int mSessionEndProgress;
    private long mProgressRate;

    // Event timestamps
    private long mLastActivityResumedTime;
    private long mLastActivityPausedTime;
    private long mLastProgressChangedTime;

    /**
     * Session information are cached in {@link android.content.SharedPreferences} and reported
     * to the analytics backend in batch. They could be purged as soon as the data is sent, so
     * do not rely on historical values to be persistent for calculating usage statistics
     * <p>
     * TODO -
     * Since a session is bounded by the app resume and stop instances, it doesn't make sense to
     * report the session at app stopped as it would incur more data bandwidth. Instead, caching
     * them locally either in memory or in shared pref, and create a periodic task to report back
     * to the server
     * <p>
     * Option 1 - onPeriodicTaskEntry - if data connection available, report all events back to
     * server, otherwise, dump them into shared pref
     */
    private final SharedPreferences mSharedPref;

    public SessionManager(Context context) {
        mEvents = new ArrayList<>();
        mProgress = 0;
        mTotalSessions = 0;
        mAverageSessionLength = 0;
        mDaysSinceLastSession = 0;

        mSharedPref = context.getSharedPreferences(EVENT_PREF_FILE, Context.MODE_PRIVATE);

        // TODO - remove, for debugging only!
        mSharedPref.edit().clear().apply();

        // Get application context to register for activity event callbacks. This allows us
        // to report anonymize usage statistics to improve recommendation relevance
        Application app = (Application) context.getApplicationContext();
        if (app == null) {
            Log.w(TAG, "unexpected null application, cannot listen for events");
        } else {
            app.registerActivityLifecycleCallbacks(this);
        }

        // Get instance ID
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    mInstanceId = instanceIdResult.getId();
                    Log.i(TAG, "instance ID: " + mInstanceId);
                }
            });
    }

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
        Event event = new Event(Event.EventType.SESSION_START);
        Log.d(TAG, "Event: " + event.toString());
        mLastActivityResumedTime = System.currentTimeMillis();

        mSessionStartProgress = mProgress;
        mDaysSinceLastSession = TimeUnit.MILLISECONDS
            .toDays(mLastActivityResumedTime - mLastActivityPausedTime);
        Log.d(TAG,
            "onActivityResumed - total sessions: " + mTotalSessions + ", gap: " + mDaysSinceLastSession);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(TAG, "onActivityPaused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Event event = new Event(Event.EventType.SESSION_END);
        Log.d(TAG, "Event: " + event.toString());

        mLastActivityPausedTime = System.currentTimeMillis();

        // session duration in seconds
        long duration = (mLastActivityPausedTime - mLastActivityResumedTime) / 1000;
        mAverageSessionLength = (mAverageSessionLength * mTotalSessions + duration) / (mTotalSessions + 1);
        mTotalSessions++;

        mSessionEndProgress = mProgress;

        Log.d(TAG, "onActivityStopped - duration: " + duration + ", avg: " + mAverageSessionLength);

        // TODO - need to be unique
        int sessionId = mTotalSessions;
        mSharedPref
            .edit()
            .putLong(getEventPrefKey(sessionId, "session", "start"), mLastActivityResumedTime)
            .putLong(getEventPrefKey(sessionId, "session", "end"), mLastActivityPausedTime)
            .apply();
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

    public void setProgress(int progress) {
        if (progress < 0 || progress > MAX_PROGRESS) {
            Log.e(TAG, "Invalid progress value, expect 0 - " + MAX_PROGRESS);
            return;
        }

        Event event = new Event(Event.EventType.PROGRESS_CHANGE);
        Log.d(TAG, "Event: " + event.toString());

        mLastProgressChangedTime = System.currentTimeMillis();

        if (progress < mProgress) {
            Log.w(TAG, "Setting progress to less than the previous value");
        }
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getTotalSessions() {
        return mTotalSessions;
    }

    /**
     * Generate the key string for a shared preference entry that represent an event
     *
     * @param type Event type (e.g. session, progress)
     * @param name the data subject associate with this event type
     * @param id   event id
     * @return
     */
    private String getEventPrefKey(int id, String type, String name) {
        return type + "." + id + "." + name;
    }
}
