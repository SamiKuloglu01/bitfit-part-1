package com.nexustech.bitfitpart1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisplayDataActivity : AppCompatActivity() {
    private lateinit var adapter: EntryAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        loadEntries()
        displayAverages()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        adapter = EntryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadEntries() {
        val db = AppDatabase.getDatabase(this)
        db.entryDao().getAllEntries().observe(this) { entries ->
            adapter.submitList(entries)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAverages() {
        val db = AppDatabase.getDatabase(this)
        val averageSleepText = findViewById<TextView>(R.id.averageSleepText)
        val averageFeelingText = findViewById<TextView>(R.id.averageFeelingText)

        db.entryDao().getAverageSleep().observe(this) { avgSleep ->
            averageSleepText.text = "Average hours of sleep: ${avgSleep ?: "--"} hrs"
        }

        db.entryDao().getAverageMood().observe(this) { avgMood ->
            averageFeelingText.text = "Average feeling: ${avgMood ?: "--"}/10"
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
