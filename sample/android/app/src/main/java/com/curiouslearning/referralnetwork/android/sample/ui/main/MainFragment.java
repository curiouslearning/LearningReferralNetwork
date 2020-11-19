package com.curiouslearning.referralnetwork.android.sample.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.curiouslearning.referralnetwork.OnReferralResultListener;
import com.curiouslearning.referralnetwork.ReferralClient;
import com.curiouslearning.referralnetwork.android.sample.R;

import com.curiouslearning.referralnetwork.api.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.api.model.ReferralItem;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.curiouslearning.referralnetwork.ReferralClient.SUCCESS;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String UTM_SOURCE = "lrn";

    private ReferralClient mReferralClient;

    private List<ReferralItem> mReferrals;

    private MainViewModel mViewModel;

    private Spinner mLocaleDropdown;
    private TextView mTitleText;

    private RecyclerView.Adapter mReferralResultAdapter;
    private RecyclerView mReferralResultsView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }

        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(view.getContext());

        // UI
        mTitleText = (TextView) view.findViewById(R.id.title);
        mLocaleDropdown = (Spinner) view.findViewById(R.id.locale_selection);
        mReferralResultsView = (RecyclerView) view.findViewById(R.id.referral_result);
        Button mReferralButton = (Button) view.findViewById(R.id.referral_button);

        //create a list of items for the spinner.
        String[] items = new String[]{"en-US", "hi-IN"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        mLocaleDropdown.setAdapter(adapter);

        // Recycler view for displaying referral results
        // use a linear layout manager
        mReferrals = new ArrayList<>();
        layoutManager = new LinearLayoutManager(view.getContext());
        mReferralResultsView.setLayoutManager(layoutManager);

        mReferralClient = ReferralClient.getInstance(view.getContext());
        mReferralClient.setProgress("letter sounds", 50);

        mReferralClient.registerReferralResultListener(new OnReferralResultListener() {
            @Override
            public void onReferralResult(List<ReferralItem> referrals, int status) {
                if (status == SUCCESS) {
                    Log.d(TAG, "Received LRN referrals");
                    // Update recycler view
                    mReferrals.clear();
                    for (ReferralItem referral : referrals) {
                        mReferrals.add(referral);
                        Log.i(TAG, referral.toString());
                    }
                    mReferralResultAdapter.notifyDataSetChanged();
                }
            }
        });

        // TODO - make this class implements OnReferralClickListener
        mReferralResultAdapter = new ReferralResultAdapter(mReferrals,
                new ReferralResultAdapter.OnClickListener() {
                    @Override
                    public void onClick(ApplicationInfo appInfo) {
                        // Send analytic event to Firebase
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, appInfo.platformId());
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, appInfo.title());
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "referral");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        Log.d(TAG, "Package name: " + getContext().getPackageName());
                        // Link to Playstore using package name from referral result
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(String.format(
                                "https://play.google.com/store/apps/details?utm_source=%s&utm_campaign=%s&id=%s",
                                UTM_SOURCE, getContext().getPackageName(), appInfo.platformId())));
                        intent.setPackage("com.android.vending");
                        startActivity(intent);
                    }
                });
        mReferralResultsView.setAdapter(mReferralResultAdapter);

        // Button to fetch referrals
        mReferralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedLocale = mLocaleDropdown.getSelectedItem().toString();
                mReferralClient.referralRequest(Locale.forLanguageTag(selectedLocale).getLanguage(), 4);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // Update UI when there are changes to application data
        mViewModel.getValue().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String value) {
                mTitleText.setText(value);
            }
        });
    }
}
