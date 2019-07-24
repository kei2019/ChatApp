package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity implements View.OnClickListener {
    private EditText status, username, gender;
    private Button update;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private String currentUID;
    private ProgressDialog progressDialog;
    private Toolbar settingToolbar;

    private final static int GalleryPick = 1;
    private StorageReference profileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        status = findViewById(R.id.status);
        username = findViewById(R.id.username);
        update = findViewById(R.id.button_update);
        profileImage = findViewById(R.id.profile_image);
        gender = findViewById(R.id.gender);

        progressDialog = new ProgressDialog(this);

        settingToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        update.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        currentUID = mAuth.getCurrentUser().getUid();

        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RetriveInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v == update) {
            if ((isFieldEmpty(username)) && (isFieldEmpty(status))) {
                updateUser();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                progressDialog.setMessage("Profile picture is updating..");
                progressDialog.show();
                Uri resultUri = result.getUri();

                final StorageReference filePath = profileImageRef.child(currentUID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                myRef.child("Users").child(currentUID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(UpdateProfile.this, "Image saved to database", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else {
                                            String error = task.getException().toString();
                                            progressDialog.dismiss();
                                            Toast.makeText(UpdateProfile.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void updateUser() {
        String nameValue = username.getText().toString().trim();
        String bioValue = status.getText().toString().trim();
        String genderValue = gender.getText().toString().trim();

        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", currentUID);
        profileMap.put("name", nameValue);
        profileMap.put("status", bioValue);
        profileMap.put("gender", genderValue);
        myRef.child("Users").child(currentUID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(UpdateProfile.this, HomeActivity.class);
                    Toast.makeText(UpdateProfile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String error = task.getException().toString();
                    Toast.makeText(UpdateProfile.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void RetriveInfo() {
        myRef.child("Users").child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))) {
                    String retrieveName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveGender = dataSnapshot.child("gender").getValue().toString();
                    String retrieveImage = dataSnapshot.child("image").getValue().toString();

                    username.setText(retrieveName);
                    status.setText(retrieveStatus);
                    gender.setText(retrieveGender);
                    Picasso.get().load(retrieveImage).into(profileImage);


                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveGender = dataSnapshot.child("gender").getValue().toString();

                    username.setText(retrieveName);
                    username.setSelection(username.getText().length());
                    status.setText(retrieveStatus);
//                    status.setSelection(status.getText().length());
                    gender.setText(retrieveGender);
                } else {
                    Toast.makeText(UpdateProfile.this, "Please set your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public Boolean isFieldEmpty(EditText view) {
        if (view.getText().toString().length() > 0) {
            return true;
        } else {
            view.setError("Field Required");
            return false;
        }
    }


}
