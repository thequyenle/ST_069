package net.android.st069_fakecallphoneprank.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.ActivityAvailableCallsBinding
import net.android.st069_fakecallphoneprank.databinding.ItemFakeCallBinding
import net.android.st069_fakecallphoneprank.utils.DateTimeUtils
import net.android.st069_fakecallphoneprank.utils.SetTimeUtils
import net.android.st069_fakecallphoneprank.utils.TalkTimeUtils
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class AvailableCallsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailableCallsBinding
    private val viewModel: FakeCallViewModel by viewModels()
    private lateinit var adapter: FakeCallAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableCallsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = FakeCallAdapter(
            onDeleteClick = { fakeCall ->
                showDeleteConfirmation(fakeCall)
            },
            onItemClick = { fakeCall ->
                // TODO: Show details or edit
                showCallDetails(fakeCall)
            }
        )

        binding.rvFakeCalls.layoutManager = LinearLayoutManager(this)
        binding.rvFakeCalls.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allFakeCalls.observe(this) { fakeCalls ->
            if (fakeCalls.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvFakeCalls.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvFakeCalls.visibility = View.VISIBLE
                adapter.submitList(fakeCalls)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDeleteConfirmation(fakeCall: FakeCall) {
        AlertDialog.Builder(this)
            .setTitle("Delete Fake Call")
            .setMessage("Are you sure you want to delete this fake call?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(fakeCall)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCallDetails(fakeCall: FakeCall) {
        val message = buildString {
            append("Name: ${fakeCall.name}\n")
            append("Phone: ${fakeCall.phoneNumber}\n")
            append("Voice: ${fakeCall.voiceType ?: "Default"}\n")
            append("Device: ${fakeCall.deviceType ?: "Default"}\n")
            append("Set Time: ${SetTimeUtils.formatSetTime(fakeCall.setTime)}\n")
            append("Talk Time: ${TalkTimeUtils.formatTalkTime(fakeCall.talkTime)}\n")
            append("Scheduled: ${DateTimeUtils.formatDateTime(fakeCall.scheduledTime)}\n")
            append("Status: ${if (fakeCall.isActive) "Active" else "Inactive"}")
        }

        AlertDialog.Builder(this)
            .setTitle("Fake Call Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

class FakeCallAdapter(
    private val onDeleteClick: (FakeCall) -> Unit,
    private val onItemClick: (FakeCall) -> Unit
) : RecyclerView.Adapter<FakeCallAdapter.ViewHolder>() {

    private var items = listOf<FakeCall>()

    fun submitList(newItems: List<FakeCall>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFakeCallBinding.inflate(
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
        private val binding: ItemFakeCallBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fakeCall: FakeCall) {
            binding.tvName.text = fakeCall.name
            binding.tvPhone.text = fakeCall.phoneNumber
            binding.tvScheduledTime.text = DateTimeUtils.formatDateTime(fakeCall.scheduledTime)

            // Load avatar
            if (!fakeCall.avatar.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(fakeCall.avatar)
                    .placeholder(R.drawable.ic_addavatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_addavatar)
            }

            // Click listeners
            binding.root.setOnClickListener {
                onItemClick(fakeCall)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(fakeCall)
            }
        }
    }
}