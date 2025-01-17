package com.example.messanger_apk;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextDOB, editTextEmail, editTextPhone, editTextHeight, editTextWeight;
    private Spinner spinnerGender;
    private Button buttonSave, buttonEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editTextName = findViewById(R.id.editTextName);
        editTextDOB = findViewById(R.id.editTextDOB);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        buttonSave = findViewById(R.id.buttonSave);
        buttonEdit = findViewById(R.id.buttonEdit);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        SharedPreferences sharedPreferences = getSharedPreferences("userProfile", MODE_PRIVATE);
        editTextName.setText(sharedPreferences.getString("username", ""));
        editTextDOB.setText(sharedPreferences.getString("dob", ""));
        editTextEmail.setText(sharedPreferences.getString("email", ""));
        editTextPhone.setText(sharedPreferences.getString("phone", ""));
        editTextHeight.setText(sharedPreferences.getString("height", ""));
        editTextWeight.setText(sharedPreferences.getString("weight", ""));
        int genderPosition = sharedPreferences.getInt("genderPosition", 0);
        spinnerGender.setSelection(genderPosition);
    }

    private void saveProfileData() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("userProfile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", editTextName.getText().toString());
            editor.putString("dob", editTextDOB.getText().toString());
            editor.putString("email", editTextEmail.getText().toString());
            editor.putString("phone", editTextPhone.getText().toString());
            editor.putString("height", editTextHeight.getText().toString());
            editor.putString("weight", editTextWeight.getText().toString());
            editor.putInt("genderPosition", spinnerGender.getSelectedItemPosition());
            editor.apply();

            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
            enableEditing(false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableEditing(boolean enable) {
        editTextName.setEnabled(enable);
        editTextDOB.setEnabled(enable);
        editTextEmail.setEnabled(enable);
        editTextPhone.setEnabled(enable);
        editTextHeight.setEnabled(enable);
        editTextWeight.setEnabled(enable);
        spinnerGender.setEnabled(enable);

        buttonSave.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> editTextDOB.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }
}
