package com.stitchtrack.app.data.di

import android.content.Context
import androidx.room.Room
import com.stitchtrack.app.core.util.SystemTimeProvider
import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.data.local.db.StitchTrackDatabase
import com.stitchtrack.app.data.repository.CounterRepositoryImpl
import com.stitchtrack.app.data.repository.NoteRepositoryImpl
import com.stitchtrack.app.data.repository.PatternRepositoryImpl
import com.stitchtrack.app.data.repository.ProjectRepositoryImpl
import com.stitchtrack.app.data.repository.TimerRepositoryImpl
import com.stitchtrack.app.domain.repository.CounterRepository
import com.stitchtrack.app.domain.repository.NoteRepository
import com.stitchtrack.app.domain.repository.PatternRepository
import com.stitchtrack.app.domain.repository.ProjectRepository
import com.stitchtrack.app.domain.repository.TimerRepository
import dagger.Binds
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
    fun provideDatabase(@ApplicationContext context: Context): StitchTrackDatabase =
        Room.databaseBuilder(
            context,
            StitchTrackDatabase::class.java,
            "stitchtrack.db",
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideProjectDao(database: StitchTrackDatabase) = database.projectDao()

    @Provides
    fun provideCounterDao(database: StitchTrackDatabase) = database.counterDao()

    @Provides
    fun provideCounterChangeDao(database: StitchTrackDatabase) = database.counterChangeDao()

    @Provides
    fun provideNoteDao(database: StitchTrackDatabase) = database.noteDao()

    @Provides
    fun provideTimerSessionDao(database: StitchTrackDatabase) = database.timerSessionDao()

    @Provides
    fun providePatternFileDao(database: StitchTrackDatabase) = database.patternFileDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindCounterRepository(impl: CounterRepositoryImpl): CounterRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTimerRepository(impl: TimerRepositoryImpl): TimerRepository

    @Binds
    @Singleton
    abstract fun bindPatternRepository(impl: PatternRepositoryImpl): PatternRepository

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider
}
