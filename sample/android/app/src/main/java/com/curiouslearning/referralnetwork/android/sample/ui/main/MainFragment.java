package com.curiouslearning.referralnetwork.android.sample.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.curiouslearning.referralnetwork.android.sample.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final String DEBUG_COLLECTION = "debug";

    private MainViewModel mViewModel;

    private EditText mProgressEditText;
    private TextView mTitleText;

    private FirebaseFirestore mFirestore;

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

        mFirestore = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        mTitleText = (TextView) view.findViewById(R.id.title);
        mProgressEditText = (EditText) view.findViewById(R.id.progress_field);
        Button mUpdateButton = (Button) view.findViewById(R.id.update_button);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>();

                Date date = new Date();
                map.put("timestamp", date.toString());

                // Add a new document with a generated ID
                mFirestore.collection(DEBUG_COLLECTION)
                        .document(mProgressEditText.getText().toString())
                        .set(map)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "write:onComplete");
                                if (!task.isSuccessful()) {
                                    // TODO - figure out how to get application context so we can
                                    //   display a Toast here for failure
                                    Log.w(TAG, "write:onComplete:failed", task.getException());
                                }
                            }
                        });

                mProgressEditText.setText("");
                Toast.makeText(getActivity(), "Writing to Firestore", Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = view.findViewById(R.id.token_button);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                // [START retrieve_current_token]
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();

                                // Log and toast
                                Log.d(TAG, token);
                                Toast.makeText(getActivity(), token, Toast.LENGTH_SHORT).show();
                            }
                        });
                // [END retrieve_current_token]
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
