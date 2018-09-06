package com.marekulip.droidoer.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.provider.BaseColumns
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

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "$TASK_NAME TEXT," +
                "$COMPLETED INTEGER)"
        const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}