package net.android.st069_fakecallphoneprank.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.model.Device
import net.android.st069_fakecallphoneprank.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val items: MutableList<Device>,
    private val onPreviewClicked: (Device) -> Unit,
    private val onDeviceSelected: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun selectDevice(device: Device) {
        items.forEach { it.isSelected = false }
        device.isSelected = true
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.ivDevicePreview.setImageResource(device.previewImage)

            // Set checkbox based on selection
            val checkIcon = if (device.isSelected) {
                R.drawable.ic_language_checked
            } else {
                R.drawable.ic_language_unchecked
            }
            binding.ivCheckbox.setImageResource(checkIcon)

            // Preview button click


            // Item click to select
            binding.root.setOnClickListener {
                onDeviceSelected(device)
            }
        }
    }
}