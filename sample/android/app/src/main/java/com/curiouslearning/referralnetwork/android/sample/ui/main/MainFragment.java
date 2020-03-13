package com.curiouslearning.referralnetwork.android.sample.ui.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
