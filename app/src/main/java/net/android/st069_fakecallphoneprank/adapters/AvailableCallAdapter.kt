package net.android.st069_fakecallphoneprank.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.api.ApiClient
import net.android.st069_fakecallphoneprank.data.model.FakeCallApi
import net.android.st069_fakecallphoneprank.databinding.ItemAvailableCallBinding

class AvailableCallAdapter(
    private val onItemClick: (FakeCallApi) -> Unit,
    private val onCallClick: (FakeCallApi) -> Unit
) : RecyclerView.Adapter<AvailableCallAdapter.ViewHolder>() {

    private var items = listOf<FakeCallApi>()

    fun submitList(newItems: List<FakeCallApi>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvailableCallBinding.inflate(
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
        private val binding: ItemAvailableCallBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(call: FakeCallApi) {
            // Set name and phone
            binding.tvName.text = call.name
            binding.tvPhone.text = call.phone

            // Load avatar from full URL
            val fullAvatarUrl = call.getFullAvatarUrl(ApiClient.MEDIA_BASE_URL)
            android.util.Log.d("AvailableCallAdapter", "Loading avatar for ${call.name}")
            android.util.Log.d("AvailableCallAdapter", "Avatar path from API: ${call.avatar}")
            android.util.Log.d("AvailableCallAdapter", "Full avatar URL: $fullAvatarUrl")

            Glide.with(binding.root.context)
                .load(fullAvatarUrl)
                .placeholder(R.drawable.ic_addavatar)
                .error(R.drawable.ic_addavatar)
                .circleCrop()
                .into(binding.ivAvatar)

            // Click listeners
            binding.root.setOnClickListener {
                onItemClick(call)
            }

            binding.btnCall.setOnClickListener {
                onCallClick(call)
            }
        }
    }
}
