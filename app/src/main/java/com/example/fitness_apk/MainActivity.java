package com.example.messanger_apk;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.animation.ValueAnimator;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private FitnessOptions fitnessOptions;

    FirebaseAuth auth;
    RecyclerView MainUserRecyclerview;
    userAdapter adapter;
    FirebaseDatabase database;
    ArrayList<User> userArrayList;
    ImageView imglogout,instructionimg;
    Button startButton, showFitnessDataButton;
    DatabaseReference reference;

    private Animation slideUpAnimation;
    private Animation buttonScaleAnimation;

    private String currentTimePeriod = "Daily";
    private Dialog fitnessDataDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make status bar transparent
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate: Initializing components");

        database=FirebaseDatabase.getInstance();

        reference=database.getReference().child("user");

        userArrayList=new ArrayList<>();
        
        auth  =FirebaseAuth.getInstance();
        MainUserRecyclerview=findViewById(R.id.MainUserRecyclerView);
        MainUserRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter=new userAdapter(MainActivity.this,userArrayList);
        MainUserRecyclerview.setAdapter(adapter);

        Log.d("MainActivity", "onCreate: Initializing components");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("MainActivity", "Data changed: " + snapshot.getChildrenCount() + " users");
                userArrayList.clear(); // Clear previous data
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user =dataSnapshot.getValue(User.class);
                    userArrayList.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Database error: " + error.getMessage());
            }
        });

        imglogout=findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(MainActivity.this,R.style.dialogue);
                dialog.setContentView(R.layout.dialogue_layout);
                Button yes,no;
                yes=dialog.findViewById(R.id.yesbtn);
                no=dialog.findViewById(R.id.nobtn);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent=new Intent(MainActivity.this, login.class);
                        startActivity(intent);
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        instructionimg=findViewById(R.id.instimg);
        instructionimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,instruct.class);
                startActivity(intent);
            }
        });

        if(auth.getCurrentUser()==null){
            Intent intent  =new Intent(MainActivity.this,login.class);
            startActivity(intent);
        }

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonScaleAnimation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, Homepage.class);
                        startActivity(intent);
                    }
                }, 100);
            }
        });

        // Initialize animations
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        buttonScaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_scale);

        // Configure Google Fit options with additional data types
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        // Check for existing sign-in
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            // Not signed in, request sign-in
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions);
        } else {
            // Already signed in, access fitness data
            requestFitnessPermissions();
        }
    }

    private void fetchFitnessData(int days) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (days * 24 * 60 * 60 * 1000); // Convert days to milliseconds

            DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

            Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    processStepData(dataReadResponse);
                    processCalorieData(dataReadResponse);
                    processDistanceData(dataReadResponse);
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Failed to read data", e));
        }
    }

    private void processStepData(DataReadResponse dataReadResponse) {
        int totalSteps = 0;
        for (DataSet dataSet : dataReadResponse.getDataSets()) {
            if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    totalSteps += dp.getValue(Field.FIELD_STEPS).asInt();
                }
            }
        }
        updateStepUI(totalSteps);
    }

    private void processCalorieData(DataReadResponse dataReadResponse) {
        float totalCalories = 0;
        for (DataSet dataSet : dataReadResponse.getDataSets()) {
            if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    totalCalories += dp.getValue(Field.FIELD_CALORIES).asFloat();
                }
            }
        }
        updateCalorieUI(totalCalories);
    }

    private void processDistanceData(DataReadResponse dataReadResponse) {
        float totalDistance = 0;
        for (DataSet dataSet : dataReadResponse.getDataSets()) {
            if (dataSet.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    totalDistance += dp.getValue(Field.FIELD_DISTANCE).asFloat();
                }
            }
        }
        updateDistanceUI(totalDistance);
    }

    private void updateStepUI(final int steps) {
        runOnUiThread(() -> {
            ValueAnimator animator = ValueAnimator.ofInt(0, steps);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                // Removed stepCountText
            });
            animator.start();
        });
    }

    private void updateCalorieUI(final float calories) {
        runOnUiThread(() -> {
            ValueAnimator animator = ValueAnimator.ofFloat(0, calories);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                // Removed caloriesText
            });
            animator.start();
        });
    }

    private void updateDistanceUI(final float distance) {
        runOnUiThread(() -> {
            ValueAnimator animator = ValueAnimator.ofFloat(0, distance / 1000); // Convert to kilometers
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                // Removed distanceText
            });
            animator.start();
        });
    }

    private void showFitnessDataDialog() {
        fitnessDataDialog = new Dialog(this);
        fitnessDataDialog.setContentView(R.layout.fitness_data_dialog);
        fitnessDataDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        setupStatsViews();
        setupRecentWorkouts();
        setupAchievements();
        setupTimeFrameSelector();

        fitnessDataDialog.show();
    }

    private void setupTimeFrameSelector() {
        RadioGroup timeFrameSelector = fitnessDataDialog.findViewById(R.id.timeFrameSelector);
        timeFrameSelector.setOnCheckedChangeListener((group, checkedId) -> {
            String timeFrame = "today";
            if (checkedId == R.id.weekButton) {
                timeFrame = "week";
            } else if (checkedId == R.id.monthButton) {
                timeFrame = "month";
            }
            updateStatsForTimeFrame(timeFrame);
        });
    }

    private void setupStatsViews() {
        TextView stepsCount = fitnessDataDialog.findViewById(R.id.stepsCount);
        TextView caloriesCount = fitnessDataDialog.findViewById(R.id.caloriesCount);
        TextView distanceCount = fitnessDataDialog.findViewById(R.id.distanceCount);

        // Initially update with today's stats
        updateStatsForTimeFrame("today");
    }

    private void updateStatsForTimeFrame(String timeFrame) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference statsRef = database.getReference()
            .child("users")
            .child(userId)
            .child("stats")
            .child(timeFrame);

        statsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int steps = snapshot.child("steps").exists() ? 
                    snapshot.child("steps").getValue(Integer.class) : 0;
                int calories = snapshot.child("calories").exists() ? 
                    snapshot.child("calories").getValue(Integer.class) : 0;
                double distance = snapshot.child("distance").exists() ? 
                    snapshot.child("distance").getValue(Double.class) : 0.0;

                // Update UI
                TextView stepsCount = fitnessDataDialog.findViewById(R.id.stepsCount);
                TextView caloriesCount = fitnessDataDialog.findViewById(R.id.caloriesCount);
                TextView distanceCount = fitnessDataDialog.findViewById(R.id.distanceCount);

                stepsCount.setText(String.valueOf(steps));
                caloriesCount.setText(String.valueOf(calories));
                distanceCount.setText(String.format("%.1f", distance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Failed to load stats", error.toException());
            }
        });
    }

    private void setupRecentWorkouts() {
        RecyclerView workoutsRecyclerView = fitnessDataDialog.findViewById(R.id.recentWorkoutsRecyclerView);
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize workout list and adapter
        ArrayList<User> workoutList = new ArrayList<>();
        userAdapter adapter = new userAdapter(MainActivity.this, workoutList);
        workoutsRecyclerView.setAdapter(adapter);

        // Load workouts from Firebase
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference workoutsRef = database.getReference()
            .child("users")
            .child(userId)
            .child("workouts");

        workoutsRef.orderByChild("timestamp").limitToLast(10)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    workoutList.clear();
                    for (DataSnapshot workoutSnapshot : snapshot.getChildren()) {
                        User workout = workoutSnapshot.getValue(User.class);
                        if (workout != null) {
                            workoutList.add(0, workout); // Add to beginning for reverse chronological order
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", "Failed to load workouts", error.toException());
                }
            });
    }

    private void setupAchievements() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference achievementsRef = database.getReference()
            .child("users")
            .child(userId)
            .child("achievements");

        achievementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get achievement data
                int totalWorkouts = snapshot.child("totalWorkouts").exists() ? 
                    snapshot.child("totalWorkouts").getValue(Integer.class) : 0;
                int streak = snapshot.child("streak").exists() ? 
                    snapshot.child("streak").getValue(Integer.class) : 0;
                boolean intermediateAchieved = snapshot.child("intermediateAchieved").exists() && 
                    snapshot.child("intermediateAchieved").getValue(Boolean.class);
                boolean advancedAchieved = snapshot.child("advancedAchieved").exists() && 
                    snapshot.child("advancedAchieved").getValue(Boolean.class);
                boolean monthlyStreakAchieved = snapshot.child("monthlyStreakAchieved").exists() && 
                    snapshot.child("monthlyStreakAchieved").getValue(Boolean.class);

                // Update badges
                ImageView beginnerBadge = fitnessDataDialog.findViewById(R.id.beginnerBadge);
                ImageView intermediateBadge = fitnessDataDialog.findViewById(R.id.intermediateBadge);
                ImageView advancedBadge = fitnessDataDialog.findViewById(R.id.advancedBadge);
                ImageView streakBadge = fitnessDataDialog.findViewById(R.id.streakBadge);

                // Set badge visibility based on achievements
                beginnerBadge.setAlpha(totalWorkouts >= 10 ? 1.0f : 0.3f);
                intermediateBadge.setAlpha(intermediateAchieved ? 1.0f : 0.3f);
                advancedBadge.setAlpha(advancedAchieved ? 1.0f : 0.3f);
                streakBadge.setAlpha(monthlyStreakAchieved ? 1.0f : 0.3f);

                // Update streak text

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Failed to load achievements", error.toException());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove database listener to prevent memory leaks
        reference.removeEventListener((ValueEventListener) this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                requestFitnessPermissions();
            } else {
                // Handle permission denial
            }
        }
    }

    private void requestFitnessPermissions() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            );
        } else {
            // Permissions already granted, access fitness data
            accessFitnessData();
        }
    }

    private void accessFitnessData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Use Fitness API to read step count and other fitness data
            long startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
            long endTime = System.currentTimeMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

            Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    // Process fitness data here
                    int totalSteps = 0;
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                            totalSteps += steps;
                        }
                    }
                    // Update UI with step count
                    updateStepCountUI(totalSteps);
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Failed to read data", e));
        }
    }

    private void updateStepCountUI(final int totalSteps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Animate the step count text
                ValueAnimator animator = ValueAnimator.ofInt(0, totalSteps);
                animator.setDuration(1000);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int animatedValue = (int) animation.getAnimatedValue();
                        // Removed stepCountText
                    }
                });
                animator.start();
            }
        });
    }
}