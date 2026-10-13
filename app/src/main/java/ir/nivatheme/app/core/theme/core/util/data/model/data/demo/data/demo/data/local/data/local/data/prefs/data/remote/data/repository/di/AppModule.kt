// app/src/main/java/ir/nivatheme/app/di/AppModule.kt
package ir.nivatheme.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.nivatheme.app.data.local.NivaDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): NivaDatabase =
        Room.databaseBuilder(ctx, NivaDatabase::class.java, "niva.db")
            .fallbackToDestructiveMigration()
            .build()
}
