package com.curiouslearning.referralnetwork;

import com.google.auto.value.AutoValue;

import java.util.Map;
import java.util.Optional;

@AutoValue
public abstract class ReferralParameters {
    public abstract String language();
    public abstract Optional<Map<String, Integer>> progressBySkill();
    public abstract Optional<Integer> maxResults();

    public static ReferralParameters.Builder builder() {
        return new AutoValue_ReferralParameters.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract ReferralParameters.Builder setLanguage(String value);
        public abstract ReferralParameters.Builder setProgressBySkill(Map<String, Integer> valueMap);
        public abstract ReferralParameters.Builder setMaxResults(int value);

        public abstract ReferralParameters build();
    }
}
