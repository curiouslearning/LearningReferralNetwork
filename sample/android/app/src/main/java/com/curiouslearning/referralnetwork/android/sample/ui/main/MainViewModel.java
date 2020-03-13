package com.curiouslearning.referralnetwork.android.sample.ui.main;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private static final String APPS_COLLECTION = "apps";

    private MutableLiveData<String> mValue;

    public LiveData<String> getValue() {
        if (mValue == null) {
            mValue = new MutableLiveData<String>();

            // Query listener registration is only removed when the main
            // activity is finished (onCleared)
            FirebaseFirestore.getInstance()
                    .collection(APPS_COLLECTION)
                    .whereEqualTo("package_name", "com.android.app")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {

                            if (e != null || snapshots == null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }
                            List<DocumentSnapshot> documents = snapshots.getDocuments();
                            if (documents.size() > 0) {
                                Object title = documents.get(0).get("title");
                                if (title == null) {
                                    Log.w(TAG, "Field 'title' not found in document");
                                    return;
                                }
                                mValue.postValue(title.toString());
                            }
                        }
                    });
        }
        return mValue;
    }


}
