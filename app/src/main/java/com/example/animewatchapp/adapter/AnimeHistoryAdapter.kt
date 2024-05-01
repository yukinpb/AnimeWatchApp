package com.example.animewatchapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.animewatchapp.R
import com.example.animewatchapp.databinding.ItemAnimeBinding
import com.example.animewatchapp.model.AnimeHistory

class AnimeHistoryAdapter(
    private var animeList: MutableList<AnimeHistory>,
    private val onAnimeHistoryClick: (AnimeHistory) -> Unit) : RecyclerView.Adapter<AnimeHistoryAdapter.AnimeHistoryViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(newList: List<AnimeHistory>) {
        animeList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeHistoryViewHolder {
        val binding = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeHistoryViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount() = animeList.size

    inner class AnimeHistoryViewHolder(
        private val binding: ItemAnimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: AnimeHistory) {
            binding.apply {
                animeImage.load(anime.animeImageURL) {
                    crossfade(true)
                    placeholder(R.drawable.loading_img)
                    error(R.drawable.ic_broken_image)
                }
                animeName.text = anime.animeName
                episodeCount.visibility = View.VISIBLE
                "EP ${anime.animeEpisode}".also { episodeCount.text = it }
            }

            itemView.setOnClickListener {
                onAnimeHistoryClick(anime)
            }
        }
    }
}