package com.marekulip.droidoer

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import com.marekulip.droidoer.database.MainTask
import com.marekulip.droidoer.database.SubTask
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.sub_item.view.*

class MyTodoRecyclerViewAdapter(var mValues: List<MainTask>,private val context: Context,private val listener: Callback):RecyclerView.Adapter<MyTodoRecyclerViewAdapter.ViewHolder>(){

    var category = 0
    var subTaskSetting = false

    override fun getItemCount(): Int = mValues.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.mView.setOnLongClickListener {
            listener.setMainTaskHldr(item)
            holder.mView.showContextMenu()
            true
        }
        holder.mView.setOnCreateContextMenuListener { menu, _, _ ->
            if(subTaskSetting){
                subTaskSetting = false
                return@setOnCreateContextMenuListener
            }
            menu.add(0,R.id.action_rename,0,R.string.rename)
            menu.add(0,R.id.action_delete,0,R.string.delete)
            if(item.completed){
                menu.add(0, R.id.action_mark_active, 0, R.string.mark_as_active)
            } else {
                menu.add(0, R.id.action_mark_complete, 0, R.string.mark_as_completed)
            }
        }
        holder.mWrapper.removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (i in item.subTasks){
            val view = inflater.inflate(R.layout.sub_item,null)
            view.text_sub_task.text = i.description
            if(category == 0) {
                view.but_cancel.setOnClickListener { listener.onCategoryChange(i,SubTask.CAT_CANCELED,position) }
                view.but_done.setOnClickListener { listener.onCategoryChange(i,SubTask.CAT_DONE,position) }
                view.but_meh.setOnClickListener { listener.onCategoryChange(i,SubTask.CAT_MEH,position) }
            }else{
                view.but_cancel.visibility = View.GONE
                view.but_meh.visibility = View.GONE
                view.but_done.text = "➞"
                view.but_done.setOnClickListener { listener.onCategoryChange(i, SubTask.CAT_NONE,position) }
            }

            view.setOnLongClickListener{
                subTaskSetting = true
                view.showContextMenu()
                listener.setSubTaskHldr(i)
                true  }

            view.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add(0,R.id.action_rename_sub,0,R.string.rename)
                menu.add(0,R.id.action_delete_sub,0,R.string.delete)
            }
            // Strange workaround - resizes view when it was already drawn so all items are displayed
            // No idea how it works... maybe magic
            // Even more weird is that when params are not assigned but modified directly
            // no change happens.
            val params = view.text_sub_task.layoutParams
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            val lParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            lParams.bottomMargin = 50
            holder.mWrapper.addView(view,lParams)
        }
        // But seriously I am afraid what doom will this code bring in the future.
        val params = holder.mWrapper.layoutParams
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        holder.mNameView.text = item.name
        holder.mAddButton.setOnClickListener { listener.onSubTaskAdding(item) }
    }




    inner class ViewHolder(val mView:View):RecyclerView.ViewHolder(mView){
        val mWrapper: LinearLayout = mView.todo_items_holder
        val mNameView = mView.task_name
        val mAddButton = mView.add_sub_task

    }

    interface Callback{
        fun onCategoryChange(subTask: SubTask, category: Int, position: Int)
        fun onSubTaskAdding(mainTask: MainTask)
        fun setMainTaskHldr(mainTask: MainTask)
        fun setSubTaskHldr(subTask: SubTask)
    }
}