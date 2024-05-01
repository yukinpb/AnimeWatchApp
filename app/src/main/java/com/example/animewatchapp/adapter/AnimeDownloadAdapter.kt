package com.example.animewatchapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.animewatchapp.R
import com.example.animewatchapp.databinding.ItemAnimeBinding
import com.example.animewatchapp.model.AnimeDownload

class AnimeDownloadAdapter(
    private var animeList: MutableList<AnimeDownload>,
    private val onAnimeDownloadClick: (AnimeDownload) -> Unit,
    private val onAnimeDownloadHold: (AnimeDownload) -> Unit) : RecyclerView.Adapter<AnimeDownloadAdapter.AnimeDownloadViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(newList: List<AnimeDownload>) {
        animeList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeDownloadViewHolder {
        val binding = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeDownloadViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount() = animeList.size

    inner class AnimeDownloadViewHolder(
        private val binding: ItemAnimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: AnimeDownload) {
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
                onAnimeDownloadClick(anime)
            }

            itemView.setOnLongClickListener {
                onAnimeDownloadHold(anime)
                true
            }
        }
    }
}