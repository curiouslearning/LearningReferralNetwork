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
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
import com.curiouslearning.referralnetwork.android.sample.BuildConfig;
import com.curiouslearning.referralnetwork.android.sample.R;

import com.curiouslearning.referralnetwork.android.sample.referral.ReferralApi;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralItem;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralRequest;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.GsonBuilder;
import com.ryanharter.auto.value.gson.AutoValueGsonTypeAdapterFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String REFERRAL_URL = "https://referral-gateway-hj2cd4bxba-de.a.run.app";

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

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(view.getContext());

        // UI
        mTitleText = (TextView) view.findViewById(R.id.title);
        mLocaleDropdown = (Spinner) view.findViewById(R.id.locale_selection);
        mReferralResultsView = (RecyclerView) view.findViewById(R.id.referral_result);
        Button mReferralButton = (Button) view.findViewById(R.id.referral_button);

        //create a list of items for the spinner.
        String[] items = new String[]{"en-us", "hi-IN"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        mLocaleDropdown.setAdapter(adapter);

        // Recycler view for displaying referral results
        // use a linear layout manager
        mReferrals = new ArrayList<>();
        layoutManager = new LinearLayoutManager(view.getContext());
        mReferralResultsView.setLayoutManager(layoutManager);
        // TODO - make this class implements OnReferralClickListener
        mReferralResultAdapter = new ReferralResultAdapter(mReferrals, new ReferralResultAdapter.OnClickListener() {
            @Override
            public void onClick(ApplicationInfo appInfo) {
                // Send analytic event to Firebase
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, appInfo.platformId());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, appInfo.title());
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "referral");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                // Link to Playstore using package name from referral result
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + appInfo.platformId()));
                intent.setPackage("com.android.vending");
                startActivity(intent);
            }
        });
        mReferralResultsView.setAdapter(mReferralResultAdapter);

        // Button to fetch referrals
        mReferralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate the RequestQueue. (Volley - obsolete)
                // RequestQueue queue = Volley.newRequestQueue(v.getContext());

                String locale = mLocaleDropdown.getSelectedItem().toString();

                // Register type adapter so we can use AutoValue with Gson
                GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(
                        new GsonBuilder()
                                .registerTypeAdapterFactory(new AutoValueGsonTypeAdapterFactory())
                                .setLenient()
                                .create());

                // OkHttp Client interceptor to log request and response data
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(REFERRAL_URL)
                        .addConverterFactory(gsonConverterFactory)
                        .client(httpClient)
                        .build();

                ReferralApi api = retrofit.create(ReferralApi.class);
                ReferralRequest body = ReferralRequest.builder()
                        .setLocale("en-us")
                        .setPackageName(v.getContext().getPackageName())
                        .build();

                Call<ReferralResponse> call = api.requestReferral(body, BuildConfig.REFERRAL_API_KEY);
                call.enqueue(new Callback<ReferralResponse>() {
                    @Override
                    public void onResponse(Call<ReferralResponse> call, Response<ReferralResponse> response) {
                        if (response.isSuccessful()) {
                            Log.i(TAG, "post submitted to API." + response.body().toString());

                            // Update recycler view
                            mReferrals.clear();
                            for (ReferralItem item : response.body().items) {
                                mReferrals.add(item);
                                Log.i(TAG, item.toString());
                            }
                            mReferralResultAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, response.errorBody().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ReferralResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

                // volley code
//                JSONObject requestBody = new JSONObject();
//                try {
//                    requestBody.put("package_name", "com.akiliandme.curiousreader.differentplaces");
//                    requestBody.put("locale", locale);
//                } catch (JSONException e) {
//                    textView.setText("Unable to create request body: " + e.getMessage());
//                    return;
//                }
//
//                JsonObjectRequest referralRequest = new JsonObjectRequest
//                        (Request.Method.POST, REFERRAL_URL, requestBody, new Response.Listener<JSONObject>() {
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                // Pretty print JSON for now
//                                Gson gson = new GsonBuilder()
//                                        .registerTypeAdapterFactory(
//                                                new AutoValueGsonTypeAdapterFactory())
//                                        .create();
//                                ApplicationInfo app = gson.fromJson(response.toString(), ApplicationInfo.class);
//                                textView.setText(app.toString());
//                            }
//                        }, new Response.ErrorListener() {
//
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                textView.setText("That didn't work!" + error.getMessage());
//                            }
//                        });
//
//                // Add the request to the RequestQueue.
//                queue.add(referralRequest);


//                Map<String, Object> map = new HashMap<>();
//
//                Date date = new Date();
//                map.put("timestamp", date.toString());
//
//                // Add a new document with a generated ID
//                mFirestore.collection(DEBUG_COLLECTION)
//                        .document(mProgressEditText.getText().toString())
//                        .set(map)
//                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Log.d(TAG, "write:onComplete");
//                                if (!task.isSuccessful()) {
//                                    // TODO - figure out how to get application context so we can
//                                    //   display a Toast here for failure
//                                    Log.w(TAG, "write:onComplete:failed", task.getException());
//                                }
//                            }
//                        });
//
//                mProgressEditText.setText("");
//                Toast.makeText(getActivity(), "Writing to Firestore", Toast.LENGTH_SHORT).show();
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
