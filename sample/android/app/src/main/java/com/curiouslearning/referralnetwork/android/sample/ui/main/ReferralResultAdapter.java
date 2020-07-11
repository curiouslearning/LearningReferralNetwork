package com.curiouslearning.referralnetwork.android.sample.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.curiouslearning.referralnetwork.android.sample.R;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.android.sample.referral.model.ReferralItem;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ReferralResultAdapter extends RecyclerView.Adapter<ReferralResultAdapter.ReferralViewHolder> {
    private static final String TAG = "ReferralResultAdapter";

    private List<ReferralItem> mReferrals;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ReferralViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public ReferralViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.referral_app_title);;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReferralResultAdapter(List<ReferralItem> referrals) {
        mReferrals = referrals;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReferralResultAdapter.ReferralViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.referral_result_view, parent, false);
        ReferralViewHolder vh = new ReferralViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReferralViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ApplicationInfo app = mReferrals.get(position).item();
        holder.textView.setText(app.title());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Link to Playstore using package name from referral result
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + app.platformId()));
                intent.setPackage("com.android.vending");
                v.getContext().startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mReferrals.size();
    }
}