package com.codepath.apps.restclienttemplate.adapters;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.Constants;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.activities.UserDetailsActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemFollowerBinding;
import com.codepath.apps.restclienttemplate.models.User;
import org.parceler.Parcels;
import java.util.List;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.ViewHolder> {

    private Context context;
    private List<User> followers;

    /* Constructor takes in the context and list of followers. */
    public FollowersAdapter(Context context, List<User> followers) {
        this.context = context;
        this.followers = followers;
    }

    /* Inflates the view for each follower. */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFollowerBinding binding = ItemFollowerBinding.inflate(LayoutInflater.from(context));
        return new ViewHolder(binding);
    }

    /* Fills in the view based on the position of the user. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User follower = followers.get(position);
        holder.bind(follower);
    }

    /* Returns how many items are in the followers list. */
    @Override
    public int getItemCount() { return followers.size(); }

    /* Defines a view holder for the image in the recycler view. */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemFollowerBinding binding;

        /* Constructor takes in a view binding and stores it as a member variable. */
        public ViewHolder(ItemFollowerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        /* Binds the follower's data to the view. */
        public void bind(User follower) {
            Glide.with(context).load(follower.profileImageUrl).circleCrop().into(binding.ivProfileImage);
            binding.tvName.setText(follower.name);
            binding.tvScreenName.setText(follower.screenName);
            binding.tvDescription.setText(follower.description);
            if (follower.verified) {
                binding.ivVerified.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.twitter));
            }
        }

        /* When the follower is clicked, navigate to the user detail screen for the follower. */
        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, UserDetailsActivity.class);
            i.putExtra(Constants.USER_KEY_NAME, Parcels.wrap(followers.get(getAdapterPosition())));
            ((Activity) context).startActivityForResult(i, Constants.USER_DETAIL_REQUEST_CODE);
        }
    }
}
