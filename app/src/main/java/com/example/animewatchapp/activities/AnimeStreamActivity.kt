package com.example.animewatchapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerControlView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animewatchapp.R
import com.example.animewatchapp.adapter.EpisodeAdapter
import com.example.animewatchapp.databinding.ActivityAnimeStreamBinding
import com.example.animewatchapp.databinding.DialogEpisodeSearchBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.model.AnimeStream
import com.example.animewatchapp.utils.Utils
import com.example.animewatchapp.viewmodels.AnimeStreamViewModel
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.CookieHandler
import java.net.CookieManager

@Suppress("DEPRECATION")
@UnstableApi
@AndroidEntryPoint
class AnimeStreamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnimeStreamBinding
    private var animeStream: AnimeStream = AnimeStream()
    private val viewModel: AnimeStreamViewModel by viewModels()
    private lateinit var dialog: Dialog
    private lateinit var playerControlView: PlayerControlView

    // Offline
    private var animeDownload: AnimeDownload = AnimeDownload()
    private lateinit var from: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getExtras()
        hideAndroidUI()

        if(from == "download") {
            setUpPlayDownloadVideo()
        }
        else {
            setUpViewModel()
            init()
            updateAllTitle()
            setUpEventListener()
        }

    }


    private fun setUpPlayDownloadVideo() {
        dialog = Dialog(this)

        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)

        binding.animePlayerView.keepScreenOn = true
        binding.animePlayerView.player = viewModel.player

        playerControlView = binding.animePlayerView.findViewById(androidx.media3.ui.R.id.exo_controller)

        if(!viewModel.animeUiState.value.isPlaying) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    viewModel.playDownloadAnime(animeDownload.animeLink)
                }
            }
        }
        else {
            viewModel.player.play()
        }

        playerControlView.findViewById<TextView>(R.id.c_exo_episode_title).text =
            getString(R.string.ep, animeDownload.animeEpisode)
        playerControlView.findViewById<TextView>(R.id.c_exo_title).text = buildString {
            append(animeDownload.animeEpisode.toString())
            append(" : ")
            append(animeDownload.animeName)
        }
        playerControlView.findViewById<ImageView>(R.id.c_exo_back_button).setOnClickListener {
            finish()
        }

        playerControlView.findViewById<ImageView>(R.id.c_exo_rotate_button).setOnClickListener {
            val currentOrientation = resources.configuration.orientation
            requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    private fun setUpEventListener() {
        binding.btnReconnect.setOnClickListener {
            if(Utils.isNetworkAvailable(this)) {
                if(!viewModel.animeUiState.value.isPlaying) {
                    playAnime()
                }
                else {
                    viewModel.player.play()
                }
            }
        }

        playerControlView.findViewById<ImageView>(R.id.c_exo_back_button).setOnClickListener {
            onFinishWatch()
        }

        playerControlView.findViewById<ImageView>(R.id.c_exo_rotate_button).setOnClickListener {
            val currentOrientation = resources.configuration.orientation
            requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        playerControlView.findViewById<MaterialCardView>(R.id.c_exo_episode_card).setOnClickListener {
            showEpisodeDialog()
        }

        playerControlView.findViewById<View>(R.id.c_exo_next).setOnClickListener {
            if(checkHasNextEpisode()) {
                viewModel.player.pause()
                animeStream.animeEpisode++
                updateAllTitle()
                playAnime()
            }
            else {
                Toast.makeText(this, "No more episodes", Toast.LENGTH_SHORT).show()
            }
        }

        playerControlView.findViewById<View>(R.id.c_exo_prev).setOnClickListener {
            if(checkHasPreviousEpisode()) {
                viewModel.player.pause()
                animeStream.animeEpisode--
                updateAllTitle()
                playAnime()
            }
            else {
                Toast.makeText(this, "No previous episodes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpViewModel() {
        lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isNextEp) {
                    if(checkHasNextEpisode()) {
                        viewModel.player.pause()
                        animeStream.animeEpisode++
                        updateAllTitle()
                        playAnime()
                    }
                    else {
                        Toast.makeText(this@AnimeStreamActivity, "No more episodes", Toast.LENGTH_SHORT).show()
                        viewModel.animeUiState.value.isNextEp = false
                    }
                }
                if(it.isLoading && it.isPlaying) {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.animeStreamError.visibility = View.GONE
                    binding.loadingAnimeLayout.visibility = View.GONE
                }
                else if(it.isLoading) {
                    binding.loadingAnimeLayout.visibility = View.VISIBLE
                    binding.loadingLayout.visibility = View.GONE
                    binding.animePlayerView.visibility = View.GONE
                    binding.animeStreamError.visibility = View.GONE
                }
                else if(it.isError) {
                    binding.animeStreamError.visibility = View.VISIBLE
                    binding.loadingAnimeLayout.visibility = View.GONE
                    binding.loadingLayout.visibility = View.GONE
                    binding.animePlayerView.visibility = View.GONE
                }
                else {
                    binding.animePlayerView.visibility = View.VISIBLE
                    binding.animeStreamError.visibility = View.GONE
                    binding.loadingAnimeLayout.visibility = View.GONE
                    binding.loadingLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun init() {
        dialog = Dialog(this)

        viewModel.checkHistoryAnime(animeStream.animeLink)

        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)

        binding.animePlayerView.keepScreenOn = true
        binding.animePlayerView.player = viewModel.player

        playerControlView = binding.animePlayerView.findViewById(androidx.media3.ui.R.id.exo_controller)

        if(!viewModel.animeUiState.value.isPlaying) {
            playAnime()
        }
        else {
            viewModel.player.play()
        }
    }

    private fun getExtras() {
        from = intent.extras?.getString("from") ?: ""
        if(from == "details")
            animeStream = intent.extras?.getSerializable("animeStream") as AnimeStream
        else
            animeDownload = intent.extras?.getSerializable("animeDownload") as AnimeDownload
    }

    private fun playAnime() {
        viewModel.getAnimeStreamLink(animeStream.animeLink, animeStream.animeEpisode)
        saveHistory()
    }

    private fun checkHasNextEpisode(): Boolean {
        return animeStream.animeEpisodes.contains(animeStream.animeEpisode + 1)
    }

    private fun checkHasPreviousEpisode(): Boolean {
        return animeStream.animeEpisodes.contains(animeStream.animeEpisode - 1)
    }

    private fun updateAllTitle() {
        playerControlView.findViewById<TextView>(R.id.c_exo_episode_title).text =
            getString(R.string.ep, animeStream.animeEpisode)
        playerControlView.findViewById<TextView>(R.id.c_exo_title).text = buildString {
            append(animeStream.animeEpisode.toString())
            append(" : ")
            append(animeStream.animeName)
        }
    }

    private fun showEpisodeDialog() {
        dialog = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Dialog(this, R.style.DialogAnimationPortraitView)
        } else {
            Dialog(this, R.style.DialogAnimationLandscapeView)
        }

        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogEpisodeSearchBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            dialogBinding.dialogEpisodeSearchLayout.setBackgroundResource(R.drawable.custom_bg_dialog_radius_topleft_topright)
        } else {
            dialogBinding.dialogEpisodeSearchLayout.setBackgroundResource(R.drawable.custom_bg_dialog_radius_topleft_bottomleft)
        }

        val window = dialog.window ?: return

        val displayMetrics = resources.displayMetrics
        val width: Int
        val height: Int

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels / 3 * 2
        } else {
            width = displayMetrics.widthPixels / 5 * 2
            height = displayMetrics.heightPixels
        }

        window.setLayout(width, height)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val windowAttributes = window.attributes
        windowAttributes.gravity = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) Gravity.BOTTOM else Gravity.END
        window.attributes = windowAttributes

        dialog.setCancelable(true)

        dialogBinding.dialogSearchView.queryHint = getString(R.string.enter_episode_number_to_search)

        dialogBinding.dialogSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = animeStream.animeEpisodes.filter {
                    it.toString().contains(newText.toString())
                }
                dialogBinding.episodeRecyclerView.adapter = EpisodeAdapter(filteredList, animeStream.animeEpisode) {
                    animeStream.animeEpisode = it
                    viewModel.player.pause()
                    updateAllTitle()
                    playAnime()
                    dialog.dismiss()
                }
                return true
            }
        })

        dialogBinding.swapIcon.setOnClickListener {
            animeStream.animeEpisodes.reverse()
            dialogBinding.episodeRecyclerView.adapter = EpisodeAdapter(animeStream.animeEpisodes, animeStream.animeEpisode) {
                animeStream.animeEpisode = it
                viewModel.player.pause()
                updateAllTitle()
                playAnime()
                dialog.dismiss()
            }
        }

        dialogBinding.episodeRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AnimeStreamActivity, 5)
            adapter = EpisodeAdapter(animeStream.animeEpisodes, animeStream.animeEpisode) {
                animeStream.animeEpisode = it
                viewModel.player.pause()
                updateAllTitle()
                playAnime()
                dialog.dismiss()
            }
        }

        dialogBinding.episodeRecyclerView.scrollToPosition(animeStream.animeEpisode - 1)

        dialog.show()
    }

    private fun hideAndroidUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun onFinishWatch() {
        viewModel.player.stop()
        val returnIntent = Intent()
        returnIntent.putExtra("episode", animeStream.animeEpisode)
        setResult(Activity.RESULT_OK, returnIntent)

        finish()
    }

    private fun saveHistory() {
        if(viewModel.animeUiState.value.isInHistory) {
            viewModel.updateHistoryAnime(animeStream.animeLink, animeStream.animeEpisode)
        }
        else {
            val anime = Anime(
                animeName = animeStream.animeName,
                animeImageURL = animeStream.animeImageURL,
                animeLink = animeStream.animeLink
            )
            viewModel.addHistoryAnime(anime, animeStream.animeEpisode)
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.player.pause()
    }


    override fun onRestart() {
        super.onRestart()

        viewModel.player.play()
    }

}