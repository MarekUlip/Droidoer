package com.marekulip.droidoer.database

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface SubTaskDao {
    @Insert(onConflict = REPLACE)
    fun insertAll(vararg subTasks: SubTask)

    @Delete
    fun deleteAll(vararg subTasks: SubTask)

    @Update
    fun updateAll(vararg subTasks: SubTask)

    @Query("SELECT * FROM ${SubTask.TABLE_NAME}")
    fun getAll():MutableList<SubTask>

    /**
     * @param category Category of subtasks. 0-3. You can use constants from [SubTask] class.
     * @return returns all subtasks based on provided category.
     */
    @Query("SELECT * FROM ${SubTask.TABLE_NAME} WHERE ${SubTask.CATEGORY}=:category")
    fun getAllByCat(category: Int):MutableList<SubTask>

    @Query("SELECT * FROM ${SubTask.TABLE_NAME} WHERE ${SubTask.MAIN_TASK}=:mainTaskId")
    fun getAllByMainTask(mainTaskId: Long):MutableList<SubTask>

    @Query("SELECT * FROM ${SubTask.TABLE_NAME} WHERE ${SubTask.MAIN_TASK}=:mainTaskId AND ${SubTask.CATEGORY} =:category")
    fun getAllByMainTaskAndCat(mainTaskId: Long, category: Int):MutableList<SubTask>
}