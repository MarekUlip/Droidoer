package com.marekulip.droidoer.database


import androidx.room.*
import android.provider.BaseColumns
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = SubTask.TABLE_NAME,
        foreignKeys = [ForeignKey(entity = MainTask::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf(SubTask.MAIN_TASK),
        onDelete = CASCADE)])
class SubTask(@PrimaryKey(autoGenerate = true) var id: Long?,
              @ColumnInfo(name = DESCRITPION) var description: String,
              @ColumnInfo(name = CATEGORY)var category: Int,
              @ColumnInfo(name = MAIN_TASK)var mainTaskId: Long): BaseColumns {
    constructor():this(null,"",0,0)

    companion object {
        const val TABLE_NAME = "Sub_task"
        const val DESCRITPION = "task_name"
        const val CATEGORY = "category"
        const val MAIN_TASK = "main_task_id"

        const val CAT_NONE = 0
        const val CAT_CANCELED = 1
        const val CAT_MEH = 2
        const val CAT_DONE = 3
    }
}