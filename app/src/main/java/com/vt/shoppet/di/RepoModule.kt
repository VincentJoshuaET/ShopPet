package com.vt.shoppet.di

import com.vt.shoppet.impl.AuthRepoImpl
import com.vt.shoppet.impl.FirestoreRepoImpl
import com.vt.shoppet.impl.LabelerRepoImpl
import com.vt.shoppet.impl.StorageRepoImpl
import com.vt.shoppet.repo.AuthRepo
import com.vt.shoppet.repo.FirestoreRepo
import com.vt.shoppet.repo.LabelerRepo
import com.vt.shoppet.repo.StorageRepo
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
    abstract fun bindFirestoreRepo(firestoreRepoImpl: FirestoreRepoImpl): FirestoreRepo

    @Binds
    @Singleton
    abstract fun bindLabelerRepo(labelerRepoImpl: LabelerRepoImpl): LabelerRepo

    @Binds
    @Singleton
    abstract fun bindStorageRepo(storageRepoImpl: StorageRepoImpl): StorageRepo

}