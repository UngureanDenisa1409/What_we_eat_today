package com.example.whatweeattoday;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatweeattoday.adapters.AdapterPdfUser;
import com.example.whatweeattoday.databinding.FragmentRecipeUserBinding;
import com.example.whatweeattoday.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecipeUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeUserFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    //view binding
    private FragmentRecipeUserBinding binding;

    private static final String TAG = "RECIPE_USER_TAG";

    public RecipeUserFragment() {
        // Required empty public constructor
    }
    public static RecipeUserFragment newInstance(String categoryId, String category, String uid) {
        RecipeUserFragment fragment = new RecipeUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentRecipeUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: Category: "+category);
        if(category.equals("All")) {
            //load all recipe
            loadAllRecipe();
        }
        else if(category.equals("Most Viewed")) {
            //load most viewed recipe
            loadMostViewedDownloadedRecipe("viewsCount");
        }
        else if(category.equals("Most Downloaded")) {
            //load most downloaded recipe
            loadMostViewedDownloadedRecipe("downloadsCount");
        }
        else {
            //load selected category recipe
            loadCategorizedRecipe();
        }

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type any letter
                try {
                    adapterPdfUser.getFilter().filter(s);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void loadAllRecipe() {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for( DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);

                    //add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                //set adapter to recyclerview
                binding.recipeRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedRecipe(String orderBy) {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.orderByChild(orderBy).limitToLast(10) //load 10 most viewed or downloaded recipe
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for( DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);

                    //add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                //set adapter to recyclerview
                binding.recipeRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategorizedRecipe() {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Recipe");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for( DataSnapshot ds: snapshot.getChildren()) {
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);

                            //add to list
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);

                        //set adapter to recyclerview
                        binding.recipeRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}