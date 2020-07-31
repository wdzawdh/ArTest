package com.cw.artest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cw.artest.augmentedimage.AugmentedImageActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View startModel = findViewById(R.id.startModel);
        startModel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("mode", true);
            startActivity(intent);
        });
        View startImage = findViewById(R.id.startImage);
        startImage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("mode", false);
            startActivity(intent);
        });
    }
}
