package com.example.animewatchapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.animewatchapp.databinding.ItemEpisodeBinding
import com.google.android.material.card.MaterialCardView

class EpisodeAdapter(
    private var episodes: List<Int>,
    private var selectedEpisode: Int,
    private var onEpisodeClick: (Int) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    private var selectedCard: RelativeLayout? = null

    inner class EpisodeViewHolder(
        val binding: ItemEpisodeBinding,
        private var onEpisodeClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Int) {
            binding.episodeNumber.text = episode.toString()
            if(episode == selectedEpisode) {
                binding.episodeCardView.isSelected = true
                binding.episodeNumber.isChecked = true
                selectedCard = binding.episodeCardView
            }
            else {
                binding.episodeCardView.isSelected = false
                binding.episodeNumber.isChecked = false
            }
            binding.episodeCardView.setOnClickListener {
                selectedCard?.isSelected = false
                binding.episodeCardView.isSelected = true
                binding.episodeNumber.isChecked = true
                selectedCard = binding.episodeCardView
                onEpisodeClick(episode)
                selectedEpisode = episode
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding, onEpisodeClick)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount() = episodes.size
}