package com.adrienshen_n_vlad.jus_audio.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adrienshen_n_vlad.jus_audio.persistence.dao.JusAudiosDao
import com.adrienshen_n_vlad.jus_audio.persistence.dao.JusAudiosFtsDao
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudios
import com.adrienshen_n_vlad.jus_audio.persistence.entities.JusAudiosFts
import com.adrienshen_n_vlad.jus_audio.utility_classes.JusAudioConstants.DATABASE_NAME

@Database(
    entities = [JusAudios::class, JusAudiosFts::class],
    version = 1,
    exportSchema = false
)
abstract class JusAudioDatabase : RoomDatabase() {
    abstract fun jusAudiosDao(): JusAudiosDao
    abstract fun jusAudiosFtsDao(): JusAudiosFtsDao

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
