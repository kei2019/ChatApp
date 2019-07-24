package com.example.chatapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedFragment extends Fragment {
    private View postView;
    private RecyclerView myPostList;
    private DatabaseReference postsRef, usersRef, likesRef;
    private FirebaseAuth mAUth;
    private String currentUID;

    private Boolean likeChecker = false;


    public NewsFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        postView = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mAUth = FirebaseAuth.getInstance();
        currentUID = mAUth.getCurrentUser().getUid();
        myPostList = (RecyclerView) postView.findViewById(R.id.post_list);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        return myPostList;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postsRef, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull final Posts model) {
//                        String userIDs = getRef(position).getKey();
                        final String postKey = getRef(position).getKey();

                        usersRef.child(currentUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    holder.userName.setText(model.getName());
                                    holder.date.setText(model.getDate());
                                    holder.description.setText(model.getDescription());
                                    Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.userImage);
                                    Picasso.get().load(model.getPostimage()).into(holder.post);

                                    holder.setLikeButtonStatus(postKey);

                                    holder.likeButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            likeChecker = true;
                                            likesRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (likeChecker.equals(true)) {
                                                        if (dataSnapshot.child(postKey).hasChild(currentUID)) {
                                                            likesRef.child(postKey).child(currentUID).removeValue();
                                                            likeChecker = false;
                                                        } else {
                                                            likesRef.child(postKey).child(currentUID).setValue(true);
                                                            likeChecker = false;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_layout, viewGroup, false);
                        PostsViewHolder viewHolder = new PostsViewHolder(view);
                        return viewHolder;
                    }
                };

        myPostList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, date, description, noOfLikes;
        CircleImageView userImage;
        ImageView post;
        Button likeButton;

        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.post_name);
            date = itemView.findViewById(R.id.post_date);
            userImage = itemView.findViewById(R.id.post_image);
            description = itemView.findViewById(R.id.description);
            post = itemView.findViewById(R.id.image);
            likeButton = itemView.findViewById(R.id.likeButton);
            noOfLikes = itemView.findViewById(R.id.noOfLikes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setText("Unlike");
                        noOfLikes.setText((Integer.toString(countLikes)) + " likes");
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setText("Like");
                        noOfLikes.setText((Integer.toString(countLikes)) + " likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


}
