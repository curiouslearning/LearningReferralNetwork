package com.curiouslearning.referralnetwork.android.sample.referral.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

import java.util.List;

@AutoValue
public abstract class ReferralItem {
    public abstract ApplicationInfo item();
    public abstract float score();

    public static TypeAdapter<ReferralItem> typeAdapter(Gson gson) {
        return new AutoValue_ReferralItem.GsonTypeAdapter(gson);
    }

    public static ReferralItem create(String appInfoJson, float score) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(
                        new AutoValueGsonTypeAdapterFactory())
                .create();
        ApplicationInfo appInfo = gson.fromJson(appInfoJson, ApplicationInfo.class);
        return new AutoValue_ReferralItem(appInfo, score);
    }
}
