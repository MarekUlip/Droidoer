package com.marekulip.droidoer.database

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = MainTask.TABLE_NAME)
class MainTask(@PrimaryKey var id: Long?,
               @ColumnInfo(name = TASK_NAME) var name: String,
               @ColumnInfo(name = COMPLETED) var completed: Boolean): BaseColumns {
    @Ignore
    var subTasks: MutableList<SubTask> = ArrayList()
    constructor():this(null,"",false)

    companion object {
        const val TABLE_NAME = "Main_task"
        const val TASK_NAME = "task_name"
        const val COMPLETED = "completed"
    }
}