package net.android.st069_fakecallphoneprank.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.android.st069_fakecallphoneprank.adapters.AvailableCallAdapter
import net.android.st069_fakecallphoneprank.api.ApiClient
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.data.Resource
import net.android.st069_fakecallphoneprank.data.model.CallCategory
import net.android.st069_fakecallphoneprank.data.model.FakeCallApi
import net.android.st069_fakecallphoneprank.databinding.ActivityAvailableCallsApiBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper
import net.android.st069_fakecallphoneprank.viewmodel.AvailableCallsViewModel

class AvailableCallsApiActivity : BaseActivity() {

    private lateinit var binding: ActivityAvailableCallsApiBinding
    private val viewModel: AvailableCallsViewModel by viewModels()
    private lateinit var adapter: AvailableCallAdapter

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableCallsApiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = AvailableCallAdapter(
            onItemClick = { fakeCall ->
                onCallItemClick(fakeCall)
            },
            onCallClick = { fakeCall ->
                onCallButtonClick(fakeCall)
            }
        )

        binding.rvCalls.apply {
            layoutManager = LinearLayoutManager(this@AvailableCallsApiActivity)
            adapter = this@AvailableCallsApiActivity.adapter
        }
    }

    private fun setupTabs() {
        // Kid tab
        binding.btnKid.setOnClickListener {
            selectTab(CallCategory.KID)
        }

        // General tab
        binding.btnGeneral.setOnClickListener {
            selectTab(CallCategory.GENERAL)
        }

        // Set initial tab
        selectTab(CallCategory.KID)
    }

    private fun selectTab(category: CallCategory) {
        viewModel.setCategory(category)

        // Update tab UI
        when (category) {
            CallCategory.KID -> {
                binding.btnKid.isSelected = true
                binding.btnGeneral.isSelected = false
            }
            CallCategory.GENERAL -> {
                binding.btnKid.isSelected = false
                binding.btnGeneral.isSelected = true
            }
            else -> {}
        }
    }

    private fun setupObservers() {
        // Observe filtered calls
        viewModel.filteredCalls.observe(this) { calls ->
            if (calls.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvCalls.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvCalls.visibility = View.VISIBLE
                adapter.submitList(calls)
            }
        }

        // Observe loading state
        viewModel.fakeCalls.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = resource.message
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun onCallItemClick(fakeCall: FakeCallApi) {
        // Pass API call data to AddFakeCallActivity
        val intent = Intent(this, AddFakeCallActivity::class.java).apply {
            putExtra("API_CALL_NAME", fakeCall.name)
            putExtra("API_CALL_PHONE", fakeCall.phone)
            putExtra("API_CALL_AVATAR", fakeCall.getFullAvatarUrl(ApiClient.MEDIA_BASE_URL))
            putExtra("API_CALL_VOICE", fakeCall.getFullMp3Url(ApiClient.MEDIA_BASE_URL))
            putExtra("API_CALL_CATEGORY", fakeCall.category)
        }
        startActivity(intent)
    }

    private fun onCallButtonClick(fakeCall: FakeCallApi) {
        // Trigger immediate incoming call with this data
        val intent = Intent(this, net.android.st069_fakecallphoneprank.activity.IncomingCallActivity::class.java).apply {
            putExtra("FAKE_CALL_ID", -1L) // -1 for API calls (not saved in DB)
            putExtra("NAME", fakeCall.name)
            putExtra("PHONE_NUMBER", fakeCall.phone)
            putExtra("AVATAR", fakeCall.getFullAvatarUrl(ApiClient.MEDIA_BASE_URL))
            putExtra("VOICE_TYPE", fakeCall.getFullMp3Url(ApiClient.MEDIA_BASE_URL))
            putExtra("DEVICE_TYPE", "Pixel 5") // Default to Pixel 5
            putExtra("TALK_TIME", 30) // Default 30 seconds
        }
        startActivity(intent)
    }
}