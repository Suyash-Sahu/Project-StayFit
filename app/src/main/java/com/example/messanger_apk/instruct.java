package com.example.messanger_apk;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class instruct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruct);

        // Initialize TextViews
        TextView introText = findViewById(R.id.introText);
        TextView disclaimerText = findViewById(R.id.disclaimerText);
        TextView creditsText = findViewById(R.id.creditsText);
        TextView footerText = findViewById(R.id.footerText);

        // Set introduction text
        String intro = "Hey there! I'm Suyash Sahu, and I'm excited to present my first Android development project. " +
                "This fitness app represents my initial venture into Android development, and I'm thrilled to share it with you.";
        introText.setText(intro);

        // Set disclaimer text
        String disclaimer = "Please Note:\n\n" +
                "The exercise images shown in this app are for reference purposes only. " +
                "These images are meant to give you a general idea of the exercises. " +
                "For your safety and best results, please perform all exercises under the guidance of a qualified fitness mentor. " +
                "This is a demo app designed to showcase functionality.";
        disclaimerText.setText(disclaimer);

        // Set credits text
        String credits = "Image Resources:\n\n" +
                "• Icons and UI elements from FontAwesome\n" +
                "  https://fontawesome.com/\n\n" +
                "• Exercise illustrations from Freepik\n" +
                "  https://www.freepik.com/";
        creditsText.setText(credits);

        // Set footer text
        String footer = "Thank you for using my app!\n" +
                "Stay fit and healthy! 💪";
        footerText.setText(footer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}