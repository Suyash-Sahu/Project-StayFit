package com.example.messanger_apk;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import com.bumptech.glide.Glide;
import com.example.messanger_apk.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity {
    ImageView imglogout;
    private static final String TAG = "Homepage";
    private FirebaseAuth auth;
    private TextView userNameText;
    private ImageView profileImage;
    private TextView stepsText;
    private TextView distanceText;
    private TextView caloriesText;
    private Button startTrackingButton;
    private ValueEventListener fitnessListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_homepage);

        auth = FirebaseAuth.getInstance();
        
        // Initialize views
        userNameText = findViewById(R.id.userNameText);
        profileImage = findViewById(R.id.profileImage);
        CardView homeWorkoutCard = findViewById(R.id.homeWorkoutCard);
        CardView gymWorkoutCard = findViewById(R.id.gymWorkoutCard);
        CardView statsCard = findViewById(R.id.statsCard);
        CardView profileCard = findViewById(R.id.profileCard);
        stepsText = findViewById(R.id.stepsText);
        distanceText = findViewById(R.id.distanceText);
        caloriesText = findViewById(R.id.caloriesText);
        startTrackingButton = findViewById(R.id.startTrackingButton);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userProfile", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "User");
        userNameText.setText("Welcome, " + savedUsername);

        // Load user data
        loadUserData();

        // Setup fitness tracking
        setupFitnessTracking();

        // Set click listeners
        homeWorkoutCard.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Home_workout.class)));
        gymWorkoutCard.setOnClickListener(v -> startActivity(new Intent(Homepage.this, Gym_workout.class)));
        statsCard.setOnClickListener(v -> showFitnessStatsDialog());
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Homepage.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Add logout button
        imglogout=findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(Homepage.this,R.style.dialogue);
                dialog.setContentView(R.layout.dialogue_layout);
                Button yes,no;
                yes=dialog.findViewById(R.id.yesbtn);
                no=dialog.findViewById(R.id.nobtn);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent=new Intent(Homepage.this, login.class);
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
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            String username = user.getUsername();
                            if (username != null) {
                                userNameText.setText("Welcome, " + username);
                            } else {
                                userNameText.setText("Welcome, User");
                            }
                            if (user.getImageURL() != null && !user.getImageURL().equals("default")) {
                                Glide.with(Homepage.this)
                                    .load(user.getImageURL())
                                    .into(profileImage);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load user data");
                }
            });
        }
    }

    private void showFitnessStatsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.fitness_data_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        TextView stepsCount = dialog.findViewById(R.id.stepsCount);
        TextView caloriesCount = dialog.findViewById(R.id.caloriesCount);
        TextView distanceCount = dialog.findViewById(R.id.distanceCount);
        RadioGroup timeFrameSelector = dialog.findViewById(R.id.timeFrameSelector);
        RecyclerView workoutsRecyclerView = dialog.findViewById(R.id.recentWorkoutsRecyclerView);
        TextView totalWorkoutsText = dialog.findViewById(R.id.totalWorkoutsText);
        ImageView beginnerBadge = dialog.findViewById(R.id.beginnerBadge);
        ImageView intermediateBadge = dialog.findViewById(R.id.intermediateBadge);
        ImageView advancedBadge = dialog.findViewById(R.id.advancedBadge);
        ImageView streakBadge = dialog.findViewById(R.id.streakBadge);

        // Load initial stats
        updateFitnessDialogStats(stepsCount, caloriesCount, distanceCount, "today");
        loadWorkoutStats(totalWorkoutsText);
        loadAchievements(beginnerBadge, intermediateBadge, advancedBadge, streakBadge);
        setupRecentWorkouts(workoutsRecyclerView);

        // Handle time frame selection
        timeFrameSelector.setOnCheckedChangeListener((group, checkedId) -> {
            String timeFrame = "today";
            if (checkedId == R.id.weekButton) {
                timeFrame = "week";
            } else if (checkedId == R.id.monthButton) {
                timeFrame = "month";
            }
            updateFitnessDialogStats(stepsCount, caloriesCount, distanceCount, timeFrame);
        });

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void updateFitnessDialogStats(TextView steps, TextView calories, TextView distance, String timeFrame) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        DatabaseReference fitnessRef = FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(user.getUid())
            .child("fitness");

        fitnessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long stepsCount = snapshot.child("steps").getValue(Long.class) != null ? 
                        snapshot.child("steps").getValue(Long.class) : 0;
                    double distanceValue = snapshot.child("distance").getValue(Double.class) != null ? 
                        snapshot.child("distance").getValue(Double.class) : 0.0;
                    double caloriesValue = snapshot.child("calories").getValue(Double.class) != null ? 
                        snapshot.child("calories").getValue(Double.class) : 0.0;

                    // Apply multiplier based on timeframe
                    if (timeFrame.equals("week")) {
                        stepsCount *= 7;
                        distanceValue *= 7;
                        caloriesValue *= 7;
                    } else if (timeFrame.equals("month")) {
                        stepsCount *= 30;
                        distanceValue *= 30;
                        caloriesValue *= 30;
                    }

                    steps.setText(String.format("%d", stepsCount));
                    distance.setText(String.format("%.1f m", distanceValue));
                    calories.setText(String.format("%.1f cal", caloriesValue));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading fitness data for dialog");
            }
        });
    }

    private void setupFitnessTracking() {
        Log.d(TAG, "Setting up fitness tracking");
        startTrackingButton.setOnClickListener(v -> {
            Log.d(TAG, "Start tracking button clicked");
            toggleFitnessTracking();
        });

        // Setup Firebase listener for real-time updates
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DatabaseReference fitnessRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("fitness");

            fitnessListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d(TAG, "Fitness data updated");
                        // Update the card view
                        long steps = snapshot.child("steps").getValue(Long.class) != null ? 
                            snapshot.child("steps").getValue(Long.class) : 0;
                        double distance = snapshot.child("distance").getValue(Double.class) != null ? 
                            snapshot.child("distance").getValue(Double.class) : 0.0;
                        double calories = snapshot.child("calories").getValue(Double.class) != null ? 
                            snapshot.child("calories").getValue(Double.class) : 0.0;

                        stepsText.setText(String.format("%d", steps));
                        distanceText.setText(String.format("%.1f m", distance));
                        caloriesText.setText(String.format("%.1f", calories));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error updating fitness data: " + error.getMessage());
                }
            };

            fitnessRef.addValueEventListener(fitnessListener);
        }
        
        // Update button state initially
        updateTrackingButtonState();
    }

    private void loadWorkoutStats(TextView totalWorkoutsText) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference statsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("achievements");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalWorkouts = snapshot.child("totalWorkouts").exists() ? 
                    snapshot.child("totalWorkouts").getValue(Integer.class) : 0;

                totalWorkoutsText.setText("Total Workouts: " + totalWorkouts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load stats");
            }
        });
    }

    private void loadAchievements(ImageView beginnerBadge, ImageView intermediateBadge, 
                                ImageView advancedBadge, ImageView streakBadge) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference achievementsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("achievements");

        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalWorkouts = snapshot.child("totalWorkouts").exists() ? 
                    snapshot.child("totalWorkouts").getValue(Integer.class) : 0;
                boolean intermediateAchieved = snapshot.child("intermediateAchieved").exists() && 
                    snapshot.child("intermediateAchieved").getValue(Boolean.class);
                boolean advancedAchieved = snapshot.child("advancedAchieved").exists() && 
                    snapshot.child("advancedAchieved").getValue(Boolean.class);
                boolean streakAchieved = snapshot.child("monthlyStreakAchieved").exists() && 
                    snapshot.child("monthlyStreakAchieved").getValue(Boolean.class);

                beginnerBadge.setAlpha(totalWorkouts >= 10 ? 1.0f : 0.3f);
                intermediateBadge.setAlpha(intermediateAchieved ? 1.0f : 0.3f);
                advancedBadge.setAlpha(advancedAchieved ? 1.0f : 0.3f);
                streakBadge.setAlpha(streakAchieved ? 1.0f : 0.3f);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load achievements");
            }
        });
    }

    private void setupRecentWorkouts(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<User> workoutList = new ArrayList<>();
        userAdapter adapter = new userAdapter(this, workoutList);
        recyclerView.setAdapter(adapter);

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference workoutsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("workouts");

        workoutsRef.orderByChild("timestamp").limitToLast(10)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    workoutList.clear();
                    for (DataSnapshot workoutSnapshot : snapshot.getChildren()) {
                        User workout = workoutSnapshot.getValue(User.class);
                        if (workout != null) {
                            workoutList.add(0, workout);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load workouts");
                }
            });
    }

    private void toggleFitnessTracking() {
        Log.d(TAG, "toggleFitnessTracking called");
        if (ServiceUtils.isServiceRunning(this, FitnessTrackingService.class)) {
            Log.d(TAG, "Stopping fitness tracking service");
            // Stop tracking
            stopService(new Intent(this, FitnessTrackingService.class));
            Toast.makeText(this, "Fitness tracking stopped", Toast.LENGTH_SHORT).show();
            updateTrackingButtonState();
        } else {
            Log.d(TAG, "Starting fitness tracking service");
            // Check for required permissions
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting permissions");
                requestPermissions(new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.FOREGROUND_SERVICE
                }, 1001);
                return;
            }

            // Reset fitness data and start tracking
            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

            Log.d(TAG, "Resetting fitness stats");
            // Reset fitness stats
            DatabaseReference fitnessRef = userRef.child("fitness");
            fitnessRef.child("steps").setValue(0);
            fitnessRef.child("distance").setValue(0.0);
            fitnessRef.child("calories").setValue(0.0);

            // Update last tracking start time
            userRef.child("lastTrackingStart").setValue(System.currentTimeMillis());

            Log.d(TAG, "Starting tracking service");
            // Start tracking service
            Intent serviceIntent = new Intent(this, FitnessTrackingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Fitness tracking started", Toast.LENGTH_SHORT).show();
            updateTrackingButtonState();
        }
    }

    private void updateTrackingButtonState() {
        boolean isTracking = ServiceUtils.isServiceRunning(this, FitnessTrackingService.class);
        Log.d(TAG, "Updating tracking button state: isTracking = " + isTracking);
        runOnUiThread(() -> {
            startTrackingButton.setText(isTracking ? "Stop Tracking" : "Start Tracking");
            startTrackingButton.setEnabled(true);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Log.d(TAG, "All permissions granted, starting tracking");
                toggleFitnessTracking(); // Try starting the service again after permissions are granted
            } else {
                Log.d(TAG, "Permissions denied");
                Toast.makeText(this, "Permissions required for fitness tracking", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            auth.signOut();
            startActivity(new Intent(Homepage.this, login.class));
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void updateStreak() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId);

        userRef.child("lastActive").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastActive = snapshot.getValue(Long.class);
                long currentTime = System.currentTimeMillis();
                
                // If first time or no last active timestamp
                if (lastActive == null) {
                    userRef.child("lastActive").setValue(currentTime);
                    userRef.child("streak").setValue(1);
                    return;
                }

                // Calculate days between last active and now
                long daysBetween = (currentTime - lastActive) / (1000 * 60 * 60 * 24);

                userRef.child("streak").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer currentStreak = snapshot.getValue(Integer.class);
                        if (currentStreak == null) currentStreak = 0;

                        // If active today, do nothing
                        if (daysBetween < 1) {
                            return;
                        }
                        // If active yesterday, increment streak
                        else if (daysBetween == 1) {
                            currentStreak++;
                            if (currentStreak >= 7) {
                                userRef.child("achievements/weeklyStreakAchieved").setValue(true);
                            }
                            if (currentStreak >= 30) {
                                userRef.child("achievements/monthlyStreakAchieved").setValue(true);
                            }
                        }
                        // If missed a day, reset streak
                        else {
                            currentStreak = 1;
                        }

                        // Update streak and last active time
                        userRef.child("streak").setValue(currentStreak);
                        userRef.child("lastActive").setValue(currentTime);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error updating streak: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking last active time: " + error.getMessage());
            }
        });
    }

    private void checkAndUpdateDailyReset() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastTrackingStart = snapshot.child("lastTrackingStart").getValue(Long.class);
                long currentTime = System.currentTimeMillis();

                // If tracking was started and 24 hours have passed
                if (lastTrackingStart != null && (currentTime - lastTrackingStart >= 86400000)) {
                    // Reset daily stats
                    DatabaseReference fitnessRef = userRef.child("fitness");
                    fitnessRef.child("steps").setValue(0);
                    fitnessRef.child("distance").setValue(0.0);
                    fitnessRef.child("calories").setValue(0.0);
                    
                    // Update last tracking start time
                    userRef.child("lastTrackingStart").setValue(currentTime);
                    
                    // Update streak
                    updateStreak();
                    Log.d(TAG, "Daily stats reset completed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking daily reset: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndUpdateDailyReset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener
        if (fitnessListener != null && auth.getCurrentUser() != null) {
            DatabaseReference fitnessRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("fitness");
            fitnessRef.removeEventListener(fitnessListener);
        }
    }

    public static class ServiceUtils {
        public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
    }
}