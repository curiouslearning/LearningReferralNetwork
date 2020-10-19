package com.curiouslearning.referralnetwork.api;

import com.curiouslearning.referralnetwork.api.model.ReferralRequest;
import com.curiouslearning.referralnetwork.api.model.ReferralResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReferralApi {
    @POST("/api/v1/referral")
    Call<ReferralResponse> requestReferral(@Body ReferralRequest body, @Query("key") String key);
}
