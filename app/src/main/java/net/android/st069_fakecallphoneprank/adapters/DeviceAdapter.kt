package net.android.st069_fakecallphoneprank.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
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

        // Try to find views with various possible IDs
        private val rootLayout: View? = itemView.findViewById(R.id.rootLayout)
            ?: itemView.findViewById(R.id.cardView)
            ?: itemView.findViewById(R.id.layoutRoot)

        private val tvDeviceName: TextView? = itemView.findViewById(R.id.tvDeviceName)
            ?: itemView.findViewById(R.id.tvName)
            ?: itemView.findViewById(R.id.tvTitle)
            ?: itemView.findViewById(R.id.textViewName)

        private val ivDeviceIcon: ImageView? = itemView.findViewById(R.id.ivDeviceIcon)
            ?: itemView.findViewById(R.id.ivIcon)
            ?: itemView.findViewById(R.id.ivDevice)
            ?: itemView.findViewById(R.id.imageViewIcon)

        private val ivSelected: ImageView? = itemView.findViewById(R.id.ivSelected)
            ?: itemView.findViewById(R.id.ivCheck)
            ?: itemView.findViewById(R.id.ivCheckmark)
            ?: itemView.findViewById(R.id.imageViewCheck)

        fun bind(device: Device, isSelected: Boolean) {
            // Set device name
            tvDeviceName?.text = device.name

            // Set device icon
            ivDeviceIcon?.setImageResource(device.iconRes)

            // Show/hide selection indicator
            ivSelected?.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Update visual appearance for selection
            if (isSelected) {
                // Try to update background
                try {
                    when {
                        rootLayout is CardView -> {
                            rootLayout.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                            rootLayout.strokeColor = Color.parseColor("#2196F3")
                            rootLayout.strokeWidth = 4
                        }
                        rootLayout is ConstraintLayout -> {
                            rootLayout.setBackgroundResource(R.drawable.bg_device_selected)
                        }
                        else -> {
                            itemView.setBackgroundResource(R.drawable.bg_device_selected)
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to simple alpha change
                    itemView.alpha = 1.0f
                }

                // Change icon tint to blue when selected
                ivDeviceIcon?.setColorFilter(Color.parseColor("#2196F3"))

                // Make text bold when selected
                tvDeviceName?.setTextColor(Color.parseColor("#2196F3"))
                tvDeviceName?.typeface = android.graphics.Typeface.DEFAULT_BOLD

            } else {
                // Unselected state
                try {
                    when {
                        rootLayout is CardView -> {
                            rootLayout.setCardBackgroundColor(Color.WHITE)
                            rootLayout.strokeColor = Color.parseColor("#E0E0E0")
                            rootLayout.strokeWidth = 2
                        }
                        rootLayout is ConstraintLayout -> {
                            rootLayout.setBackgroundResource(R.drawable.bg_device_unselected)
                        }
                        else -> {
                            itemView.setBackgroundResource(R.drawable.bg_device_unselected)
                        }
                    }
                } catch (e: Exception) {
                    itemView.alpha = 0.7f
                }

                // Default icon color
                ivDeviceIcon?.clearColorFilter()

                // Default text style
                tvDeviceName?.setTextColor(Color.parseColor("#2F2F2F"))
                tvDeviceName?.typeface = android.graphics.Typeface.DEFAULT
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