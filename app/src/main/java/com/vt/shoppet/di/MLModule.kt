package com.vt.shoppet.di

import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideImageLabeler() =
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(0.75f).build())

}