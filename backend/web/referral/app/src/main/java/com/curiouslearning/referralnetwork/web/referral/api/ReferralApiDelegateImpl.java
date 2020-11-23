package com.curiouslearning.referralnetwork.web.referral.api;

import com.curiouslearning.referralnetwork.web.referral.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralRequestBody;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralResult;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralResultApp;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReferralApiDelegateImpl implements ReferralApiDelegate {

  @Override
  public ResponseEntity<ReferralResult> getReferral(ReferralRequestBody referralRequestBody) {
    ReferralResult result = new ReferralResult();
    ReferralResultApp resultApp = new ReferralResultApp();
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.setAppId("com.curiouslearning.android");
    resultApp.setRelevance(0.25f);
    resultApp.setInfo(appInfo);

    result.setStatus(BigDecimal.ZERO);
    result.addAppItem(resultApp);
    return ResponseEntity.ok(result);
  }
}
