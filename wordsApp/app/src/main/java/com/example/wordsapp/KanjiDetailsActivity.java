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

public class KanjiDetailsActivity extends AppCompatActivity {

    DatabaseReference reference;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);

        TextView word_details_text_view = findViewById(R.id.word_details_text_view);
        word_details_text_view.setText("Kanji details");

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

        ImageView show_image = findViewById(R.id.show_img);

        TextView details = findViewById(R.id.word_details);
        details.setText("kanji details");
        details.setMovementMethod(new ScrollingMovementMethod());
        details.setTextIsSelectable(true);

        reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("kanji").child(word).exists()) {
                    System.out.println("\n\nHERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR\n\n");
                    snapshot = snapshot.child("kanji").child(word);
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
                else {
                    System.out.println("\n\nHEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE\n\n");
                    //snapshot = snapshot.child("word").child(word);
                    Intent intent = new Intent(KanjiDetailsActivity.this, WordDetailsActivity.class);
                    intent.putExtra("word", word);
                    intent.putExtra("isWord", true);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}