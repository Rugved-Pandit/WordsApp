package com.example.wordsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.wordsapp.Adapters.MessageAdapter;
import com.example.wordsapp.Classes.MessageClass;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

public class Activity2 extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ArrayList<MessageClass> arrayList;
    RecyclerView chat_recycler_view;
    MessageAdapter messageAdapter;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        //to keep the keyboard from activating like a whore just cause there is an edit text
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        EditText text_text = findViewById(R.id.text_text);

        Button go_button = findViewById(R.id.go_button);
        go_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = text_text.getText().toString();
                if(!message.isEmpty()) {
                    sendMessage(message);
                }
                text_text.setText("");
            }
        });

        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        chat_recycler_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Activity2.this);
        linearLayoutManager.setStackFromEnd(true);
        chat_recycler_view.setLayoutManager(linearLayoutManager);

        reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid()).child("chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                readMessage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        EditText search_bar = findViewById(R.id.search_bar);
        search_bar.clearFocus();

        ImageButton search_up_button = findViewById(R.id.search_up_button);
        search_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchMessage = search_bar.getText().toString();
                int lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(lastCompletelyVisibleItemPosition <0) lastCompletelyVisibleItemPosition = chat_recycler_view.getChildCount();
                searchText(searchMessage, true, lastCompletelyVisibleItemPosition);
            }
        });

        ImageButton search_down_button = findViewById(R.id.search_down_button);
        search_down_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchMessage = search_bar.getText().toString();
                int firstCompletelyVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                searchText(searchMessage, false, firstCompletelyVisibleItemPosition);
            }
        });

        Button words_button = findViewById(R.id.words_button);
        words_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(Activity2.this, WordsActivity.class));
            }
        });

        Button kanji_button = findViewById(R.id.kanji_button);
        kanji_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(Activity2.this, KanjiActivity.class));
            }
        });

        Button test_button = findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(Activity2.this, Activity3.class));
            }
        });

        //options menu thing
        ImageButton options_activity_2_button = findViewById(R.id.options_activity_2_button);
        options_activity_2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(Activity2.this, v);
                // This activity implements OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.info_option:
                                startActivity(new Intent(Activity2.this, InfoActivity.class));
                                return true;
                            case R.id.logout_option:
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.inflate(R.menu.menu_activity_2);
                popup.show();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("uploads").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ImageButton img_button = findViewById(R.id.img_button);
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

    // todo make a proper function of this and remove the code from above image button
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent keyEvent) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            PopupMenu popup = new PopupMenu(Activity2.this, getCurrentFocus());
            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.info_option:
                            startActivity(new Intent(Activity2.this, InfoActivity.class));
                            return true;
                        case R.id.logout_option:
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.inflate(R.menu.menu_activity_2);
            popup.show();
            return true;
        }
        return super.onKeyLongPress(keyCode, keyEvent);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = Objects.requireNonNull(getApplicationContext()).getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(Activity2.this);
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

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("message", "");
                        hashMap.put("sender", "img");
                        hashMap.put("imageURL", mUri);

                        reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                        String key = reference.child("chat").push().getKey();
                        reference.child("chat").child(key).setValue(hashMap);
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

    private void sendMessage(String message) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("sender", "user");

        reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
        String key = reference.child("chat").push().getKey();
        reference.child("chat").child(key).setValue(hashMap);

        systemReply(message);
    }

    private void readMessage() {
        arrayList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid()).child("chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for(DataSnapshot snap : snapshot.getChildren()) {
                    MessageClass mes = snap.getValue(MessageClass.class);
                    arrayList.add(mes);
                }

                messageAdapter = new MessageAdapter(Activity2.this, arrayList);
                chat_recycler_view.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchText(String searchMessage, boolean up_or_down, int pos) {
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chat");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<Integer, ArrayList<String> > treeMap = new TreeMap<>(Collections.reverseOrder());;
                if(up_or_down) {
                    treeMap = new TreeMap<>(Collections.reverseOrder());
                } else {
                    treeMap = new TreeMap<>();
                }

                int index = 0;
                for(DataSnapshot snap : snapshot.getChildren()) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(snap.getKey());
                    arrayList.add(snap.child("message").getValue().toString());
                    treeMap.put(index++, arrayList);
                }

                boolean flag = true;
                for(Map.Entry<Integer, ArrayList<String>> o : treeMap.entrySet()) {
                    ArrayList<String> a = o.getValue();

                    if(up_or_down && Integer.parseInt(o.getKey().toString()) <= pos) {
                        if (a.get(1).toLowerCase().contains(searchMessage.toLowerCase())) {
                            flag = false;
                            chat_recycler_view.smoothScrollToPosition(Integer.parseInt(o.getKey().toString()));
                            break;
                        }
                    } else if(!up_or_down && Integer.parseInt(o.getKey().toString()) >= pos) {
                        if(a.get(1).toLowerCase().contains(searchMessage.toLowerCase())) {
                            flag = false;
                            chat_recycler_view.smoothScrollToPosition(Integer.parseInt(o.getKey().toString()));
                            break;
                        }
                    }
                }
                if(flag) Toast.makeText(Activity2.this, "foundn't", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void systemReply(String message) {
        if(message.contains("hello")) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("message", "こにちは");
            hashMap.put("sender", "sys");

            reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
            String key = reference.child("chat").push().getKey();
            reference.child("chat").child(key).setValue(hashMap);
        }

        if(message.contains("@")) {
            String[] words = message.split("\\s+");

            if(words.length == 2 && words[1].equals("random")) {
                reference = FirebaseDatabase.getInstance().getReference("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String word_or_kanji = random_number_generator(10)%3==0 ? "words" : "kanji";
                        if(snapshot.child(word_or_kanji).exists()) {
                            int count = (int) snapshot.child("words").getChildrenCount();
                            int rand = random_number_generator(count);
                            int x=0;
                            for(DataSnapshot snap : snapshot.child(word_or_kanji).getChildren()) {
                                if(x==rand) {
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("message", snap.child("word").getValue().toString());
                                    hashMap.put("sender", "sys");

                                    reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                                    String key = reference.child("chat").push().getKey();
                                    reference.child("chat").child(key).setValue(hashMap);

                                    break;
                                } else x++;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            // added the above part for random thing
            else if(words.length == 2) {
                reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String reply = "not found";
                        if(snapshot.child("words").child(words[1]).exists()) {
                            reply = snapshot.child("words").child(words[1]).child("meaning").getValue().toString();
                        } else if(snapshot.child("kanji").child(words[1]).exists()) {
                            reply = snapshot.child("kanji").child(words[1]).child("meaning").getValue().toString();
                        }

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("message", words[1] + "\n" + reply);
                        hashMap.put("sender", "sys");

                        reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                        String key = reference.child("chat").push().getKey();
                        reference.child("chat").child(key).setValue(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if(words.length==3) {
                if(words[2].equals("full")) {
                    reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean flag = false;
                            String imgURL = "";
                            String reply = "not found";
                            if(snapshot.child("words").child(words[1]).exists()) {
                                reply = "";
                                for(DataSnapshot snap : snapshot.child("words").child(words[1]).getChildren()) {
                                    if(snap.getKey().equals("image")) {
                                        imgURL = snap.getValue().toString();
                                        flag = true;
                                    } else {
                                        reply = reply.concat(snap.getKey() + ": " + snap.getValue() + "\n");
                                    }
                                }
                            } else if(snapshot.child("kanji").child(words[1]).exists()) {
                                reply = "";
                                for(DataSnapshot snap : snapshot.child("kanji").child(words[1]).getChildren()) {
                                    if(snap.getKey().equals("image")) {
                                        imgURL = snap.getValue().toString();
                                        flag = true;
                                    } else {
                                        reply = reply.concat(snap.getKey() + ": " + snap.getValue() + "\n");
                                    }
                                }
                            }

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("message", words[1] + "\n\n" + reply);
                            hashMap.put("sender", "sys");

                            reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                            String key = reference.child("chat").push().getKey();
                            reference.child("chat").child(key).setValue(hashMap);

                            if(flag) {
                                HashMap<String, String> hashMap2 = new HashMap<>();
                                hashMap2.put("message", "");
                                hashMap2.put("sender", "img");
                                hashMap2.put("imageURL", imgURL);

                                reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                                String key2 = reference.child("chat").push().getKey();
                                reference.child("chat").child(key2).setValue(hashMap2);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            else if(words.length == 3) {
                reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean flag = false;
                        String imgURL = "";
                        String reply = "not found";
                        if(snapshot.child("words").child(words[1]).exists()) {
                            if(words[2].equals("image")) {
                                imgURL = snapshot.child("words").child(words[1]).child("image").getValue().toString();
                                flag = true;
                            } else {
                                reply = snapshot.child("words").child(words[1]).child(words[2]).getValue().toString();
                            }
                        } else if(snapshot.child("kanji").child(words[1]).exists()) {
                            if(words[2].equals("image")) {
                                imgURL = snapshot.child("kanji").child(words[1]).child("image").getValue().toString();
                                flag = true;
                            } else {
                                reply = snapshot.child("kanji").child(words[1]).child(words[2]).getValue().toString();
                            }
                        }

                        if(flag) {
                            HashMap<String, String> hashMap2 = new HashMap<>();
                            hashMap2.put("message", "");
                            hashMap2.put("sender", "img");
                            hashMap2.put("imageURL", imgURL);

                            reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                            String key2 = reference.child("chat").push().getKey();
                            reference.child("chat").child(key2).setValue(hashMap2);
                        } else {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("message", words[1] + "\n" + reply);
                            hashMap.put("sender", "sys");

                            reference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseUser.getUid());
                            String key = reference.child("chat").push().getKey();
                            reference.child("chat").child(key).setValue(hashMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }

    private int random_number_generator(int r) {
        return new Random().nextInt(r);
    }
}