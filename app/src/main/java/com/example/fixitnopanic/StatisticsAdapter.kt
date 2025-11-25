package com.example.fixitnopanic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatisticsAdapter(
    private var requestList: List<RequestItem>,
    private val context: Context
    // private val requestDao: RequestDao // Убираем DAO, если не используется в адаптере статистики
) : RecyclerView.Adapter<StatisticsAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewId: TextView = itemView.findViewById(R.id.textViewId)
        val textViewClient: TextView = itemView.findViewById(R.id.textViewClient)
        val textViewModel: TextView = itemView.findViewById(R.id.textViewModel)
        val textViewProblem: TextView = itemView.findViewById(R.id.textViewProblem) // Добавлено
        val textViewDateCreated: TextView = itemView.findViewById(R.id.textViewDateCreated)
        val textViewDateCompleted: TextView = itemView.findViewById(R.id.textViewDateCompleted)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        // Убираем кнопки, они не нужны в статистике
        // val editButton: ImageView = itemView.findViewById(R.id.editButton)
        // val statusButton: ImageView = itemView.findViewById(R.id.statusButton)
        // val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_statistic, parent, false) // Убедитесь, что layout существует
        return RequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val currentItem = requestList[position]

        holder.textViewId.text = currentItem.id.toString()
        holder.textViewClient.text = currentItem.client
        holder.textViewModel.text = currentItem.model
        holder.textViewProblem.text = currentItem.problem // Установка значения
        holder.textViewDateCreated.text = currentItem.dateCreated.substringBefore(' ') // Только дата
        // Обработка dateCompleted
        if (currentItem.dateCompleted != null && currentItem.dateCompleted.isNotEmpty()) {
            holder.textViewDateCompleted.text = currentItem.dateCompleted.substringBefore(' ') // Только дата
            holder.textViewDateCompleted.visibility = View.VISIBLE
        } else {
            holder.textViewDateCompleted.visibility = View.GONE
        }

        // Обновляем статус и цвет фона
        val statusText = if (currentItem.status == "in_progress") "в работе" else "выполнено"
        holder.textViewStatus.text = statusText
        val backgroundRes = if (currentItem.status == "in_progress") {
            R.drawable.status_badge_background
        } else {
            R.drawable.status_completed_badge_background
        }
        holder.textViewStatus.setBackgroundResource(backgroundRes)
    }

    override fun getItemCount() = requestList.size

    // Метод для обновления данных извне (например, из StatisticsActivity)
    fun updateData(newList: List<RequestItem>) {
        requestList = newList
        notifyDataSetChanged()
    }
}