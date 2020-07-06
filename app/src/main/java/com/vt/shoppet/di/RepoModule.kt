package com.vt.shoppet.di

import com.vt.shoppet.firebase.AuthRepo
import com.vt.shoppet.firebase.FirestoreRepo
import com.vt.shoppet.firebase.LabelerRepo
import com.vt.shoppet.firebase.StorageRepo
import com.vt.shoppet.impl.AuthRepoImpl
import com.vt.shoppet.impl.FirestoreRepoImpl
import com.vt.shoppet.impl.LabelerRepoImpl
import com.vt.shoppet.impl.StorageRepoImpl
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
    abstract fun bindAuthRepo(impl: AuthRepoImpl): AuthRepo

    @Binds
    @Singleton
    abstract fun bindFirestoreRepo(impl: FirestoreRepoImpl): FirestoreRepo

    @Binds
    @Singleton
    abstract fun bindLabelerRepo(impl: LabelerRepoImpl): LabelerRepo

    @Binds
    @Singleton
    abstract fun bindStorageRepo(impl: StorageRepoImpl): StorageRepo

}