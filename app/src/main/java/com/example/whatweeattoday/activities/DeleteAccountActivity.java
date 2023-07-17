package com.example.whatweeattoday.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.whatweeattoday.R;
import com.example.whatweeattoday.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.Utils;

public class DeleteAccountActivity extends AppCompatActivity {

    //view binding
    private ActivityDeleteAccountBinding binding;

    //tag for logs in logcat
    private static final String TAG = "DELETE_ACCOUNT_TAG";

    //ProgressDialog to show while sending password recovery instructions
    private ProgressDialog progressDialog;

    //FirebaseAuth for auth relates tasks
    private FirebaseAuth firebaseAuth;

    //FirebaseUser to get current user and delete
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init and setup progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //get instance of FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        //handle backBtn click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        //handle deleteBtn click, start account deletion
        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        Log.d(TAG, "deleteAccount: ");

        progressDialog.setMessage("Deleting User Account");
        progressDialog.show();

        //step1: delete user account
        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user account deleted
                        Log.d(TAG, "onSuccess: Account deleted");

                        progressDialog.setMessage("Deleting User Data");
                        //step2: remove user data
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseUser.getUid())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //account data deleted
                                        Log.d(TAG, "onSuccess: User data deleted...");
                                        startActivity(new Intent(DeleteAccountActivity.this, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to delete user data
                                        //Log.d(TAG, "onFailure: "+ e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(DeleteAccountActivity.this, "Failed to delete user data due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(DeleteAccountActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(DeleteAccountActivity.this, "Failed to delete account due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}