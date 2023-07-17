package com.example.whatweeattoday.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatweeattoday.R;
import com.example.whatweeattoday.adapters.AdapterReview;
import com.example.whatweeattoday.databinding.ActivityPdfDetailBinding;
import com.example.whatweeattoday.databinding.ActivityRateBinding;
import com.example.whatweeattoday.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class RateActivity extends AppCompatActivity {

    //view binding
    private ActivityRateBinding binding;

    private String recipeId;

    //firebase auth, get/update user data using uid
    private FirebaseAuth firebaseAuth;

    //arraylist to hold comments
    private ArrayList<ModelReview> reviewArrayList;

    //adapter to set to recyclerview
    private AdapterReview adapterReview;

    private TextView ratingsTv;

    private RatingBar ratingBar;

    private RecyclerView reviewsRv;

    private ImageView profileIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileIv = findViewById(R.id.profileIv);

        //get recipe uid from intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");

        firebaseAuth = FirebaseAuth.getInstance();

        //reviewsRv = findViewById(R.id.reviewsRv);

        //if user has written review to this recipe, load it

        //loadReviewDetails(); //recipe name, image of user
        loadMyReview(); //review list
        loadReviews();

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //input data
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

    }

    private float ratingSum = 0;
    private void loadReviews() {

        //db path to load ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear arraylist before start adding data into it
                        ratingSum = 0;
                        for( DataSnapshot ds: snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;
                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        binding.ratingsTv.setText(String.format("%.2f", avgRating) + "[" + numberOfReviews + "]");
                        binding.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //my review is available in this shop

                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String id = ""+snapshot.child("id").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ binding.ratingBar.getRating();
        String review = binding.reviewEt.getText().toString().trim();

        //for time of review
        String timestamp = ""+System.currentTimeMillis();

        //setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("id",""+timestamp);
        hashMap.put("recipeId",""+recipeId);

        //put to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.child(recipeId).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(RateActivity.this,"Rating published successfully...",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding review to db
                        Toast.makeText(RateActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}