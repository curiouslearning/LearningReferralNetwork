package com.curiouslearning.referralnetwork.android.sample.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.curiouslearning.referralnetwork.android.sample.R;
import com.curiouslearning.referralnetwork.api.model.ApplicationInfo;
import com.curiouslearning.referralnetwork.api.model.ReferralItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/** Recycler view adapter to display app referrals in a list */
public class ReferralResultAdapter extends RecyclerView.Adapter<ReferralResultAdapter.ReferralViewHolder> {
    private static final String TAG = "ReferralResultAdapter";

    private List<ReferralItem> mReferrals;
    private OnClickListener mOnClickListener;

    // On click listener callback interface for the adapter
    public interface OnClickListener {
        void onClick(ApplicationInfo appInfo);
    }

    // Provide a reference to the views for each referral item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ReferralViewHolder extends RecyclerView.ViewHolder {
        // TODO - each referral item is just the package name string for the demo
        public TextView textView;
        public ImageView iconImageView;
        public Context context;
        public ReferralViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.referral_app_title);
            iconImageView = (ImageView) v.findViewById(R.id.referral_app_icon);
            context = v.getContext();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReferralResultAdapter(List<ReferralItem> referrals, OnClickListener listener) {
        mReferrals = referrals;
        mOnClickListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReferralResultAdapter.ReferralViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.referral_result_view, parent, false);
        return new ReferralViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReferralViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ApplicationInfo app = mReferrals.get(position).appInfo();
        holder.textView.setText(app.title());
        Picasso.get().load(app.iconUrl()).fit().into(holder.iconImageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onClick(app);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mReferrals.size();
    }
}