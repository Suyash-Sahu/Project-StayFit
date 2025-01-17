package com.example.messanger_apk;

public class User {
    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String search;
    
    // Workout specific fields
    private String workoutType;      // "Home" or "Gym"
    private String difficulty;       // "Beginner", "Intermediate", or "Advanced"
    private String timestamp;        // When the workout was completed
    private String duration;         // Duration of the workout
    private int caloriesBurned;      // Calories burned during workout
    private boolean completed;       // Whether the workout was completed
    private int totalWorkouts;       // Total number of workouts completed
    private int streak;              // Current streak
    private boolean intermediateAchieved;
    private boolean advancedAchieved;
    private boolean monthlyStreakAchieved;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String workoutType, String difficulty, String timestamp, 
                String duration, int caloriesBurned, boolean completed) {
        this.workoutType = workoutType;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.completed = completed;
    }

    public User(String id, String username, String imageURL, String status, String search) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
    }

    public User(String id, String username, String imageURL, String status, String search, 
                int caloriesBurned, int totalWorkouts, int streak) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.caloriesBurned = caloriesBurned;
        this.totalWorkouts = totalWorkouts;
        this.streak = streak;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    // Workout specific getters and setters
    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(int totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public boolean isIntermediateAchieved() { return intermediateAchieved; }
    public void setIntermediateAchieved(boolean intermediateAchieved) { 
        this.intermediateAchieved = intermediateAchieved; 
    }

    public boolean isAdvancedAchieved() { return advancedAchieved; }
    public void setAdvancedAchieved(boolean advancedAchieved) { 
        this.advancedAchieved = advancedAchieved; 
    }

    public boolean isMonthlyStreakAchieved() { return monthlyStreakAchieved; }
    public void setMonthlyStreakAchieved(boolean monthlyStreakAchieved) { 
        this.monthlyStreakAchieved = monthlyStreakAchieved; 
    }
}