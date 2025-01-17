package com.example.messanger_apk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class beginner_Home extends AppCompatActivity {
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
            "Push-ups",
            "3 sets of 10-12 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Start in plank position\n" +
            "2. Hands shoulder-width apart\n" +
            "3. Lower chest to ground\n" +
            "4. Push back up with control",
            baseUrl + "pushup.jpg"));

        pages.add(new WorkoutPage(
            "Bodyweight Squats",
            "3 sets of 15 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Stand with feet shoulder-width apart\n" +
            "2. Keep chest up, back straight\n" +
            "3. Lower until thighs are parallel\n" +
            "4. Push through heels to stand",
            baseUrl + "bodyweight_squat.jpg"));

        pages.add(new WorkoutPage(
            "Mountain Climbers",
            "3 sets of 30 seconds\nRest 45 seconds between sets",
            "Instructions:\n\n" +
            "1. Start in plank position\n" +
            "2. Drive knees alternately to chest\n" +
            "3. Keep core engaged\n" +
            "4. Maintain steady pace",
            baseUrl + "mountain_climbers.jpg"));

        pages.add(new WorkoutPage(
            "Glute Bridges",
            "3 sets of 15 reps\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Lie on back, knees bent\n" +
            "2. Feet flat on ground\n" +
            "3. Lift hips toward ceiling\n" +
            "4. Squeeze glutes at top\n" +
            "5. Lower with control",
            baseUrl + "glute_bridge.jpg"));

        pages.add(new WorkoutPage(
            "Bird Dogs",
            "3 sets of 10 each side\nRest 60 seconds between sets",
            "Instructions:\n\n" +
            "1. Start on hands and knees\n" +
            "2. Extend opposite arm and leg\n" +
            "3. Keep back straight\n" +
            "4. Hold for 2 seconds\n" +
            "5. Alternate sides",
            baseUrl + "bird_dog.jpg"));
        
        pagerAdapter = new WorkoutPagerAdapter(pages, this);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_beginner_home);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager();
        
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
            "Beginner",
            getCurrentTimestamp(),
            "30 minutes",
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
            .setMessage("Great job! Your workout has been recorded.")
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

        achievementsRef.child("totalWorkouts")
            .get()
            .addOnSuccessListener(snapshot -> {
                int total = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                achievementsRef.child("totalWorkouts").setValue(total + 1);
            });

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
        return 150; 
    }
}