package com.example.messanger_apk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    TextView logsignup;
    Button button;
    EditText email,password;
    FirebaseAuth auth;
    String emailpattern="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    android.app.ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        logsignup=findViewById(R.id.logsignup);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait....");
        progressDialog.setCancelable(false);

        logsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(login.this, registration.class);
                startActivity(intent);
                finish();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth=FirebaseAuth.getInstance();
        button=findViewById(R.id.logbutton);
        email=findViewById(R.id.editTextlogEmailAddress);
        password=findViewById(R.id.editTextlogPassword);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email=email.getText().toString();
                String Password=password.getText().toString();

                if(TextUtils.isEmpty(Email)) {
                    progressDialog.dismiss();
                    Toast.makeText(login.this, "Enter the email", Toast.LENGTH_SHORT).show();
                } else if(TextUtils.isEmpty(Password)){
                    progressDialog.dismiss();
                    Toast.makeText(login.this, "Enter the password", Toast.LENGTH_SHORT).show();
                } else if (!Email.matches(emailpattern)) {
                    progressDialog.dismiss();
                    email.setError("Give proper email");
                }else if(Password.length()<6){
                    progressDialog.dismiss();
                    password.setError("More than 6 characters");
                }
                else {
                    auth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                progressDialog.show();
                                try {
                                    Intent intent=new Intent(login.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    Toast.makeText(login.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(login.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}