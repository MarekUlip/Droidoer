package com.marekulip.droidoer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MainTask::class, SubTask::class], version = 1)
abstract class DroidoerDatabase: RoomDatabase() {

    abstract fun mainTaskDataDao(): MainTaskDao
    abstract fun subTaskDataDao(): SubTaskDao

    companion object {
        private var INSTANCE: DroidoerDatabase? = null

        fun getInstance(context: Context):DroidoerDatabase?{
            if(INSTANCE == null){
                synchronized(DroidoerDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,DroidoerDatabase::class.java,"droidoer.db").allowMainThreadQueries().build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}