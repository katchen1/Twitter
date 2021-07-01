package com.codepath.apps.restclienttemplate.activities;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.Constants;
import com.codepath.apps.restclienttemplate.databinding.ActivityUserDetailsBinding;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.R;
import org.parceler.Parcels;
import java.util.Locale;

public class UserDetailsActivity extends AppCompatActivity {

    private User user; // The user whose details are shown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply view binding to reduce view boilerplate
        ActivityUserDetailsBinding binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // Initialize member variables
        user = Parcels.unwrap(getIntent().getParcelableExtra(Constants.USER_KEY_NAME));

        // Populate the view's components with the user's data
        binding.tvName.setText(user.name);
        binding.tvScreenName.setText(String.format(Locale.US, "@%s", user.screenName));
        binding.tvDescription.setText(user.description);
        binding.tvLocation.setText(user.location);
        binding.tvUrl.setText(user.url);
        binding.tvCreatedAt.setText(user.createdAt);
        binding.tvFollowingCount.setText(String.format(Locale.US, "%d", user.friendsCount));
        binding.tvFollowerCount.setText(String.format(Locale.US, "%d", user.followerCount));
        Glide.with(this).load(user.profileImageUrl).circleCrop().into(binding.ivProfileImage);
        if (!user.profileBannerUrl.isEmpty()) {
            Glide.with(this).load(user.profileBannerUrl).into(binding.ivProfileBanner);
        }
        if (user.verified) {
            binding.ivVerified.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twitter));
        }
    }

    /* Goes back to the previous activity and pass in the updated user. */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.USER_KEY_NAME, Parcels.wrap(user));
        setResult(RESULT_OK, intent);
        finish();
    }

    /* If the follower or following info is clicked, navigate to followers activity to show the list. */
    public void followingOnClick(View view) {
        Intent i = new Intent(UserDetailsActivity.this, FollowersActivity.class);
        i.putExtra("user", Parcels.wrap(user));
        if (view.getId() == R.id.tvFollowerCount || view.getId() == R.id.tvFollower) {
            i.putExtra("mode", "follower");
        } else if (view.getId() == R.id.tvFollowingCount || view.getId() == R.id.tvFollowing) {
            i.putExtra("mode", "following");
        }
        startActivity(i);
    }
}