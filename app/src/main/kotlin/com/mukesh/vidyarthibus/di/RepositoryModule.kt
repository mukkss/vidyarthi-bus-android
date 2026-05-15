package com.mukesh.vidyarthibus.di

import com.mukesh.vidyarthibus.data.local.PreferenceRepositoryImpl
import com.mukesh.vidyarthibus.data.repository.BusRepositoryImpl
import com.mukesh.vidyarthibus.domain.repository.BusRepository
import com.mukesh.vidyarthibus.domain.repository.PreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBusRepository(
        busRepositoryImpl: BusRepositoryImpl
    ): BusRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(
        preferenceRepositoryImpl: PreferenceRepositoryImpl
    ): PreferenceRepository
}
