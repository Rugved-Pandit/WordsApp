package com.example.wordsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            startActivity(new Intent(MainActivity.this, Activity2.class));
            finish();
        }
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) { }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        EditText username, email, password;
        username = findViewById(R.id.enter_username);
        email = findViewById(R.id.enter_email);
        password = findViewById(R.id.enter_password);

        Button register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username_txt = username.getText().toString();
                String email_text = email.getText().toString();
                String password_txt = password.getText().toString();

                if(username_txt!= null && email_text != null && password_txt != null) {
                    if(password_txt.length() > 7) {
                        auth.createUserWithEmailAndPassword(email_text, password_txt)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()) {
                                            firebaseUser = auth.getCurrentUser();
                                            String userid = firebaseUser.getUid();
                                            reference = FirebaseDatabase.getInstance().getReference().child("user").child(userid);

                                            HashMap<String, String> hashMap = new HashMap<>();
                                            hashMap.put("id", userid);
                                            hashMap.put("username", username_txt);

                                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Intent intent = new Intent(MainActivity.this, Activity2.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        } else Toast.makeText(MainActivity.this, "REGISTRATION FAILED", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else Toast.makeText(MainActivity.this, "PASSWORD MUST BE ATLEAST 8 CHARS LONG", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(MainActivity.this, "ALL FIELDS ARE COMPULSORY", Toast.LENGTH_SHORT).show();
            }
        });

        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_text = email.getText().toString();
                String password_txt = password.getText().toString();

                if(email_text != null && password_txt != null) {
                    if(!email_text.contains("@")) {
                        email_text = email_text.concat("@gmail.com");
                    }
                    auth.signInWithEmailAndPassword(email_text, password_txt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, Activity2.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else Toast.makeText(MainActivity.this, "LOGIN FAILED", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else Toast.makeText(MainActivity.this, "EMAIL AND PASSWORD ARE COMPULSORY", Toast.LENGTH_SHORT).show();
            }
        });
    }
}