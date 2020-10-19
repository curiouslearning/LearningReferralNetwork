package com.curiouslearning.referralnetwork.api.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

@AutoValue
public abstract class ReferralItem {
    @SerializedName("item")
    public abstract ApplicationInfo appInfo();
    @SerializedName("score")
    public abstract float relevance();

    public static TypeAdapter<ReferralItem> typeAdapter(Gson gson) {
        return new AutoValue_ReferralItem.GsonTypeAdapter(gson);
    }

    public static ReferralItem create(String appInfoJson, float relevance) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(
                        new AutoValueGsonTypeAdapterFactory())
                .create();
        ApplicationInfo appInfo = gson.fromJson(appInfoJson, ApplicationInfo.class);
        return new AutoValue_ReferralItem(appInfo, relevance);
    }
}
