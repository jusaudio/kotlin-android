package com.curiolabs.jusaudio.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.curiolabs.jusaudio.persistence.dao.JusAudiosDao
import com.curiolabs.jusaudio.persistence.dao.JusAudiosFtsDao
import com.curiolabs.jusaudio.persistence.entities.JusAudios
import com.curiolabs.jusaudio.persistence.entities.JusAudiosFts
import com.curiolabs.jusaudio.utility_classes.JusAudioConstants.DATABASE_NAME

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
            val tempInstance =
                INSTANCE
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
