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

public class advanced_home extends AppCompatActivity {
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
            "One-Arm Push-ups",
            "3 sets of 5-8 reps each arm\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Start in push-up position\n" +
            "2. Place one arm behind back\n" +
            "3. Lower chest to ground\n" +
            "4. Push back up with control",
            baseUrl + "one_arm_pushup.jpg"));

        pages.add(new WorkoutPage(
            "Pistol Squats",
            "3 sets of 6 reps each leg\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Stand on one leg\n" +
            "2. Extend other leg forward\n" +
            "3. Lower into single-leg squat\n" +
            "4. Push back up maintaining balance",
            baseUrl + "pistol_squat.jpg"));

        pages.add(new WorkoutPage(
            "Planche Push-ups",
            "3 sets of max reps\nRest 2 minutes between sets",
            "Instructions:\n\n" +
            "1. Start in planche lean position\n" +
            "2. Keep body parallel to ground\n" +
            "3. Lower chest while maintaining position\n" +
            "4. Push back up with straight body",
            baseUrl + "planche_pushup.jpg"));

        pages.add(new WorkoutPage(
            "Handstand Push-ups",
            "4 sets of 5-8 reps\nRest 2 minutes between sets",
            "Instructions:\n\n" +
            "1. Kick up into handstand against wall\n" +
            "2. Lower head to ground slowly\n" +
            "3. Push back up explosively\n" +
            "4. Maintain tight core throughout\n" +
            "5. Keep elbows in",
            baseUrl + "handstand_pushup.jpg"));

        pages.add(new WorkoutPage(
            "Front Lever Rows",
            "3 sets of max holds + 3-5 rows\nRest 2 minutes between sets",
            "Instructions:\n\n" +
            "1. Hang from pull-up bar\n" +
            "2. Pull body to horizontal position\n" +
            "3. Maintain position and perform rows\n" +
            "4. Keep body straight throughout\n" +
            "5. Lower with control",
            baseUrl + "front_lever.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_home);

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
            "Advanced",
            getCurrentTimestamp(),
            "60 minutes", // Advanced workouts are longer
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
            .setMessage("Outstanding achievement! Your advanced workout has been recorded.")
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
                
                // Check for advanced milestone
                if (total + 1 >= 50) {
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
                    achievementsRef.child("streak").setValue(streak + 1);
                    
                    // Check for streak achievements
                    if (streak + 1 >= 30) {
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
        // More calories for advanced workout
        return 500; // Highest value for advanced level
    }
}
