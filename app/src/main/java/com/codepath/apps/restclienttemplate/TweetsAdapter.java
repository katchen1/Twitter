package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }
    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // Define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvRelativeTimestamp;
        RecyclerView rvImages;
        ImageView ivImage;
        LinearLayout btnRow;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            rvImages = itemView.findViewById(R.id.rvImages);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnRow = itemView.findViewById(R.id.btnRow);
            tvName = itemView.findViewById(R.id.tvName);
            itemView.setOnClickListener(this);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvRelativeTimestamp.setText(tweet.getRelativeTimeAgo());
            Glide.with(context).load(tweet.user.profileImageUrl).circleCrop().into(ivProfileImage);

            // Horizontal scroll view for images
            rvImages.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            ImagesAdapter adapter = new ImagesAdapter(context, tweet.imageUrls);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnRow.getLayoutParams();
            rvImages.setAdapter(adapter);
            if (tweet.imageUrls.isEmpty()) {
                rvImages.setVisibility(View.GONE);
                ivImage.setVisibility(View.GONE);
                params.addRule(RelativeLayout.BELOW, R.id.tvRelativeTimestamp);
            } else if (tweet.imageUrls.size() == 1) {
                rvImages.setVisibility(View.GONE);
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.imageUrls.get(0))
                        .transform(new RoundedCornersTransformation(70, 15))
                        .into(ivImage);
                params.addRule(RelativeLayout.BELOW, R.id.ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
                rvImages.setVisibility(View.VISIBLE);
                params.addRule(RelativeLayout.BELOW, R.id.rvImages);
            }
        }

        /* Displays a new activity via an Intent when the user clicks on a row. */
        @Override
        public void onClick(View v) {
            // Get the item's position
            int position = getAdapterPosition();

            // Ensure that the position is valid
            if (position != RecyclerView.NO_POSITION) {

                // Get the movie at the position
                Tweet tweet = tweets.get(position);

                // Create an intent for the new activity
                Intent intent = new Intent(context, TweetDetailsActivity.class);

                // Serialize the movie using parceler, use its short name as a key
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));

                // Show the activity
                context.startActivity(intent);
            }
        }
    }
}
