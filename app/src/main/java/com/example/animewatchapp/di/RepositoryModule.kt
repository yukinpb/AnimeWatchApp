package com.example.animewatchapp.di

import com.example.animewatchapp.repository.AnimeRepository
import com.example.animewatchapp.repository.AnimeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(ViewModelComponent::class)
@ExperimentalCoroutinesApi
abstract class RepositoryModule {

    @Binds
    abstract fun bindAnimeRepository(repository: AnimeRepositoryImpl): AnimeRepository
}