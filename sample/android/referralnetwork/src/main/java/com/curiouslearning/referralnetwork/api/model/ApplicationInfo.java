package com.curiouslearning.referralnetwork.api.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class ApplicationInfo {

    @SerializedName("platform_id")
    public abstract String platformId();
    @SerializedName("icon_url")
    public abstract String iconUrl();

    public abstract String locale();
    public abstract String title();
    public abstract String description();
    public abstract List<String> skills();

    public static TypeAdapter<ApplicationInfo> typeAdapter(Gson gson) {
        return new AutoValue_ApplicationInfo.GsonTypeAdapter(gson);
    }
}
