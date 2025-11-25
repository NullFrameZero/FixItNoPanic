package com.example.fixitnopanic

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class RequestAdapter(
    private var requestList: List<RequestItem>,
    private val context: Context,
    private val requestDao: RequestDao
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewId: TextView = itemView.findViewById(R.id.textViewId)
        val textViewClient: TextView = itemView.findViewById(R.id.textViewClient)
        val textViewModel: TextView = itemView.findViewById(R.id.textViewModel)
        val textViewProblem: TextView = itemView.findViewById(R.id.textViewProblem)
        val textViewDateCreated: TextView = itemView.findViewById(R.id.textViewDateCreated)
        val textViewDateCompleted: TextView = itemView.findViewById(R.id.textViewDateCompleted)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val editButton: ImageView = itemView.findViewById(R.id.editButton)
        val statusButton: ImageView = itemView.findViewById(R.id.statusButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_main, parent, false)
        return RequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val currentItem = requestList[position]
        holder.textViewId.text = currentItem.id.toString()
        holder.textViewClient.text = currentItem.client
        holder.textViewModel.text = currentItem.model
        holder.textViewProblem.text = currentItem.problem
        holder.textViewDateCreated.text = currentItem.dateCreated.substringBefore(' ')

        if (currentItem.dateCompleted != null && currentItem.dateCompleted.isNotEmpty()) {
            holder.textViewDateCompleted.text = currentItem.dateCompleted.substringBefore(' ')
            holder.textViewDateCompleted.visibility = View.VISIBLE
        } else {
            holder.textViewDateCompleted.visibility = View.GONE
        }

        val statusText = if (currentItem.status == "in_progress") "в работе" else "выполнено"
        holder.textViewStatus.text = statusText
        val backgroundRes = if (currentItem.status == "in_progress") {
            R.drawable.status_badge_background
        } else {
            R.drawable.status_completed_badge_background
        }
        holder.textViewStatus.setBackgroundResource(backgroundRes)

        holder.editButton.setOnClickListener {
            val intent = Intent(context, CreateRequestActivity::class.java).apply {
                putExtra("request_id", currentItem.id)
                putExtra("client", currentItem.client)
                putExtra("phone", currentItem.phone)
                putExtra("model", currentItem.model)
                putExtra("problem", currentItem.problem)
                putExtra("dateCreated", currentItem.dateCreated)
                putExtra("dateCompleted", currentItem.dateCompleted)
                putExtra("status", currentItem.status)
            }
            context.startActivity(intent)
        }

        holder.statusButton.setOnClickListener {
            val newStatus = if (currentItem.status == "in_progress") "completed" else "in_progress"
            val rowsUpdated = requestDao.updateRequestStatus(currentItem.id, newStatus)
            if (rowsUpdated > 0) {
                val updatedList = requestList.toMutableList()
                val updatedItem = currentItem.copy(status = newStatus)
                updatedList[position] = updatedItem
                requestList = updatedList
                notifyItemChanged(position)
                Toast.makeText(context, "Статус изменен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Ошибка изменения статуса", Toast.LENGTH_SHORT).show()
            }
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удалить заявку")
                .setMessage("Вы уверены, что хотите удалить заявку ID ${currentItem.id}?")
                .setPositiveButton("Да") { _, _ ->
                    val rowsDeleted = requestDao.deleteRequest(currentItem.id)
                    if (rowsDeleted > 0) {
                        val updatedList = requestList.toMutableList()
                        updatedList.removeAt(position)
                        requestList = updatedList
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Заявка удалена", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Ошибка удаления заявки", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    override fun getItemCount() = requestList.size

    fun updateData(newList: List<RequestItem>) {
        requestList = newList
        notifyDataSetChanged()
    }
}