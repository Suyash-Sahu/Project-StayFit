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

public class beginner_gym extends AppCompatActivity {
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
            "Assisted Pull-ups",
            "3 sets of 8 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Use assisted pull-up machine\n" +
            "2. Select appropriate counterweight\n" +
            "3. Grip bar slightly wider than shoulders\n" +
            "4. Pull up with controlled movement",
            baseUrl + "assisted_pullups.jpg"));

        pages.add(new WorkoutPage(
            "Machine Chest Press",
            "3 sets of 10 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Adjust seat height\n" +
            "2. Keep back against pad\n" +
            "3. Push handles forward smoothly\n" +
            "4. Return with control",
            baseUrl + "machine_chest_press.jpg"));

        pages.add(new WorkoutPage(
            "Smith Machine Squats",
            "3 sets of 12 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Position bar on upper back\n" +
            "2. Feet shoulder-width apart\n" +
            "3. Lower until thighs are parallel\n" +
            "4. Push through heels to stand",
            baseUrl + "smith_machine_squat.jpg"));

        pages.add(new WorkoutPage(
            "Seated Cable Rows",
            "3 sets of 12 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Sit with feet on platform\n" +
            "2. Grab cable attachment\n" +
            "3. Pull towards lower chest\n" +
            "4. Keep back straight\n" +
            "5. Return to start position slowly",
            baseUrl + "seated_cable_rows.jpg"));

        pages.add(new WorkoutPage(
            "Leg Press Machine",
            "3 sets of 15 reps\nRest 90 seconds between sets",
            "Instructions:\n\n" +
            "1. Adjust seat position\n" +
            "2. Place feet shoulder-width apart\n" +
            "3. Lower weight until knees form 90 degrees\n" +
            "4. Push through heels\n" +
            "5. Don't lock knees at top",
            baseUrl + "leg_press.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beginner_gym);

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
            "Beginner",
            getCurrentTimestamp(),
            "45 minutes", // Gym workouts are typically longer
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
        new AlertDialog.Builder(this)
            .setTitle("Workout Completed!")
            .setMessage("Great job! Your gym workout has been recorded.")
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
        // Gym workouts burn more calories
        return 200; // Default value for beginner gym workout
    }
}