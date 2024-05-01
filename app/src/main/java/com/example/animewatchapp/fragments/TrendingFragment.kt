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
import com.example.animewatchapp.databinding.FragmentTrendingBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.utils.Utils
import com.example.animewatchapp.utils.Utils.isNetworkAvailable
import com.example.animewatchapp.viewmodels.TrendingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrendingFragment : Fragment() {

    private var _binding: FragmentTrendingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TrendingViewModel by viewModels()
    private lateinit var animeAdapter: AnimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendingBinding.inflate(inflater, container, false)
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
            binding.trendingNestedScrollView.scrollTo(0, 0)
        }
        binding.btnReconnect.setOnClickListener {
            if(isNetworkAvailable(requireContext())) {
                viewModel.getTrendingAnimeList()
            }
        }
    }

    private fun init() {
        animeAdapter = AnimeAdapter(viewModel.animeUiState.value.animeList) {
            onAnimeClick(it)
        }
        binding.trendingRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = animeAdapter
        }
    }

    private fun setUpViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isLoading) {
                    binding.trendingLoading.visibility = View.VISIBLE
                    binding.trendingError.visibility = View.GONE
                }
                else if(it.isError) {
                    binding.trendingError.visibility = View.VISIBLE
                    binding.trendingLoading.visibility = View.GONE
                }
                else {
                    binding.trendingLoading.visibility = View.GONE
                    animeAdapter.submitData(it.animeList)
                }
            }
        }
    }

    private fun onAnimeClick(anime: Anime) {
        val intent = Intent(requireContext(), AnimeDetailsActivity::class.java)
        intent.putExtra("anime", anime)
        intent.putExtra("from", "trending")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}