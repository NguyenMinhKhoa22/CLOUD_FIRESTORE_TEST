package com.example.firestorage2.ACTIVITY;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorage2.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SignUpAc extends AppCompatActivity {
    private de.hdodenhof.circleimageview.CircleImageView signInAvatarUser;
    private EditText signInName,signInPhone, signInEmail, signInPassword;
    private Button btnSignUp;
    ProgressBar LoadingSignUp;
    private TextView switchToSignIn;
    Uri imgAvatar = null;
    String userID;
    UploadTask uploadTask;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseAuth mAuth;
    FirebaseFirestore mFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        CallFindViewById();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("profile_images");
        mAuth = FirebaseAuth.getInstance();
        mFire = FirebaseFirestore.getInstance();

        // if user was login, go Home
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadingSignUp.setVisibility(View.VISIBLE);
                CreateAccountWithEmailPassword();
            }
        });

        switchToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToSignIn();
            }
        });

        signInAvatarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseAvatarFromDevice();
            }
        });

    }

    private void CreateAccountWithEmailPassword() {

        final String name = signInName.getText().toString();
        final String phone = signInPhone.getText().toString();
        final String email = signInEmail.getText().toString();
        final String password = signInPassword.getText().toString();
        if (name.equals("") || email.equals("") || password.equals("") ||  phone.equals("") ){
            Toast.makeText(SignUpAc.this, "MISSING", Toast.LENGTH_SHORT).show();
        } else {

            final StorageReference reference1 = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imgAvatar));
            uploadTask = reference1.putFile(imgAvatar);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return reference1.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    // get USER ID in AUTHENTICATION
                                    userID = mAuth.getCurrentUser().getUid();
                                    // create :v
                                    DocumentReference documentReference = mFire.collection("USER_ACCOUNT").document(userID);
                                        // upload data to fire
                                    Map<String, String> user = new HashMap<>();
                                    user.put("eName", name);
                                    user.put("eMail", email);
                                    user.put("ePhone", phone);
                                    user.put("eAvatar",downloadUri.toString());
                                    // set user into userId of email create
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SignUpAc.this, "User Created", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    Log.d(TAG, "ERROR +" + task.getException().getMessage());
                                    Toast.makeText(SignUpAc.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                                    LoadingSignUp.setVisibility(View.INVISIBLE);
                                }
                            }

                        });
                    }

                }
            });

        }
    }

    // detect file of image
    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void ChooseAvatarFromDevice() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent,100);
    }
    /// set image choose from gallery to signInAvatarUser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imgAvatar = data.getData();
            Picasso.get().load(imgAvatar).into(signInAvatarUser);
//            addAvatarUser.setImageURI(pickedImgUri);
        }
    }

    private void GoToSignIn() {
        Intent SignIn_intent = new Intent(getApplicationContext(), SignInAc.class);
        startActivity(SignIn_intent);
        finish();
    }

    private void CallFindViewById() {
        signInAvatarUser = findViewById(R.id.signUpAvatarUser);
        signInName = findViewById(R.id.edtName);
        signInPhone = findViewById(R.id.edtPhone);
        signInEmail = findViewById(R.id.edtEmail);
        signInPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        LoadingSignUp= findViewById(R.id.loadingSignUp);
        switchToSignIn = findViewById(R.id.switchToSignIn);
    }
}