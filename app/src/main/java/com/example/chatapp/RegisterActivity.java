package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.firebase.FirebaseApp.*;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText Email, Password;
    private Button Register;
    private TextView Have_Account;

    private FirebaseAuth mAuth;
    //private FirebaseApp firebaseApp;
    private ProgressDialog progressDialog;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Email = findViewById(R.id.reg_email);
        Password = findViewById(R.id.reg_password);
        Register = findViewById(R.id.reg_signUp);
        Have_Account = findViewById(R.id.already_have_an_account);
        Have_Account.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        myRef = FirebaseDatabase.getInstance().getReference();
        //  firebaseApp = initializeApp(this);
        progressDialog = new ProgressDialog(this);
        Register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == Have_Account) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }

        if (v == Register) {

            if ((isFieldEmpty(Email)) && isFieldEmpty(Password)) {
                if ((isEmailValid(Email))) {
                    registerUser();
                }
            }
        }
    }

    private void registerUser() {
        String emailValue = Email.getText().toString().trim();
        String passValue = Password.getText().toString().trim();
        Toast.makeText(this, "email " + emailValue, Toast.LENGTH_SHORT).show();
        progressDialog.setMessage("Creating account, Please wait!");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(emailValue, passValue).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String currentUserid = mAuth.getCurrentUser().getUid();
                    myRef.child("Users").child(currentUserid).setValue("");
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterActivity.this, UpdateProfile.class));
                    Toast.makeText(RegisterActivity.this, "Account registered successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    progressDialog.dismiss();
                    String error = task.getException().toString();
                    Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
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

    public Boolean isEmailValid(EditText view) {
        String value = view.getText().toString();
        if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return true;
        } else {
            view.setError("Invalid email");
            return false;
        }

    }

}
