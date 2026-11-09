package com.mathlator.app.di

import android.content.Context
import androidx.room.Room
import com.mathlator.app.data.db.AppDatabase
import com.mathlator.app.data.db.HistoryDao
import com.mathlator.app.data.db.RateDao
import com.mathlator.app.data.rates.FrankfurterProvider
import com.mathlator.app.data.rates.OpenErApiProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "mathlator.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
    @Provides fun provideRateDao(db: AppDatabase): RateDao = db.rateDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideOpenErApiProvider(client: OkHttpClient) = OpenErApiProvider(client)

    @Provides
    @Singleton
    fun provideFrankfurterProvider(client: OkHttpClient) = FrankfurterProvider(client)
}
