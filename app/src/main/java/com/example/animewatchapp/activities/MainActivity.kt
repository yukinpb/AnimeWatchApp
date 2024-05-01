package com.example.animewatchapp.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.animewatchapp.R
import com.example.animewatchapp.adapter.MainViewPagerAdapter
import com.example.animewatchapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        lockScreenOrientation()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val adapter = MainViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavigation.menu.getItem(position).isChecked = true
                when (position) {
                    0 -> supportActionBar?.title = "Latest"
                    1 -> supportActionBar?.title = "Trending"
                    2 -> supportActionBar?.title = "Favorite"
                    3 -> supportActionBar?.title = "History"
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_latest -> binding.viewPager.setCurrentItem(0, false)
                R.id.navigation_trending -> binding.viewPager.setCurrentItem(1, false)
                R.id.navigation_favorite -> binding.viewPager.setCurrentItem(2, false)
                R.id.navigation_history -> binding.viewPager.setCurrentItem(3, false)
            }
            true
        }
    }

    private fun lockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this, SearchAnimeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_download -> {
                val intent = Intent(this, AnimeDownloadActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}