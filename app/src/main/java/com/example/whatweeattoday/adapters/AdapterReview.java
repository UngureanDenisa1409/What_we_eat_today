package com.example.whatweeattoday.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatweeattoday.MyApplication;
import com.example.whatweeattoday.R;
import com.example.whatweeattoday.databinding.RowReviewBinding;
import com.example.whatweeattoday.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {

    //context
    private Context context;

    //view binding
    private @NonNull RowReviewBinding binding;

    private FirebaseAuth firebaseAuth;

    //arraylist to hold comments
    private ArrayList<ModelReview> reviewArrayList;

    //constructor
    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate/bind the view xml
        binding = RowReviewBinding.inflate(LayoutInflater.from(context), parent, false);

        //View view  = LayoutInflater.from(context).inflate(R.layout.activity_rate, parent, false);
        //return new HolderReview(view);
        return new HolderReview(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data
        ModelReview modelReview = reviewArrayList.get(position);
        String review = modelReview.getReview();
        String ratings = modelReview.getRatings();
        String uid = modelReview.getUid();
        String timestamp = modelReview.getTimestamp();

        //load using uid we stored in each comment
        loadUserDetails(modelReview, holder);

        //format date, already made function in MyApplication class
        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        //set data
        holder.dateTv.setText(date);
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);

        /*//handle click, show option to delete comment
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())){
                    deleteRatings(modelReview, holder);
                }
            }
        });*/
    }

    /*private void deleteRatings(ModelReview modelReview, HolderReview holder) {
        //show confirm dialog before deleting comment
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Rating")
                .setMessage("Are you sure you want to delete this rating? ")

                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete from dialog clicked, begin delete

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
                        ref.child(modelReview.getRecipeId())
                                .child("Ratings")
                                .child(modelReview.getId()) //rate id
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context,"Deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Failed to delete due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel clicked
                        dialog.dismiss();
                    }
                })
                .show();
    }*/

    private void loadUserDetails(ModelReview modelReview, HolderReview holder) {
        String uid = modelReview.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTv.setText(name);
                        try {
                            Picasso.get()
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(holder.profileIv);
                        }
                        catch (Exception e) {
                            //if anything goes wrong
                            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size(); //return records size, number of records
    }

    //view holder class for row_comment.xm;
    class HolderReview extends RecyclerView.ViewHolder{

        //ui views of row_review.xml
        ShapeableImageView profileIv;
        TextView nameTv, dateTv, reviewTv;

        RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            //init ui views
            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            ratingBar = binding.ratingBar;
            dateTv = binding.dateTv;
            reviewTv = binding.reviewTv;

        }
    }
}
