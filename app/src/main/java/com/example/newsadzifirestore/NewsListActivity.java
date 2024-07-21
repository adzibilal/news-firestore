package com.example.newsadzifirestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewsListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList = new ArrayList<>();
    private TextView headerTextView;
    private Button logoutButton;
    private FloatingActionButton fab;
    private static final int ADD_NEWS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        headerTextView = findViewById(R.id.headerTextView);
        logoutButton = findViewById(R.id.logoutButton);
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        fab = findViewById(R.id.fab); // Initialize FAB

        // Set header with user's email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            headerTextView.setText("Hai, " + userEmail);
        }

        // RecyclerView setup
        newsAdapter = new NewsAdapter(newsList, this);
        newsRecyclerView.setAdapter(newsAdapter);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Fetch news data
        CollectionReference newsRef = db.collection("news");
        fetchNewsData(newsRef);

        // Logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(NewsListActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(NewsListActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Floating action button click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NewsListActivity.this, "add news", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NewsListActivity.this, AddNewsActivity.class);
                startActivityForResult(intent, ADD_NEWS_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEWS_REQUEST && resultCode == RESULT_OK) {
            fetchNewsData(db.collection("news"));
        }
    }

    private void fetchNewsData(CollectionReference newsRef) {
        newsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    newsList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        String title = document.getString("title");
                        String desc = document.getString("desc");
                        String imageUrl = document.getString("imageUrl");
                        newsList.add(new NewsItem(documentId ,title, desc, imageUrl));
                    }
                    newsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(NewsListActivity.this, "Error fetching news", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}