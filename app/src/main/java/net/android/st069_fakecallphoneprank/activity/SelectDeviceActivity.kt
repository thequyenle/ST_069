package net.android.st069_fakecallphoneprank.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.adapters.DeviceAdapter
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.data.model.Device
import net.android.st069_fakecallphoneprank.databinding.ActivitySelectDeviceBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class SelectDeviceActivity : BaseActivity() {

    private lateinit var binding: ActivitySelectDeviceBinding
    private lateinit var deviceAdapter: DeviceAdapter
    private var selectedDevice: Device? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDeviceList()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupDeviceList() {
        // Get current device from intent
        val currentDevice = intent.getStringExtra("CURRENT_DEVICE")

        // Create device list
        val devices = getDeviceList(currentDevice)

        // Setup adapter
        deviceAdapter = DeviceAdapter(
            devices.toMutableList(),
            onDeviceClick = { device ->
                selectedDevice = device
                updateButtonState()
            },
            onWatchClick = { device ->
                // Preview device - open IncomingCallActivity in preview mode
                previewDevice(device)
            }
        )

        // Setup RecyclerView with 2 columns grid
        binding.rvDevices.apply {
            layoutManager = GridLayoutManager(this@SelectDeviceActivity, 2)
            adapter = deviceAdapter
            setHasFixedSize(true)
        }

        // Set initial selection if exists
        selectedDevice = devices.find { it.isSelected }
        updateButtonState()
    }

    private fun previewDevice(device: Device) {
        // Show device watch preview dialog
        showWatchPreviewDialog(device)
    }

    private fun showWatchPreviewDialog(device: Device) {
        // Determine watch image based on device type
        val watchImage = when (device.name) {
            "Oppo" -> R.drawable.watch_oppo
            "Pixel 5" -> R.drawable.watch_pixel5
            else -> R.drawable.watch_oppo
        }

        // Create fullscreen dialog to show watch image
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_watch_preview)

        val ivWatchPreview = dialog.findViewById<android.widget.ImageView>(R.id.ivWatchPreview)
        val btnBack = dialog.findViewById<android.widget.ImageButton>(R.id.btnBack)

        ivWatchPreview.setImageResource(watchImage)

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getDeviceList(currentDevice: String?): List<Device> {
        return listOf(
            Device(
                id = 1,
                name = "Oppo",
                iconRes = R.drawable.oppo,
                isSelected = currentDevice == "Oppo"
            ),
            Device(
                id = 2,
                name = "Pixel 5",
                iconRes = R.drawable.pixel5,
                isSelected = currentDevice == "Pixel 5"
            )
        )
    }

    private fun setupButtons() {
        binding.btnDone.setOnClickListener {
            selectedDevice?.let { device ->
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_DEVICE", device.name)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun updateButtonState() {
        // Enable/disable Done button based on selection
        binding.btnDone.isEnabled = selectedDevice != null
        binding.btnDone.alpha = if (selectedDevice != null) 1.0f else 0.5f
    }
}