package com.example.firestorage2.ACTIVITY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorage2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInAc extends AppCompatActivity {
    private EditText signInEmail, signInPassword;
    private Button btnSignIn;
    ProgressBar LoadingSignIn;
    private TextView switchToSignUp;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        CallFindViewById();
        mAuth = FirebaseAuth.getInstance();


        switchToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToSignUp();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingSignIn.setVisibility(View.VISIBLE);
                loginAccount();
            }
        });



    }

    private void loginAccount() {
        String email = signInEmail.getText().toString();
        String password = signInPassword.getText().toString();
        if ( TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(SignInAc.this, "MISSING", Toast.LENGTH_SHORT).show();
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignInAc.this, "Sign In OK", Toast.LENGTH_SHORT).show();
                    GoToHome();
                } else {
                    Toast.makeText(SignInAc.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                    LoadingSignIn.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void GoToHome() {
        Intent Home_intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(Home_intent);
        finish();
    }

    private void GoToSignUp() {
        Intent SignUp_intent = new Intent(getApplicationContext(), SignUpAc.class);
        startActivity(SignUp_intent);
        finish();
    }

    private void CallFindViewById() {
        signInEmail = findViewById(R.id.signInEmail);
        signInPassword = findViewById(R.id.signInPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        LoadingSignIn= findViewById(R.id.loadingSignIn);
        switchToSignUp = findViewById(R.id.switchToSignUp);
    }

}