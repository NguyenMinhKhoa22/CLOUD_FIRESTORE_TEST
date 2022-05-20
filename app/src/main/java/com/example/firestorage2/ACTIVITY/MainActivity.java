package com.example.firestorage2.ACTIVITY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firestorage2.ADAPTER.PostAdapter;
import com.example.firestorage2.MODEL.PostModel;
import com.example.firestorage2.R;
import com.example.firestorage2.MODEL.UserAccount_Model;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private FloatingActionButton btnSignOut, btnProfileUser, btnUploadPost;
    private de.hdodenhof.circleimageview.CircleImageView avatarProfileUser;
    private ImageView imgPost;
    private EditText edtNameUser, edtPhoneUser;
    private TextView edtEmailUser, titleP, descripP;
    private Button btnSaveProfileUser, btnUploadP;
    private RecyclerView recyclerView_post;
    private ArrayList<PostModel> postModelList;
    private ArrayList<UserAccount_Model> usersList;
    private PostAdapter postAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFire;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    Uri imgPostUpload = null;
    String userID;

    UploadTask uploadTaskPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CallFindViewById();

        mAuth = FirebaseAuth.getInstance();
        mFire = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("Post_images");

        call_post_list();

        btnUploadPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogToUploadPost();
            }
        });

        btnProfileUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoCurrentUser();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOutToLogin();
            }
        });
    }


    // SET DATA TO RECYCLER VIEW OF POST
    private void call_post_list() {

        recyclerView_post.setHasFixedSize(true);
        recyclerView_post.setLayoutManager(new LinearLayoutManager(this));
        postModelList = new ArrayList<>();
        postAdapter = new PostAdapter(postModelList, usersList, MainActivity.this);
        recyclerView_post.setAdapter(postAdapter);
        mFire.collection("POST").orderBy("eTime", Query.Direction.DESCENDING ).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> listPost = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d:listPost){
                            String postId = d.getId();

                            PostModel obj = d.toObject(PostModel.class).withId(postId);
                            postModelList.add(obj);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

   // DIALOG UPLOAD POST
    private void showDialogToUploadPost() {
        Dialog dialogUploadPost = new Dialog(this);
        dialogUploadPost.setContentView(R.layout.post_show);
        dialogUploadPost.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        imgPost = dialogUploadPost.findViewById(R.id.imgPost);
        descripP = dialogUploadPost.findViewById(R.id.DescriptionPost);
        titleP = dialogUploadPost.findViewById(R.id.titlePost);
        btnUploadP = dialogUploadPost.findViewById(R.id.btnUpload);

        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseImgFromDevice();
            }
        });

        btnUploadP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get string from dialog post
                String titlePost = titleP.getText().toString();
                String descripPost = descripP.getText().toString();

                if (titlePost.equals("") || descripPost.equals("") || imgPostUpload == null) {
                    Toast.makeText(MainActivity.this, "MISSING IN POST", Toast.LENGTH_SHORT).show();
                } else {
                    final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imgPostUpload));
                    uploadTaskPost = reference.putFile(imgPostUpload);

                    // convert from time millis to date form
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy" + " HH:mm");
                    String dateString = formatter.format(new Date(System.currentTimeMillis()));

                    Task<Uri> uriTask = uploadTaskPost.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return reference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                userID = mAuth.getCurrentUser().getUid();

                                Map<String, Object> post = new HashMap<>();
                                post.put("eTitle", titlePost);
                                post.put("eDescription", descripPost);
                                post.put("eImgPost", downloadUri.toString());
                                post.put("eUser", userID);
                                post.put("eTime", dateString);
//                                post.put("eTime", FieldValue.serverTimestamp());

                                mFire.collection("POST").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "added", Toast.LENGTH_SHORT).show();
                                            dialogUploadPost.dismiss();
                                        }
                                        else {
                                            Toast.makeText(MainActivity.this, "error when add", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        dialogUploadPost.show();
    }


    // DISPLAY INFORMATION OF CURRENT USER
    private void showInfoCurrentUser() {
        Dialog dialogCurrentUser = new Dialog(this);
        dialogCurrentUser.setContentView(R.layout.profile_user_show);
        dialogCurrentUser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        avatarProfileUser = dialogCurrentUser.findViewById(R.id.avatarCurrentUser);
        edtNameUser = dialogCurrentUser.findViewById(R.id.nameCurrentUser);
        edtPhoneUser = dialogCurrentUser.findViewById(R.id.phoneCurrentuser);
        edtEmailUser = dialogCurrentUser.findViewById(R.id.emailCurrentUser);
        btnSaveProfileUser = dialogCurrentUser.findViewById(R.id.saveProfile);

        DocumentReference reference = mFire.collection("USER_ACCOUNT").document(userID);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    String nameCurrent = task.getResult().getString("eName");
                    String emailCurrent = task.getResult().getString("eMail");
                    String phoneCurrent = task.getResult().getString("ePhone");
                    String urlAvatar = task.getResult().getString("eAvatar");
                    Picasso.get().load(urlAvatar).into(avatarProfileUser);
                    edtNameUser.setText(nameCurrent);
                    edtPhoneUser.setText(phoneCurrent);
                    edtEmailUser.setText(emailCurrent);
                }
            }
        });

        btnSaveProfileUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String edtName = edtNameUser.getText().toString();
                String edtPhone = edtPhoneUser.getText().toString();
                reference.update("eName", edtName);
                reference.update("ePhone", edtPhone);
            }
        });
        dialogCurrentUser.show();
    }

    private void SignOutToLogin() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), SignInAc.class));
        finish();
    }

    // detect end file of image
    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void ChooseImgFromDevice() {
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
            imgPostUpload = data.getData();
            Picasso.get().load(imgPostUpload).into(imgPost);
//            addAvatarUser.setImageURI(pickedImgUri);
        }
    }
    private void CallFindViewById() {
        recyclerView_post = findViewById(R.id.rec_post);
        btnSignOut = findViewById(R.id.SignOut);
        btnProfileUser = findViewById(R.id.profileUser);
        btnUploadPost = findViewById(R.id.UploadPost);
    }

}