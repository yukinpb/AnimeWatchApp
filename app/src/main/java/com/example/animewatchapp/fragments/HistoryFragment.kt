package com.example.animewatchapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animewatchapp.activities.AnimeDetailsActivity
import com.example.animewatchapp.adapter.AnimeAdapter
import com.example.animewatchapp.adapter.AnimeHistoryAdapter
import com.example.animewatchapp.databinding.FragmentHistoryBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeHistory
import com.example.animewatchapp.viewmodels.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment: Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var animeHistoryAdapter: AnimeHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setUpViewModel()
        setUpEventListener()
    }

    private fun setUpEventListener() {
        binding.fabScrollToTop.setOnClickListener {
            binding.historyNestedScrollView.scrollTo(0, 0)
        }
    }

    private fun init() {
        animeHistoryAdapter = AnimeHistoryAdapter(viewModel.animeUiState.value.animeList) {
            onAnimeClick(it)
        }
        binding.historyRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = animeHistoryAdapter
        }
    }

    private fun setUpViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isLoading) {
                    binding.historyLoading.visibility = View.VISIBLE
                } else {
                    binding.historyLoading.visibility = View.GONE
                    animeHistoryAdapter.submitData(it.animeList)
                }
            }
        }
    }

    private fun onAnimeClick(animeHistory: AnimeHistory) {
        val intent = Intent(requireContext(), AnimeDetailsActivity::class.java)
        val animeLink = animeHistory.animeLink.replace("/anime/", "/watch/") + animeHistory.animeEpisode + "/"
        val anime = Anime(
            animeName = animeHistory.animeName,
            animeImageURL = animeHistory.animeImageURL,
            animeLink = animeLink
        )
        intent.putExtra("anime", anime)
        intent.putExtra("from", "history")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}