package com.example.animewatchapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.animewatchapp.R
import com.example.animewatchapp.databinding.ItemAnimeBinding
import com.example.animewatchapp.model.Anime

class AnimeAdapter(
    private var animeList: MutableList<Anime>,
    private val onAnimeClick: (Anime) -> Unit) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun submitData(newList: List<Anime>) {
        animeList = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeViewHolder(binding, onAnimeClick)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(animeList[position])
    }

    override fun getItemCount() = animeList.size

    inner class AnimeViewHolder(
        private val binding: ItemAnimeBinding,
        private val onAnimeClick: (Anime) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: Anime) {
            binding.apply {
                animeImage.load(anime.animeImageURL) {
                    crossfade(true)
                    placeholder(R.drawable.loading_img)
                    error(R.drawable.ic_broken_image)
                }
                animeName.text = anime.animeName
            }

            itemView.setOnClickListener {
                onAnimeClick(anime)
            }
        }
    }
}