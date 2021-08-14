package com.example.wordsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class WordDetailsActivity extends AppCompatActivity {

    DatabaseReference reference;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ImageButton back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intent = getIntent();
        String word = intent.getStringExtra("word");
        boolean isWord = intent.getBooleanExtra("isWord", false);

        if(!isWord) {
            if(!word.toLowerCase().matches(".*[a-z].*")) {
                Intent intent = new Intent(WordDetailsActivity.this, KanjiDetailsActivity.class);
                intent.putExtra("word", word);
                startActivity(intent);
                finish();
            }
        }



        ImageView show_image = findViewById(R.id.show_img);

        TextView details = findViewById(R.id.word_details);
        details.setMovementMethod(new ScrollingMovementMethod());
        details.setTextIsSelectable(true);

        reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("words").child(word);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String detailed_string = "";
                for(DataSnapshot snap : snapshot.getChildren()) {
                    if(snap.getKey().equals("image")) {
                        Picasso.get()
                                .load(snap.getValue().toString())
                                .into(show_image);
                    } else {
                        detailed_string = detailed_string.concat(snap.getKey() + ": " + snap.getValue().toString() + "\n\n");
                    }
                }
                details.setText(detailed_string);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}