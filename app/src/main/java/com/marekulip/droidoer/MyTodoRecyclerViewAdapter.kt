package com.marekulip.droidoer

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.marekulip.droidoer.database.MainTask
import com.marekulip.droidoer.database.SubTask
import kotlinx.android.synthetic.main.list_item.view.*

class MyTodoRecyclerViewAdapter(var mValues: List<MainTask>,private val context: Context,private val listener: Callback):RecyclerView.Adapter<MyTodoRecyclerViewAdapter.ViewHolder>(){

    var category = 0
    private val textDone = context.getString(R.string.check_mark)
    private val textMeh = context.getString(R.string.meh_mark)
    private val textCancel = context.getString(R.string.delete_mark)
    private val textReturn = context.getString(R.string.return_task)
    private val colorCancel = Color.parseColor("#EA7A46")
    private val colorMeh = Color.parseColor("#00D9D9")
    private val colorDone = Color.parseColor("#00A500")
    /**
     * Indicates whether context menu was raised by sub task
     */
    private var isSubTaskSetting = false

    override fun getItemCount(): Int = mValues.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        // Set context menu for list item
        holder.mView.setOnLongClickListener {
            listener.setMainTaskHldr(item)
            holder.mView.showContextMenu()
            true
        }
        holder.mView.setOnCreateContextMenuListener { menu, _, _ ->
            if(isSubTaskSetting){
                // Context menu was raised by some sub items. Don't display anything else
                isSubTaskSetting = false
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

        // This parameters tells if view is already present at layout so Constraint setting is not
        // necessary
        var isShouldSkip: Boolean
        for ((index, value) in item.subTasks.withIndex()){
            val view:SubViewHolder
            if(index<holder.mSubItemViews.size){
                view = holder.mSubItemViews[index]
                isShouldSkip = true
            }else {
                view = SubViewHolder(holder.mView as ConstraintLayout)
                isShouldSkip = false
            }
            view.descriptionView.text = value.description
            if(category == 0) {
                view.cancelBut.visibility = View.VISIBLE
                view.mehBut.visibility = View.VISIBLE
                view.doneBut.text = textDone
                view.cancelBut.text = textCancel
                view.mehBut.text = textMeh
                view.cancelBut.setOnClickListener { listener.onCategoryChange(value,SubTask.CAT_CANCELED,position) }
                view.doneBut.setOnClickListener { listener.onCategoryChange(value,SubTask.CAT_DONE,position) }
                view.mehBut.setOnClickListener { listener.onCategoryChange(value,SubTask.CAT_MEH,position) }
                view.descriptionView.setBackgroundColor(Color.WHITE)
            }else{
                view.cancelBut.visibility = View.GONE
                view.mehBut.visibility = View.GONE
                view.doneBut.text = textReturn
                view.doneBut.setOnClickListener { listener.onCategoryChange(value, SubTask.CAT_NONE,position) }
                when(value.category){
                    0 -> view.descriptionView.setBackgroundColor(Color.WHITE)
                    1 -> view.descriptionView.setBackgroundColor(colorCancel)
                    2 -> view.descriptionView.setBackgroundColor(colorMeh)
                    3 -> view.descriptionView.setBackgroundColor(colorDone)
                }
            }
            view.descriptionView.setOnLongClickListener{
                isSubTaskSetting = true
                view.descriptionView.showContextMenu()
                listener.setSubTaskHldr(value)
                true  }

            view.descriptionView.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add(0,R.id.action_rename_sub,0,R.string.rename)
                menu.add(0,R.id.action_delete_sub,0,R.string.delete)
            }
            if(isShouldSkip){
                // Constraints were set do not set them again
                continue
            }
            if(index == 0){
                view.generateRow(holder.mAddButton)
            }
            else if (index >= holder.mSubItemViews.size){
                view.generateRow(holder.mSubItemViews[index-1].descriptionView)
            }
            holder.mSubItemViews.add(view)
        }
        if(holder.mSubItemViews.size > item.subTasks.size){
            // If some items were taken away make sure that views disappear as well
            for (i in item.subTasks.size until holder.mSubItemViews.size){
                holder.mSubItemViews[i].removeView()
            }
            // Cut views from list
            holder.mSubItemViews = holder.mSubItemViews.subList(0,item.subTasks.size)
        }

        holder.mNameView.text = item.name
        holder.mAddButton.setOnClickListener { listener.onSubTaskAdding(item) }
    }


    inner class ViewHolder(val mView:View):RecyclerView.ViewHolder(mView){
        val mNameView = mView.task_name
        val mAddButton = mView.add_sub_task
        var mSubItemViews:MutableList<SubViewHolder> = ArrayList()
    }

    /**
     * Sub view holder acting as wrapping layout except it does not have a layout. It has been created
     * to increase performance because layout in layout in list item is really bad idea... easy to work with
     * but BAD! With this holder the app is taking only one third of a time to display list items. Neat right?
     * Only con is that it does not look so good... its decent... but not good.
     */
    inner class SubViewHolder(val parent:ConstraintLayout){
        /**
         * Precomputed minimal height. Dimensions are set in pixels so they have to be transformed into
         * dp unit before use... well they don't have to but then it looks bad.
         */
        private val minHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40f,
                parent.context.resources.displayMetrics).toInt()
        /**
         * Margin of single items. It was not transferred into dp because effect in pixels was good enough.
         */
        private val margin = 8
        private val doneButTextColor = Color.parseColor("#00A800")

        val doneBut = Button(parent.context)
        val mehBut = Button(parent.context)
        val cancelBut = Button(parent.context)
        val descriptionView = TextView(parent.context)

        init {
            // Init views with properties that don't change
            descriptionView.id = View.generateViewId()
            descriptionView.setPadding(margin,margin,margin,margin)
            descriptionView.setTextColor(Color.BLACK)

            doneBut.id = View.generateViewId()
            doneBut.setTextColor(doneButTextColor)

            mehBut.text = textMeh
            mehBut.id = View.generateViewId()

            cancelBut.text = textCancel
            cancelBut.id = View.generateViewId()
        }

        /**
         * Removes all views from parent constraint layout
         */
        fun removeView(){
            parent.removeView(doneBut)
            parent.removeView(mehBut)
            parent.removeView(cancelBut)
            parent.removeView(descriptionView)
        }

        /**
         * Creates list item 'row' by setting constraints to single items positioning them in process
         * 'row' means that it looks like all views are in a row but technically all views are on their own.
         */
        fun generateRow(prevView: View){
            // First add view before cloning otherwise changes won't happen
            parent.addView(descriptionView)
            parent.addView(doneBut)
            parent.addView(mehBut)
            parent.addView(cancelBut)
            val constraintSet = ConstraintSet()
            //all views added? Ok clone constraints
            constraintSet.clone(parent)
            // position TextView
            constraintSet.connect(descriptionView.id,ConstraintSet.TOP,prevView.id,ConstraintSet.BOTTOM,margin*2)
            constraintSet.connect(descriptionView.id,ConstraintSet.LEFT,parent.id,ConstraintSet.LEFT,margin)
            constraintSet.connect(descriptionView.id,ConstraintSet.RIGHT,cancelBut.id,ConstraintSet.LEFT)
            constraintSet.constrainWidth(descriptionView.id,ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainHeight(descriptionView.id,ConstraintSet.WRAP_CONTENT)

            // position cancelBut
            constraintSet.connect(cancelBut.id,ConstraintSet.TOP,prevView.id,ConstraintSet.BOTTOM,margin)
            constraintSet.connect(cancelBut.id,ConstraintSet.RIGHT,mehBut.id,ConstraintSet.LEFT)
            constraintSet.constrainWidth(cancelBut.id,minHeight)
            constraintSet.constrainHeight(cancelBut.id,minHeight)

            // position mehBut
            constraintSet.connect(mehBut.id,ConstraintSet.TOP,prevView.id,ConstraintSet.BOTTOM,margin)
            constraintSet.connect(mehBut.id,ConstraintSet.RIGHT,doneBut.id,ConstraintSet.LEFT)
            constraintSet.constrainWidth(mehBut.id,minHeight)
            constraintSet.constrainHeight(mehBut.id,minHeight)

            // position doneBut
            constraintSet.connect(doneBut.id,ConstraintSet.TOP,prevView.id,ConstraintSet.BOTTOM,margin)
            constraintSet.connect(doneBut.id,ConstraintSet.RIGHT,parent.id,ConstraintSet.RIGHT,margin)
            constraintSet.constrainWidth(doneBut.id,minHeight)
            constraintSet.constrainHeight(doneBut.id,minHeight)

            // Make sure that text view is at least as high as buttons - all list items are aligned vertically
            // based on bottom part of previous text view
            descriptionView.minHeight = minHeight

            // And finally make the changes happen.
            TransitionManager.beginDelayedTransition(parent)
            constraintSet.applyTo(parent)
        }
    }

    interface Callback{
        fun onCategoryChange(subTask: SubTask, category: Int, position: Int)
        fun onSubTaskAdding(mainTask: MainTask)
        fun setMainTaskHldr(mainTask: MainTask)
        fun setSubTaskHldr(subTask: SubTask)
    }
}