package com.example.wordsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class NewKanjiActivity extends AppCompatActivity {

    DatabaseReference reference;
    HashMap<String, String> hashMap_fields = new HashMap<>();
    HashMap<String, String> hashMap_create;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word);

        TextView new_word_text_view = findViewById(R.id.new_word_text_view);
        new_word_text_view.setText("New kanji");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ImageButton back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText word_name = findViewById(R.id.enter_word);
        word_name.setHint("enter kanji ..");
        EditText word_meaning = findViewById(R.id.enter_meaning);
        EditText attribute = findViewById(R.id.enter_attribute);
        EditText value = findViewById(R.id.enter_value);

        TextView new_fields = findViewById(R.id.new_fields);
        new_fields.setMovementMethod(new ScrollingMovementMethod());
        new_fields.setTextIsSelectable(true);

        Button add_field_button = findViewById(R.id.add_fields_button);
        add_field_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String attribute_text = attribute.getText().toString();
                String value_text = value.getText().toString();
                if(!attribute_text.equals("") && !value_text.equals("")) {
                    new_fields.setVisibility(View.VISIBLE);
                    String new_fields_text = new_fields.getText().toString();
                    new_fields_text = new_fields_text.concat("\n" + attribute_text + ": " + value_text + "\n");
                    new_fields.setText(new_fields_text);

                    hashMap_fields.put(attribute_text, value_text);
                    attribute.setText("");
                    value.setText("");

                    System.out.println("A " + hashMap_fields.get("a1") + "\tA2 " + hashMap_fields.get("a2") + "\tA3 " + hashMap_fields.get("a3"));
                }
            }
        });

        Button create_word = findViewById(R.id.create_word_button);
        create_word.setText("create kanji");
        create_word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word_name_text = word_name.getText().toString();
                String word_meaning_text = word_meaning.getText().toString();
                if(!word_name_text.equals("") && !word_meaning_text.equals("")) {
                    hashMap_create = new HashMap<>();
                    hashMap_create.put("word", word_name_text);
                    hashMap_create.put("meaning", word_meaning_text);
                    if(hashMap_fields != null)
                        hashMap_create.putAll(hashMap_fields);

                    reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    reference.child("kanji").child(word_name_text).setValue(hashMap_create);
                    finish();
                }
            }
        });

        //img part
        storageReference = FirebaseStorage.getInstance().getReference("uploads").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ImageButton img_button = findViewById(R.id.add_img_button);
        img_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST);
            }
        });
    }

    //img part
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = Objects.requireNonNull(getApplicationContext()).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(NewKanjiActivity.this);
        pd.setMessage("Uploading...");
        pd.show();

        if(imageUri!=null){

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." +
                    getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        hashMap_fields.put("image", mUri);
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    pd.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data!= null && data.getData()!=null){
            imageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Uploading image... ", Toast.LENGTH_SHORT).show();
            }else{
                uploadImage();
            }
        }
    }
}