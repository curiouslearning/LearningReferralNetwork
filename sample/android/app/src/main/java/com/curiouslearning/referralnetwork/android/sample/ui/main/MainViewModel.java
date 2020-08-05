package com.curiouslearning.referralnetwork.android.sample.ui.main;

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
