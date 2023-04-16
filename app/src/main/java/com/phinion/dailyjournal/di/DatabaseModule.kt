package com.phinion.dailyjournal.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phinion.dailyjournal.connectivity.NetworkConnectivityObserver
import com.phinion.dailyjournal.data.database.ImagesDatabase
import com.phinion.dailyjournal.util.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = IMAGES_DATABASE
        ).build()
    }

    @Provides
    @Singleton
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao()

    @Provides
    @Singleton
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao()

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ) = NetworkConnectivityObserver(context)

}