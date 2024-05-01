package com.example.animewatchapp.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animewatchapp.adapter.AnimeAdapter
import com.example.animewatchapp.databinding.ActivitySearchAnimeBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.utils.Utils.isNetworkAvailable
import com.example.animewatchapp.viewmodels.SearchAnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchAnimeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchAnimeBinding
    private val viewModel: SearchAnimeViewModel by viewModels()
    private lateinit var animeAdapter: AnimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lockScreenOrientation()
        init()
        setUpViewModel()
        setUpEventListener()
    }

    private fun setUpEventListener() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                searchAnime()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnReconnect.setOnClickListener {
            if(isNetworkAvailable(this)) {
                searchAnime()
            }
        }

        binding.searchIcon.setOnClickListener {
            searchAnime()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setUpViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.animeUiState.collectLatest {
                    if(it.isLoading) {
                        binding.searchLoading.visibility = View.VISIBLE
                        binding.searchError.visibility = View.GONE
                    }
                    else if(it.isError) {
                        binding.searchError.visibility = View.VISIBLE
                        binding.searchLoading.visibility = View.GONE
                    }
                    else {
                        binding.searchLoading.visibility = View.GONE
                        animeAdapter.submitData(it.animeList)
                    }
                }
            }
        }
    }

    private fun lockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun init() {
        binding.searchEditText.setText(viewModel.animeUiState.value.searchKeyword)
        animeAdapter = AnimeAdapter(mutableListOf()) {
            onAnimeClick(it)
        }
        binding.searchRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = animeAdapter
        }
    }

    private fun searchAnime() {
        val keyword = binding.searchEditText.text.toString()
        viewModel.animeUiState.value.searchKeyword = keyword
        viewModel.searchAnime()

        hideKeyboard()
    }

    private fun onAnimeClick(anime: Anime) {
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        intent.putExtra("anime", anime)
        intent.putExtra("from", "search")
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

}