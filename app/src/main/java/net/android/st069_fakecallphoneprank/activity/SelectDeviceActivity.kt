package net.android.st069_fakecallphoneprank.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.adapters.DeviceAdapter
import net.android.st069_fakecallphoneprank.data.model.Device
import net.android.st069_fakecallphoneprank.databinding.ActivitySelectDeviceBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class SelectDeviceActivity : AppCompatActivity() {

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
        deviceAdapter = DeviceAdapter(devices.toMutableList()) { device ->
            selectedDevice = device
            updateButtonState()
        }

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

    private fun getDeviceList(currentDevice: String?): List<Device> {
        return listOf(
            Device(
                id = 1,
                name = "Oppo",
                iconRes = R.drawable.ic_oppo_select_device,
                isSelected = currentDevice == "Oppo"
            ),
            Device(
                id = 2,
                name = "Pixel 5",
                iconRes = R.drawable.ic_pixel5_select_device,
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