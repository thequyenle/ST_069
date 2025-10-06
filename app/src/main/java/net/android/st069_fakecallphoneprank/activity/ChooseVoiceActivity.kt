package net.android.st069_fakecallphoneprank.activity

import android.Manifest
import android.app.Dialog
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
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.adapters.VoiceAdapter
import net.android.st069_fakecallphoneprank.data.model.Voice
import net.android.st069_fakecallphoneprank.databinding.ActivityChooseVoiceBinding
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
    }

    private fun startRecordingInDialog(
        rootLayout: androidx.constraintlayout.widget.ConstraintLayout?,
        tvTimer: TextView?,
        btnPlay: ImageView?,
        btnStop: ImageView?
    ) {
        try {
            // Create audio file path
            val tempDir = File(cacheDir, "temp_recordings")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            audioFilePath = File(tempDir, "temp_recording.3gp").absolutePath

            // Setup MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)

                try {
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
                    Toast.makeText(this@ChooseVoiceActivity, "Recording failed", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecordingInDialog() {
        try {
            mediaRecorder?.apply {
                stop()
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
            Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show()
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_name, null)
        val input = dialogView.findViewById<EditText>(R.id.etVoiceName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

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

        // Setup media player (simplified - you would load actual audio)
        btnPlayPause?.setOnClickListener {
            // Toggle play/pause
            Toast.makeText(this, "Playing ${voice.name}", Toast.LENGTH_SHORT).show()
        }

        btnClose?.setOnClickListener {
            playingDialog?.dismiss()
        }

        playingDialog?.setOnDismissListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }

        playingDialog?.show()
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