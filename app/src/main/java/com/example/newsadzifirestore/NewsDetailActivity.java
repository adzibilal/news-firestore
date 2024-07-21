package com.example.newsadzifirestore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewsDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;
    private ImageView newsImageView;

    private Button updateButton;
    private Button deleteButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        titleTextView = findViewById(R.id.titleTextView);
        contentTextView = findViewById(R.id.contentTextView);
        newsImageView = findViewById(R.id.newsImageView);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Ambil data yang dikirim dari NewsListActivity
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String documentId = getIntent().getStringExtra("documentId");

        // Set text pada TextView
        titleTextView.setText(title);
        contentTextView.setText(content);

        // Load image using Glide
        Glide.with(this).load(imageUrl).into(newsImageView);

        // Set click listener on update button
        updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewsDetailActivity.this, UpdateNewsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("documentId", documentId);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat AlertDialog untuk konfirmasi
                new AlertDialog.Builder(NewsDetailActivity.this)
                        .setTitle("Delete News") // Judul dialog
                        .setMessage("Are you sure you want to delete this news?") // Pesan konfirmasi
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Panggil metode deleteNews() jika pengguna mengonfirmasi
                                deleteNews();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null) // Tidak melakukan apa-apa jika pengguna memilih 'No'
                        .setIcon(android.R.drawable.ic_dialog_alert) // Ikon dialog
                        .show(); // Menampilkan dialog
            }
        });
    }

    private void deleteNews() {
        // Ambil documentId dari intent
        String documentId = getIntent().getStringExtra("documentId");

        if (documentId == null) {
            Toast.makeText(this, "Error: Document ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buat referensi ke koleksi berita
        DocumentReference newsRef = FirebaseFirestore.getInstance().collection("news").document(documentId);

        // Hapus dokumen
        newsRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewsDetailActivity.this, "News deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NewsDetailActivity.this, NewsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewsDetailActivity.this, "Error deleting news", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
