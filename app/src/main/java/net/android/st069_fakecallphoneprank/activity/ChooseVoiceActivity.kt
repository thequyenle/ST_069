package net.android.st069_fakecallphoneprank.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.adapters.VoiceAdapter
import net.android.st069_fakecallphoneprank.data.model.Voice
import net.android.st069_fakecallphoneprank.databinding.ActivityChooseVoiceBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper
import java.io.File
import java.io.IOException

class ChooseVoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseVoiceBinding
    private lateinit var adapter: VoiceAdapter
    private lateinit var voiceList: MutableList<Voice>

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingDialog: Dialog? = null
    private var playingDialog: Dialog? = null

    private var audioFilePath: String? = null
    private var isRecording = false
    private var recordingStartTime = 0L
    private var timerHandler = Handler(Looper.getMainLooper())

    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private var selectedVoice: Voice? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVoiceList()
        setupRecyclerView()
        setupClickListeners()

        // Get currently selected voice
        val currentVoice = intent.getStringExtra("CURRENT_VOICE")
        currentVoice?.let { voice ->
            voiceList.find { it.name == voice }?.let {
                it.isSelected = true
                selectedVoice = it
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupVoiceList() {
        voiceList = mutableListOf(
            Voice("1", "Mom", "00:02  2Sl", null, R.drawable.ic_music, false, false),
            Voice("2", "Loona", "00:02  2Sl", null, R.drawable.ic_music, false, false),
            Voice("3", "My friend", "00:02  2Sl", null, R.drawable.ic_music, false, false),
            Voice("4", "My love", "00:02  2Sl", null, R.drawable.ic_music, false, false),
            Voice("5", "Cattry", "00:02  2Sl", null, R.drawable.ic_music, false, false),
            Voice("6", "Male police voice", "00:02  2Sl", null, R.drawable.ic_music, false, false)
        )

        // Load custom recordings
        loadCustomVoices()
    }

    private fun loadCustomVoices() {
        val customVoicesDir = File(filesDir, "custom_voices")
        if (customVoicesDir.exists()) {
            customVoicesDir.listFiles()?.forEach { file ->
                if (file.extension == "3gp" || file.extension == "mp3") {
                    val voiceName = file.nameWithoutExtension
                    val duration = getAudioDuration(file.absolutePath)
                    voiceList.add(
                        Voice(
                            file.name,
                            voiceName,
                            duration,
                            file.absolutePath,
                            R.drawable.ic_music,
                            true,
                            false
                        )
                    )
                }
            }
        }
    }

    private fun getAudioDuration(filePath: String): String {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            val duration = mp.duration / 1000
            mp.release()
            String.format("%02d:%02d  %dSl", duration / 60, duration % 60, duration)
        } catch (e: Exception) {
            "00:00  0Sl"
        }
    }

    private fun setupRecyclerView() {
        adapter = VoiceAdapter(voiceList) { voice ->
            // Show media playing dialog
            showMediaPlayingDialog(voice)
        }

        binding.rvVoices.layoutManager = LinearLayoutManager(this)
        binding.rvVoices.adapter = adapter

        // Add swipe-to-delete functionality
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition

                    // Only allow deletion for custom voices
                    if (!adapter.isCustomVoice(position)) {
                        Toast.makeText(
                            this@ChooseVoiceActivity,
                            "Cannot delete preset voices",
                            Toast.LENGTH_SHORT
                        ).show()
                        adapter.notifyItemChanged(position)
                        return
                    }

                    // Remove item from adapter
                    val deletedVoice = adapter.removeItem(position)

                    // Show Snackbar with Undo option
                    val snackbar = com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "${deletedVoice.name} deleted",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    )

                    snackbar.setAction("UNDO") {
                        // Restore item
                        adapter.restoreItem(deletedVoice, position)
                        Toast.makeText(
                            this@ChooseVoiceActivity,
                            "${deletedVoice.name} restored",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    snackbar.addCallback(object : com.google.android.material.snackbar.Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: com.google.android.material.snackbar.Snackbar?, event: Int) {
                            // If not undone, delete the actual file
                            if (event != DISMISS_EVENT_ACTION) {
                                deletedVoice.filePath?.let { filePath ->
                                    try {
                                        File(filePath).delete()
                                        android.util.Log.d("ChooseVoice", "Deleted file: $filePath")
                                    } catch (e: Exception) {
                                        android.util.Log.e("ChooseVoice", "Error deleting file", e)
                                    }
                                }
                            }
                        }
                    })

                    snackbar.show()
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val position = viewHolder.adapterPosition
                    // Disable swipe for preset voices
                    return if (adapter.isCustomVoice(position)) {
                        super.getSwipeDirs(recyclerView, viewHolder)
                    } else {
                        0 // No swipe
                    }
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.rvVoices)
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            selectedVoice?.let { voice ->
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_VOICE", voice.name)
                    putExtra("VOICE_FILE_PATH", voice.filePath)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Please select a voice", Toast.LENGTH_SHORT).show()
            }
        }

        // Add voice section - show recording dialog
        binding.layoutAddVoice.setOnClickListener {
            checkPermissionAndShowRecordingDialog()
        }
    }

    private fun checkPermissionAndShowRecordingDialog() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
            return
        }

        showRecordingDialog()
    }

    private fun showRecordingDialog() {
        recordingDialog = Dialog(this)
        recordingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        recordingDialog?.setContentView(R.layout.dialog_voice_record)
        recordingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rootLayout = recordingDialog?.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        val tvRecordingTimer = recordingDialog?.findViewById<TextView>(R.id.tvRecordingTimer)
        val btnPlayRecord = recordingDialog?.findViewById<ImageView>(R.id.btnPlayRecord)
        val btnStopRecord = recordingDialog?.findViewById<ImageView>(R.id.btnStopRecord)

        // Initially show play button
        btnPlayRecord?.visibility = View.VISIBLE
        btnStopRecord?.visibility = View.GONE
        tvRecordingTimer?.visibility = View.GONE

        // Play button - start recording
        btnPlayRecord?.setOnClickListener {
            startRecordingInDialog(rootLayout, tvRecordingTimer, btnPlayRecord, btnStopRecord)
        }

        // Stop button - stop recording and immediately show name dialog
        btnStopRecord?.setOnClickListener {
            stopRecordingInDialog()
        }

        recordingDialog?.setOnDismissListener {
            if (isRecording) {
                // Clean up if dialog dismissed while recording
                try {
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null
                    isRecording = false
                    timerHandler.removeCallbacksAndMessages(null)

                    // Delete temp file
                    audioFilePath?.let { File(it).delete() }
                    audioFilePath = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        recordingDialog?.show()

        // Set dim background with custom color
        recordingDialog?.window?.apply {
            setDimAmount(0.5f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun startRecordingInDialog(
        rootLayout: androidx.constraintlayout.widget.ConstraintLayout?,
        tvTimer: TextView?,
        btnPlay: ImageView?,
        btnStop: ImageView?
    ) {
        if (isRecording) {
            Toast.makeText(this, "Already recording", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Create audio file path
            val tempDir = File(cacheDir, "temp_recordings")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            audioFilePath = File(tempDir, "temp_recording.3gp").absolutePath

            // Setup MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()

                    isRecording = true

                    // Change background to running state
                    rootLayout?.setBackgroundResource(R.drawable.bg_recording_area_run)

                    // Update UI
                    btnPlay?.visibility = View.GONE
                    btnStop?.visibility = View.VISIBLE
                    tvTimer?.visibility = View.VISIBLE

                    // Start timer
                    recordingStartTime = System.currentTimeMillis()
                    startTimer(tvTimer)

                } catch (e: IOException) {
                    Toast.makeText(this@ChooseVoiceActivity, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("ChooseVoice", "Error starting recording", e)

                    // Clean up on failure
                    try {
                        release()
                    } catch (e2: Exception) {
                        // Ignore
                    }
                    mediaRecorder = null
                    isRecording = false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting recording: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ChooseVoice", "Error in startRecordingInDialog", e)
            mediaRecorder = null
            isRecording = false
        }
    }

    private fun stopRecordingInDialog() {
        if (!isRecording) {
            Toast.makeText(this, "No recording in progress", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Check if recording duration is sufficient (at least 1 second)
            val recordingDuration = (System.currentTimeMillis() - recordingStartTime) / 1000
            if (recordingDuration < 1) {
                Toast.makeText(this, "Recording too short. Please record at least 1 second.", Toast.LENGTH_SHORT).show()
                return
            }

            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    // Handle stop() failure
                    android.util.Log.e("ChooseVoice", "Error stopping MediaRecorder", e)
                }
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Stop timer
            timerHandler.removeCallbacksAndMessages(null)

            // Close recording dialog
            recordingDialog?.dismiss()

            // Immediately show name input dialog
            showSaveRecordingDialog()

        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping recording: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ChooseVoice", "Error in stopRecordingInDialog", e)

            // Clean up
            try {
                mediaRecorder?.release()
            } catch (e2: Exception) {
                // Ignore
            }
            mediaRecorder = null
            isRecording = false
            timerHandler.removeCallbacksAndMessages(null)

            // Delete temp file if it exists
            audioFilePath?.let {
                try {
                    File(it).delete()
                } catch (e2: Exception) {
                    // Ignore
                }
            }
            audioFilePath = null
        }
    }

    private fun startTimer(tvTimer: TextView?) {
        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val elapsedTime = (System.currentTimeMillis() - recordingStartTime) / 1000
                    val minutes = elapsedTime / 60
                    val seconds = elapsedTime % 60
                    tvTimer?.text = String.format("%02d:%02d", minutes, seconds)
                    timerHandler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun showSaveRecordingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_voice_name, null)
        val input = dialogView.findViewById<EditText>(R.id.etVoiceName)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)  // Changed to TextView
        val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)          // Changed to TextView

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            // Delete temp file
            audioFilePath?.let { File(it).delete() }
            audioFilePath = null
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            val voiceName = input.text.toString().trim()

            // Validate input - dialog stays open if empty
            if (voiceName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save and dismiss only if name is provided
            saveCustomVoice(voiceName)
            dialog.dismiss()
        }

        dialog.show()
        val width = (259 * resources.displayMetrics.density).toInt()
        val height = (192 * resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(width, height)

        // Set dim background with custom color
        dialog.window?.apply {
            setDimAmount(0.5f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun saveCustomVoice(name: String) {
        try {
            // Create custom voices directory
            val customVoicesDir = File(filesDir, "custom_voices")
            if (!customVoicesDir.exists()) {
                customVoicesDir.mkdirs()
            }

            // Move temp file to permanent location
            val tempFile = File(audioFilePath!!)
            val newFile = File(customVoicesDir, "$name.3gp")
            tempFile.renameTo(newFile)

            // Add to list
            val duration = getAudioDuration(newFile.absolutePath)
            val newVoice = Voice(
                newFile.name,
                name,
                duration,
                newFile.absolutePath,
                R.drawable.ic_music,
                true,
                false
            )
            voiceList.add(newVoice)
            adapter.notifyItemInserted(voiceList.size - 1)

            Toast.makeText(this, "Voice saved successfully!", Toast.LENGTH_SHORT).show()
            audioFilePath = null

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving voice: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMediaPlayingDialog(voice: Voice) {
        playingDialog = Dialog(this)
        playingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        playingDialog?.setContentView(R.layout.dialog_media_playing)
        playingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set dim background with custom color
        playingDialog?.window?.apply {
            setDimAmount(0.5f)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        val tvVoiceName = playingDialog?.findViewById<TextView>(R.id.tvVoiceName)
        val ivMusicIcon = playingDialog?.findViewById<ImageView>(R.id.ivMusicIcon)
        val seekBar = playingDialog?.findViewById<SeekBar>(R.id.seekBar)
        val tvCurrentTime = playingDialog?.findViewById<TextView>(R.id.tvCurrentTime)
        val tvTotalTime = playingDialog?.findViewById<TextView>(R.id.tvTotalTime)
        val btnPlayPause = playingDialog?.findViewById<ImageButton>(R.id.btnPlayPause)
        val btnClose = playingDialog?.findViewById<TextView>(R.id.btnClose)

        tvVoiceName?.text = voice.name
        ivMusicIcon?.setImageResource(R.drawable.ic_music)

        // Select this voice
        selectedVoice = voice
        adapter.selectVoice(voice)

        // Setup media player for custom voices
        if (voice.filePath != null && File(voice.filePath).exists()) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(voice.filePath)
                    prepare()

                    // Set duration
                    val duration = this.duration
                    seekBar?.max = duration
                    tvTotalTime?.text = formatTime(duration)
                    tvCurrentTime?.text = "00:00"
                }

                // Play/Pause button
                var isPlaying = false
                btnPlayPause?.setOnClickListener {
                    mediaPlayer?.let { player ->
                        if (isPlaying) {
                            player.pause()
                            btnPlayPause.setImageResource(R.drawable.ic_start_voice)
                            isPlaying = false
                        } else {
                            player.start()
                            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                            isPlaying = true
                            updateSeekBar(seekBar, tvCurrentTime, player)
                        }
                    }
                }

                // Seek bar
                seekBar?.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mediaPlayer?.seekTo(progress)
                            tvCurrentTime?.text = formatTime(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
                })

                // Auto stop at end
                mediaPlayer?.setOnCompletionListener {
                    btnPlayPause?.setImageResource(R.drawable.ic_start_voice)
                    isPlaying = false
                    seekBar?.progress = 0
                    tvCurrentTime?.text = "00:00"
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Error loading audio: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("ChooseVoice", "Error playing voice", e)
            }
        } else {
            // For preset voices without file
            btnPlayPause?.setOnClickListener {
                Toast.makeText(this, "Preview not available for preset voices", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose?.setOnClickListener {
            playingDialog?.dismiss()
        }

        playingDialog?.setOnDismissListener {
            try {
                mediaPlayer?.apply {
                    try {
                        if (isPlaying) {
                            stop()
                        }
                    } catch (e: IllegalStateException) {
                        // MediaPlayer already in illegal state, just release
                        android.util.Log.e("ChooseVoice", "MediaPlayer in illegal state on dismiss", e)
                    }
                    release()
                }
            } catch (e: Exception) {
                android.util.Log.e("ChooseVoice", "Error dismissing media player", e)
            } finally {
                mediaPlayer = null
                // Stop any pending timer updates
                timerHandler.removeCallbacksAndMessages(null)
            }
        }

        playingDialog?.show()
    }

    private fun updateSeekBar(seekBar: android.widget.SeekBar?, tvCurrentTime: TextView?, player: MediaPlayer) {
        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    // Check if player is still valid and playing
                    if (player.isPlaying) {
                        seekBar?.progress = player.currentPosition
                        tvCurrentTime?.text = formatTime(player.currentPosition)
                        timerHandler.postDelayed(this, 100)
                    }
                } catch (e: IllegalStateException) {
                    // MediaPlayer is in illegal state, stop updating
                    android.util.Log.e("ChooseVoice", "MediaPlayer in illegal state", e)
                } catch (e: Exception) {
                    // Any other exception, stop updating
                    android.util.Log.e("ChooseVoice", "Error updating seek bar", e)
                }
            }
        }, 100)
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showRecordingDialog()
                } else {
                    Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer?.release()
        mediaPlayer = null
        timerHandler.removeCallbacksAndMessages(null)
        recordingDialog?.dismiss()
        playingDialog?.dismiss()
    }
}