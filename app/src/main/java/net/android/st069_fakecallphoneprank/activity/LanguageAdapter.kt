package net.android.st069_fakecallphoneprank.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val items: MutableList<LanguageItem>,
    private val onItemSelected: (LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            // Unselect all items
            items.forEachIndexed { index, lang ->
                lang.isSelected = index == position
            }
            notifyDataSetChanged()
            onItemSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem) {
            binding.tvLanguage.text = item.name
            binding.ivFlag.setImageResource(item.flagResId)

            // Set check/uncheck icon based on selection state
            val checkIcon = if (item.isSelected) {
                R.drawable.ic_language_checked  // Blue checkmark icon
            } else {
                R.drawable.ic_language_unchecked  // Empty circle icon
            }

            binding.radioButton.setImageResource(checkIcon)

            // Set selection state for background drawable
            binding.layoutLanguageItem.isSelected = item.isSelected

            // Change background based on selection - use shadow version for selected
            if (item.isSelected) {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.frame_language_check_shadow)
            } else {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.frame_language_uncheck)
            }
        }
    }
}