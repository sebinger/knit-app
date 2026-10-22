package com.stitchtrack.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stitchtrack.app.data.local.converter.ProjectStatusConverter
import com.stitchtrack.app.data.local.dao.CounterChangeDao
import com.stitchtrack.app.data.local.dao.CounterDao
import com.stitchtrack.app.data.local.dao.NoteDao
import com.stitchtrack.app.data.local.dao.PatternFileDao
import com.stitchtrack.app.data.local.dao.ProjectDao
import com.stitchtrack.app.data.local.dao.TimerSessionDao
import com.stitchtrack.app.data.local.entity.CounterChangeEntity
import com.stitchtrack.app.data.local.entity.CounterEntity
import com.stitchtrack.app.data.local.entity.NoteEntity
import com.stitchtrack.app.data.local.entity.PatternFileEntity
import com.stitchtrack.app.data.local.entity.ProjectEntity
import com.stitchtrack.app.data.local.entity.TimerSessionEntity

@Database(
    entities = [
        ProjectEntity::class,
        CounterEntity::class,
        CounterChangeEntity::class,
        NoteEntity::class,
        TimerSessionEntity::class,
        PatternFileEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(ProjectStatusConverter::class)
abstract class StitchTrackDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    abstract fun counterDao(): CounterDao

    abstract fun counterChangeDao(): CounterChangeDao

    abstract fun noteDao(): NoteDao

    abstract fun timerSessionDao(): TimerSessionDao

    abstract fun patternFileDao(): PatternFileDao
}
