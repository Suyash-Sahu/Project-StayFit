package com.example.messanger_apk;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    private TextView loginbut;
    private EditText rg_username, rg_email, rg_password, rg_repassword;
    private Button rg_signup;
    private CircleImageView rg_profile;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_registration);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        loginbut = findViewById(R.id.loginbutsg);
        rg_username = findViewById(R.id.sgusername);
        rg_email = findViewById(R.id.sgemail);
        rg_password = findViewById(R.id.sgpassword);
        rg_repassword = findViewById(R.id.sgrepassword);
        rg_signup = findViewById(R.id.buttonsg);
        rg_profile = findViewById(R.id.userimg);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing your account");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        loginbut.setOnClickListener(v -> {
            startActivity(new Intent(registration.this, login.class));
            finish();
        });

        rg_signup.setOnClickListener(v -> registerUser());

        rg_profile.setOnClickListener(v ->
                Toast.makeText(this, "Profile picture upload is disabled", Toast.LENGTH_SHORT).show());
    }

    private void registerUser() {
        String name = rg_username.getText().toString().trim();
        String email = rg_email.getText().toString().trim();
        String password = rg_password.getText().toString().trim();
        String repassword = rg_repassword.getText().toString().trim();

        if (validateInput(name, email, password, repassword)) {
            performFirebaseRegistration(name, email, password);
        }
    }

    private boolean validateInput(String name, String email, String password, String repassword) {
        if (TextUtils.isEmpty(name)) {
            rg_username.setError("Enter username");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            rg_email.setError("Enter email");
            return false;
        }
        if (!email.matches(EMAIL_PATTERN)) {
            rg_email.setError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            rg_password.setError("Enter password");
            return false;
        }
        if (password.length() < 6) {
            rg_password.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(repassword)) {
            rg_repassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void performFirebaseRegistration(String name, String email, String password) {
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        User user = new User(
                                userId,
                                name,
                                "", // imageURL
                                "Hey, I'm using this app", // status
                                "", // search
                                0, // caloriesBurned
                                0, // totalWorkouts
                                0  // streak
                        );

                        reference.setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    progressDialog.dismiss();
                                    if (dbTask.isSuccessful()) {
                                        startActivity(new Intent(registration.this, MainActivity.class));
                                        finish();
                                    } else {
                                        android.util.Log.e("FirebaseError", "Failed to save user: " + dbTask.getException().getMessage());
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(registration.this,
                                "Registration error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}