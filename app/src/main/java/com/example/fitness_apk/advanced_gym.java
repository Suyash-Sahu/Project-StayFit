package com.example.messanger_apk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class advanced_gym extends AppCompatActivity {
    private ViewPager2 viewPager;
    private WorkoutPagerAdapter pagerAdapter;

    static class WorkoutPage {
        String title;
        String sets;
        String instructions;
        String imageUrl;

        WorkoutPage(String title, String sets, String instructions, String imageUrl) {
            this.title = title;
            this.sets = sets;
            this.instructions = instructions;
            this.imageUrl = imageUrl;
        }
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView setsText;
        TextView instructionsText;
        ImageView imageView;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.workoutTitle);
            setsText = itemView.findViewById(R.id.workoutSets);
            instructionsText = itemView.findViewById(R.id.workoutInstructions);
            imageView = itemView.findViewById(R.id.workoutImage);
        }
    }

    private static class WorkoutPagerAdapter extends RecyclerView.Adapter<WorkoutViewHolder> {
        private List<WorkoutPage> pages;
        private AppCompatActivity context;

        WorkoutPagerAdapter(List<WorkoutPage> pages, AppCompatActivity context) {
            this.pages = pages;
            this.context = context;
        }

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.workout_page_item, parent, false);
            return new WorkoutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            WorkoutPage page = pages.get(position);
            holder.titleText.setText(page.title);
            holder.setsText.setText(page.sets);
            holder.instructionsText.setText(page.instructions);
            
            // Load image using Glide
            Glide.with(context)
                .load(page.imageUrl)
                .centerCrop()
                .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return pages.size();
        }
    }

    private void setupViewPager() {
        List<WorkoutPage> pages = new ArrayList<>();
        String baseUrl = "https://raw.githubusercontent.com/Suyash-Sahu/images/main/";
        
        pages.add(new WorkoutPage(
            "Weighted Pull-ups",
            "4 sets of 8-10 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Attach weight belt or hold dumbbell between feet\n" +
            "2. Pull up until chin over bar\n" +
            "3. Lower with control\n" +
            "4. Maintain strict form",
            baseUrl + "weighted_pullups.jpg"));

        pages.add(new WorkoutPage(
            "Barbell Bench Press",
            "5 sets of 5 reps\nRest 2 minutes between sets",
            "Instructions:\n\n" +
            "1. Lie on bench with feet flat\n" +
            "2. Grip bar slightly wider than shoulders\n" +
            "3. Lower bar to chest\n" +
            "4. Press up with control",
            baseUrl + "bench_press.jpg"));

        pages.add(new WorkoutPage(
            "Heavy Deadlifts",
            "5 sets of 5 reps\nRest 2-3 minutes between sets",
            "Instructions:\n\n" +
            "1. Stand with feet hip-width apart\n" +
            "2. Bend at hips and knees to grip bar\n" +
            "3. Keep back straight, lift by extending hips and knees\n" +
            "4. Return weight to ground with control",
            baseUrl + "deadlift.jpg"));

        pages.add(new WorkoutPage(
            "Barbell Squats",
            "5 sets of 5 reps\nRest 2-3 minutes between sets",
            "Instructions:\n\n" +
            "1. Position bar on upper back\n" +
            "2. Feet shoulder-width apart\n" +
            "3. Break at hips and knees simultaneously\n" +
            "4. Keep chest up, squat to parallel\n" +
            "5. Drive through heels to stand",
            baseUrl + "barbell_squat.jpg"));

        pages.add(new WorkoutPage(
            "Weighted Dips",
            "4 sets of 8-10 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Attach weight belt or hold dumbbell between legs\n" +
            "2. Lower body until upper arms parallel\n" +
            "3. Push back up explosively\n" +
            "4. Keep slight forward lean throughout",
            baseUrl + "weighted_dips.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_gym);
        
        viewPager = findViewById(R.id.viewPager);
        setupViewPager();
        
        // Add Complete Workout button
        Button completeButton = findViewById(R.id.completeWorkoutButton);
        completeButton.setOnClickListener(v -> saveWorkoutToFirebase());
    }

    private void saveWorkoutToFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference workoutRef = FirebaseDatabase.getInstance()
            .getReference()
            .child("users")
            .child(userId)
            .child("workouts")
            .push();

        User workout = new User(
            "Gym",
            "Advanced",
            getCurrentTimestamp(),
            "90 minutes", // Advanced gym workouts are longer
            calculateCalories(),
            true
        );

        workoutRef.setValue(workout)
            .addOnSuccessListener(aVoid -> {
                updateAchievements();
                showCompletionDialog();
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to save workout: " + e.getMessage(), 
                    android.widget.Toast.LENGTH_SHORT).show();
            });
    }

    private void showCompletionDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Workout Completed!")
            .setMessage("Outstanding achievement! Your advanced gym workout has been recorded.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .show();
    }

    private void updateAchievements() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference achievementsRef = FirebaseDatabase.getInstance()
            .getReference()
            .child("users")
            .child(userId)
            .child("achievements");

        // Update total workouts and check for advanced badge
        achievementsRef.child("totalWorkouts")
            .get()
            .addOnSuccessListener(snapshot -> {
                int total = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                total++;
                achievementsRef.child("totalWorkouts").setValue(total);
                
                // Check for advanced badge
                if (total >= 50) {
                    achievementsRef.child("advancedAchieved").setValue(true);
                }
            });

        // Update streak
        updateStreak(achievementsRef);
    }

    private void updateStreak(DatabaseReference achievementsRef) {
        achievementsRef.child("lastWorkoutDate").get().addOnSuccessListener(snapshot -> {
            String lastDate = snapshot.exists() ? snapshot.getValue(String.class) : null;
            String today = getCurrentDate();
            
            if (isConsecutiveDay(lastDate, today)) {
                achievementsRef.child("streak").get().addOnSuccessListener(streakSnapshot -> {
                    int streak = streakSnapshot.exists() ? streakSnapshot.getValue(Integer.class) : 0;
                    streak++;
                    achievementsRef.child("streak").setValue(streak);
                    
                    // Check for streak achievement
                    if (streak >= 30) {
                        achievementsRef.child("monthlyStreakAchieved").setValue(true);
                    }
                });
            } else {
                achievementsRef.child("streak").setValue(1);
            }
            achievementsRef.child("lastWorkoutDate").setValue(today);
        });
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isConsecutiveDay(String lastDate, String today) {
        if (lastDate == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date last = sdf.parse(lastDate);
            Date current = sdf.parse(today);
            long diffInDays = (current.getTime() - last.getTime()) / (24 * 60 * 60 * 1000);
            return diffInDays == 1;
        } catch (ParseException e) {
            return false;
        }
    }

    private int calculateCalories() {
        // Advanced gym workouts burn more calories
        return 600; // Default value for advanced gym workout
    }
}