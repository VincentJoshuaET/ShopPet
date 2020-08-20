package com.vt.shoppet.di

import com.vt.shoppet.impl.*
import com.vt.shoppet.repo.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepo(authRepoImpl: AuthRepoImpl): AuthRepo

    @Binds
    @Singleton
    abstract fun bindDataRepo(dataRepoImpl: DataRepoImpl): DataRepo

    @Binds
    @Singleton
    abstract fun bindFirestoreRepo(firestoreRepoImpl: FirestoreRepoImpl): FirestoreRepo

    @Binds
    @Singleton
    abstract fun bindVisionRepo(visionRepoImpl: VisionRepoImpl): VisionRepo

    @Binds
    @Singleton
    abstract fun bindStorageRepo(storageRepoImpl: StorageRepoImpl): StorageRepo

}