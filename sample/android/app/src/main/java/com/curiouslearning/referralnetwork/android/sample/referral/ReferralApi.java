package com.curiouslearning.referralnetwork.android.sample.referral;

import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralRequest;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReferralApi {
    @POST("/api/v1/referral")
    Call<ReferralResponse> requestReferral(@Body ReferralRequest body, @Query("key") String key);
}
