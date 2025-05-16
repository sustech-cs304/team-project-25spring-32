package com.example.pa.ui.social;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

import java.util.List;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.ViewHolder> {

    private List<SocialPost> postList;

    public SocialPostAdapter(List<SocialPost> postList) {
        this.postList = postList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView contentText;
        ImageView postImage;

        public ViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.textUsername);
            contentText = view.findViewById(R.id.textContent);
            postImage = view.findViewById(R.id.imagePost);
        }
    }

    @NonNull
    @Override
    public SocialPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_social_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SocialPost post = postList.get(position);
        holder.usernameText.setText(post.getUsername());
        holder.contentText.setText(post.getContent());
        holder.postImage.setImageResource(post.getImageResId());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
