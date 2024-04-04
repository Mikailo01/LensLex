package com.bytecause.lenslex.di

import android.content.Context
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideAuthClient(@ApplicationContext context: Context): FireBaseAuthClient =
        FireBaseAuthClient(context, provideSignInClient(context))

    @Singleton
    @Provides
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient =
        Identity.getSignInClient(context)
}