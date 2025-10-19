package net.android.st069_fakecallphoneprank.activity

import android.content.Context
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import net.android.st069_fakecallphoneprank.adapters.HistoryPagerAdapter
import net.android.st069_fakecallphoneprank.base.BaseActivity
import net.android.st069_fakecallphoneprank.databinding.ActivityHistoryBinding
import net.android.st069_fakecallphoneprank.utils.LocaleHelper

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupTabs()
        setupClickListeners()
    }

    private fun setupViewPager() {
        val adapter = HistoryPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Disable user swipe
        binding.viewPager.isUserInputEnabled = true

        // Listen to page changes to update tab selection
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabSelection(position)
            }
        })
    }

    private fun setupTabs() {
        // Custom tab
        binding.btnCustom.setOnClickListener {
            binding.viewPager.currentItem = 0
        }

        // Available tab
        binding.btnAvailable.setOnClickListener {
            binding.viewPager.currentItem = 1
        }

        // Set initial tab
        updateTabSelection(0)
    }

    private fun updateTabSelection(position: Int) {
        when (position) {
            0 -> {
                binding.btnCustom.isSelected = true
                binding.btnAvailable.isSelected = false
            }
            1 -> {
                binding.btnCustom.isSelected = false
                binding.btnAvailable.isSelected = true
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}