package com.example.firestorage2.ADAPTER;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.firestorage2.MODEL.CommentModel_inBottomSheetDialog;
import com.example.firestorage2.R;
import com.example.firestorage2.MODEL.UserAccount_Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CommentAdapter_inBottomSheetDialog extends RecyclerView.Adapter<CommentAdapter_inBottomSheetDialog.CmtViewHolder> {

    private Activity context;

    // use to get avatar and name user
    private ArrayList<UserAccount_Model> userList;

    // use to get COMMENT on firestore
    private ArrayList<CommentModel_inBottomSheetDialog> mCmtList;

    private FirebaseFirestore mFire;
    private FirebaseAuth mAuth;

    public CommentAdapter_inBottomSheetDialog(Activity context, ArrayList<UserAccount_Model> userList, ArrayList<CommentModel_inBottomSheetDialog> mCmtList) {
        this.context = context;
        this.userList = userList;
        this.mCmtList = mCmtList;
    }

    @NonNull
    @Override
    public CmtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
       mAuth = FirebaseAuth.getInstance();
       mFire = FirebaseFirestore.getInstance();
       return new CmtViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CmtViewHolder holder, int position) {
        holder.contentCmt.setText(mCmtList.get(position).getECmt());
        // get Avatar ( eAvatar ) and Name ( eName ) from Firestore
        String userID = mCmtList.get(position).geteUser();
        mFire.collection("USER_ACCOUNT").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String imageAvatar = task.getResult().getString("eAvatar");
                    String nameUser = task.getResult().getString("eName");
                    holder.setAvatarUserCmt(imageAvatar);
                    holder.setNameUserCmt(nameUser);
                }else {
                    Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mCmtList.size();
    }

    public class CmtViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView userCmtAvatar;
        TextView contentCmt, userNameCmt, DateCmt;
        public CmtViewHolder(@NonNull View itemView) {
            super(itemView);

            contentCmt = itemView.findViewById(R.id.contentCmt);
        }

        public void setAvatarUserCmt(String imageAvatarCmt) {
            userCmtAvatar = itemView.findViewById(R.id.userCmtAvatar);
            Glide.with(context).load(imageAvatarCmt).into(userCmtAvatar);
        }
        public void setNameUserCmt(String nameUserCmt) {
            userNameCmt = itemView.findViewById(R.id.userNameCmt);
            userNameCmt.setText(nameUserCmt);
        }
        public void setDateUserCmt(String dateUserCmt) {
            DateCmt = itemView.findViewById(R.id.DateCmt);
            DateCmt.setText(dateUserCmt);
        }
    }
}
