package com.curiouslearning.referralnetwork.api.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Optional;

@AutoValue
public abstract class ReferralRequest {

    @SerializedName("package_name")
    public abstract String packageName();
    public abstract String locale();
    public abstract Optional<Integer> totalSessions();
    public abstract Optional<Integer> averageSessionLength();
    public abstract Optional<Integer> daysSinceLastSession();
    public abstract Optional<Map<String, Integer>> progressBySkill();
    public abstract Optional<Integer> maxResults();
    public abstract Optional<Map<String, String>> parameters();

    public static Builder builder() {
        return new AutoValue_ReferralRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setPackageName(String value);
        public abstract Builder setLocale(String value);
        public abstract Builder setTotalSessions(int value);
        public abstract Builder setAverageSessionLength(int value);
        public abstract Builder setDaysSinceLastSession(int value);
        public abstract Builder setProgressBySkill(Map<String, Integer> valueMap);
        public abstract Builder setMaxResults(int value);
        public abstract Builder setParameters(Map<String, String> paramMap);

        public abstract ReferralRequest build();
    }

    public static TypeAdapter<ReferralRequest> typeAdapter(Gson gson) {
        return new AutoValue_ReferralRequest.GsonTypeAdapter(gson);
    }
}
