package com.marekulip.droidoer.database

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface MainTaskDao {

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg mainTasks: MainTask)

    @Delete
    fun deleteAll(mainTask: MainTask)

    @Update
    fun updateAll(vararg mainTasks: MainTask)

    @Query("SELECT * FROM ${MainTask.TABLE_NAME}")
    fun getAll():MutableList<MainTask>

    /**
     * Select all completed or uncompleted main tasks
     */
    @Query("SELECT * FROM ${MainTask.TABLE_NAME} WHERE ${MainTask.COMPLETED}=:completed")
    fun getSome(completed: Boolean):MutableList<MainTask>

    @Query("SELECT * FROM ${MainTask.TABLE_NAME} WHERE id =:Id")
    fun findById(Id: Long):MainTask
}