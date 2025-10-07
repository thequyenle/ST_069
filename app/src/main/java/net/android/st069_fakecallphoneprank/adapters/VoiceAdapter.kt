package net.android.st069_fakecallphoneprank.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.model.Voice
import net.android.st069_fakecallphoneprank.databinding.ItemVoiceBinding

class VoiceAdapter(
    private val items: MutableList<Voice>,
    private val onItemClicked: (Voice) -> Unit
) : RecyclerView.Adapter<VoiceAdapter.VoiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceViewHolder {
        val binding = ItemVoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoiceViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        // Item click - show media player dialog
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun selectVoice(voice: Voice) {
        items.forEach { it.isSelected = false }
        voice.isSelected = true
        notifyDataSetChanged()
    }

    fun removeItem(position: Int): Voice {
        val removedVoice = items.removeAt(position)
        notifyItemRemoved(position)
        return removedVoice
    }

    fun restoreItem(voice: Voice, position: Int) {
        items.add(position, voice)
        notifyItemInserted(position)
    }

    fun isCustomVoice(position: Int): Boolean {
        return items[position].isCustom
    }

    inner class VoiceViewHolder(
        val binding: ItemVoiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(voice: Voice) {
            binding.tvVoiceName.text = voice.name
            binding.tvDuration.text = voice.duration

            // Always use ic_music icon
            binding.ivVoiceIcon.setImageResource(R.drawable.ic_music)

            // Set selection state
            val checkIcon = if (voice.isSelected) {
                R.drawable.ic_language_checked
            } else {
                R.drawable.ic_language_unchecked
            }
            binding.radioButton.setImageResource(checkIcon)

            // Set background based on selection
            binding.layoutVoiceItem.isSelected = voice.isSelected

            if (voice.isSelected) {
                binding.layoutVoiceItem.setBackgroundResource(R.drawable.frame_language_check)
            } else {
                binding.layoutVoiceItem.setBackgroundResource(R.drawable.frame_language_uncheck)
            }
        }
    }
}