package com.codepath.apps.restclienttemplate;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    Context context;
    List<String> imageUrls;

    // Pass in the context and list of image urls
    public ImagesAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        String imageUrl = imageUrls.get(position);
        // Bind the image url with the view holder
        Glide.with(context)
                .load(imageUrl)
                .transform(new RoundedCornersTransformation(70, 15))
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() { return imageUrls.size(); }

    // Define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
