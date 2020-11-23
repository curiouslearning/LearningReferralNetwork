package com.curiouslearning.referralnetwork.web.referral.api;

import com.curiouslearning.referralnetwork.web.referral.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralRequestBody;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralResult;
import com.curiouslearning.referralnetwork.web.referral.model.ReferralResultApp;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ReferralApiDelegateImpl implements ReferralApiDelegate {

  // TODO - move these to somewhere else that can be shared with other services (ie. admin)
  //   it may be another dependency at the backend level (ie package com.curiouslearning.referralnetwork.db)
  public static final String APPS_COLLECTION_NAME = "apps";
  public static final String APP_ID_FIELD = "platform_id";
  public static final String TITLE_FIELD = "title";
  public static final String LOCALE_FIELD = "locale";
  public static final String DESCRIPTION_FIELD = "description";
  public static final String ICON_URL_FIELD = "icon_url";

  // TODO - move somewhere else
  public static final int STATUS_SUCCESS = 0;
  public static final int STATUS_FAILURE = 1;

  // TODO - Field injection is not recommended
  @Autowired
  Firestore firestore;

  @Override
  public ResponseEntity<ReferralResult> getReferral(ReferralRequestBody referralRequestBody) {
    ReferralResult result = new ReferralResult();
    ArrayList<ReferralResultApp> referralApps = new ArrayList<>();

    result.setStatus(BigDecimal.valueOf(STATUS_SUCCESS));

    // Test code read from firestore
    CollectionReference appsRef = this.firestore.collection(APPS_COLLECTION_NAME);
    // TODO - use @Async?
    ApiFuture<QuerySnapshot> query = appsRef
        .whereEqualTo(LOCALE_FIELD, referralRequestBody.getMetadata().getLocale())
        .limit(referralRequestBody.getMaxResults())
        .get();

    try {
      QuerySnapshot querySnapshot = query.get();
      List<QueryDocumentSnapshot> docs = querySnapshot.getDocuments();
      for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
        ReferralResultApp referralApp = new ReferralResultApp();
        referralApp.setInfo(new ApplicationInfo()
            .appId(doc.getString(APP_ID_FIELD))
            .title(doc.getString(TITLE_FIELD))
            .description(doc.getString(DESCRIPTION_FIELD))
            .iconUrl(doc.getString(ICON_URL_FIELD))
            .skills((List<String>) doc.get("skills")));
        // TODO - relevance score is hardcoded for now
        referralApp.setRelevance(0.5f);
        referralApps.add(referralApp);
      }
    } catch (Exception e) {
      System.out.println("Failed to read from Firestore: " + e.getMessage());
      result.setStatus(BigDecimal.valueOf(STATUS_FAILURE));
    }

    result.setApp(referralApps);
    return ResponseEntity.ok(result);
  }
}
