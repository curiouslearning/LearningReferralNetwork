package com.curiouslearning.referralnetwork.android.sample.referral.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferralResponse {

    @SerializedName("result")
    public List<ReferralItem> items;
}
