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
            mValue = new MutableLiveData<String>("Select a language");
        }
        return mValue;
    }


}
