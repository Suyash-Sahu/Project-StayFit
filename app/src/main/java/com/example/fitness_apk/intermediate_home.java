package com.example.messanger_apk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

public class intermediate_home extends AppCompatActivity {
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
            "Dumbbell Rows",
            "3 sets of 12 reps each arm\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Place knee and hand on bench\n" +
            "2. Hold dumbbell in other hand\n" +
            "3. Pull weight to chest\n" +
            "4. Lower with control\n" +
            "5. Complete all reps, then switch sides",
            baseUrl + "dumbbell_rows.jpg"));

        pages.add(new WorkoutPage(
            "Bulgarian Split Squats",
            "3 sets of 10 reps each leg\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Rest back foot on bench\n" +
            "2. Keep front foot planted\n" +
            "3. Lower until thigh is parallel\n" +
            "4. Push through front heel\n" +
            "5. Maintain upright posture",
            baseUrl + "bulgarian_split_squat.jpg"));

        pages.add(new WorkoutPage(
            "Weighted Dips",
            "3 sets of 8-10 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Add weight via belt/vest\n" +
            "2. Grip parallel bars\n" +
            "3. Lower body with control\n" +
            "4. Push back to start\n" +
            "5. Keep chest slightly forward",
            baseUrl + "weighted_dips.jpg"));

        pages.add(new WorkoutPage(
            "Romanian Deadlift",
            "3 sets of 10-12 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Hold bar at hip level\n" +
            "2. Hinge at hips\n" +
            "3. Lower bar along legs\n" +
            "4. Feel stretch in hamstrings\n" +
            "5. Drive hips forward to stand",
            baseUrl + "romanian_deadlift.jpg"));

        pages.add(new WorkoutPage(
            "Lat Pulldown",
            "3 sets of 12 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Grip bar wider than shoulders\n" +
            "2. Lean back slightly\n" +
            "3. Pull bar to upper chest\n" +
            "4. Control the return\n" +
            "5. Keep core engaged",
            baseUrl + "lat_pulldown.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate_home);

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
            "Home",
            "Intermediate",
            getCurrentTimestamp(),
            "45 minutes", // Intermediate workouts are longer
            calculateCalories(),
            true
        );

        workoutRef.setValue(workout)
            .addOnSuccessListener(aVoid -> {
                updateAchievements();
                showCompletionDialog();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save workout: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Workout Completed!")
            .setMessage("Excellent work! Your intermediate workout has been recorded.")
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

        // Update total workouts
        achievementsRef.child("totalWorkouts")
            .get()
            .addOnSuccessListener(snapshot -> {
                int total = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                achievementsRef.child("totalWorkouts").setValue(total + 1);
                
                // Check for intermediate milestone
                if (total + 1 >= 20) {
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
                    achievementsRef.child("streak").setValue(streak + 1);
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
        // More calories for intermediate workout
        return 300; // Higher value for intermediate level
    }
}