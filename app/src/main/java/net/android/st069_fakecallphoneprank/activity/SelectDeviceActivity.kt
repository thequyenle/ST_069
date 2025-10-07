package net.android.st069_fakecallphoneprank.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.adapters.DeviceAdapter
import net.android.st069_fakecallphoneprank.data.model.Device
import net.android.st069_fakecallphoneprank.databinding.ActivitySelectDeviceBinding

class SelectDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectDeviceBinding
    private lateinit var adapter: DeviceAdapter
    private lateinit var deviceList: MutableList<Device>
    private var selectedDevice: Device? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDeviceList()
        setupRecyclerView()
        setupClickListeners()

        // Get currently selected device
        val currentDevice = intent.getStringExtra("CURRENT_DEVICE")
        currentDevice?.let { device ->
            deviceList.find { it.name == device }?.let {
                it.isSelected = true
                selectedDevice = it
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupDeviceList() {
        deviceList = mutableListOf(
            Device("1", "Pixel 5", R.drawable.device_pixel5, false),
            Device("2", "Oppo", R.drawable.device_oppo, false),

        )
    }

    private fun setupRecyclerView() {
        adapter = DeviceAdapter(
            deviceList,
            onPreviewClicked = { device ->
                showPreviewDialog(device)
            },
            onDeviceSelected = { device ->
                selectedDevice = device
                adapter.selectDevice(device)
            }
        )

        binding.rvDevices.layoutManager = GridLayoutManager(this, 2)
        binding.rvDevices.adapter = adapter
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Done button
        binding.btnDone.setOnClickListener {
            selectedDevice?.let { device ->
                val resultIntent = Intent().apply {
                    putExtra("SELECTED_DEVICE", device.name)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Please select a device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPreviewDialog(device: Device) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_device_preview)

        val ivFullPreview = dialog.findViewById<ImageView>(R.id.ivFullPreview)
        val tvDeviceName = dialog.findViewById<TextView>(R.id.tvDeviceName)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

        ivFullPreview.setImageResource(device.previewImage)
        tvDeviceName.text = device.name

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}