package com.example.animewatchapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.animewatchapp.fragments.TrendingFragment
import com.example.animewatchapp.fragments.FavoriteFragment
import com.example.animewatchapp.fragments.HistoryFragment
import com.example.animewatchapp.fragments.LatestFragment

class MainViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LatestFragment()
            1 -> TrendingFragment()
            2 -> FavoriteFragment()
            3 -> HistoryFragment()
            else -> LatestFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }
}