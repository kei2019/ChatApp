package com.example.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiveUserId, senderUID, current_state;

    private TextView userProfileName, userProfileStatus;
    private FirebaseAuth mAuth;


    private Button requestButton, cancelButton;
    private CircleImageView visitProfileImage;
    private DatabaseReference userRef, chatReqRef, contactsRef, notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiveUserId = getIntent().getExtras().get("visitUserId").toString();

        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        requestButton = findViewById(R.id.request_button);
        cancelButton = findViewById(R.id.cancel_request);
        visitProfileImage = findViewById(R.id.visit_profile_image);

        mAuth = FirebaseAuth.getInstance();
        senderUID = mAuth.getCurrentUser().getUid();

        current_state = "new";

        retriveUserInfo();
    }

    private void retriveUserInfo() {
        userRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(visitProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatRequest();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        chatReqRef.child(senderUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiveUserId)) {
                    String request_type = dataSnapshot.child(receiveUserId).child("request_type").getValue().toString();
//                    Toast.makeText(ProfileActivity.this, ""+request_type, Toast.LENGTH_SHORT).show();
                    if (request_type.equals("sent")) {
                        current_state = "request_sent";
                        requestButton.setText("Cancel Request");
                    } else if (request_type.equals("received")) {
                        current_state = "request_received";
                        requestButton.setText("Accept Request");
                        cancelButton.setVisibility(View.VISIBLE);
                        cancelButton.setEnabled(true);
                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelRequest();
                            }
                        });
                    }
                } else {
                    contactsRef.child(senderUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiveUserId)) {
                                current_state = "friends";
                                requestButton.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!senderUID.equals(receiveUserId)) {
            requestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestButton.setEnabled(false);
                    if ((current_state.equals("new"))) {
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        cancelRequest();
                    }

                    if (current_state.equals("request_received")) {
                        acceptRequest();
                    }

                    if (current_state.equals("friends")) {
                        removeContact();
                    }
                }
            });

        } else {
            requestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact() {
        contactsRef.child(senderUID).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(receiveUserId).child(senderUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requestButton.setEnabled(true);
                                current_state = "new";
                                requestButton.setText("Send Request");

                                cancelButton.setVisibility(View.INVISIBLE);
                                cancelButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptRequest() {
        contactsRef.child(senderUID).child(receiveUserId).child("Contacts")
                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(receiveUserId).child(senderUID).child("Contacts")
                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatReqRef.child(senderUID).child(receiveUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    chatReqRef.child(receiveUserId).child(senderUID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    requestButton.setEnabled(true);
                                                                    current_state = "friends";
                                                                    requestButton.setText("Remove this Contact");

                                                                    cancelButton.setVisibility(View.INVISIBLE);
                                                                    cancelButton.setEnabled(false);

                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelRequest() {
        chatReqRef.child(senderUID).child(receiveUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatReqRef.child(receiveUserId).child(senderUID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requestButton.setEnabled(true);
                                current_state = "new";
                                requestButton.setText("Send Request");

                                cancelButton.setVisibility(View.INVISIBLE);
                                cancelButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {

        chatReqRef.child(senderUID).child(receiveUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatReqRef.child(receiveUserId).child(senderUID).child("request_type")
                                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from", senderUID);
                                        chatNotificationMap.put("type", "request");
                                        notificationRef.child(receiveUserId).push().setValue(chatNotificationMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            requestButton.setEnabled(true);
                                                            current_state = "request_sent";
                                                            requestButton.setText("Cancel Request");
                                                        }
                                                    }
                                                });


                                    }
                                }
                            });
                        }
                    }
                });
    }
}
