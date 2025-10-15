package net.android.st069_fakecallphoneprank.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.model.Device

class DeviceAdapter(
    private var devices: MutableList<Device>,
    private val onDeviceClick: (Device) -> Unit
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

        // Cast to MaterialCardView instead of CardView
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val ivDevicePreview: ImageView = itemView.findViewById(R.id.ivDevicePreview)
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        private val ivCheckbox: ImageView = itemView.findViewById(R.id.ivCheckbox)

        fun bind(device: Device, isSelected: Boolean) {
            // Set device preview image
            ivDevicePreview.setImageResource(device.iconRes)

            // Set device name
            tvDeviceName.text = device.name

            // Update selection state
            if (isSelected) {
                // Selected state
                cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                cardView.strokeColor = Color.parseColor("#2196F3")
                cardView.strokeWidth = 8

                // Show selected checkbox
                ivCheckbox.setImageResource(R.drawable.ic_selected_devices)

            } else {
                // Unselected state
                cardView.setCardBackgroundColor(Color.WHITE)
                cardView.strokeColor = Color.parseColor("#E0E0E0")
                cardView.strokeWidth = 4

                // Show unselected checkbox
                ivCheckbox.setImageResource(R.drawable.ic_un_selected_devices)
            }

            // Click listener
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    updateSelection(position)
                    onDeviceClick(device)
                }
            }
        }
    }
}