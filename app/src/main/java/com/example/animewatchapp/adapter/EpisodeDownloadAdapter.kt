package com.example.animewatchapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.animewatchapp.databinding.ItemEpisodeDownloadBinding

class EpisodeDownloadAdapter(
    private var episodes: List<Int>,
    private var canDownload: (Int) -> Boolean,
    private var onEpisodeDownloadClick: (Int, ItemEpisodeDownloadBinding) -> Unit
) : RecyclerView.Adapter<EpisodeDownloadAdapter.EpisodeDownloadViewHolder>() {
    private var isEnableDownload = true

    inner class EpisodeDownloadViewHolder(
        val binding: ItemEpisodeDownloadBinding,
        private var onEpisodeDownloadClick: (Int, ItemEpisodeDownloadBinding) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Int) {
            binding.episodeNumber.text = episode.toString()
            if(!canDownload(episode)) {
                binding.episodeDownloadProgress.visibility = View.GONE
                binding.episodeDownloadProgress.progress = 0
                binding.episodeCardView.isSelected = true
                binding.episodeNumber.isChecked = true
                binding.episodeCardView.isClickable = false
            }
            else {
                binding.episodeCardView.setOnClickListener {
                    Log.d("EpisodeDownloadAdapter", "Episode $episode clicked ${isEnableDownload}")
                    if(!isEnableDownload) return@setOnClickListener
                    binding.episodeDownloadProgress.visibility = View.VISIBLE
                    isEnableDownload = false
                    onEpisodeDownloadClick(episode, binding)
                }
            }
        }
    }

    fun enableDownload() {
        isEnableDownload = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeDownloadViewHolder {
        val binding = ItemEpisodeDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeDownloadViewHolder(binding, onEpisodeDownloadClick)
    }

    override fun onBindViewHolder(holder: EpisodeDownloadViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount() = episodes.size
}