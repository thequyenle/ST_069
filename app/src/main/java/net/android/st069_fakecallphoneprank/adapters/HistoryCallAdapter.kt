package net.android.st069_fakecallphoneprank.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.ItemHistoryCallBinding

class HistoryCallAdapter(
    private val showEditButton: Boolean = true,
    private val onEditClick: (FakeCall) -> Unit,
    private val onCallClick: (FakeCall) -> Unit
) : RecyclerView.Adapter<HistoryCallAdapter.ViewHolder>() {

    private var items = listOf<FakeCall>()

    fun submitList(newItems: List<FakeCall>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryCallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemHistoryCallBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fakeCall: FakeCall) {
            binding.tvName.text = fakeCall.name
            binding.tvPhone.text = fakeCall.phoneNumber

            if (!fakeCall.avatar.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(fakeCall.avatar)
                    .placeholder(R.drawable.ic_addavatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            }

            binding.btnEdit.visibility = if (showEditButton) View.VISIBLE else View.GONE

            binding.btnEdit.setOnClickListener {
                onEditClick(fakeCall)
            }

            binding.btnCall.setOnClickListener {
                onCallClick(fakeCall)
            }
        }
    }
}