package com.curiouslearning.referralnetwork;

import com.curiouslearning.referralnetwork.api.model.ReferralItem;

import java.util.List;

public interface OnReferralResultListener {
    void onReferralResult(List<ReferralItem> referrals);
}
