package com.example.firestorage2.ADAPTER;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firestorage2.MODEL.CommentModel_inBottomSheetDialog;
import com.example.firestorage2.MODEL.PostModel;
import com.example.firestorage2.R;
import com.example.firestorage2.MODEL.UserAccount_Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{
    private de.hdodenhof.circleimageview.CircleImageView userCurrentAvatar_cmt, avatarUserProfile;
    private TextView nameUserProfile;
    private EditText commentCurrentUser_cmt;
    private Button sendCmtCurrentUser_cmt;
    private ArrayList<PostModel> mList;
    private ArrayList<UserAccount_Model> usersList;

    private Activity context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFire;

    // test rec cmt
    private ArrayList<CommentModel_inBottomSheetDialog> mCmtList;
    //////

    public PostAdapter(ArrayList<PostModel> mList, ArrayList<UserAccount_Model> usersList, Activity context) {
        this.mList = mList;
        this.usersList = usersList;
        this.context = context;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View viewP = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
        mAuth = FirebaseAuth.getInstance();
            mFire = FirebaseFirestore.getInstance();
       return new PostViewHolder(viewP);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        // do so much @@
        holder.tvTitle.setText(mList.get(position).geteTitle());
        holder.tvDescription.setText(mList.get(position).geteDescription());
        holder.tvDescription.setSelected(true);
        holder.tvDatePost.setText(mList.get(position).geteTime());
        holder.setPostPic(mList.get(position).geteImgPost());

        // go to Avatar ( eAvatar ) and Name ( eName ) from Firestore
        String userID = mList.get(position).geteUser();
        mFire.collection("USER_ACCOUNT").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String imageAvatar = task.getResult().getString("eAvatar");
                    String nameUser = task.getResult().getString("eName");
                    holder.setAvatarUserPost(imageAvatar);
                    holder.setNameUserPost(nameUser);

                    // test click avatar user in post to open bottom sheet
                    holder.avatarUserPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                            bottomSheetDialog.setContentView(R.layout.layout_bsd_profile);
                            avatarUserProfile = bottomSheetDialog.findViewById(R.id.avatarUserProfile);
                            nameUserProfile = bottomSheetDialog.findViewById(R.id.nameUserProfile);
                            bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            nameUserProfile.setText(nameUser);
                            Picasso.get().load(imageAvatar).into(avatarUserProfile);
                            bottomSheetDialog.show();
                        }
                    });
                }else {
                    Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        String postId = mList.get(position).PostId;

        /// TEST TEST
        holder.LikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Like clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // ADD DATA COMMENT TO FIREBASE HERE

        holder.CommentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCmtBottomSheetDialog();

                sendCmtCurrentUser_cmt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String contentCmt = commentCurrentUser_cmt.getText().toString();
                        if (!contentCmt.isEmpty()) {
                            String currentUserIdCmt = mAuth.getCurrentUser().getUid();
                            Map<String, Object> commentsMap = new HashMap<>();
                            commentsMap.put("eCmt", contentCmt);
                            commentsMap.put("eTime", FieldValue.serverTimestamp());
                            commentsMap.put("eUser", currentUserIdCmt);
                            mFire.collection("POST/" + postId + "/COMMENT").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(view.getContext(), "Comment ADDED", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(view.getContext(), "ERROR WHEN COMMENT", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(view.getContext(), "MISSING IN COMMENT", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
//
        // Show delete button for current user
        String currentUserID = mAuth.getCurrentUser().getUid();
        if (currentUserID.equals(mList.get(position).geteUser())) {
            holder.DeletePost.setVisibility(View.VISIBLE);
            holder.DeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                String title = holder.tvTitle.getText().toString();
                alert.setTitle("DELETE POST "+ title)
                        .setMessage("SURE ?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            mFire.collection("POST/" + postId + "/COMMENT").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                            mFire.collection("POST/" + postId + "/COMMENT").document(snapshot.getId()).delete();
                                            }
                                        }
                                    });
                            mFire.collection("POST").document(postId).delete();
                            mList.remove(position);
                            notifyDataSetChanged();
                            }
                        });
                alert.show();
            }
        });
        }
    }

    ///// show layout when click image COMMENT at post
    private void showCmtBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet_comment);
        userCurrentAvatar_cmt = bottomSheetDialog.findViewById(R.id.userCurrentAvatar_cmt);
        commentCurrentUser_cmt = bottomSheetDialog.findViewById(R.id.commentCurrentUser_cmt);
        sendCmtCurrentUser_cmt = bottomSheetDialog.findViewById(R.id.sendCmtCurrentUser_cmt);
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
         de.hdodenhof.circleimageview.CircleImageView avatarUserPost;
         TextView tvNameUser, tvDatePost, tvTitle, tvDescription;
         ImageView imgUserPost, LikePost, CommentPost, DeletePost;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDatePost     = itemView.findViewById(R.id.dateUserPost_row);
            tvTitle        = itemView.findViewById(R.id.title_row);
            tvDescription  = itemView.findViewById(R.id.description_row);
            LikePost       = itemView.findViewById(R.id.LikePost);
            CommentPost    = itemView.findViewById(R.id.CommentPost);
            DeletePost     = itemView.findViewById(R.id.delete_post_row);
    }
        public void setPostPic(String urlPost){
            imgUserPost = itemView.findViewById(R.id.imgPostuser_row);
            Glide.with(context).load(urlPost).into(imgUserPost);
        }
        public void setNameUserPost(String username){
            tvNameUser = itemView.findViewById(R.id.nameUserPost_row);
            tvNameUser.setText(username);
        }
        public void setAvatarUserPost(String urlProfile){
            avatarUserPost = itemView.findViewById(R.id.avatarUserPost_row);
            Glide.with(context).load(urlProfile).into(avatarUserPost);
        }
    }
}
