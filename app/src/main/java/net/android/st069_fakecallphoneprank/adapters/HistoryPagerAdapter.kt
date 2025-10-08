package net.android.st069_fakecallphoneprank.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.android.st069_fakecallphoneprank.fragments.AvailableCallsFragment
import net.android.st069_fakecallphoneprank.fragments.CustomCallsFragment

class HistoryPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CustomCallsFragment()
            1 -> AvailableCallsFragment()
            else -> CustomCallsFragment()
        }
    }
}