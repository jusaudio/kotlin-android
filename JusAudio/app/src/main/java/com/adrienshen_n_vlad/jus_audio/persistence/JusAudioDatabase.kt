package com.adrienshen_n_vlad.jus_audio.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adrienshen_n_vlad.jus_audio.persistence.dao.JusAudiosDao
import com.adrienshen_n_vlad.jus_audio.persistence.dao.PlayHistoryDao
import com.adrienshen_n_vlad.jus_audio.persistence.dao.RecommendedAudiosDao
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.persistence.entities.PlayHistory
import com.adrienshen_n_vlad.jus_audio.persistence.entities.RecommendedAudios
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.DATABASE_NAME

@Database(
    entities = [JusAudios::class, PlayHistory::class, RecommendedAudios::class],
    version = 1,
    exportSchema = false
)
abstract class JusAudioDatabase : RoomDatabase() {
    abstract fun jusAudiosDao(): JusAudiosDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun recommendedAudiosDao(): RecommendedAudiosDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: JusAudioDatabase? = null

        fun getDatabase(context: Context): JusAudioDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JusAudioDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
