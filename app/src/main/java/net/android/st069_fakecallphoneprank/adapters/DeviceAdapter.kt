package net.android.st069_fakecallphoneprank.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.model.Device

class DeviceAdapter(
    private var devices: MutableList<Device>,
    private val onDeviceClick: (Device) -> Unit,
    private val onWatchClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    private var selectedPosition = -1

    init {
        // Find initially selected device
        selectedPosition = devices.indexOfFirst { it.isSelected }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], position == selectedPosition)
    }

    override fun getItemCount() = devices.size

    fun updateSelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position

        // Update the list
        devices = devices.mapIndexed { index, device ->
            device.copy(isSelected = index == position)
        }.toMutableList()

        // Notify changes
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        notifyItemChanged(position)
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardDevice: View = itemView.findViewById(R.id.cardDevice)
        private val ivDevicePreview: ImageView = itemView.findViewById(R.id.ivDevicePreview)
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val ivCheckbox: ImageView = itemView.findViewById(R.id.ivCheckbox)
        private val ivWatch: ImageView = itemView.findViewById(R.id.watch)

        fun bind(device: Device, isSelected: Boolean) {
            // Set device preview image
            ivDevicePreview.setImageResource(device.iconRes)

            // Set device name
            tvDeviceName.text = device.name

            // Update selection state
            if (isSelected) {
                // Selected state - change background drawable
                cardDevice.setBackgroundResource(R.drawable.bg_device_item_selected)

                // Show selected checkbox
                ivCheckbox.setImageResource(R.drawable.ic_selected_devices)

            } else {
                // Unselected state - change background drawable
                cardDevice.setBackgroundResource(R.drawable.bg_device_item)

                // Show unselected checkbox
                ivCheckbox.setImageResource(R.drawable.ic_un_selected_devices)
            }

            // Click listener for selecting device
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    updateSelection(position)
                    onDeviceClick(device)
                }
            }

            // Click listener for watch icon - preview device
            ivWatch.setOnClickListener {
                onWatchClick(device)
            }
        }
    }
}