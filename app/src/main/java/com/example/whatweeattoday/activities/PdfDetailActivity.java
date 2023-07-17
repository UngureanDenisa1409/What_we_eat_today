package com.example.whatweeattoday.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatweeattoday.MyApplication;
import com.example.whatweeattoday.R;
import com.example.whatweeattoday.adapters.AdapterComment;
import com.example.whatweeattoday.adapters.AdapterReview;
import com.example.whatweeattoday.databinding.ActivityPdfDetailBinding;
import com.example.whatweeattoday.databinding.DialogCommentAddBinding;
import com.example.whatweeattoday.models.ModelComment;
import com.example.whatweeattoday.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;

    //view binding
    private ActivityPdfDetailBinding binding;

    //pdf id, get from intent
    String recipeId, recipeTitle, recipeUrl;

    //progress dialog
    private ProgressDialog progressDialog;

    //arraylist to hold comments
    private ArrayList<ModelComment> commentArrayList;

    //adapter to set to recyclerview
    private AdapterComment adapterComment;

    //arraylist to hold comments
    private ArrayList<ModelReview> reviewArrayList;

    //adapter to set to recyclerview
    private AdapterReview adapterReview;

    private TextView ratingsTv;

    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");

        //at start hide download button, need recipe url that i will load later in loadRecipeDetails();
        binding.downloadRecipeBtn.setVisibility(View.GONE);

        ratingsTv = findViewById(R.id.ratingsTv);
        ratingBar = findViewById(R.id.ratingBar);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        
        loadRecipeDetails();
        loadComments();

        //load recipe info: name, image
        loadReviews();

        //increment recipe view count, whenever this page starts
        MyApplication.incrementRecipeViewCount(recipeId);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, open to view pdf
        binding.readRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("recipeId", recipeId);
                startActivity(intent1);
            }
        });

        //handle click, download pdf
        binding.downloadRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    MyApplication.downloadRecipe(PdfDetailActivity.this, "" + recipeId, "" + recipeTitle, "" + recipeUrl);
                }
            }
        });

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this,"You're not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isInMyFavorite) {
                        //in favorite, remove from favorite
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,recipeId);
                    }
                    else {
                        //not in favorite, add to favorite
                        MyApplication.addToFavorite(PdfDetailActivity.this,recipeId);
                    }
                }
            }
        });

        //handle click, show comment add dialog
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user must be logged in to add comment
                if( firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(PdfDetailActivity.this,"You're not logged in...",Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });

        //handle click, show comment add dialog
        binding.writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user must be logged in to add comment
                if( firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(PdfDetailActivity.this,"You're not logged in...",Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent1 = new Intent(PdfDetailActivity.this, RateActivity.class);
                    intent1.putExtra("recipeId", recipeId); //needed for write review
                    startActivity(intent1);
                }
            }
        });
    }

    private void loadComments() {
        //init arraylist before adding data into it
        commentArrayList = new ArrayList<>();

        //db path to load comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear arraylist before start adding data into it
                        commentArrayList.clear();
                        for( DataSnapshot ds: snapshot.getChildren()) {
                            //get data as model, spellings of variables in model must be as same as in firebase
                            ModelComment model = ds.getValue(ModelComment.class);

                            //add to arraylist
                            commentArrayList.add(model);
                        }
                        //setup adapter
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);

                        //set adapter to recyclerview
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        //init arraylist before adding data into it
        reviewArrayList = new ArrayList<>();

        //db path to load ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear arraylist before start adding data into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for( DataSnapshot ds: snapshot.getChildren()) {
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;

                            //get data as model, spellings of variables in model must be as same as in firebase
                            ModelReview modelReview = ds.getValue(ModelReview.class);

                            //add to arraylist
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(PdfDetailActivity.this, reviewArrayList);

                        //set adapter to recyclerview
                        binding.reviewsRv.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        String ratings1 = (String.format("%.2f", avgRating) + "[" + numberOfReviews + "]");

                        ratingBar.setRating(avgRating);
                        ratingsTv.setText(ratings1);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String comment = "";
    private void addCommentDialog() {
        //inflate bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        //setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //handle click, dismiss dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        //handle click, add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get data
                comment = commentAddBinding.commentEt.getText().toString().trim();
                //validate data
                if(TextUtils.isEmpty(comment)) {
                    Toast.makeText(PdfDetailActivity.this,"Enter your comment...",Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });
    }

    private void addComment() {
        //show progress
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        //timestamp for comment id, comment time
        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("recipeId",""+recipeId);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //db path to add into it
        //recipe > recipeId > comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this,"Comment Added...",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add comment
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this,"Failed to add comment due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //request storage permission

    private void loadRecipeDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        recipeTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        recipeUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();


                        //required data is loaded, show download button
                        binding.downloadRecipeBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+recipeUrl,
                                ""+recipeTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );

                        MyApplication.loadPdfSize(
                                ""+recipeUrl,
                                ""+recipeTitle,
                                binding.sizeTv
                        );

                        //set data
                        binding.titleTv.setText(recipeTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){
        //logged in check if its in favorite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(recipeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists(); //true if exists, false if not
                        if(isInMyFavorite) {
                            //exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else {
                            //not exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0);
                            binding.favoriteBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}