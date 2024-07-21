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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UpdateNewsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText updateTitleEditText;
    private EditText updateContentEditText;
    private EditText updateImageUrlEditText;
    private Button updateChooseImageButton;
    private Button updateSaveButton;
    private ImageView updateImagePreview;
    private Uri imageUri;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String documentId;
    private String currentImageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_news);

        updateTitleEditText = findViewById(R.id.updateTitleEditText);
        updateContentEditText = findViewById(R.id.updateContentEditText);
        updateImageUrlEditText = findViewById(R.id.updateImageUrlEditText);
        updateChooseImageButton = findViewById(R.id.updateChooseImageButton);
        updateSaveButton = findViewById(R.id.updateSaveButton);
        updateImagePreview = findViewById(R.id.updateImagePreview);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Ambil data yang dikirim dari NewsDetailActivity
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        currentImageUrl = getIntent().getStringExtra("imageUrl");
        documentId = getIntent().getStringExtra("documentId");

        // Set data ke EditText dan ImageView
        updateTitleEditText.setText(title);
        updateContentEditText.setText(content);
        Glide.with(this).load(currentImageUrl).into(updateImagePreview);

        updateChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        updateSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadFileAndSaveNews();
                } else {
                    saveNews(updateTitleEditText.getText().toString().trim(),
                            updateContentEditText.getText().toString().trim(),
                            currentImageUrl);
                }
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
            updateImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).into(updateImagePreview);
        }
    }

    private void uploadFileAndSaveNews() {
        final String title = updateTitleEditText.getText().toString().trim();
        final String desc = updateContentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)) {
            Toast.makeText(UpdateNewsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UpdateNewsActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveNews(String title, String desc, String imageUrl) {
        Map<String, Object> news = new HashMap<>();
        news.put("title", title);
        news.put("desc", desc);
        news.put("imageUrl", imageUrl);

        db.collection("news").document(documentId)
                .update(news)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateNewsActivity.this, "News updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateNewsActivity.this, NewsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateNewsActivity.this, "Error updating news", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
