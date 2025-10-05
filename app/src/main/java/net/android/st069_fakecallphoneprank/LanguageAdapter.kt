package net.android.st069_fakecallphoneprank

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.LanguageItem
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
            // Set selection state for background drawable
            binding.layoutLanguageItem.isSelected = item.isSelected

            // Optional: Change card background color
            if (item.isSelected) {
                binding.cardView.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.language_selected_bg)
                )
            } else {
                binding.cardView.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            }
        }
    }
}