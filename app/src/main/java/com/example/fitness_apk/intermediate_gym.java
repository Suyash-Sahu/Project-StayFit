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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class intermediate_gym extends AppCompatActivity {
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
            "4 sets of 6-8 reps\nRest 120 seconds between sets",
            "Instructions:\n\n" +
            "1. Attach weight belt\n" +
            "2. Grip bar wider than shoulders\n" +
            "3. Pull up with controlled movement\n" +
            "4. Lower slowly",
            baseUrl + "weighted_pullups.jpg"));

        pages.add(new WorkoutPage(
            "Barbell Bench Press",
            "4 sets of 8-10 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Lie on bench, feet flat\n" +
            "2. Grip bar slightly wider than shoulders\n" +
            "3. Lower bar to chest\n" +
            "4. Press up with control",
            baseUrl + "barbell_bench_press.jpg"));

        pages.add(new WorkoutPage(
            "Barbell Squats",
            "4 sets of 8-10 reps\nRest 120 seconds between sets",
            "Instructions:\n\n" +
            "1. Bar on upper back\n" +
            "2. Feet shoulder-width\n" +
            "3. Keep chest up\n" +
            "4. Squat to parallel\n" +
            "5. Drive through heels",
            baseUrl + "barbell_squats.jpg"));

        pages.add(new WorkoutPage(
            "Bent Over Rows",
            "4 sets of 10-12 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Hinge at hips\n" +
            "2. Back straight\n" +
            "3. Pull bar to lower chest\n" +
            "4. Lower with control",
            baseUrl + "bent_over_rows.jpg"));

        pages.add(new WorkoutPage(
            "Romanian Deadlifts",
            "4 sets of 10-12 reps\nRest 120 seconds between sets",
            "Instructions:\n\n" +
            "1. Hold bar at thighs\n" +
            "2. Hinge at hips\n" +
            "3. Lower bar along legs\n" +
            "4. Feel hamstring stretch\n" +
            "5. Drive hips forward",
            baseUrl + "romanian_deadlifts.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate_gym);

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
            "Intermediate",
            getCurrentTimestamp(),
            "60 minutes", // Intermediate gym workouts are longer
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
            .setMessage("Excellent work! Your intermediate gym workout has been recorded.")
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

        // Update total workouts and check for intermediate badge
        achievementsRef.child("totalWorkouts")
            .get()
            .addOnSuccessListener(snapshot -> {
                int total = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                total++;
                achievementsRef.child("totalWorkouts").setValue(total);
                
                // Check for intermediate badge
                if (total >= 20) {
                    achievementsRef.child("intermediateAchieved").setValue(true);
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
        // Intermediate gym workouts burn more calories
        return 400; // Default value for intermediate gym workout
    }
}