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
            intent.putExtra("showType", 0);
            startActivity(intent);
        });
        View startImage = findViewById(R.id.startImage);
        startImage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("showType", 1);
            startActivity(intent);
        });
        View startVideo = findViewById(R.id.startVideo);
        startVideo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("showType", 2);
            startActivity(intent);
        });
        View startVideo2 = findViewById(R.id.startVideo2);
        startVideo2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("showType", 3);
            startActivity(intent);
        });
        View startVideo3 = findViewById(R.id.startVideo3);
        startVideo3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AugmentedImageActivity.class);
            intent.putExtra("showType", 4);
            startActivity(intent);
        });
    }
}
