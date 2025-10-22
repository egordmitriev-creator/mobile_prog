package com.example.bugs.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugs.R
import com.example.bugs.data.dao.RecordWithUser
import java.text.SimpleDateFormat
import java.util.*

class RecordsAdapter(private var records: List<RecordWithUser>) :
    RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    fun updateData(newRecords: List<RecordWithUser>) {
        this.records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position], position + 1)
    }

    override fun getItemCount(): Int = records.size

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
        private val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvZodiac: TextView = itemView.findViewById(R.id.tvZodiac)

        fun bind(record: RecordWithUser, position: Int) {
            tvPosition.text = "#$position"
            tvPlayerName.text = record.fullName
            tvScore.text = "${record.score} очков"
            tvDifficulty.text = "Уровень: ${record.difficultyLevel}"
            tvZodiac.text = record.zodiacSign

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            tvDate.text = dateFormat.format(Date(record.date))

            // Подсветка топ-3 записей
            when (position) {
                1 -> itemView.setBackgroundResource(R.color.teal_200)
                2 -> itemView.setBackgroundResource(R.color.teal_700)
                3 -> itemView.setBackgroundResource(R.color.purple_700)
                else -> itemView.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}