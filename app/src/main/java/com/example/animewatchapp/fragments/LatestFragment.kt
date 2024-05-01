package com.example.animewatchapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animewatchapp.R
import com.example.animewatchapp.activities.AnimeDetailsActivity
import com.example.animewatchapp.adapter.AnimeAdapter
import com.example.animewatchapp.databinding.FragmentLatestBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.utils.Utils.isNetworkAvailable
import com.example.animewatchapp.viewmodels.LatestViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LatestFragment : Fragment() {
    private var _binding: FragmentLatestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LatestViewModel by viewModels()
    private lateinit var animeAdapter: AnimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatestBinding.inflate(inflater, container, false)
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
            binding.latestNestedScrollView.scrollTo(0, 0)
        }
        binding.btnReconnect.setOnClickListener {
            if(isNetworkAvailable(requireContext())) {
                viewModel.getLatestAnimeList()
            }
        }
        binding.latestNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY != 0) {
                binding.fabScrollToTop.show()
            } else {
                binding.fabScrollToTop.hide()
            }

            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                viewModel.getLatestAnimeList()
            }
        })
    }

    private fun init() {
        animeAdapter = AnimeAdapter(viewModel.animeUiState.value.animeList) {
            onAnimeClick(it)
        }
        binding.latestRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = animeAdapter
        }
    }

    private fun setUpViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isLoading) {
                    binding.latestLoading.visibility = View.VISIBLE
                    binding.latestError.visibility = View.GONE
                }
                else if(it.isError) {
                    binding.latestError.visibility = View.VISIBLE
                    binding.latestLoading.visibility = View.GONE
                }
                else {
                    binding.latestLoading.visibility = View.GONE
                    animeAdapter.submitData(it.animeList)
                }
            }
        }
    }

    private fun onAnimeClick(anime: Anime) {
        val intent = Intent(requireContext(), AnimeDetailsActivity::class.java)
        intent.putExtra("anime", anime)
        intent.putExtra("from", "latest")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}