package com.example.messanger_apk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

public class FitnessDiagnosticActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_HEALTH,
        Manifest.permission.POST_NOTIFICATIONS
    };
    private TextView statusStepSensor;
    private TextView statusGPS;
    private TextView statusPermissions;
    private TextView statusService;
    private Button fixButton;
    private boolean permissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_diagnostic);

        try {
            initializeViews();
            runDiagnostics();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            statusStepSensor = findViewById(R.id.statusStepSensor);
            statusGPS = findViewById(R.id.statusGPS);
            statusPermissions = findViewById(R.id.statusPermissions);
            statusService = findViewById(R.id.statusService);
            fixButton = findViewById(R.id.fixButton);

            fixButton.setOnClickListener(v -> fixIssues());
        } catch (Exception e) {
            Toast.makeText(this, "Error in views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void runDiagnostics() {
        try {
            checkStepSensor();
            checkGPS();
            checkAndRequestPermissions();
            checkServiceStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Error in diagnostics: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkStepSensor() {
        try {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager == null) {
                statusStepSensor.setText("❌ Step Sensor: Service Unavailable");
                return;
            }
            
            Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor != null) {
                statusStepSensor.setText("✅ Step Sensor: Available");
            } else {
                statusStepSensor.setText("❌ Step Sensor: Not Available");
            }
        } catch (Exception e) {
            statusStepSensor.setText("❌ Step Sensor: Error");
        }
    }

    private void checkGPS() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                statusGPS.setText("❌ GPS: Service Unavailable");
                return;
            }
            
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                statusGPS.setText("✅ GPS: Enabled");
            } else {
                statusGPS.setText("❌ GPS: Disabled");
            }
        } catch (Exception e) {
            statusGPS.setText("❌ GPS: Error");
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            statusPermissions.setText("❌ Missing required permissions");
        } else {
            statusPermissions.setText("✅ All permissions granted");
            permissionsGranted = true;
        }
        updateFixButtonState();
    }

    private void checkServiceStatus() {
        try {
            boolean isRunning = ServiceUtils.isServiceRunning(this, FitnessTrackingService.class);
            if (isRunning) {
                statusService.setText("✅ Tracking Service: Running");
            } else {
                statusService.setText("❌ Tracking Service: Stopped");
            }
        } catch (Exception e) {
            statusService.setText("❌ Tracking Service: Error");
            Toast.makeText(this, "Error checking service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fixIssues() {
        try {
            // Check GPS
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            // Check if service is already running
            if (!ServiceUtils.isServiceRunning(this, FitnessTrackingService.class)) {
                startFitnessTrackingIfReady();
            } else {
                Toast.makeText(this, "Fitness tracking is already running", Toast.LENGTH_SHORT).show();
            }

            // Refresh status
            refreshStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Error fixing issues: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startFitnessTrackingIfReady() {
        if (permissionsGranted) {
            if (!ServiceUtils.isServiceRunning(this, FitnessTrackingService.class)) {
                startFitnessTracking();
            } else {
                Toast.makeText(this, "Fitness tracking is already running", Toast.LENGTH_SHORT).show();
                refreshStatus();
            }
        } else {
            Toast.makeText(this, "Please grant all required permissions first", Toast.LENGTH_LONG).show();
        }
    }

    private void startFitnessTracking() {
        try {
            Intent serviceIntent = new Intent(this, FitnessTrackingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Fitness tracking started", Toast.LENGTH_SHORT).show();
            refreshStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting tracking: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshStatus() {
        try {
            checkStepSensor();
            checkGPS();
            checkAndRequestPermissions();
            checkServiceStatus();
        } catch (Exception e) {
            Toast.makeText(this, "Error refreshing status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFixButtonState() {
        if (permissionsGranted) {
            fixButton.setEnabled(true);
        } else {
            fixButton.setEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                permissionsGranted = true;
                updateFixButtonState();
                startFitnessTrackingIfReady();
            } else {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            runDiagnostics();
        } catch (Exception e) {
            Toast.makeText(this, "Error in resume: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
