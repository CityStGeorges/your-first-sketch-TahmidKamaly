package com.companies.smartwaterintake.domain.service

import android.content.Context
import com.companies.smartwaterintake.domain.remote.WeatherApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stellerbyte.uptodo.services.implementation.AccountServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    private const val BASE_URL = "https://api.openweathermap.org/"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }
}