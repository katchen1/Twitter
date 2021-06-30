package com.codepath.apps.restclienttemplate.adapters;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.activities.ComposeActivity;
import com.codepath.apps.restclienttemplate.activities.TweetDetailsActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import org.parceler.Parcels;
import java.util.List;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public final int RADIUS = 70;
    public final int MARGIN = 15;
    public final int REPLY_REQUEST_CODE = 2;
    Context context;
    List<Tweet> tweets;

    /* Constructor takes in the context and list of tweets. */
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    /* For each tweet item, inflate the layout. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    /* Bind values of the tweet view based on the tweet's position in the list. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    /* Returns the number of tweets in the recycler view. */
    @Override
    public int getItemCount() { return tweets.size(); }

    /* Clears the adapter and notifies that data has changed. */
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    /* Adds a list of items to the adapter and notifies that data has change. */
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    /* Defines a view holder. */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvRelativeTimestamp;
        RecyclerView rvImages;
        ImageView ivImage;
        LinearLayout btnRow;
        TextView tvName;
        ImageButton imgBtnReply;

        /* Constructor gets the components of the tweet view. */
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
            imgBtnReply = itemView.findViewById(R.id.imgBtnReply);
            itemView.setOnClickListener(this);
        }

        /* Binds the values of the tweet to the view holder's components. */
        public void bind(final Tweet tweet) {
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvRelativeTimestamp.setText(tweet.getRelativeTimeAgo());
            Glide.with(context).load(tweet.user.profileImageUrl).circleCrop().into(ivProfileImage);

            // Populate the images view based on the number of images embedded in the tweet
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnRow.getLayoutParams();
            if (tweet.imageUrls.isEmpty()) {
                rvImages.setVisibility(View.GONE);
                ivImage.setVisibility(View.GONE);
                params.addRule(RelativeLayout.BELOW, R.id.tvRelativeTimestamp);
            } else if (tweet.imageUrls.size() == 1) {
                bindSingleImageView(tweet);
                params.addRule(RelativeLayout.BELOW, R.id.ivImage);
            } else {
                bindMultiImageView(tweet);
                params.addRule(RelativeLayout.BELOW, R.id.rvImages);
            }

            // When the reply button is clicked
            imgBtnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Compose a tweet and pass in the tweet being replied to
                    Intent i = new Intent(context, ComposeActivity.class);
                    i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    ((Activity) context).startActivityForResult(i, REPLY_REQUEST_CODE);
                }
            });
        }

        /* Handles binding when the tweet has exactly one image. */
        private void bindSingleImageView(Tweet tweet) {
            rvImages.setVisibility(View.GONE);
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(tweet.imageUrls.get(0))
                    .transform(new RoundedCornersTransformation(RADIUS, MARGIN))
                    .into(ivImage);
        }

        /* Handles binding when the tweet has more than one image. */
        private void bindMultiImageView(Tweet tweet) {
            rvImages.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            ImagesAdapter adapter = new ImagesAdapter(context, tweet.imageUrls);
            rvImages.setAdapter(adapter);
            ivImage.setVisibility(View.GONE);
            rvImages.setVisibility(View.VISIBLE);
        }

        /* Displays a new activity via an Intent when the user clicks on a tweet. */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // get the tweet's position
            if (position != RecyclerView.NO_POSITION) { // ensure that the position is valid
                Tweet tweet = tweets.get(position); // get the tweet at the position

                // Navigate to the details activity and pass in the tweet
                Intent intent = new Intent(context, TweetDetailsActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }
    }
}