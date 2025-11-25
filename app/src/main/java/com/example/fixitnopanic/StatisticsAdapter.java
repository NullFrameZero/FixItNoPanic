package com.example.fixitnopanic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.RequestViewHolder> {

    private List<RequestItem> requestList;
    private final Context context;

    public StatisticsAdapter(List<RequestItem> requestList, Context context) {
        this.requestList = requestList;
        this.context = context;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewId;
        public final TextView textViewClient;
        public final TextView textViewModel;
        public final TextView textViewProblem;
        public final TextView textViewDateCreated;
        public final TextView textViewDateCompleted;
        public final TextView textViewStatus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewClient = itemView.findViewById(R.id.textViewClient);
            textViewModel = itemView.findViewById(R.id.textViewModel);
            textViewProblem = itemView.findViewById(R.id.textViewProblem);
            textViewDateCreated = itemView.findViewById(R.id.textViewDateCreated);
            textViewDateCompleted = itemView.findViewById(R.id.textViewDateCompleted);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_request_statistic, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestItem item = requestList.get(position);
        holder.textViewId.setText(String.valueOf(item.getId()));
        holder.textViewClient.setText(item.getClient());
        holder.textViewModel.setText(item.getModel());
        holder.textViewProblem.setText(item.getProblem());
        holder.textViewDateCreated.setText(item.getDateCreated().split(" ")[0]);

        if (item.getDateCompleted() != null && !item.getDateCompleted().isEmpty()) {
            holder.textViewDateCompleted.setText(item.getDateCompleted().split(" ")[0]);
            holder.textViewDateCompleted.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDateCompleted.setVisibility(View.GONE);
        }

        String statusText = "in_progress".equals(item.getStatus()) ? "в работе" : "выполнено";
        holder.textViewStatus.setText(statusText);
        holder.textViewStatus.setBackgroundResource(
                "in_progress".equals(item.getStatus())
                        ? R.drawable.status_badge_background
                        : R.drawable.status_completed_badge_background
        );
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void updateData(List<RequestItem> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }
}