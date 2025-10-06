package net.android.st069_fakecallphoneprank.intro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.android.st069_fakecallphoneprank.databinding.ItemIntroPageBinding

class IntroAdapter(
    private val pages: List<IntroPage>
) : RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val binding = ItemIntroPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IntroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    inner class IntroViewHolder(
        private val binding: ItemIntroPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: IntroPage) {
            binding.ivIntro.setImageResource(page.imageRes)
            binding.tvTitle.text = page.title
            // No description - removed
        }
    }
}