package com.companies.smartwaterintake.domain.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stellerbyte.uptodo.services.implementation.AccountServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object StorageServiceModule {

    @Provides
    @Singleton
    fun provideAccountService(impl: AccountServiceImpl) : AccountService{
        return impl
    }

    @Provides fun firestore(): FirebaseFirestore = Firebase.firestore
    @Provides
    fun provideFirebaseAuthInstance() = FirebaseAuth.getInstance()

}