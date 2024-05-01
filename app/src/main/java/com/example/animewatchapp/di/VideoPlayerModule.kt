package com.example.animewatchapp.di

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object VideoPlayerModule {

    @OptIn(UnstableApi::class) @Provides
    @ViewModelScoped
    fun provideVideoPlayer(app: Application): Player {
//        val headerMap = mutableMapOf(
//            "Accept" to "*/*",
//            "Connection" to "keep-alive",
//            "Upgrade-Insecure-Requests" to "1"
//        )
//
//        val dataSourceFactory
//                : DataSource.Factory = DefaultHttpDataSource.Factory()
//            .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36")
//            .setDefaultRequestProperties(headerMap)
//            .setReadTimeoutMs(20000)
//            .setConnectTimeoutMs(20000)
//
//        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        return ExoPlayer.Builder(app)
//            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

}