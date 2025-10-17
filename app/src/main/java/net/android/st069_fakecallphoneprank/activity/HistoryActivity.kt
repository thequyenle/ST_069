package net.android.st069_fakecallphoneprank.activity

import android.content.Context
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import net.android.st069_fakecallphoneprank.R
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
        setupClickListeners()
    }

    private fun setupViewPager() {
        val adapter = HistoryPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.custom)
                1 -> getString(R.string.available)
                else -> ""
            }
        }.attach()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}