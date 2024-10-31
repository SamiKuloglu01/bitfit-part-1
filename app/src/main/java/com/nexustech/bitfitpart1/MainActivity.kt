package com.nexustech.bitfitpart1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: EntryAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var currentPhotoPath: String = ""
    private lateinit var photoImageView: ImageView
    private lateinit var selectImageButton: MaterialButton
    private lateinit var sleepSeekBar: SeekBar
    private lateinit var feelingSeekBar: SeekBar
    private lateinit var notesEditText: EditText
    private lateinit var sleepValueText: TextView
    private lateinit var feelingValueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        photoImageView = findViewById(R.id.photoImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        sleepSeekBar = findViewById(R.id.sleepSeekBar)
        feelingSeekBar = findViewById(R.id.feelingSeekBar)
        notesEditText = findViewById(R.id.notesEditText)
        sleepValueText = findViewById(R.id.sleepValueText)
        feelingValueText = findViewById(R.id.feelingValueText)

        setupRecyclerView()
        setupSeekBars()
        setupSaveButton()
        observeEntries()
        loadAverages()
        setupSelectImageButton()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        adapter = EntryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSeekBars() {
        setupSeekBarWithDisplay(sleepSeekBar, sleepValueText, " hrs")
        setupSeekBarWithDisplay(feelingSeekBar, feelingValueText, "/10")
    }

    private fun setupSeekBarWithDisplay(seekBar: SeekBar, valueText: TextView, suffix: String) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueText.text = "$progress$suffix"
                valueText.visibility = TextView.VISIBLE
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                valueText.visibility = TextView.VISIBLE
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Keep the value visible here if you need
            }
        })
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<MaterialButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            val sleepHours = sleepSeekBar.progress.toFloat()
            val moodRating = feelingSeekBar.progress
            val notes = notesEditText.text.toString()
            saveEntry(sleepHours, moodRating, notes, currentPhotoPath)
        }
    }

    private fun saveEntry(sleepHours: Float, moodRating: Int, notes: String, photoPath: String) {
        val formattedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        val entry = Entry(
            date = formattedDate,
            sleepHours = sleepHours,
            moodRating = moodRating,
            notes = notes,
            photoPath = photoPath.takeIf { it.isNotEmpty() } ?: "No image selected"
        )
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.entryDao().insert(entry)
            }
            navigateToDisplayDataActivity()
            resetFields()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetFields() {
        sleepSeekBar.progress = 0
        feelingSeekBar.progress = 0
        sleepValueText.text = "0 hrs"
        feelingValueText.text = "0/10"
        notesEditText.text.clear()
        currentPhotoPath = ""
        selectImageButton.text = "Select Image"
        photoImageView.setImageResource(R.drawable.ic_camera)
    }


    private fun navigateToDisplayDataActivity() {
        val intent = Intent(this, DisplayDataActivity::class.java)
        startActivity(intent)
    }

    private fun setupSelectImageButton() {
        selectImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.nexustech.bitfitpart1.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectImageButton.text = "Image Selected"
            photoImageView.visibility = ImageView.GONE
        }
    }

    private fun observeEntries() {
        db.entryDao().getAllEntries().observe(this) { entries ->
            adapter.submitList(entries)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadAverages() {
        val averageSleepText = findViewById<TextView>(R.id.averageSleepText)
        val averageFeelingText = findViewById<TextView>(R.id.averageFeelingText)
        db.entryDao().getAverageSleep().observe(this) { avgSleep ->
            averageSleepText.text = "Average hours of sleep: ${avgSleep ?: "--"} hrs"
        }
        db.entryDao().getAverageMood().observe(this) { avgMood ->
            averageFeelingText.text = "Average feeling: ${avgMood ?: "--"}/10"
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val REQUEST_IMAGE_CAPTURE = 1002
    }
}
