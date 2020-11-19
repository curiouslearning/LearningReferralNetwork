package com.curiouslearning.referralnetwork;

import com.curiouslearning.referralnetwork.api.model.ReferralItem;

import java.util.List;

/**
 * Listener interface for referral result callback
 */
public interface OnReferralResultListener {
    /**
     * Invoked on a successful referral request
     *
     * @param referrals list of referral items containing application metadata
     * @param status
     */
    void onReferralResult(List<ReferralItem> referrals, int status);
}
