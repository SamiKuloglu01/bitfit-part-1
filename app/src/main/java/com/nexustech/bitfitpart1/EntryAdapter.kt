package com.nexustech.bitfitpart1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EntryAdapter : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    private val entries = mutableListOf<Entry>()

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val sleepText: TextView = itemView.findViewById(R.id.sleepText)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val notesText: TextView = itemView.findViewById(R.id.notesText)
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)

        fun bind(entry: Entry) {
            dateText.text = entry.date
            sleepText.text = "Slept: ${entry.sleepHours} hours"
            moodText.text = "Feeling: ${entry.moodRating}/10"
            notesText.text = entry.notes ?: "No notes"

            if (entry.photoPath != null) {
                photoImageView.visibility = View.VISIBLE
                Glide.with(photoImageView.context)
                    .load(entry.photoPath)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(photoImageView)
            } else {
                photoImageView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    fun submitList(newEntries: List<Entry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }
}
