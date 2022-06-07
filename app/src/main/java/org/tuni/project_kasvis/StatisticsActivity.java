package org.tuni.project_kasvis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    public static String TAG = "ZZ Statistics..";
    TextView textViewValue;
    RecyclerView recyclerView;

    ImageAdapter adapter;
    ImageViewModel viewModel;
    Button getButton;

    Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        textViewValue = findViewById(R.id.textViewValue);

        getButton = findViewById(R.id.button);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ImageAdapter(new ImageAdapter.ItemDiff());

        getTotalValue();
//        getButton.setOnClickListener(view -> getTotalValue());

        viewModel = new ViewModelProvider(this).get(ImageViewModel.class);
        viewModel.getAllImages().observe(this, adapter::submitList);
        recyclerView.setAdapter(adapter);
    }

    public void getTotalValue() {
        executor.execute(() -> runOnUiThread(() -> viewModel.getCounts().observe(this, integer -> {
            Log.d(TAG, "get value: " + integer);
            textViewValue.setText(String.valueOf(integer));
        })));
    }
}