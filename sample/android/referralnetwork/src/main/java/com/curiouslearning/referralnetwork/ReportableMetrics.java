package com.curiouslearning.referralnetwork;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Manages the collection, aggregation and reporting of LRN analytics
 * <p>
 * Event (reporting) triggers
 * - session end
 * - progress level change
 * <p>
 * Metrics ideas
 * - active time between progress levels
 * - progress level granularity depends on app
 *
 * <p>
 * TODO - implement reporting API
 * TODO - cache metrics into shared preference and perform batched reporting
 */
public class ReportableMetrics implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "LRN.Metrics";

    /**
     * Maximum value for learning progress (100%)
     */
    public static final int MAX_PROGRESS = 100;

    /**
     * The user's progress in the learning application as expressed on a scale from
     * zero to {@link #MAX_PROGRESS}
     */
    private int mProgress;

    // Number of sessions, a session is defined as the application in the Started/Resumed state
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

    public ReportableMetrics() {
        mProgress = 0;
        mTotalSessions = 0;
        mAverageSessionLength = 0;
        mDaysSinceLastSession = 0;
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
        mLastActivityResumedTime = System.currentTimeMillis();

        mTotalSessions++;
        mSessionStartProgress = mProgress;
        mDaysSinceLastSession = TimeUnit.MILLISECONDS
                .toDays(mLastActivityResumedTime - mLastActivityPausedTime);
        Log.d(TAG,
                "onActivityResumed - total sessions: " + mTotalSessions + ", gap: " + mDaysSinceLastSession);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        mLastActivityPausedTime = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - mLastActivityResumedTime;

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

    public void setProgress(int progress) {
        if (progress < 0 || progress > MAX_PROGRESS) {
            Log.e(TAG, "Invalid progress value, expect 0 - " + MAX_PROGRESS);
            return;
        }


        mLastProgressChangedTime = System.currentTimeMillis();

        if (progress < mProgress) {
            Log.w(TAG, "Setting progress to less than the previous value");
        }
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    public void getAggregatedMetricsSummary() {
        return;
    }
}
