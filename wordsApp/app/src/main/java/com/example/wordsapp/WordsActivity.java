package com.example.wordsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.wordsapp.Adapters.WordAdapter;
import com.example.wordsapp.Classes.WordClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WordsActivity extends AppCompatActivity {

    DatabaseReference reference;

    RecyclerView word_recycler_view;
    WordAdapter wordAdapter;
    ArrayList<WordClass> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        TextView words_text_view = findViewById(R.id.words_text_view);
        words_text_view.setText("Words (-)");

        ImageButton back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button new_word_button = findViewById(R.id.new_word_button);
        new_word_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WordsActivity.this, NewWordActivity.class));
            }
        });

        word_recycler_view = findViewById(R.id.words_recycler_view);
        word_recycler_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        word_recycler_view.setLayoutManager(linearLayoutManager);

        arrayList = new ArrayList<>();

        //read words
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for(DataSnapshot snap : snapshot.child("words").getChildren()) {
                    WordClass word = snap.getValue(WordClass.class);
                    arrayList.add(word);
                }

                wordAdapter = new WordAdapter(WordsActivity.this, arrayList);
                word_recycler_view.setAdapter(wordAdapter);

                words_text_view.setText("Words (" + String.valueOf(wordAdapter.getItemCount()) + ")" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        EditText search_text = findViewById(R.id.search_bar);
        ImageButton search_button = findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search_word = search_text.getText().toString();
                reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayList.clear();
                        for(DataSnapshot snap : snapshot.child("words").getChildren()) {
                            WordClass word = snap.getValue(WordClass.class);
                            if(word.getWord().contains(search_word)) {
                                arrayList.add(word);
                            }
                        }

                        wordAdapter = new WordAdapter(WordsActivity.this, arrayList);
                        word_recycler_view.setAdapter(wordAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}