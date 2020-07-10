package com.curiouslearning.referralnetwork.android.sample.referral.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;

@AutoValue
public abstract class ApplicationInfo {

    @SerializedName("platform_id")
    public abstract String platformId();

    public abstract String locale();
    public abstract String title();
    public abstract String description();
    public abstract List<String> skills();

    public static TypeAdapter<ApplicationInfo> typeAdapter(Gson gson) {
        return new AutoValue_ApplicationInfo.GsonTypeAdapter(gson);
    }
}
