package com.example.messanger_apk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
    ImageView logoImage;
    TextView appNameText, sloganText, creatorLabel, creatorName;
    Animation fadeInAnimation, slideUpAnimation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Initialize views
        logoImage = findViewById(R.id.logoImage);
        appNameText = findViewById(R.id.appNameText);
        sloganText = findViewById(R.id.sloganText);
        creatorLabel = findViewById(R.id.creatorLabel);
        creatorName = findViewById(R.id.creatorName);

        // Load animations
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Apply animations
        logoImage.startAnimation(fadeInAnimation);
        appNameText.startAnimation(slideUpAnimation);
        sloganText.startAnimation(slideUpAnimation);
        
        // Delayed animations for creator info
        new Handler().postDelayed(() -> {
            creatorLabel.startAnimation(fadeInAnimation);
            creatorName.startAnimation(fadeInAnimation);
        }, 1000);

        // Navigate to registration after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splash.this, registration.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}