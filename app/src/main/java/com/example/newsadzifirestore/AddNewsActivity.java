package com.example.newsadzifirestore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddNewsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText titleEditText;
    private EditText descEditText;
    private Button chooseImageButton;
    private Button saveButton;
    private ImageView imagePreview;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descEditText);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        saveButton = findViewById(R.id.saveButton);
        imagePreview = findViewById(R.id.imagePreview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFileAndSaveNews();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(imagePreview);
        }
    }

    private void uploadFileAndSaveNews() {
        final String title = titleEditText.getText().toString().trim();
        final String desc = descEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc) || imageUri == null) {
            Toast.makeText(AddNewsActivity.this, "Please fill all fields and choose an image", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = storage.getReference();
        final StorageReference fileReference = storageRef.child("news_images/" + System.currentTimeMillis() + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                saveNews(title, desc, imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNewsActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNews(String title, String desc, String imageUrl) {
        CollectionReference newsRef = db.collection("news");

        Map<String, String> news = new HashMap<>();
        news.put("title", title);
        news.put("desc", desc);
        news.put("imageUrl", imageUrl);

        newsRef.add(news).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddNewsActivity.this, "News added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddNewsActivity.this, "Error adding news", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
