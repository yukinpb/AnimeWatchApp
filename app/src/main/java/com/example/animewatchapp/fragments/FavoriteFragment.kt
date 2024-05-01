package com.example.animewatchapp.fragments

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animewatchapp.activities.AnimeDetailsActivity
import com.example.animewatchapp.adapter.AnimeAdapter
import com.example.animewatchapp.databinding.FragmentFavoriteBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.viewmodels.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var animeAdapter: AnimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
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
            binding.favoriteNestedScrollView.scrollTo(0, 0)
        }
    }

    private fun init() {
        animeAdapter = AnimeAdapter(viewModel.animeUiState.value.animeList) {
            onAnimeClick(it)
        }
        binding.favoriteRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = animeAdapter
        }
    }

    private fun setUpViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isLoading) {
                    binding.favoriteLoading.visibility = View.VISIBLE
                } else {
                    binding.favoriteLoading.visibility = View.GONE
                    animeAdapter.submitData(it.animeList)
                }
            }
        }
    }

    private fun onAnimeClick(anime: Anime) {
        val intent = Intent(requireContext(), AnimeDetailsActivity::class.java)
        intent.putExtra("anime", anime)
        intent.putExtra("from", "favorite")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}