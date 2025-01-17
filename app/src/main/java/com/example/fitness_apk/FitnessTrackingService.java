package com.example.messanger_apk;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class FitnessTrackingService extends Service implements SensorEventListener {
    private static final String CHANNEL_ID = "FitnessTrackingChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "FitnessTrackingService";
    private static final String PREFS_NAME = "FitnessPrefs";
    private static final String INITIAL_STEP_COUNT_KEY = "initialStepCount";
    private static final String LAST_STEP_COUNT_KEY = "lastStepCount";
    
    // Constants for calculations
    private static final float STEPS_PER_METER = 1.32f; // Number of steps per meter
    private static final float METERS_PER_STEP = 1.0f / STEPS_PER_METER; // About 0.758 meters per step
    private static final float CALORIES_PER_KM = 60.0f; // Average calories burned per kilometer walking
    
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private DatabaseReference userRef;
    private SharedPreferences prefs;
    private FirebaseAuth auth;
    
    private long initialStepCount = -1;
    private long lastStepCount = 0;
    private float totalDistance = 0; // in meters
    private float totalCalories = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FitnessTrackingService onCreate");
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        
        // Get the last reset time
        long lastResetTime = prefs.getLong("lastResetTime", 0);
        long currentTime = System.currentTimeMillis();
        
        // Check if 24 hours have passed since last reset
        if (currentTime - lastResetTime >= 24 * 60 * 60 * 1000) {
            // Reset daily stats
            prefs.edit()
                .putLong(INITIAL_STEP_COUNT_KEY, -1)
                .putLong(LAST_STEP_COUNT_KEY, 0)
                .putLong("lastResetTime", currentTime)
                .apply();
            
            // Reset Firebase stats
            if (auth.getCurrentUser() != null) {
                DatabaseReference fitnessRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("fitness");
                fitnessRef.child("steps").setValue(0);
                fitnessRef.child("distance").setValue(0.0);
                fitnessRef.child("calories").setValue(0.0);
            }
        } else {
            // Load the previous values
            initialStepCount = prefs.getLong(INITIAL_STEP_COUNT_KEY, -1);
            lastStepCount = prefs.getLong(LAST_STEP_COUNT_KEY, 0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FitnessTrackingService onStartCommand");
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        if (!initializeSensors()) {
            Log.e(TAG, "Failed to initialize sensors");
            Toast.makeText(this, "Step sensor not available on this device", Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!initializeFirebase()) {
            Log.e(TAG, "Failed to initialize Firebase");
            Toast.makeText(this, "Failed to initialize tracking. Please try again.", Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.d(TAG, "FitnessTrackingService started successfully");
        Toast.makeText(this, "Fitness tracking started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    private boolean initializeSensors() {
        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            
            if (stepSensor == null) {
                Log.e(TAG, "Step sensor not available");
                return false;
            }
            
            boolean sensorRegistered = sensorManager.registerListener(this, stepSensor, 
                SensorManager.SENSOR_DELAY_NORMAL);
            
            if (!sensorRegistered) {
                Log.e(TAG, "Failed to register step sensor listener");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sensors: " + e.getMessage());
            return false;
        }
    }

    private boolean initializeFirebase() {
        try {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                Log.e(TAG, "No user logged in");
                return false;
            }
            
            userRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("fitness");
                
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            long steps = (long) event.values[0];
            
            // Initialize the initial step count if not set
            if (initialStepCount == -1) {
                initialStepCount = steps;
                prefs.edit().putLong(INITIAL_STEP_COUNT_KEY, initialStepCount).apply();
            }
            
            // Calculate steps taken since service started
            long currentSteps = steps - initialStepCount + lastStepCount;
            
            // Save the current progress
            prefs.edit().putLong(LAST_STEP_COUNT_KEY, currentSteps).apply();
            
            // Calculate distance and calories
            float distance = currentSteps * METERS_PER_STEP;
            float calories = (distance / 1000) * CALORIES_PER_KM;
            
            // Update Firebase
            if (userRef != null) {
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("steps", currentSteps);
                updates.put("distance", distance);
                updates.put("calories", calories);
                userRef.updateChildren(updates);
            }
            
            // Update notification
            updateNotification(currentSteps, distance, calories);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fitness Tracking",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows fitness tracking progress");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, Homepage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StayFit Tracking Active")
            .setContentText(String.format("Steps: %d | Distance: %.1fm | Calories: %.1f", 
                lastStepCount, totalDistance, totalCalories))
            .setSmallIcon(R.drawable.ic_fitness_tracking)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void updateNotification(long steps, float distance, float calories) {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Intent notificationIntent = new Intent(this, Homepage.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            );

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("StayFit Tracking Active")
                .setContentText(String.format("Steps: %d | Distance: %.1fm | Calories: %.1f", 
                    steps, distance, calories))
                .setSmallIcon(R.drawable.ic_fitness_tracking)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        // Save the current progress when service stops
        if (initialStepCount != -1) {
            long currentSteps = lastStepCount;
            prefs.edit()
                .putLong(LAST_STEP_COUNT_KEY, currentSteps)
                .apply();
        }
    }
}
