package com.example.animewatchapp.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.animewatchapp.R
import com.example.animewatchapp.adapter.AnimeDownloadAdapter
import com.example.animewatchapp.databinding.ActivityAnimeDownloadBinding
import com.example.animewatchapp.databinding.CustomAlertDialogBinding
import com.example.animewatchapp.model.AnimeDownload
import com.example.animewatchapp.utils.OUTPUT_PATH
import com.example.animewatchapp.viewmodels.DownloadViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class AnimeDownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimeDownloadBinding
    private val viewModel: DownloadViewModel by viewModels()
    private lateinit var animeAdapter: AnimeDownloadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lockScreenOrientation()
        init()
        setUpViewModel()
        setUpEventListener()
    }

    private fun setUpEventListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchAnime(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.btnDeleteAll.setOnClickListener {
            showDialogConfirmDeleteAll()
        }
    }

    private fun lockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun showDialogConfirmDeleteAll() {
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

        dialogBinding.dialogTitle.text = getString(R.string.delete_all_anime)
        dialogBinding.dialogContent.text = getString(R.string.are_you_sure_delete_all_anime)

        dialogBinding.btnConfirm.setOnClickListener {
            deleteAllAnime()
            viewModel.deleteAllDownloadAnime()
            dialog.dismiss()
            finish()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setUpViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.animeUiState.collectLatest {
                    if(it.isLoading) {
                        binding.downloadLoading.visibility = View.VISIBLE
                    }
                    else {
                        binding.downloadLoading.visibility = View.GONE
                        animeAdapter.submitData(it.animeList)
                    }
                }
            }
        }
    }

    private fun init() {
        animeAdapter = AnimeDownloadAdapter(mutableListOf(), this::onAnimeClick, this::showDeleteConfirmation)
        binding.downloadRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = animeAdapter
        }
    }

    private fun searchAnime(keyword: String) {
        viewModel.animeUiState.value.searchKeyword = keyword
        viewModel.searchDownloadAnime(keyword)

        hideKeyboard()
    }

    @OptIn(UnstableApi::class) private fun onAnimeClick(animeDownload: AnimeDownload) {
        val intent = Intent(this, AnimeStreamActivity::class.java)
        intent.putExtra("animeDownload", animeDownload)
        intent.putExtra("from", "download")
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun showDeleteConfirmation(anime: AnimeDownload) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val list = listOf("Delete")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        bottomSheetDialog.setContentView(R.layout.item_bottom_sheet)
        val listView = bottomSheetDialog.findViewById<ListView>(R.id.list)
        listView?.adapter = adapter

        listView?.setOnItemClickListener { _, _, _, _ ->
            viewModel.deleteDownloadAnime(anime.animeLink)
            deleteAnime(anime.animeLink)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun deleteAllAnime() {
        val downloadDir = File(applicationContext.filesDir, OUTPUT_PATH)

        Log.d("AnimeDownloadActivity", "deleteAllAnime: $downloadDir")

        if (downloadDir.exists() && downloadDir.isDirectory) {
            val files = downloadDir.listFiles()

            if (files != null) {
                Log.d("AnimeDownloadActivity", "deleteAllAnime: ${files.size}")
                for (file in files) {
                    val deleted = file.delete()
                    if (!deleted) {
                        Toast.makeText(this, "Failed to delete all anime", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            Toast.makeText(this, "Anime watch dir not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAnime(filePath: String) {
        val file = File(filePath)

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Toast.makeText(this, "Anime deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete anime", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Anime not found", Toast.LENGTH_SHORT).show()
        }
    }
}