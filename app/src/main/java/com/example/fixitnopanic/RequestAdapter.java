package com.example.fixitnopanic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<RequestItem> requestList;
    private final Context context;
    private final RequestDao requestDao;

    public RequestAdapter(List<RequestItem> requestList, Context context, RequestDao requestDao) {
        this.requestList = requestList;
        this.context = context;
        this.requestDao = requestDao;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        public final TextView textViewId;
        public final TextView textViewClient;
        public final TextView textViewModel;
        public final TextView textViewProblem;
        public final TextView textViewDateCreated;
        public final TextView textViewDateCompleted;
        public final TextView textViewStatus;
        public final ImageView editButton;
        public final ImageView statusButton;
        public final ImageView deleteButton;

        public RequestViewHolder(View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewClient = itemView.findViewById(R.id.textViewClient);
            textViewModel = itemView.findViewById(R.id.textViewModel);
            textViewProblem = itemView.findViewById(R.id.textViewProblem);
            textViewDateCreated = itemView.findViewById(R.id.textViewDateCreated);
            textViewDateCompleted = itemView.findViewById(R.id.textViewDateCompleted);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            editButton = itemView.findViewById(R.id.editButton);
            statusButton = itemView.findViewById(R.id.statusButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request_main, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RequestViewHolder holder, int position) {
        RequestItem currentItem = requestList.get(position);

        holder.textViewId.setText(String.valueOf(currentItem.getId()));
        holder.textViewClient.setText(currentItem.getClient());
        holder.textViewModel.setText(currentItem.getModel());
        holder.textViewProblem.setText(currentItem.getProblem());
        holder.textViewDateCreated.setText(currentItem.getDateCreated().split(" ")[0]);

        if (currentItem.getDateCompleted() != null && !currentItem.getDateCompleted().isEmpty()) {
            holder.textViewDateCompleted.setText(currentItem.getDateCompleted().split(" ")[0]);
            holder.textViewDateCompleted.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDateCompleted.setVisibility(View.GONE);
        }

        // Используем строки из ресурсов
        String statusText = "in_progress".equals(currentItem.getStatus())
                ? context.getString(R.string.status_in_progress)
                : context.getString(R.string.status_completed);
        holder.textViewStatus.setText(statusText);

        int bgRes = "in_progress".equals(currentItem.getStatus())
                ? R.drawable.status_badge_background
                : R.drawable.status_completed_badge_background;
        holder.textViewStatus.setBackgroundResource(bgRes);

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CreateRequestActivity.class);
            intent.putExtra("request_id", currentItem.getId());
            intent.putExtra("client", currentItem.getClient());
            intent.putExtra("phone", currentItem.getPhone());
            intent.putExtra("model", currentItem.getModel());
            intent.putExtra("problem", currentItem.getProblem());
            intent.putExtra("dateCreated", currentItem.getDateCreated());
            intent.putExtra("dateCompleted", currentItem.getDateCompleted());
            intent.putExtra("status", currentItem.getStatus());
            context.startActivity(intent);
        });

        holder.statusButton.setOnClickListener(v -> {
            String newStatus = "in_progress".equals(currentItem.getStatus()) ? "completed" : "in_progress";
            int rows = requestDao.updateRequestStatus(currentItem.getId(), newStatus);
            if (rows > 0) {
                RequestItem updated = new RequestItem(
                        currentItem.getId(),
                        currentItem.getClient(),
                        currentItem.getPhone(),
                        currentItem.getModel(),
                        currentItem.getProblem(),
                        currentItem.getDateCreated(),
                        currentItem.getDateCompleted(),
                        newStatus
                );
                requestList.set(position, updated);
                notifyItemChanged(position);
                Toast.makeText(context, "Статус изменен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ошибка изменения статуса", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Удалить заявку")
                    .setMessage("Вы уверены, что хотите удалить заявку ID " + currentItem.getId() + "?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        int rows = requestDao.deleteRequest(currentItem.getId());
                        if (rows > 0) {
                            requestList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Заявка удалена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Ошибка удаления заявки", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Нет", null)
                    .show();
        });
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