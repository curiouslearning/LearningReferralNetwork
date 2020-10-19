package com.curiouslearning.referralnetwork.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReferralResponse {

    @SerializedName("result")
    public List<ReferralItem> items;
}
