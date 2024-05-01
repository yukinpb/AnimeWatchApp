package com.example.animewatchapp.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFprobe
import com.example.animewatchapp.AnimeWatchApplication.Companion.CHANNEL_ID
import com.example.animewatchapp.R
import com.example.animewatchapp.adapter.EpisodeAdapter
import com.example.animewatchapp.adapter.EpisodeDownloadAdapter
import com.example.animewatchapp.databinding.ActivityAnimeDetailsBinding
import com.example.animewatchapp.databinding.CustomAlertDialogBinding
import com.example.animewatchapp.databinding.DialogEpisodeSearchBinding
import com.example.animewatchapp.databinding.ItemEpisodeDownloadBinding
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.model.AnimeStream
import com.example.animewatchapp.viewmodels.AnimeDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Suppress("DEPRECATION")
@AndroidEntryPoint
class AnimeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnimeDetailsBinding
    private lateinit var anime: Anime
    private val viewModel: AnimeDetailsViewModel by viewModels()
    private lateinit var from: String
    private var episode: Int = 1
    private var episodeList: MutableList<Int> = mutableListOf()
    private lateinit var dialogEp: Dialog
    private lateinit var dialogDownload: Dialog
    private lateinit var downloadAdapter: EpisodeDownloadAdapter
    private var initDialogEp = false
    private var initDialogDownload = false
    private var isDownloading = false
    private val NOTIFICATION_ID = 1
    private var currentEpisodeDownload = 1
    private var outputPathDownload = ""
    private lateinit var builderNotification: NotificationCompat.Builder
    private var initNotificationBuilder = false

    private val episodeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            episode = data?.getIntExtra("episode", episode) ?: episode
            binding.animeDetailsEpisode.text = getString(R.string.episode, episode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lockScreenOrientation()
        showAndroidUI()
        getExtras()
        setUpViewModel()
        init()
        setUpEventListener()
    }

    @OptIn(UnstableApi::class)
    private fun setUpEventListener() {
        binding.epCard.setOnClickListener {
            showEpisodeDialog()
        }
        binding.playCard.setOnClickListener {
            val intent = Intent(this@AnimeDetailsActivity, AnimeStreamActivity::class.java)
            val animeStream = AnimeStream(
                animeName = anime.animeName,
                animeImageURL = anime.animeImageURL,
                animeLink = anime.animeLink,
                animeEpisode = episode,
                animeEpisodes = episodeList
            )
            intent.putExtra("animeStream", animeStream)
            intent.putExtra("from", "details")
            episodeResultLauncher.launch(intent)
        }

        binding.btnReconnect.setOnClickListener {
            getAnimeDetails(anime.animeLink)
        }

        binding.backButton.setOnClickListener {
            finishActivity()
        }

        binding.downloadButton.setOnClickListener {
           showDownloadDialog()
        }

    }

    private fun init() {
        if(from == "latest" || from == "history") {
            val splitLink = anime.animeLink.split("/")
            episode = splitLink[splitLink.size - 2].toInt()

            val contentLink =
                anime.animeLink.replace("/watch/", "/anime/").substringBeforeLast("/").substringBeforeLast("/") + "/"

            anime.animeName = anime.animeName.substringAfter(':').trim()
            anime.animeLink = contentLink
            getAnimeDetails(contentLink)
        }
        else {
            getAnimeDetails(anime.animeLink)
        }
    }

    private fun setUpViewModel() {
        lifecycleScope.launch {
            viewModel.animeUiState.collectLatest {
                if(it.isLoading) {
                    binding.animeDetailsLoading.visibility = View.VISIBLE
                    binding.animeDetailsError.visibility = View.GONE
                }
                else if(it.isError) {
                    binding.animeDetailsError.visibility = View.VISIBLE
                    binding.animeDetailsLoading.visibility = View.GONE
                }
                else {
                    if(it.isFavorite) {
                        binding.imgFav.setImageResource(R.drawable.ic_remove_favorite)
                        binding.imgFav.setColorFilter(resources.getColor(R.color.md_theme_dark_error))
                        binding.favCard.setOnClickListener {
                            viewModel.removeFavoriteAnime(anime.animeLink)
                        }
                    } else {
                        binding.imgFav.setImageResource(R.drawable.ic_add_favorite)
                        binding.imgFav.setColorFilter(resources.getColor(R.color.md_theme_light_shadow))
                        binding.favCard.setOnClickListener {
                            viewModel.addFavoriteAnime(anime)
                        }
                    }
                    binding.animeDetailsLoading.visibility = View.GONE
                    it.animeDetails?.let { animeDetail ->
                        binding.apply {
                            animeDetailsImage.load(animeDetail.animeCover) {
                                crossfade(true)
                                placeholder(R.drawable.loading_img)
                                error(R.drawable.ic_broken_image)
                            }
                            animeDetailsTitle.text = animeDetail.animeName
                            animeDetailsDescription.text = animeDetail.animeDesc
                            animeDetailsEpisode.text = getString(R.string.episode, episode)
                            episodeList.addAll(animeDetail.animeEpisodes)
                        }
                    }
                }
            }
        }
    }

    private fun getExtras() {
        anime = intent.extras?.getSerializable("anime") as Anime
        from = intent.extras?.getString("from").toString()
    }

    private fun getAnimeDetails(contentLink: String) {
        viewModel.getAnimeDetails(contentLink)
        viewModel.checkAnimeFavorite(anime.animeLink)
    }

    private fun showEpisodeDialog() {
        if(!initDialogEp) {
            dialogEp = Dialog(this)
            val dialogBinding = DialogEpisodeSearchBinding.inflate(layoutInflater)
            dialogEp.setContentView(dialogBinding.root)

            val window = dialogEp.window ?: return

            val displayMetrics = resources.displayMetrics
            val width = displayMetrics.widthPixels - 100
            val height = displayMetrics.heightPixels / 2

            window.setLayout(width, height)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val windowAttributes = window.attributes
            windowAttributes.gravity = Gravity.CENTER
            window.attributes = windowAttributes

            dialogEp.setCancelable(true)

            dialogBinding.dialogSearchView.queryHint = getString(R.string.enter_episode_number_to_search)

            dialogBinding.dialogSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val filteredList = episodeList.filter {
                        it.toString().contains(newText.toString())
                    }
                    dialogBinding.episodeRecyclerView.adapter = EpisodeAdapter(filteredList, episode) {
                        episode = it
                        binding.animeDetailsEpisode.text = getString(R.string.episode, episode)
                        dialogEp.dismiss()
                    }

                    return true
                }
            })

            dialogBinding.swapIcon.setOnClickListener {
                episodeList.reverse()
                dialogBinding.episodeRecyclerView.adapter = EpisodeAdapter(episodeList, episode) {
                    episode = it
                    binding.animeDetailsEpisode.text = getString(R.string.episode, episode)
                    dialogEp.dismiss()
                }
            }

            dialogBinding.episodeRecyclerView.apply {
                layoutManager = GridLayoutManager(this@AnimeDetailsActivity, 5)
                adapter = EpisodeAdapter(episodeList, episode) {
                    episode = it
                    binding.animeDetailsEpisode.text = getString(R.string.episode, episode)
                    dialogEp.dismiss()
                }
            }

            dialogBinding.episodeRecyclerView.scrollToPosition(episode - 1)
            initDialogEp = true
            dialogEp.show()
        }
        else {
            dialogEp.show()
        }
    }

    private fun showDownloadDialog() {
        if(!initDialogDownload) {
            dialogDownload = Dialog(this)
            val dialogBinding = DialogEpisodeSearchBinding.inflate(layoutInflater)
            dialogDownload.setContentView(dialogBinding.root)

            val window = dialogDownload.window ?: return

            val displayMetrics = resources.displayMetrics
            val width = displayMetrics.widthPixels - 100
            val height = displayMetrics.heightPixels / 2

            window.setLayout(width, height)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val windowAttributes = window.attributes
            windowAttributes.gravity = Gravity.CENTER
            window.attributes = windowAttributes

            dialogDownload.setCancelable(true)

            dialogBinding.dialogSearchView.queryHint = getString(R.string.enter_episode_number_to_search)
            dialogBinding.dialogTitle.text = getString(R.string.download_episode)

            dialogBinding.dialogSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val filteredList = episodeList.filter {
                        it.toString().contains(newText.toString())
                    }
                    downloadAdapter = EpisodeDownloadAdapter(filteredList, this@AnimeDetailsActivity::checkCanDownload, this@AnimeDetailsActivity::downloadAnime)
                    dialogBinding.episodeRecyclerView.adapter = downloadAdapter

                    return true
                }
            })

            dialogBinding.swapIcon.setOnClickListener {
                episodeList.reverse()
                downloadAdapter = EpisodeDownloadAdapter(episodeList, this::checkCanDownload, this::downloadAnime)
                dialogBinding.episodeRecyclerView.adapter = downloadAdapter
            }

            downloadAdapter = EpisodeDownloadAdapter(episodeList, this::checkCanDownload, this::downloadAnime)
            dialogBinding.episodeRecyclerView.apply {
                layoutManager = GridLayoutManager(this@AnimeDetailsActivity, 5)
                adapter = downloadAdapter
            }
            initDialogDownload = true
            dialogDownload.show()
        }
        else {
            dialogDownload.show()
        }
    }

    private fun showDialogConfirmStopDownloading() {
        val dialog = Dialog(this)
        val dialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val window = dialog.window ?: return

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels - 100

        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        window.setBackgroundDrawableResource(android.R.color.transparent)
        val windowAttributes = window.attributes
        windowAttributes.gravity = Gravity.CENTER
        window.attributes = windowAttributes

        dialog.setCancelable(true)

        dialogBinding.btnConfirm.setOnClickListener {
            isDownloading = false
            showDownloadFailedNotification()
            FFmpeg.cancel()
            deleteAnime(outputPathDownload)
            dialog.dismiss()
            finish()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAndroidUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun checkCanDownload(episode: Int): Boolean {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val str = anime.animeLink
        val parts = str.split("/")
        val nameAnime = parts[parts.size - 2]
        val outputFilePath = "${downloadDir.absolutePath}/AnimeWatch/$nameAnime-$episode.mp4"
        val file = File(outputFilePath)
        return !file.exists()
    }

    private fun deleteAnime(filePath: String) {
        val file = File(filePath)

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
//                Toast.makeText(this, "Anime deleted", Toast.LENGTH_SHORT).show()
                Log.d("AnimeDetailsActivity", "Anime deleted")
            } else {
//                Toast.makeText(this, "Failed to delete anime", Toast.LENGTH_SHORT).show()
                Log.d("AnimeDetailsActivity", "Failed to delete anime")
            }
        } else {
//            Toast.makeText(this, "Anime not found", Toast.LENGTH_SHORT).show()
            Log.d("AnimeDetailsActivity", "Anime not found")
        }
    }

    private suspend fun downloadVideo(episode: Int, videoLink: String, outputPath: String, progressBar: ProgressBar): Boolean =
        withContext(Dispatchers.IO) {
            isDownloading = true
            currentEpisodeDownload = episode
            outputPathDownload = outputPath
            val mediaInformation = FFprobe.getMediaInformation(videoLink)
            val totalDuration: Double
            if(mediaInformation != null)
                 totalDuration = mediaInformation.duration.toDouble()
            else
                return@withContext false

            Config.enableStatisticsCallback { newStatistics ->
                val progress = (newStatistics.time / totalDuration) * 100 / 1000

                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        progressBar.progress = progress.toInt()
                        showDownloadNotification(progress.toInt())
                    }
                }
            }

            Log.d("DownloadVideo", "Downloading video from $videoLink")
//            val command = arrayOf("-i", videoLink, "-c", "copy", "-y", outputPath)
//            val command = arrayOf("-y", "-i", videoLink, "-c:v", "mpeg4", "-q:v", "23", "-threads", "64", "-c:a", "aac", "-q:a", "100", outputPath)
            val command = arrayOf("-y", "-i", videoLink, "-c:v", "libx264", "-preset", "ultrafast", "-crf", "30", "-threads", "64", "-c:a", "aac", "-b:a", "128k", outputPath)
            val rc = FFmpeg.execute(command)


            if (rc == Config.RETURN_CODE_SUCCESS) {
                return@withContext true
            } else {
                Log.d("DownloadVideo", "Command execution failed with rc=$rc")
                Log.d("DownloadVideo", "Command output: ${Config.getLastCommandOutput()}")
                return@withContext false
            }
        }


    private fun downloadAnime(episode: Int, binding: ItemEpisodeDownloadBinding) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dirPath = "${downloadDir.absolutePath}/AnimeWatch"
                val dir = File(dirPath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val str = anime.animeLink
                val parts = str.split("/")
                val nameAnime = parts[parts.size - 2]
                val outputFilePath = "$dirPath/$nameAnime-$episode.mp4"
                val animeStreamUrl = viewModel.getAnimeStreamLink(anime.animeLink, episode)
                val result = downloadVideo(episode, animeStreamUrl, outputFilePath, binding.episodeDownloadProgress)

                if(result) {
                    val animeDownload = AnimeDownload(
                        animeName = anime.animeName,
                        animeImageURL = anime.animeImageURL,
                        animeLink = outputFilePath,
                        animeEpisode = episode
                    )
                    withContext(Dispatchers.Main) {
                        binding.episodeDownloadProgress.visibility = View.GONE
                        binding.episodeDownloadProgress.progress = 0
                        binding.episodeCardView.isSelected = true
                        binding.episodeNumber.isChecked = true
                        binding.episodeCardView.isClickable = false
                        showDownloadSuccessNotification()
                    }
                    viewModel.addDownloadAnime(animeDownload)
                }
                else {
                    withContext(Dispatchers.Main) {
                        binding.episodeDownloadProgress.visibility = View.GONE
                        binding.episodeDownloadProgress.progress = 0
                        binding.episodeCardView.isSelected = false
                        binding.episodeNumber.isChecked = false
                        binding.episodeCardView.isClickable = true
                        Toast.makeText(this@AnimeDetailsActivity, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
                downloadAdapter.enableDownload()
                isDownloading = false
            }
        }
    }

    private fun showDownloadNotification(progress: Int) {
        if(isDownloading) {
            Log.d("AnimeDetailsActivity", "showDownloadNotification")
            if(!initNotificationBuilder) {
                builderNotification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
                    setContentTitle("Download")
                    setContentText("Download anime ${anime.animeName} episode $currentEpisodeDownload")
                    setSmallIcon(R.drawable.ic_download)
                    setProgress(100, progress, false)
                    priority = NotificationCompat.PRIORITY_LOW
                    setOnlyAlertOnce(true)
                }
            }
            else {
                builderNotification.setProgress(100, progress, false)
            }

            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(
                        this@AnimeDetailsActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                notify(NOTIFICATION_ID, builderNotification.build())
            }
        }
    }

    private fun showDownloadFailedNotification() {
        Log.d("AnimeDetailsActivity", "showDownloadFailedNotification")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Download Failed")
            setContentText("Download of anime ${anime.animeName} episode $currentEpisodeDownload failed.")
            setSmallIcon(R.drawable.ic_download)
            priority = NotificationCompat.PRIORITY_LOW
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@AnimeDetailsActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun lockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun showDownloadSuccessNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Download Success")
            setContentText("Download of anime ${anime.animeName} episode $currentEpisodeDownload success.")
            setSmallIcon(R.drawable.ic_download)
            priority = NotificationCompat.PRIORITY_LOW
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@AnimeDetailsActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun finishActivity() {
        if(isDownloading) {
            showDialogConfirmStopDownloading()
        }
        else {
            finish()
        }
    }

//    override fun onDestroy() {
//        Log.d("AnimeDetailsActivity", "onDestroy")
//        if(isDownloading) {
//            FFmpeg.cancel()
//            showDownloadFailedNotification()
//        }
//        super.onDestroy()
//    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(isDownloading) {
            showDialogConfirmStopDownloading()
        } else {
            super.onBackPressed()
        }


    }
}