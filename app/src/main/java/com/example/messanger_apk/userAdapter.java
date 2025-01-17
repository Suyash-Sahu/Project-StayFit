package com.example.messanger_apk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class userAdapter extends RecyclerView.Adapter<userAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    public userAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        
        // Set workout information
        holder.workoutType.setText(user.getWorkoutType() + " - " + user.getDifficulty());
        holder.timestamp.setText(user.getTimestamp());
        holder.duration.setText("Duration: " + user.getDuration());
        holder.calories.setText("Calories: " + user.getCaloriesBurned());
        
        // Set completion status
        if (user.isCompleted()) {
            holder.completionStatus.setVisibility(View.VISIBLE);
            holder.completionStatus.setText("✓ Completed");
            holder.completionStatus.setTextColor(android.graphics.Color.GREEN);
        } else {
            holder.completionStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView workoutType;
        public TextView timestamp;
        public TextView duration;
        public TextView calories;
        public TextView completionStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutType = itemView.findViewById(R.id.workoutType);
            timestamp = itemView.findViewById(R.id.timestamp);
            duration = itemView.findViewById(R.id.duration);
            calories = itemView.findViewById(R.id.calories);
            completionStatus = itemView.findViewById(R.id.completionStatus);
        }
    }
}